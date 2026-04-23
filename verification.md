# 验证记录

- 日期：2026-04-23
- 执行者：Codex

本次修复已确认：

- `authController` 启动报错的直接根因是验证码 SPI 注册文件存在 UTF-8 BOM。
- 两个注册文件已改为无 BOM 文本。
- 新增测试 `CaptchaSpiResourceTest` 已通过。

当前仍存在的独立问题：

- `wms-framework/wms-starter-biz-tenant/src/main/java/com/jsmauto/wms/framework/tenant/config/TenantAutoConfiguration.java` 为损坏内容，导致完整整包构建/启动无法继续。
