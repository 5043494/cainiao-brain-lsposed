# 开发、构建与发布流程

## 日常开发

1. 修改 Java、资源或构建配置；
2. 更新 `CHANGELOG.md` 和 `docs/DEVELOPMENT_LOG.md`；
3. 执行 `./gradlew clean assembleRelease`；
4. 使用 ADB 覆盖安装并进行真机验证；
5. 检查 LSPosed 注入、运行日志、抓包日志、设置开关和导出功能；
6. 创建具有明确主题和详细正文的 Git 提交；
7. post-commit Hook 自动推送到私有 GitHub 仓库；
8. GitHub Actions 自动执行独立构建并保存 APK Artifact。

## 提交规范

建议使用以下前缀：

- `feat:` 新功能；
- `fix:` 缺陷修复；
- `perf:` 性能优化；
- `ui:` 界面调整；
- `docs:` 文档变更；
- `build:` 构建、依赖和 CI；
- `test:` 测试代码或验证记录；
- `release:` 版本发布。

提交正文应记录问题现象、根因、实现方案、影响范围和测试结果。

## 发布检查清单

- [ ] 版本号和 versionCode 已递增；
- [ ] CHANGELOG 已更新；
- [ ] 开发日志已更新；
- [ ] release 构建成功；
- [ ] APK 签名验证成功；
- [ ] LSPosed 模块可被识别并启用；
- [ ] 菜鸟作用域正确；
- [ ] 运行日志可读、可刷新、可清空、可导出；
- [ ] 抓包日志可读、可刷新、可清空、可导出；
- [ ] 大型抓包日志进入速度正常；
- [ ] GitHub Actions 构建通过；
- [ ] APK 和 SHA-256 已附加到对应 Release。
