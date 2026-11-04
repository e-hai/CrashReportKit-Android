# AGENTS.md

给在 `CrashReportKit-Android` 仓库工作的 AI / 自动化助手的项目约定。人类可读说明见 [README.md](./README.md)。

## 项目是什么

- **CrashReportKit-Android**：Android 平台的统一崩溃上报 SDK 仓库。
- 模块构成：
  - `:crashreport` — 对外发布的 SDK 库（门面抽象 + 后端适配器）。
  - `:sample` — 示例工程 App，用于本地验证，不属于发布产物。
- 当前后端：**Firebase Crashlytics（临时）**。业务代码严禁直接依赖 Firebase Crashlytics API。

## 架构约束（必须严格遵守）

```text
宿主业务 / sample  →  CrashReport（门面）  →  CrashReporter（接口）  →  XxxCrashReporter → 厂商 SDK
```

| 允许 | 禁止 |
|:---|:---|
| 业务只依赖 `com.kit.crashreport.CrashReport` | 在业务或 sample 里直接使用 `FirebaseCrashlytics.getInstance()` |
| 新后端置于 `crashreport/.../internal/` | 把厂商具体类型暴露到公共 API 签名中 |
| 通过 `CrashBackend` / `setReporter` 扩展新后端 | 为更换厂商改动业务层的调用点 |

- **公共包名**：`com.kit.crashreport`
- **内部包名**：`com.kit.crashreport.internal`（宿主代码不可见，禁止将 internal 类型作为公开 API 返回值）。

## 目录地图

```text
CrashReportKit-Android/
├── crashreport/          # SDK 核心库
│   └── src/main/java/com/kit/crashreport/
│       ├── CrashReport.kt        # 对外统一门面
│       ├── CrashReportConfig.kt  # 初始化参数模型
│       ├── CrashBackend.kt       # 后端通道枚举
│       ├── CrashReporter.kt      # 底层统一契约接口
│       └── internal/             # 厂商具体适配器 (Firebase / NoOp)
├── sample/               # 演示工程；applicationId = com.kit.crashreport.sample
├── gradle/libs.versions.toml
├── jitpack.yml           # JitPack 构建声明：JDK 17 + 发布 :crashreport
├── README.md
└── AGENTS.md
```

## 改动指引

### 发布 / JitPack 规范

- 只发布 `:crashreport` 模块；`sample` 绝不进入 Maven 产物。
- 远程发布坐标：`com.github.e-hai.CrashReportKit-Android:crashreport:<Tag>`。
- 版本号解析：优先读取环境变量 `VERSION`（即 Git Tag，自动剥离前缀 `v`/`V`）；本地默认读取 `gradle.properties` 中的 `VERSION_NAME`。
- 本地构建与验证：`./gradlew :crashreport:publishToMavenLocal -Dorg.gradle.configuration-cache=false`。
- **严禁**在 `:crashreport` 库模块应用 `google-services` / `firebase-crashlytics` Gradle 插件，插件只应应用在宿主 application 模块。

### 改 SDK 行为

- 新增或调整对外能力，优先扩展 `CrashReport` + `CrashReporter`，再在具体适配器中补齐实现。
- 初始化参数统一维护在 `CrashReportConfig` 中，必须为新参数提供兼容的默认值。
- Firebase 专属逻辑仅在 `internal/FirebaseCrashReporter.kt` 与模块 `build.gradle.kts` 中收敛。

### 扩展新后端

1. 在 `crashreport/.../internal/` 新建 `XxxCrashReporter : CrashReporter`。
2. 在 `CrashBackend` 增加新枚举项。
3. 在 `CrashReport.init` 的 `when` 分支中实例化对应适配器。
4. 更新 README 中的架构与接入说明。
5. **绝对不要**改变业务层调用范式（业务代码永远只调用 `CrashReport.*`）。

## 验证命令

```bash
# 构建检查与测试
./gradlew :crashreport:assembleDebug :sample:assembleDebug :crashreport:testDebugUnitTest

# 校验本地 Maven 发布
./gradlew :crashreport:publishToMavenLocal -Dorg.gradle.configuration-cache=false
```
