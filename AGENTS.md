# AGENTS.md

给在本仓库工作的 AI / 自动化助手的项目约定。人类可读说明见 [README.md](./README.md)。

## 项目是什么

- **CrashReportKit-Android**：Android 崩溃上报 SDK 仓库。
- 模块：
  - `:crashreport` — 对外库（门面 + 后端适配）。
  - `:sample` — 演示 App，不是发布产物。
- 当前后端：**Firebase Crashlytics（临时）**。业务代码不得直接依赖 Firebase Crashlytics API。

## 架构约束（必须遵守）

```text
业务 / sample  →  CrashReport（门面）  →  CrashReporter  →  XxxCrashReporter → 厂商 SDK
```

| 允许 | 禁止 |
|------|------|
| 业务只调用 `com.kit.crashreport.CrashReport` | 在业务或 sample 里直接 `FirebaseCrashlytics.getInstance()` |
| 新后端放在 `crashreport/.../internal/` | 把厂商类型泄漏到公共 API 签名里 |
| 通过 `CrashBackend` / `setReporter` 扩展 | 为换厂商去改遍全仓 `recordException` 调用点 |

公共包：`com.kit.crashreport`  
内部包：`com.kit.crashreport.internal`（对宿主不可见约定，勿把 internal 类型做成公开返回值）。

## 目录地图

```text
CrashReportKit/
├── crashreport/          # SDK
│   └── src/main/java/com/kit/crashreport/
│       ├── CrashReport.kt
│       ├── CrashReportConfig.kt
│       ├── CrashBackend.kt
│       ├── CrashReporter.kt
│       └── internal/     # Firebase / NoOp 等适配器
├── sample/               # 演示；applicationId = com.kit.crashreport.sample
├── gradle/libs.versions.toml
├── jitpack.yml           # JitPack：JDK17 + 只 publish :crashreport
├── README.md
└── AGENTS.md
```

## 改动指引

### 发布 / JitPack

- 只发布 `:crashreport`；`sample` 不进 Maven 产物。
- 坐标由 GitHub 路径决定：`com.github.e-hai.CrashReportKit-Android:crashreport:<Tag>`。
- 版本：`VERSION` 环境变量（JitPack Tag）优先，否则 `gradle.properties` 的 `VERSION_NAME`；会去掉 `v`/`V` 前缀。
- 改发布元数据只动 `crashreport/build.gradle.kts` 的 `publishing {}` 与根目录 `jitpack.yml`。
- 本地校验：`./gradlew :crashreport:publishToMavenLocal -Dorg.gradle.configuration-cache=false`。
- 不要在 `:crashreport` 上应用 `google-services` / `firebase-crashlytics` Gradle 插件。

### 改 SDK 行为

- 对外能力优先加在 `CrashReport` + `CrashReporter`，再在各适配器实现。
- 初始化参数放进 `CrashReportConfig`，保持二进制兼容的默认值。
- Firebase 相关只动 `internal/FirebaseCrashReporter.kt` 与库 `build.gradle.kts` 依赖。

### 换 / 加上报厂商

1. 新增 `internal/XxxCrashReporter : CrashReporter`。
2. 在 `CrashBackend` 增加枚举项。
3. 在 `CrashReport.init` 的 `when` 中创建实例。
4. 更新 README 架构说明与宿主 Gradle 要求（插件、配置文件）。
5. **不要**改 sample 以外的业务调用示例风格（仍只展示 `CrashReport.*`）。

### 改 Sample

- 包名：`com.kit.crashreport.sample`。
- 初始化只放在 `SampleApplication`。
- `sample/google-services.json` 可为占位；文档需写明真实上报要替换。

### 改构建

- 版本与依赖统一走 `gradle/libs.versions.toml`。
- 根 `build.gradle.kts` 只 `apply false` 插件；`google-services` / `crashlytics` 插件加在 **application** 模块，不要加在 `:crashreport` 库模块。
- `:crashreport` 对 Crashlytics 使用 `api`/`implementation` 时保持「宿主能编译、门面不泄漏类型」的平衡；当前为 `api(platform(firebase-bom))` + `api(firebase-crashlytics)`。

## 编码风格

- Kotlin，与现有文件风格一致；公共 API 保留 `@JvmStatic` 便于 Java 调用。
- 不主动新增无关 Markdown / 文档，除非用户要求。
- 不提交密钥、真实 `google-services.json`、本地 `local.properties`。
- 不跑 `git commit` / `push`，除非用户明确要求。

## 验证

本机需可用 JDK（例如 Android Studio 自带 JBR）：

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :crashreport:assembleDebug :sample:assembleDebug :crashreport:testDebugUnitTest
./gradlew :crashreport:publishToMavenLocal -Dorg.gradle.configuration-cache=false
```

最小回归：库能编过；sample 能编过；`CrashReportTest` 用假 `CrashReporter` 覆盖门面转发；`publishToMavenLocal` 成功（`~/.m2/repository/com/github/e-hai/crashreport/`）。

## 不要做的事

- 不要把 Crashlytics / 其他厂商 SDK 写成业务层单例。
- 不要在库模块应用 `com.google.gms.google-services`（应由宿主 App 应用）。
- 不要为了「演示方便」删除 `CrashReporter` 抽象、让门面直接调用 Firebase。
- 不要扩大 `minSdk` / 无必要升依赖大版本，除非任务明确要求。
