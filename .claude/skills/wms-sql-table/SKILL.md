---
name: wms-sql-table
description: "Use when designing or reviewing WMS MySQL tables and SQL migration scripts. Enforces module-prefixed table names, common audit fields, tenant fields, logical delete, comments, indexes, and sync with DO/Mapper/VO."
---

# WMS SQL Table Skill

## Overview

Use this skill when designing, writing, or reviewing MySQL table definitions and SQL scripts for the WMS project.

SQL must match the project's MyBatis-Plus, tenant, logical delete, and naming conventions.

## Project Context

SQL directory:

```text
sql/mysql/
├─ wms-asrs.sql
└─ quartz.sql
```

Related conventions:

- Business script: `sql/mysql/wms-asrs.sql`
- Quartz script: `sql/mysql/quartz.sql`
- Java fields: camelCase
- Database fields: snake_case
- Logic delete: `deleted`, 0 means not deleted, 1 means deleted

## Hard Rules

- Business table names MUST use module prefix.
- Field names MUST use snake_case.
- Every table and field MUST have `COMMENT`.
- Primary key SHOULD be `id`.
- Tenant business tables MUST include `tenant_id`.
- Logical delete field MUST be `deleted` where logical deletion is needed.
- Audit fields SHOULD include `creator`, `create_time`, `updater`, `update_time`.
- Indexes MUST match real query conditions.
- Do not commit one-off local test data.
- Schema changes MUST be reflected in DO, Mapper queries, VO, and generated pages where applicable.

## Table Name Convention

| Module | Prefix | Example |
| --- | --- | --- |
| system | `system_` | `system_users` |
| infra | `infra_` | `infra_codegen_table` |
| quartz | `qrtz_` | `qrtz_job_details` |
| demo | `demo_` | `demo_order` |

## Good Table Pattern

```sql
CREATE TABLE demo_order (
    id          bigint       NOT NULL COMMENT '编号',
    order_no    varchar(64)  NOT NULL COMMENT '订单号',
    status      tinyint      NOT NULL COMMENT '状态',
    tenant_id   bigint       DEFAULT NULL COMMENT '租户编号',
    creator     varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_demo_order_no (order_no),
    KEY idx_demo_order_tenant_status (tenant_id, status)
) COMMENT = '示例订单';
```

## Index Rules

Good index candidates:

- Unique business code, such as `order_no`.
- Tenant + status query, such as `(tenant_id, status)`.
- Tenant + business code, such as `(tenant_id, code)`.
- Fields used by frequent filters and pagination.

Avoid:

- Indexes that do not match any query.
- Low-selectivity single-column indexes without business context.
- Excessive indexes on high-write tables.

## Code Sync Requirements

After changing a table, check:

```text
DO      -> @TableName and fields
Mapper  -> query conditions and ordering
VO      -> request/response fields
Service -> business validation
Menu    -> generated permissions if CRUD page changed
Frontend -> page fields and form controls
```

## Bad Pattern

```sql
CREATE TABLE order_info (
    orderNo varchar(64),
    status int
);
```

Problems:

- Missing module prefix.
- camelCase column name.
- Missing primary key.
- Missing comments.
- Missing audit fields.
- Missing tenant and delete fields.

## Checklist

Before finishing SQL work, verify:

- [ ] Table name has module prefix.
- [ ] Columns use snake_case.
- [ ] Table and columns have comments.
- [ ] Primary key is defined.
- [ ] Tenant field exists when required.
- [ ] Logical delete field exists when required.
- [ ] Audit fields exist where appropriate.
- [ ] Indexes match query paths.
- [ ] No local test data is committed.
- [ ] DO, Mapper, VO, and frontend are updated if needed.
