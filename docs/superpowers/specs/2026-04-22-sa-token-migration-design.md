# Sa-Token 兼容迁移设计文档

- 日期：2026-04-22
- 执行者：Codex
- 范围：主业务请求链路 Spring Security -> Sa-Token 兼容迁移设计
- 目标：保留现有 OAuth2 Token 校验与 Bearer Token 传递方式，仅替换安全框架底座

## 1. 背景与现状

当前项目的认证核心并不是 Spring Security 原生登录表单，而是基于自定义 Token 过滤器实现：

1. 从请求头 `Authorization` 或请求参数 `token` 中提取 token
2. 调用 `OAuth2TokenCommonApi.checkAccessToken(token)` 校验 token
3. 构建 `LoginUser`
4. 写入 Spring Security 上下文
5. 方法级权限主要通过 `@PreAuthorize("@ss.hasPermission('xxx')")` 完成

当前扫描结果：

- `@PreAuthorize`：254 处
- `@PermitAll`：31 处
- Spring Security 核心集中在：
  - `wms-framework/wms-starter-security`
  - `wms-framework/wms-starter-websocket`
  - `wms-module-infra/.../AdminServerConfiguration.java`

## 2. 迁移目标

### 2.1 本阶段目标

1. 主业务请求认证不再依赖 Spring Security 过滤链
2. 保留现有 Bearer Token 传递方式
3. 保留 `OAuth2TokenCommonApi.checkAccessToken(token)` 作为真实 token 校验入口
4. 保留 `LoginUser` 模型
5. 保留 `PermissionCommonApi` 权限判断逻辑
6. 尽量不改前端和业务代码

### 2.2 本阶段明确不做

1. 不替换 OAuth2 token 签发与存储逻辑
2. 不替换密码加密逻辑，`PasswordEncoder` / `BCryptPasswordEncoder` 暂保留
3. 不立即改造 Spring Boot Admin 的独立安全配置
4. 不一次性全量替换全部控制器权限注解

## 3. 总体方案

采用“桥接式兼容迁移”：

1. 引入 Sa-Token 作为新的 Web 层认证与鉴权框架底座
2. 新增基于 Sa-Token 的认证过滤器
3. 过滤器内部继续调用 `OAuth2TokenCommonApi.checkAccessToken(token)`
4. 将解析后的 `LoginUser` 写入新的登录上下文容器
5. `SecurityFrameworkUtils` 改为从新上下文中读取登录人信息
6. 全局异常统一适配 Sa-Token 的未登录/无权限异常
7. 第二阶段再逐步把 `@PreAuthorize` / `@PermitAll` 替换为 Sa-Token 注解

## 4. 新旧职责映射

| 旧组件 | 新组件/策略 | 说明 |
| --- | --- | --- |
| `WebSecurityConfigurerAdapter` | `SaTokenConfiguration` + 路由拦截配置 | 替代 Spring Security 主过滤链 |
| `TokenAuthenticationFilter` | `SaTokenAuthenticationFilter` | 保留 token 校验逻辑，改写上下文承载方式 |
| `SecurityContextHolder` | `LoginUserContextHolder` + Sa-Token 上下文 | 不再依赖 Spring Security 上下文 |
| `AuthenticationEntryPointImpl` | 全局异常处理扩展 | 统一转换 `NotLoginException` |
| `AccessDeniedHandlerImpl` | 全局异常处理扩展 | 统一转换权限不足异常 |
| `AuthorizeRequestsCustomizer` | `SaRouteCustomizer` | 继续支持模块级白名单扩展 |
| `@PermitAll` | 过渡期扫描放行，后续换 `@SaIgnore` | 分阶段迁移 |
| `@PreAuthorize` | 后续换 `@SaCheckPermission/@SaCheckRole` 等 | 第二阶段处理 |

## 5. 核心设计

### 5.1 认证链路

请求进入后：

1. 通过 Sa-Token Servlet 过滤器进入认证前置逻辑
2. 调用 `SecurityFrameworkUtils.obtainAuthorization()` 获取 token
3. 根据请求路径解析 `userType`
4. 调用 `OAuth2TokenCommonApi.checkAccessToken(token)`
5. 构造 `LoginUser`
6. 写入 `LoginUserContextHolder`
7. 补充写入 `WebFrameworkUtils` 现有 request 属性，兼容日志链路
8. 对需要登录的接口触发 Sa-Token 登录校验

注意：
- 现有 accessToken 仍由系统 OAuth2 模块负责校验
- 不将现有 accessToken 替换为 Sa-Token 自己签发的 token
- Sa-Token 在本阶段承担的是框架容器、路由拦截、注解鉴权职责

### 5.2 登录上下文

新增 `LoginUserContextHolder`：

- 基于 `TransmittableThreadLocal<LoginUser>`
- 提供 `set/get/clear`
- 替代现有 `SecurityContextHolder`
- 支持异步线程透传

`SecurityFrameworkUtils` 保留原方法签名，内部实现改为读取 `LoginUserContextHolder`

### 5.3 白名单机制

白名单来源保留三类：

1. `SecurityProperties.permitAllUrls`
2. 扫描 `@PermitAll`
3. 各模块自定义放行规则

为此新增：

- `PermitAllUrlCollector`
- `SaRouteCustomizer`

统一在 Sa-Token 路由配置中注册放行规则。

### 5.4 异常处理

Sa-Token 常见异常：

- `NotLoginException`
- `NotPermissionException`
- `NotRoleException`
- 自定义 scope 校验异常

统一在 `GlobalExceptionHandler` 中转换成当前系统已有响应格式：

- 未登录 -> 401
- 无权限 -> 403

### 5.5 WebSocket

WebSocket 放行规则不再依赖 Spring Security 的 `requestMatchers`。

策略：

1. WebSocket 握手地址加入 Sa-Token 白名单
2. 握手前的 token 解析仍走统一 token 提取逻辑
3. `LoginUserHandshakeInterceptor` 继续从 `SecurityFrameworkUtils.getLoginUser()` 获取当前用户
4. 因为 `SecurityFrameworkUtils` 已改底层实现，所以 WebSocket 侧改动应尽量缩小

### 5.6 AdminServer

`wms-module-infra/.../AdminServerConfiguration.java` 第一阶段保持不动。

原因：

- 它当前是独立的表单登录 + Basic Auth 体系
- 不走主业务 Bearer Token 链路
- 若一并迁移，风险与回归范围显著扩大

## 6. 文件级改造清单

### 6.1 `wms-framework/wms-starter-security`

#### 6.1.1 `pom.xml`

修改点：

- 新增 Sa-Token 依赖
- 保留 `spring-security-crypto`（用于 `PasswordEncoder`）
- 逐步移除 `spring-boot-starter-security` 对主业务链路的依赖
- 若第一阶段为了 AdminServer 间接依赖保留，也要将其影响范围限制在 infra 模块

#### 6.1.2 `config/WebSecurityConfigurerAdapter.java`

处理策略：下线。

替代为：

- `config/SaTokenConfiguration.java`
- `config/SaTokenRouteConfiguration.java`

#### 6.1.3 `config/SecurityAutoConfiguration.java`

保留：

- `PasswordEncoder`
- `SecurityFrameworkService`（过渡期可保留）

删除/替换：

- `AuthenticationEntryPoint`
- `AccessDeniedHandler`
- `TokenAuthenticationFilter` Bean
- `SecurityContextHolder` 策略初始化

新增：

- `SaTokenAuthenticationFilter` Bean
- `LoginUserContextHolder` Bean（若使用静态工具类则不必声明 Bean）
- `PermitAllUrlCollector`
- `SaRouteCustomizer` 聚合器

#### 6.1.4 `config/SecurityProperties.java`

本阶段可先保留类名与配置前缀不变，避免改动配置文件。

仅补充说明：

- `tokenHeader`
- `tokenParameter`
- `permitAllUrls`
- `mockEnable/mockSecret`

这些配置继续被 Sa-Token 认证桥接层使用。

#### 6.1.5 `config/AuthorizeRequestsCustomizer.java`

处理策略：废弃并替换为 `SaRouteCustomizer`。

新接口建议能力：

- 支持追加放行路径
- 支持模块自定义拦截/排除规则
- 保留 `buildAdminApi/buildAppApi` 辅助能力

#### 6.1.6 `core/filter/TokenAuthenticationFilter.java`

处理策略：替换为 `core/filter/SaTokenAuthenticationFilter.java`

保留逻辑：

- 从 Header/Parameter 取 token
- 调用 `OAuth2TokenCommonApi.checkAccessToken`
- `mockLoginUser`
- 构造 `LoginUser`

改动点：

- 不再写 `SecurityContextHolder`
- 改写 `LoginUserContextHolder`
- 兼容 Sa-Token 的登录校验链
- 请求结束后清理上下文，避免线程复用污染

#### 6.1.7 `core/util/SecurityFrameworkUtils.java`

处理策略：重写内部实现，保留外部接口。

要求：

- `obtainAuthorization()` 原样保留
- `getLoginUser()` 改从 `LoginUserContextHolder` 获取
- `setLoginUser()` 改写入 `LoginUserContextHolder`
- 继续兼容 `WebFrameworkUtils.setLoginUserId/setLoginUserType`
- 新增 `clearLoginUser()` 供过滤器 finally 块调用

#### 6.1.8 `core/service/SecurityFrameworkServiceImpl.java`

处理策略：保留。

只需要保证：

- `getLoginUserId()`
- `SecurityFrameworkUtils.getLoginUser()`

改造后仍能正常工作。

#### 6.1.9 `core/context/TransmittableThreadLocalSecurityContextHolderStrategy.java`

处理策略：删除或废弃。

替代：

- `core/context/LoginUserContextHolder.java`

#### 6.1.10 `core/handler/AuthenticationEntryPointImpl.java`
#### 6.1.11 `core/handler/AccessDeniedHandlerImpl.java`

处理策略：下线主链路使用。

若保留文件：

- 仅作过渡注释，不再注入主业务链路

### 6.2 `wms-framework/wms-starter-web`

#### `src/main/java/.../GlobalExceptionHandler.java`

新增 Sa-Token 异常适配：

- `NotLoginException` -> `UNAUTHORIZED`
- `NotPermissionException` -> `FORBIDDEN`
- `NotRoleException` -> `FORBIDDEN`
- 自定义 scope 异常 -> `FORBIDDEN`

### 6.3 `wms-framework/wms-starter-websocket`

#### `core/security/WebSocketAuthorizeRequestsCustomizer.java`

处理策略：替换为 WebSocket 对应的 `SaRouteCustomizer` 实现。

#### `config/WebSocketAutoConfiguration.java`

修改点：

- 注册新的 `SaRouteCustomizer`
- 移除对 `AuthorizeRequestsCustomizer` 的依赖

#### `core/security/LoginUserHandshakeInterceptor.java`

处理策略：尽量轻改。

若 `SecurityFrameworkUtils.getLoginUser()` 改造完成，这里可能仅需修改注释。

### 6.4 `wms-module-infra`

#### `framework/security/config/SecurityConfiguration.java`

处理策略：把匿名路径规则从 `AuthorizeRequestsCustomizer` 改成新的 `SaRouteCustomizer`。

#### `framework/monitor/config/AdminServerConfiguration.java`

处理策略：本阶段不动。

### 6.5 业务控制器与模板

#### 控制器

本阶段不全量改注解，但需要记录第二阶段规则：

- `@PermitAll` -> `@SaIgnore`
- `@PreAuthorize("@ss.hasPermission('xxx')")` -> `@SaCheckPermission("xxx")`
- `@PreAuthorize("@ss.hasRole('xxx')")` -> `@SaCheckRole("xxx")`
- `@PreAuthorize("@ss.hasScope('xxx')")` -> 自定义 scope 校验注解

#### 代码生成模板

后续需要同步修改：

- `wms-module-infra/src/main/resources/codegen/java/controller/controller.vm`

避免继续生成 `@PreAuthorize`。

## 7. 实施分期

### 第一阶段：底座桥接

1. 引入 Sa-Token 依赖
2. 新增 `LoginUserContextHolder`
3. 新增 `SaTokenAuthenticationFilter`
4. 重写 `SecurityFrameworkUtils`
5. 增加 Sa-Token 白名单配置
6. 在 `GlobalExceptionHandler` 中接入 Sa-Token 异常
7. 下线 `WebSecurityConfigurerAdapter`

### 第二阶段：模块放行与 WebSocket 收口

1. 替换 `AuthorizeRequestsCustomizer`
2. 兼容 `@PermitAll` 扫描
3. 替换 WebSocket 路由放行配置
4. 回归验证 WebSocket 握手

### 第三阶段：注解迁移

1. 批量替换 `@PermitAll`
2. 批量替换 `@PreAuthorize`
3. 处理 scope 校验
4. 修改代码生成模板

### 第四阶段：清理与收口

1. 清理主业务 Spring Security 依赖
2. 仅保留必要的 crypto 依赖
3. 单独评估 AdminServer 是否迁移

## 8. 风险与控制

### 8.1 风险

1. 第一阶段若仍保留大量 `@PreAuthorize`，方法鉴权仍会依赖 Spring Method Security
2. scope 体系不是 Sa-Token 原生概念，需要单独桥接
3. AdminServer 仍在使用 Spring Security，会形成阶段性双体系
4. 异步场景需要确保登录上下文透传与清理完整

### 8.2 控制措施

1. 第一阶段仅替换请求认证底座，不宣称已完全去除所有 Spring Security 能力
2. 第二阶段尽快完成注解迁移
3. 所有对外工具类签名尽量不变，优先减少业务层改动
4. 回归重点覆盖：登录接口、菜单权限接口、导出接口、WebSocket、租户切换、操作日志

## 9. 结论

推荐按“兼容迁移”推进：

- 先把主业务请求链替换到 Sa-Token 底座
- 保留现有 token 校验链路
- 再逐步替换方法注解与外围模块

这样能够在最小化业务影响的前提下，完成从 Spring Security 到 Sa-Token 的平滑迁移。
