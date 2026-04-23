# WMS-ASRS 项目说明

## 1. 项目简介

本项目是当前仓库对应的 **WMS / ASRS 管理系统** 工程，采用 **Spring Boot 3 + Vue 3** 的前后端分离结构。

当前仓库已经完成命名空间调整：

- Maven `groupId`：`com.jsmauto.boot`
- Java 包路径：`com.jsmauto.wms`
- 根工程名：`wms`

> 说明：仓库来源于既有脚手架的二次整理版本，README 仅保留当前项目实际可用的信息，不再保留上游宣传、演示地址、无关模块介绍等内容。

---

## 2. 仓库结构

```text
.
├─ wms-dependencies        Maven 依赖版本统一管理
├─ wms-framework           公共框架与基础 starter
├─ wms-module-system       系统管理模块
├─ wms-module-infra        基础设施模块
├─ wms-server              后端启动工程
├─ wms-ui                  前端工程目录
│  └─ wms-ui-admin-vue3    Vue3 管理后台
├─ sql                     数据库脚本
├─ script                  部署 / Docker / Jenkins 等辅助脚本
└─ README.md
```

当前根 `pom.xml` 实际启用模块：

- `wms-dependencies`
- `wms-framework`
- `wms-server`
- `wms-module-system`
- `wms-module-infra`

---

## 3. 技术栈

### 后端

- JDK 17
- Spring Boot 3.5.x
- Maven 多模块
- MyBatis Plus
- Druid
- Redis / Redisson
- Spring Security
- Quartz
- SpringDoc / Swagger UI

### 前端

- Vue 3
- Vite 5
- TypeScript
- Element Plus
- Pinia
- Vue Router

---

## 4. 运行环境要求

建议本地环境：

- JDK 17
- Maven 3.9+
- Node.js 18+
- pnpm 8+
- MySQL 8.x
- Redis 6.x+

---

## 5. 后端启动

### 5.1 初始化数据库

按需执行以下脚本：

- 业务库脚本：`sql/mysql/ruoyi-vue-pro.sql`
- Quartz 脚本：`sql/mysql/quartz.sql`

> 当前默认本地配置中的数据库名仍是 `ruoyi-vue-pro`，如需调整，请同步修改 `wms-server/src/main/resources/application-local.yaml`。

### 5.2 修改本地配置

主要配置文件：

- `wms-server/src/main/resources/application.yaml`
- `wms-server/src/main/resources/application-local.yaml`

当前本地开发默认值：

- 服务端口：`48080`
- MySQL：`127.0.0.1:3306/ruoyi-vue-pro`
- Redis：`127.0.0.1:6379`
- 默认激活环境：`local`

### 5.3 启动方式

启动类：

- `wms-server/src/main/java/com/jsmauto/wms/server/ServerApplication.java`

命令行启动示例：

```bash
mvn -pl wms-server -am spring-boot:run
```

打包示例：

```bash
mvn clean package -DskipTests
```

### 5.4 启动后访问地址

- 后端服务：`http://localhost:48080`
- Swagger UI：`http://localhost:48080/swagger-ui`
- OpenAPI：`http://localhost:48080/v3/api-docs`

---

## 6. 前端启动

前端工程目录：

```text
wms-ui/wms-ui-admin-vue3
```

### 6.1 安装依赖

```bash
cd wms-ui/wms-ui-admin-vue3
pnpm install
```

### 6.2 本地启动

```bash
pnpm dev
```

### 6.3 常用命令

```bash
pnpm dev
pnpm build:local
pnpm build:prod
pnpm ts:check
pnpm lint:eslint
```

### 6.4 前端本地联调配置

文件：`wms-ui/wms-ui-admin-vue3/.env.local`

当前默认配置：

- 后端地址：`http://localhost:48080`
- API 前缀：`/admin-api`

---

## 7. 开发说明

### 7.1 包路径与坐标

当前项目统一使用以下命名：

- Java 包：`com.jsmauto.wms`
- Maven 组织：`com.jsmauto.boot`

### 7.2 关于配置前缀

当前后端配置中仍保留了部分历史配置前缀，例如：

- `wms.*`

这类配置前缀属于 **配置键名**，不影响 Java 包路径和模块命名；如需继续统一命名，建议单独评估后再处理，避免影响现有配置加载行为。

### 7.3 当前 README 编写原则

本 README 仅描述：

- 当前仓库实际存在的模块
- 当前可直接使用的启动方式
- 当前本地开发需要关注的配置

不再包含：

- 上游项目宣传内容
- 与本仓库无关的演示地址
- 当前仓库未启用的模块说明
- 大量截图与冗长特性列表

---

## 8. 常见目录说明

- `sql/`：数据库初始化脚本
- `script/docker/`：Docker 相关脚本与说明
- `script/jenkins/`：Jenkins 构建脚本
- `wms-framework/`：公共 starter 与基础能力
- `wms-module-system/`：系统管理相关能力
- `wms-module-infra/`：基础设施能力
- `wms-server/`：后端服务入口
- `wms-ui/wms-ui-admin-vue3/`：前端管理后台

---

## 9. 后续建议

如果后续继续整理项目，建议优先处理以下内容：

1. 统一数据库脚本文件命名
2. 清理配置文件中的历史品牌字段
3. 清理无实际使用的上游遗留文档与脚本
4. 增补项目自己的业务模块说明与部署文档

---

## 10. License

本项目遵循仓库根目录中的 `LICENSE` 文件。
