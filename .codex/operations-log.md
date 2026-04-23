# 2026-04-23 操作日志

执行者：Codex

- 10:00 左右，检查 `AuthController` 与 `AdminAuthServiceImpl` 的依赖链，确认失败点位于 `captchaService` Bean 创建阶段。
- 10:05 左右，读取本地 `wms-server.log`，拿到最底层异常：`ServiceConfigurationError`，指向验证码 SPI 文件中的非法 provider-class 名称。
- 10:08 左右，使用十六进制视图确认两个 `META-INF/services` 文件首字节为 `EF BB BF`，即 UTF-8 BOM。
- 10:12 左右，移除两个 SPI 文件的 BOM，并新增 `CaptchaSpiResourceTest` 回归测试。
- 10:24 至 10:25，执行 `mvn -pl wms-module-system -am test -Dtest=CaptchaSpiResourceTest -Dsurefire.failIfNoSpecifiedTests=false`，测试通过。
- 10:26 之后，尝试做完整启动复验；构建过程暴露独立问题：`wms-framework/wms-starter-biz-tenant/.../TenantAutoConfiguration.java` 已损坏为二进制内容，阻塞了进一步的整包启动验证。
