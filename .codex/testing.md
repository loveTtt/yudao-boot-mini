# 2026-04-23 测试记录

执行者：Codex

## 已执行

- 字节级检查
  - `wms-module-system/src/main/resources/META-INF/services/com.anji.captcha.service.CaptchaCacheService`
  - `wms-module-system/src/main/resources/META-INF/services/com.anji.captcha.service.CaptchaService`
  - 结果：源文件与 `target/classes` 中对应资源均已无 UTF-8 BOM。

- Maven 测试
  - 命令：
    `mvn -pl wms-module-system -am test -Dtest=CaptchaSpiResourceTest -Dsurefire.failIfNoSpecifiedTests=false`
  - 结果：
    `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`

## 未完全完成

- 完整应用启动复验
  - 尝试打包并启动 `wms-server` 时，构建链被 `wms-framework/wms-starter-biz-tenant/src/main/java/com/jsmauto/wms/framework/tenant/config/TenantAutoConfiguration.java` 的源文件损坏问题阻塞。
  - 该问题与本次验证码 SPI 修复无直接关系。
