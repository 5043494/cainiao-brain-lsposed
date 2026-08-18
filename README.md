# 菜鸟智脑（Cainiao Brain）

菜鸟智脑是一款面向 **菜鸟 App** 的 LSPosed 模块，提供运行状态追踪、MTOP 网络请求/响应日志、抓包开关、日志快速预览与完整导出能力。项目采用原生 Android Java 实现，并使用高对比度的紫色、青色和橙色界面设计。

> 当前版本：**v1.0.2**  
> 模块包名：`com.cainiao.brain`  
> 作用域：`com.cainiao.wireless`  
> 最低 Android 版本：Android 6.0（API 23）

## 核心功能

### 1. 全部运行日志

用于查看模块自身的完整运行状态，包括：

- LSPosed 是否成功注入目标进程；
- 注入的进程名称和 Xposed API 版本；
- MTOP、底层 Request、MtopResponse 等 Hook 的安装结果；
- 配置变更、异常以及未命中组件信息；
- 菜鸟抓包开关的状态变化记录。

### 2. 菜鸟抓包日志

在菜鸟进程内拦截 MTOP 网络对象并记录：

- MTOP API 名称、版本及请求参数；
- 底层网络 Request 对象内容；
- 响应头字段；
- MTOP 响应正文；
- 日志来源和毫秒级时间戳。

抓包日志页面采用异步加载和文件尾部读取策略，只预览最新 220 KB，从而避免大型日志造成页面进入延迟。日志文件达到 8 MB 后自动轮转。

### 3. 完整日志导出

抓包日志及运行日志页面均提供“导出”按钮：

1. 点击“导出”；
2. 在 Android 系统文件选择器中指定保存位置；
3. 模块将完整日志写入带时间戳的 TXT 文件；
4. 页面预览限制不会影响导出内容的完整性。

### 4. 设置

设置页当前包含“基础”模块及“菜鸟抓包”开关。切换后会向已注入的菜鸟进程发送配置更新，同时记录一条运行日志。重新启动菜鸟可确保所有进程应用最新状态。

## 技术架构

```text
com.cainiao.brain
├── data
│   ├── LogStore       # 日志追加、尾部预览、轮转、清理和完整导出
│   ├── LogProvider    # 跨进程日志写入入口
│   └── LogReceiver    # Hook 进程向模块进程投递日志
├── hook
│   ├── CainiaoHook    # LSPosed 入口及 MTOP/Request/Response Hook
│   └── HookLog        # Hook 侧日志分发与抓包状态
└── ui
    ├── MainActivity   # 状态主页
    ├── LogActivity    # 异步日志预览、刷新、清空和导出
    └── SettingsActivity
```

Hook 入口在 `app/src/main/assets/xposed_init` 中声明；模块作用域在 `res/values/arrays.xml` 及 Manifest 的 `xposedscope` 元数据中声明。

## 构建

### 环境要求

- JDK 17 或更高版本；
- Android SDK Platform 34；
- Android Build Tools；
- Gradle Wrapper（仓库已包含）；
- 可访问 Google Maven 与 Maven Central。

### 构建命令

```bash
./gradlew clean assembleRelease
```

生成文件：

```text
app/build/outputs/apk/release/菜鸟智脑-release-v<版本号>.apk
```

当前 release 构建使用开发签名，仅用于项目测试与迭代。正式分发时应在安全环境中配置独立签名，并妥善保存密钥。

## 安装与启用

1. 安装生成的 APK；
2. 打开 LSPosed 管理器；
3. 启用“菜鸟智脑”；
4. 将作用域设置为“菜鸟”（`com.cainiao.wireless`）；
5. 强制停止并重新打开菜鸟；
6. 返回菜鸟智脑主页确认显示“LSPosed 已注入 · 抓包开启”。

## 已验证环境

- Android 13；
- 菜鸟包名：`com.cainiao.wireless`；
- 菜鸟版本：`8.11.805`；
- LSPosed API：102；
- v1.0.2 抓包日志页面真机显示耗时约 523 ms；
- MTOP 请求、响应头、响应正文、日志清理和完整导出均已验证。

## 自动同步与持续集成

仓库配置了两层自动化：

1. `.githooks/post-commit`：每次本地提交后自动推送当前提交到 GitHub，并将结果写入 `.git/cainiao-sync.log`；
2. GitHub Actions：每次推送或 Pull Request 自动执行 release 构建，并上传 APK 构建产物。

首次克隆后启用仓库内 Hook：

```bash
./scripts/setup-auto-sync.sh
```

查看最近同步日志：

```bash
./scripts/show-sync-log.sh
```

## 文档索引

- [版本更新日志](CHANGELOG.md)
- [开发与测试记录](docs/DEVELOPMENT_LOG.md)
- [开发、构建和发布流程](docs/DEVELOPMENT.md)

## 仓库属性

该仓库设置为 **Private**，用于保存菜鸟智脑源代码、开发记录、自动构建配置和版本发布资料。

## GitHub 仓库

- 地址：<https://github.com/5043494/cainiao-brain-lsposed>
- 可见性：Private
- 默认分支：`main`
- 本机自动同步：已启用
- 持续集成：Android CI（每次 push、Pull Request 和手动触发时构建）
