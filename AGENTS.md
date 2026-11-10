# AGENTS.md (CrashReportKit-Android)

给在 `CrashReportKit-Android` 仓库工作或为宿主 App 接入本 SDK 的 AI / 自动化助手的项目规范与详细指引。

---

## 🤖 Agent 角色与核心准则 (Mission & Principles)

你是 Android 基础设施与崩溃监控领域的专家 Agent。在处理此工程或为业务集成该 SDK 时，必须严格遵循以下架构铁律：

1. **统一门面隔离**：业务侧只允许调用 `com.kit.crashreport.CrashReport`，**严禁**在业务层直接引用任何具体厂商 SDK（如 `FirebaseCrashlytics.getInstance()`）。
2. **零厂商泄漏**：所有厂商适配器均封装在 `internal` 作用域内，公共 API 签名不得包含任何第三方库专有类型。
3. **Fail-Safe 绝对防崩**：监控 SDK 绝不能反噬宿主应用。无论配置缺失、缺少 `google-services.json` 还是后端抛出异常，SDK 必须捕获并自动降级为 `NoOpCrashReporter`，状态标记为 `FAILED`，绝不引发 App 启动闪退。
4. **100% 向后兼容**：公共 API（特别是 `init`）通过 `@JvmOverloads` 保障无回调调用的二进制与源码兼容性。

---

## 🛠️ 引导 Agent 接入与使用的详细步骤 (Step-by-Step Guide)

当引导或协助宿主工程接入 `CrashReportKit-Android` 时，按以下 6 个步骤编写代码：

### 步骤 1：配置仓库与声明依赖

1. 在宿主 `settings.gradle.kts` 中添加 JitPack 仓库：
   ```kotlin
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
           maven { url = uri("https://jitpack.io") }
       }
   }
   ```
2. 在宿主 `app/build.gradle.kts` 中引入 SDK 依赖与 Firebase 官方插件：
   ```kotlin
   plugins {
       alias(libs.plugins.android.application)
       id("com.google.gms.google-services")
       id("com.google.firebase.crashlytics")
   }

   dependencies {
       // CrashReportKit 核心门面
       implementation("com.github.e-hai.CrashReportKit-Android:crashreport:1.0.1")

       // Firebase BOM
       implementation(platform("com.google.firebase:firebase-bom:34.17.0"))
       implementation("com.google.firebase:firebase-analytics")
   }
   ```
3. 确保宿主 `app/` 目录下放置真实的 `google-services.json`。

---

### 步骤 2：在 Application 生命周期中初始化并监听结果

在宿主的 `Application.onCreate` 中调用 `CrashReport.init`。推荐使用带状态回调的重载：

```kotlin
package com.example.app

import android.app.Application
import android.util.Log
import com.kit.crashreport.CrashBackend
import com.kit.crashreport.CrashReport
import com.kit.crashreport.CrashReportConfig
import com.kit.crashreport.CrashReportStatus

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化 CrashReportKit 并感知就绪状态
        CrashReport.init(
            context = this,
            config = CrashReportConfig(
                enabled = true,                     // 主开关：false 时全部静默走 NoOp
                collectionEnabled = true,          // 采集开关：支持合规授权动态控制
                backend = CrashBackend.FIREBASE     // 默认上报后端
            )
        ) { result ->
            when (result.status) {
                CrashReportStatus.READY -> {
                    Log.i("CrashReport", "✅ SDK 初始化就绪: ${result.message}")
                }
                CrashReportStatus.DISABLED -> {
                    Log.i("CrashReport", "ℹ️ 崩溃采集已被配置显式禁用: ${result.message}")
                }
                CrashReportStatus.FAILED -> {
                    // Fail-Safe: SDK 自动降级为 NoOp，App 正常运行，控制台输出排查日志
                    Log.w("CrashReport", "⚠️ SDK 初始化降级为 NoOp: ${result.message}", result.cause)
                }
                CrashReportStatus.NOT_INITIALIZED -> Unit
            }
        }
    }
}
```

---

### 步骤 3：绑定用户标识与全局诊断维度 (Metadata)

在用户登录成功或会话变更时调用：

```kotlin
// 1. 设置用户唯一标识（登出时传 null 或空串清除）
CrashReport.setUserId(userId = "user_10086")

// 2. 设置自定义维度键值（随崩溃和非致命异常附带上传，支持 String, Boolean, Int, Long, Float, Double）
CrashReport.setCustomKey("app_flavor", "production")
CrashReport.setCustomKey("channel", "google_play")
CrashReport.setCustomKey("vip_level", 3)
CrashReport.setCustomKey("is_guest", false)
CrashReport.setCustomKey("api_timeout_sec", 15.0)
```

---

### 步骤 4：关键操作记录面包屑 (Breadcrumbs)

在关键页面跳转、按钮点击、核心状态流转时打点（平时保存在本地内存环形队列，崩溃时随同上传）：

```kotlin
// 记录重要操作轨迹
CrashReport.log("User clicked [Checkout] button on OrderDetailActivity")
CrashReport.log("PaymentService: request sent to gateway with orderId=SO12345")
```

---

### 步骤 5：捕获与上报非致命业务异常 (Non-fatal Exceptions)

在业务 `try-catch` 中捕获意外错误，避免 App 闪退的同时将错误堆栈上报至云端排查：

```kotlin
try {
    val result = parseComplexServerPayload(rawPayload)
    applyToView(result)
} catch (e: Exception) {
    // 捕获异常并不杀进程，上报至后台聚合排查
    CrashReport.recordException(e)
}
```

---

### 步骤 6：合规控制与测试验证

```kotlin
// 1. 动态合规控制：用户在设置中关闭数据分析或撤销隐私授权时调用
CrashReport.setCollectionEnabled(false)

// 2. 随时主动查询 SDK 运行状态
val currentStatus: CrashReportStatus = CrashReport.getStatus()

// 3. QA / 开发调试验证：强制触发致命崩溃（验证 mapping 符号化）
// ⚠️ 仅在 DEBUG 环境调用！
// CrashReport.testCrash()
```

---

## 📋 完整公共 API 速查表 (API Reference)

| API 签名 | 返回类型 | 参数说明 | 线程安全 | 异常安全 | 行为描述 |
|:---|:---:|:---|:---:|:---:|:---|
| `init(context, config, callback)` | `Unit` | `context: Context`<br/>`config: CrashReportConfig = CrashReportConfig()`<br/>`callback: CrashReportInitCallback? = null` | ✅ | ✅ | 初始化 SDK 门面，支持尾随 Lambda，捕获底层异常并透出状态 |
| `getStatus()` | `CrashReportStatus` | 无 | ✅ | ✅ | 返回当前状态：`NOT_INITIALIZED` / `READY` / `DISABLED` / `FAILED` |
| `isInitialized()` | `Boolean` | 无 | ✅ | ✅ | 是否已完成初始化流程 |
| `setCollectionEnabled(enabled)` | `Unit` | `enabled: Boolean` | ✅ | ✅ | 动态开启/关闭崩溃与异常数据上报 |
| `setUserId(userId)` | `Unit` | `userId: String?` | ✅ | ✅ | 绑定关联用户 ID，传 null 或空串解绑 |
| `setCustomKey(key, value)` | `Unit` | `key: String`, `value: String/Boolean/Int/Long/Float/Double` | ✅ | ✅ | 设置诊断元数据键值对 |
| `log(message)` | `Unit` | `message: String` | ✅ | ✅ | 追加面包屑行为轨迹日志 |
| `recordException(throwable)` | `Unit` | `throwable: Throwable` | ✅ | ✅ | 记录非致命异常堆栈并上报 |
| `setReporter(customReporter)` | `Unit` | `customReporter: CrashReporter` | ✅ | ✅ | 注入自定义上报器（用于单测 Mock 或扩展自研 APM） |
| `testCrash()` | `Nothing` | 无 | ✅ | 抛异常 | 强制抛出 `RuntimeException`（仅限测试） |

---

## 🚫 Agent 研发与维护红线 (Strict Rules)

1. **绝对禁止厂商类型外露**：严禁在 `CrashReport` 公共方法或 `CrashReportConfig` 中引用 `com.google.firebase.*`。
2. **扩展新厂商必须遵循契约**：
   - 在 `crashreport/.../internal/` 下新建 `XxxCrashReporter : CrashReporter`；
   - 在 `CrashBackend` 扩展枚举值；
   - 在 `CrashReport.init` 的 `when` 分支中实例化，**且必须包裹在 `try-catch` 内**，失败时回退至 `NoOpCrashReporter` 并标记 `FAILED`。
3. **保持混淆规则完备**：新增公开类必须在 `crashreport/consumer-rules.pro` 中显式添加 `-keep`。
4. **禁止主动 push**：不跑 `git push`，除非用户明确要求。

---

## 🧪 验证命令 (Verification)

在提出改动前，必须在本机执行并通过以下命令：

```bash
# 1. 运行全部单元测试（覆盖未初始化、禁用、异常捕获防崩与门面转发）
./gradlew :crashreport:testDebugUnitTest

# 2. 验证示例 App 能够正常构建打包
./gradlew :sample:assembleDebug

# 3. 验证本地 Maven 产物能够正确打包发布
./gradlew :crashreport:publishToMavenLocal -Dorg.gradle.configuration-cache=false
```
