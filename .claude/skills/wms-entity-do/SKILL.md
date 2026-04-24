---
name: wms-entity-do
description: "Use when creating or reviewing WMS DO/entity classes. Enforces MyBatis-Plus mapping, TableName, TableId, BaseDO/TenantBaseDO, JSON type handlers, and separation from API VO classes."
---

# WMS Entity DO Skill

## Overview

Use this skill when creating or reviewing database mapping objects in the WMS backend.

This project uses MyBatis-Plus. Persistent objects are called DO classes and live under `dal/dataobject`. DO classes represent table structure only; they are not API request or response objects.

## Project Context

Common path:

```text
wms-module-*/src/main/java/com/jsmauto/wms/module/*/dal/dataobject/**
```

Typical example:

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

## Hard Rules

- DO classes MUST be placed under `dal/dataobject`.
- DO class names MUST end with `DO`.
- DO classes MUST use `@TableName`.
- Primary key field MUST use `@TableId`.
- Multi-tenant business tables SHOULD extend `TenantBaseDO`.
- Non-tenant tables SHOULD extend the project base DO type where applicable.
- JSON fields MUST use `@TableField(typeHandler = JacksonTypeHandler.class)` and `autoResultMap = true` on `@TableName`.
- DO classes MUST NOT use Controller validation annotations like `@NotBlank` for API validation.
- DO classes MUST NOT be returned directly from Controller.

## Table Mapping Convention

Database field names use snake_case. Java fields use camelCase.

```text
create_time -> createTime
tenant_id   -> tenantId
post_ids    -> postIds
```

MyBatis-Plus config enables underscore-to-camel mapping:

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```

## Base Class Choice

| Table Type | Base class |
| --- | --- |
| Tenant business table | `TenantBaseDO` |
| Common auditable table | project base DO type |
| Pure relation table | evaluate existing project pattern first |

When uncertain, inspect neighboring DO classes in the same module before creating a new pattern.

## Good Pattern

```java
@TableName("demo_order")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoOrderDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String orderNo;
    private Integer status;
}
```

## JSON Field Pattern

```java
@TableName(value = "demo_order", autoResultMap = true)
public class DemoOrderDO extends TenantBaseDO {

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Long> tagIds;
}
```

## Bad Patterns

Do not write this:

```java
@Data
public class DemoOrderDO {
    @NotBlank
    private String orderNo;
}
```

Problems:

- Missing `@TableName`.
- Missing `@TableId`.
- API validation is placed on DO.
- Missing base class for audit and tenant fields.

## Checklist

Before finishing DO work, verify:

- [ ] File is under `dal/dataobject`.
- [ ] Class name ends with `DO`.
- [ ] `@TableName` matches the actual table.
- [ ] `@TableId` is present on `id`.
- [ ] Correct base class is used.
- [ ] JSON fields have type handler and `autoResultMap` if needed.
- [ ] Field names follow camelCase.
- [ ] No API validation annotations are placed on DO.
- [ ] Sensitive fields are not accidentally exposed through Controller.
