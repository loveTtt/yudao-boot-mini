# WMS 后端技术 README

本文档面向后端开发，说明当前 WMS / ASRS 后端工程的模块架构、分层约定、接口规范、缓存规范、ORM 使用规范、表设计规范和本地开发要求。示例均来自当前项目的既有代码模式。

## 1. 项目定位与技术栈

当前仓库是 WMS / ASRS 管理系统工程，后端采用 Spring Boot 3 + Maven 多模块架构。

核心坐标与环境：

- Java：JDK 17
- Maven groupId：`com.jsmauto.boot`
- Java 包根路径：`com.jsmauto.wms`
- Spring Boot：`3.5.9`
- 启动模块：`wms-server`

主要后端技术栈：

- Web：Spring Boot Web
- API 文档：SpringDoc OpenAPI、Knife4j
- 权限认证：Sa-Token
- ORM：MyBatis、MyBatis-Plus、MyBatis-Plus-Join
- 数据源：Druid、dynamic-datasource
- 缓存：Spring Cache、Redis、Redisson、`StringRedisTemplate`
- Bean 转换：MapStruct、项目内 `BeanUtils`
- 参数校验：Jakarta Validation
- 定时任务：Quartz

## 2. 后端模块架构

根 `pom.xml` 当前启用模块：

```text
.
├─ wms-dependencies        依赖版本统一管理
├─ wms-framework           公共框架、starter、通用能力
├─ wms-module-system       系统管理模块
├─ wms-module-infra        基础设施模块
└─ wms-server              后端启动工程
```

模块职责：

| 模块 | 职责 |
| --- | --- |
| `wms-dependencies` | 统一管理 Spring Boot、MyBatis-Plus、Sa-Token、Redis 等依赖版本 |
| `wms-framework` | 提供通用返回、异常、Web、Redis、MyBatis、安全、日志等基础能力 |
| `wms-module-system` | 用户、角色、菜单、部门、权限、短信、OAuth2 等系统能力 |
| `wms-module-infra` | 配置、文件、代码生成、API 文档等基础设施能力 |
| `wms-server` | 应用启动入口，聚合业务模块并加载配置 |

后端启动类：

```text
wms-server/src/main/java/com/jsmauto/wms/server/ServerApplication.java
```

启动模块只负责装配，不应沉淀业务逻辑。新增业务能力应放入对应 `wms-module-*` 模块。

## 3. 分层与包结构规范

后端模块按 Controller、Service、DAL、转换器和 VO 分层。

典型结构：

```text
wms-module-system
└─ src/main/java/com/jsmauto/wms/module/system
   ├─ controller/admin/user
   │  ├─ UserController.java
   │  └─ vo/user
   │     ├─ UserSaveReqVO.java
   │     ├─ UserPageReqVO.java
   │     └─ UserRespVO.java
   ├─ service/user
   │  ├─ AdminUserService.java
   │  └─ AdminUserServiceImpl.java
   ├─ dal/dataobject/user
   │  └─ AdminUserDO.java
   ├─ dal/mysql/user
   │  └─ AdminUserMapper.java
   └─ convert/user
      └─ UserConvert.java
```

分层职责：

| 层级 | 职责 |
| --- | --- |
| Controller | 接收 HTTP 请求、参数校验、权限注解、返回 `CommonResult` |
| VO | 定义请求和响应结构，承载 OpenAPI 注解与参数校验 |
| Service | 编排业务逻辑、事务、领域校验，不直接暴露 DO 给 Controller |
| DAL / Mapper | 封装数据库访问，使用 MyBatis-Plus 查询条件 |
| DO | 数据库表映射对象，只表达持久化结构 |
| Convert | 负责 VO、DO、DTO 之间转换 |

新增功能时不要把 SQL、Redis、业务规则和返回拼装全部塞进 Controller。Controller 只做接口层工作。

## 4. 接口规范

### 4.1 Controller 命名与路径

接口路径使用模块前缀，例如系统模块用户接口：

```java
@Tag(name = "管理后台 - 用户")
@RestController
@RequestMapping("/system/user")
@Validated
public class UserController {

    @Resource
    private AdminUserService userService;

    @PostMapping("/create")
    @Operation(summary = "创建用户")
    @SaCheckPermission("system:user:create")
    public CommonResult<Long> createUser(@Valid @RequestBody UserSaveReqVO reqVO) {
        Long id = userService.createUser(reqVO);
        return success(id);
    }
}
```

规范：

- Controller 类使用 `@RestController`
- Controller 类使用 `@RequestMapping("/模块/资源")`
- 类上使用 `@Tag` 描述接口分组
- 方法上使用 `@Operation` 描述接口用途
- 管理后台接口必须加权限注解，例如 `@SaCheckPermission`
- 请求体参数使用 `@Valid @RequestBody`
- 查询参数可使用 `@Validated` + VO 字段校验

### 4.2 HTTP 方法约定

| 操作 | HTTP 方法 | 路径示例 |
| --- | --- | --- |
| 创建 | `POST` | `/create` |
| 更新 | `PUT` | `/update` |
| 删除 | `DELETE` | `/delete` |
| 详情 | `GET` | `/get` |
| 分页 | `GET` | `/page` |
| 导出 | `GET` | `/export-excel` |

分页接口示例：

```java
@GetMapping("/page")
@Operation(summary = "获得用户分页列表")
@SaCheckPermission("system:user:query")
public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO reqVO) {
    PageResult<AdminUserDO> pageResult = userService.getUserPage(reqVO);
    return success(UserConvert.INSTANCE.convertPage(pageResult));
}
```

### 4.3 统一返回规范

接口统一返回 `CommonResult<T>`，分页统一使用 `PageResult<T>`。

```java
@Data
public class CommonResult<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = GlobalErrorCodeConstants.SUCCESS.getCode();
        result.data = data;
        result.msg = "";
        return result;
    }
}
```

Controller 不直接返回 DO，不直接拼接 Map，不绕过 `CommonResult`。

### 4.4 参数校验规范

请求 VO 使用 Jakarta Validation 和 OpenAPI 注解。

```java
@Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "wms")
@NotBlank(message = "用户账号不能为空")
@Pattern(regexp = "^[a-zA-Z0-9]{4,30}$", message = "用户账号由数字、字母组成")
@Size(min = 4, max = 30, message = "用户账号长度为 4-30 个字符")
private String username;

@Schema(description = "用户邮箱", example = "wms@iocoder.cn")
@Email(message = "邮箱格式不正确")
@Size(max = 50, message = "邮箱长度不能超过 50 个字符")
private String email;
```

规范：

- 必填字符串使用 `@NotBlank`
- 集合、字符串长度使用 `@Size`
- 邮箱使用 `@Email`
- 格式类字段使用 `@Pattern`
- 字段说明使用 `@Schema`
- 不在 Controller 手写重复的空值校验

## 5. 缓存使用规范

项目使用 Spring Cache 和 Redis。固定 TTL 的普通缓存优先使用 Spring Cache；需要动态 TTL、复杂结构或精细控制时使用 RedisDAO。

### 5.1 缓存 key 集中管理

缓存 key 定义在模块内 `RedisKeyConstants`，不要在业务代码中散落硬编码 key。

```java
public interface RedisKeyConstants {

    String DEPT_CHILDREN_ID_LIST = "dept_children_ids";
    String ROLE = "role";
    String USER_ROLE_ID_LIST = "user_role_ids";
    String MENU_ROLE_ID_LIST = "menu_role_ids";
    String PERMISSION_MENU_ID_LIST = "permission_menu_ids";
    String OAUTH_CLIENT = "oauth_client";
    String OAUTH2_ACCESS_TOKEN = "oauth2_access_token:%s";
    String SMS_TEMPLATE = "sms_template";
}
```

规范：

- key 名必须表达业务含义
- 带变量的 key 使用格式化模板，例如 `oauth2_access_token:%s`
- 新增缓存先补充 `RedisKeyConstants`
- 禁止在多个类中重复定义同一个缓存 key

### 5.2 Spring Cache 使用规范

适合缓存按 code、id 查询的稳定数据。

```java
@Override
@Cacheable(cacheNames = RedisKeyConstants.SMS_TEMPLATE, key = "#code", unless = "#result == null")
public SmsTemplateDO getSmsTemplateByCodeFromCache(String code) {
    return smsTemplateMapper.selectByCode(code);
}
```

数据变更后必须清理缓存：

```java
@Override
@CacheEvict(cacheNames = RedisKeyConstants.SMS_TEMPLATE, allEntries = true)
public void updateSmsTemplate(SmsTemplateSaveReqVO updateReqVO) {
    SmsTemplateDO updateObj = BeanUtils.toBean(updateReqVO, SmsTemplateDO.class);
    smsTemplateMapper.updateById(updateObj);
}
```

规范：

- 查询缓存使用 `@Cacheable`
- 更新、删除后使用 `@CacheEvict`
- 空结果一般不缓存，使用 `unless = "#result == null"`
- 修改数据但不清缓存是 bug
- 缓存粒度优先按业务对象划分，不要复用不相关 key

### 5.3 RedisDAO 使用规范

动态过期时间或复杂序列化场景使用 RedisDAO。

```java
@Repository
public class OAuth2AccessTokenRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public OAuth2AccessTokenDO get(String accessToken) {
        String redisKey = formatKey(accessToken);
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), OAuth2AccessTokenDO.class);
    }

    public void set(OAuth2AccessTokenDO accessTokenDO) {
        String redisKey = formatKey(accessTokenDO.getAccessToken());
        accessTokenDO.setUpdater(null).setUpdateTime(null).setCreateTime(null).setCreator(null).setDeleted(null);
        long time = LocalDateTimeUtil.between(LocalDateTime.now(), accessTokenDO.getExpiresTime(), ChronoUnit.SECONDS);
        if (time > 0) {
            stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(accessTokenDO), time, TimeUnit.SECONDS);
        }
    }

    private static String formatKey(String accessToken) {
        return String.format(OAUTH2_ACCESS_TOKEN, accessToken);
    }
}
```

规范：

- Redis 读写封装在 `dal/redis`，Service 不直接拼 Redis key
- JSON 序列化统一使用项目工具类
- 设置动态 TTL 前必须计算剩余有效期
- 写入 Redis 前去掉无意义审计字段，降低缓存体积
- token、验证码、临时状态等必须设置过期时间

## 6. ORM 操作规范

项目使用 MyBatis-Plus，Mapper 默认继承 `BaseMapperX<DO>`。

### 6.1 Mapper 查询规范

```java
@Mapper
public interface AdminUserMapper extends BaseMapperX<AdminUserDO> {

    default AdminUserDO selectByUsername(String username) {
        return selectOne(AdminUserDO::getUsername, username);
    }

    default PageResult<AdminUserDO> selectPage(UserPageReqVO reqVO,
                                               Collection<Long> deptIds,
                                               Collection<Long> userIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AdminUserDO>()
                .likeIfPresent(AdminUserDO::getUsername, reqVO.getUsername())
                .likeIfPresent(AdminUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(AdminUserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AdminUserDO::getCreateTime, reqVO.getCreateTime())
                .inIfPresent(AdminUserDO::getDeptId, deptIds)
                .inIfPresent(AdminUserDO::getId, userIds)
                .orderByDesc(AdminUserDO::getId));
    }
}
```

规范：

- 单表查询优先使用 `BaseMapperX` 和 `LambdaQueryWrapperX`
- 可选条件使用 `xxxIfPresent`，避免手写大量空值判断
- 分页查询返回 `PageResult<DO>`
- 排序必须明确，分页接口禁止无序返回
- 查询字段、条件字段应尽量走索引
- 禁止循环内单条查询造成 N+1，优先批量查询后在内存分组

### 6.2 Service 调用规范

Service 层负责业务规则和事务边界，Controller 不直接访问 Mapper。

```java
@Service
@Validated
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private AdminUserMapper userMapper;

    @Override
    public AdminUserDO getUser(Long id) {
        return userMapper.selectById(id);
    }
}
```

规范：

- Controller 只依赖 Service
- Service 可以组合多个 Mapper、RedisDAO、外部服务
- 涉及多表写入时在 Service 方法上加事务
- Service 对外返回业务需要的数据，不暴露无关字段

### 6.3 DO 映射规范

```java
@TableName(value = "system_users", autoResultMap = true)
@KeySequence("system_users_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private Long deptId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Long> postIds;
}
```

规范：

- DO 类放在 `dal/dataobject/*`
- 表名使用 `@TableName`
- 主键字段使用 `@TableId`
- 多租户业务表继承 `TenantBaseDO`
- JSON 字段使用 `@TableField(typeHandler = JacksonTypeHandler.class)`
- DO 不承载接口校验注解，校验放在 VO

## 7. 表命名与字段规范

### 7.1 表命名

表名使用模块前缀 + 业务名，全部小写，下划线分隔。

| 模块 | 表名前缀 | 示例 |
| --- | --- | --- |
| system | `system_` | `system_users` |
| infra | `infra_` | `infra_config` |
| quartz | `qrtz_` | `qrtz_job_details` |

规范：

- 新表必须带模块前缀
- 禁止使用无业务含义的缩写
- 表名和 DO 的 `@TableName` 保持一致
- Java 字段使用 camelCase，数据库字段使用 snake_case

### 7.2 通用字段

业务表建议包含以下通用字段：

```sql
CREATE TABLE system_demo (
    id          bigint       NOT NULL COMMENT '编号',
    tenant_id   bigint       DEFAULT NULL COMMENT '租户编号',
    creator     varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (id)
) COMMENT = '示例表';
```

字段规范：

- 主键统一使用 `id`
- 多租户表使用 `tenant_id`
- 逻辑删除字段使用 `deleted`
- 创建字段使用 `creator`、`create_time`
- 更新字段使用 `updater`、`update_time`
- 时间字段使用 `datetime`
- 字段必须写 `COMMENT`

### 7.3 逻辑删除与租户

MyBatis-Plus 当前配置：

```yaml
mybatis-plus:
  global-config:
    db-config:
      id-type: NONE
      logic-delete-value: 1
      logic-not-delete-value: 0
```

规范：

- 逻辑删除值：`1`
- 未删除值：`0`
- 查询默认应遵守逻辑删除条件
- 多租户数据表必须保留 `tenant_id`
- 跨租户查询必须有明确业务理由，不能随意绕过租户隔离

## 8. SQL 与数据迁移规范

当前 MySQL 脚本目录：

```text
sql/mysql/
├─ wms-asrs.sql
└─ quartz.sql
```

规范：

- 业务初始化脚本放在 `sql/mysql/wms-asrs.sql`
- Quartz 初始化脚本放在 `sql/mysql/quartz.sql`
- 新增表必须包含主键、审计字段、逻辑删除字段
- 新增索引必须匹配实际查询条件
- 不提交一次性本地测试数据
- 字段变更必须同时更新 DO、Mapper 查询和相关 VO
- 删除字段前先确认代码中没有读写路径

索引示例：

```sql
CREATE INDEX idx_system_demo_tenant_name ON system_demo (tenant_id, name);
```

索引原则：

- 高频等值查询字段优先建索引
- 多租户表常用组合索引以 `tenant_id` 开头
- 分页排序字段需要评估索引覆盖
- 不为低选择性字段盲目建单列索引

## 9. 异常、安全与日志规范

### 9.1 异常规范

业务异常应在 Service 层抛出，Controller 不负责拼错误响应。

```java
if (user == null) {
    throw exception(USER_NOT_EXISTS);
}
```

规范：

- 参数格式错误交给 Validation
- 业务规则错误使用项目统一异常机制
- 不吞异常，不返回 `null` 表示失败
- 不把底层异常堆栈直接返回给前端

### 9.2 权限规范

管理后台接口必须声明权限点。

```java
@SaCheckPermission("system:user:update")
@PutMapping("/update")
@Operation(summary = "更新用户")
public CommonResult<Boolean> updateUser(@Valid @RequestBody UserSaveReqVO reqVO) {
    userService.updateUser(reqVO);
    return success(true);
}
```

规范：

- 权限字符串格式：`模块:资源:动作`
- 查询、创建、更新、删除、导出分别配置权限
- 不依赖前端隐藏按钮作为权限控制
- 涉及敏感数据的接口必须做后端权限校验

### 9.3 日志规范

导出等重要操作使用访问日志注解。

```java
@GetMapping("/export-excel")
@Operation(summary = "导出用户")
@SaCheckPermission("system:user:export")
@ApiAccessLog(operateType = EXPORT)
public void exportUserExcel(@Valid UserPageReqVO exportReqVO, HttpServletResponse response) throws IOException {
    // export data
}
```

规范：

- 重要写操作、导出操作需要可审计
- 日志不打印密码、token、验证码、密钥
- 异常日志保留必要上下文，但避免泄露敏感数据
- 批量任务日志要控制数量，避免刷屏

## 10. 本地开发与测试

### 10.1 本地配置

主要配置文件：

```text
wms-server/src/main/resources/application.yaml
wms-server/src/main/resources/application-local.yaml
```

默认激活环境：

```yaml
spring:
  profiles:
    active: local
```

### 10.2 后端启动

```bash
mvn -pl wms-server -am spring-boot:run
```

打包：

```bash
mvn clean package -DskipTests
```

### 10.3 接口文档

本地启动后访问：

```text
http://localhost:48080/swagger-ui
http://localhost:48080/v3/api-docs
```

接口文档配置：

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui
```

### 10.4 MyBatis 配置

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  type-aliases-package: ${wms.info.base-package}.module.*.dal.dataobject

mybatis-plus-join:
  sub-table-logic: true
  ms-cache: true
  table-alias: t
  logic-del-type: on
```

规范：

- 本地开发前确认 MySQL、Redis 已启动
- 修改 SQL 后同步验证启动和相关接口
- 修改 Mapper 后至少验证对应 Service 或接口路径
- 修改缓存逻辑后验证更新、删除是否清理缓存

## 11. 新增功能 checklist

新增一个后端功能前，按以下清单检查：

### 11.1 数据库

- [ ] 表名是否带模块前缀
- [ ] 是否包含 `id`、审计字段、`deleted`
- [ ] 多租户数据是否包含 `tenant_id`
- [ ] 索引是否匹配查询条件
- [ ] SQL 是否放入 `sql/mysql/wms-asrs.sql` 或对应迁移脚本

### 11.2 后端分层

- [ ] 是否新增 DO，并正确配置 `@TableName`
- [ ] 是否新增 Mapper，并继承 `BaseMapperX`
- [ ] 是否新增 Service 接口和实现
- [ ] 是否新增 Controller，而不是让前端直接依赖内部实现
- [ ] VO、DO、DTO 是否职责清晰

### 11.3 接口

- [ ] Controller 是否使用 `@Tag`、`@Operation`
- [ ] 是否使用 `CommonResult<T>` 返回
- [ ] 分页是否使用 `PageResult<T>`
- [ ] 请求参数是否使用 Validation 注解
- [ ] 管理后台接口是否配置 `@SaCheckPermission`

### 11.4 缓存

- [ ] 是否确实需要缓存，而不是过早优化
- [ ] 缓存 key 是否定义在 `RedisKeyConstants`
- [ ] 更新和删除路径是否清理缓存
- [ ] 动态 TTL 场景是否使用 RedisDAO
- [ ] 是否避免缓存敏感字段或无意义大对象

### 11.5 ORM 与性能

- [ ] 是否避免 N+1 查询
- [ ] 大数据量列表是否分页
- [ ] 是否避免无条件全表扫描
- [ ] 是否避免循环单条插入或更新
- [ ] 排序字段和过滤字段是否有索引支撑

### 11.6 安全与日志

- [ ] 是否做后端权限校验
- [ ] 是否避免日志输出密码、token、验证码
- [ ] 业务异常是否使用统一异常机制
- [ ] 导出、批量修改等重要操作是否有审计日志
- [ ] 是否避免把 DO 中敏感字段直接返回给前端
