---
name: wms-mapper-orm
description: "Use when creating or reviewing WMS MyBatis-Plus Mapper and ORM queries. Enforces BaseMapperX, LambdaQueryWrapperX, conditional query helpers, pagination, sorting, indexing awareness, and avoiding N+1 queries."
---

# WMS Mapper ORM Skill

## Overview

Use this skill when writing or reviewing Mapper code and database query logic in the WMS backend.

The project uses MyBatis-Plus with project extensions such as `BaseMapperX` and `LambdaQueryWrapperX`.

## Project Context

Common path:

```text
wms-module-*/src/main/java/com/jsmauto/wms/module/*/dal/mysql/**
```

Typical example:

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

## Hard Rules

- Mapper interfaces MUST be annotated with `@Mapper`.
- Mapper interfaces SHOULD extend `BaseMapperX<DO>`.
- Single-table dynamic queries SHOULD use `LambdaQueryWrapperX`.
- Optional conditions SHOULD use `likeIfPresent`, `eqIfPresent`, `betweenIfPresent`, `inIfPresent`, etc.
- Pagination queries MUST return `PageResult<DO>`.
- Pagination queries MUST specify deterministic ordering.
- Avoid N+1 queries. Batch query and group in memory when needed.
- Large result sets MUST be paginated.
- Do not write raw SQL when MyBatis-Plus query wrappers are sufficient.
- Do not use string field names when lambda getters are available.

## Query Pattern

```java
default PageResult<DemoOrderDO> selectPage(DemoOrderPageReqVO reqVO) {
    return selectPage(reqVO, new LambdaQueryWrapperX<DemoOrderDO>()
            .likeIfPresent(DemoOrderDO::getOrderNo, reqVO.getOrderNo())
            .eqIfPresent(DemoOrderDO::getStatus, reqVO.getStatus())
            .betweenIfPresent(DemoOrderDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(DemoOrderDO::getId));
}
```

## Batch Query Pattern

Prefer this:

```java
List<DemoOrderDO> orders = orderMapper.selectBatchIds(orderIds);
Map<Long, DemoOrderDO> orderMap = convertMap(orders, DemoOrderDO::getId);
```

Avoid this:

```java
for (Long orderId : orderIds) {
    DemoOrderDO order = orderMapper.selectById(orderId);
}
```

The second pattern creates N+1 queries.

## Index Awareness

Before adding a query condition, consider whether the table has an index for it.

Good candidates:

- id equality
- tenant_id + business status
- tenant_id + unique business code
- create_time range with pagination

Bad candidates:

- Low-selectivity single-column status indexes without tenant or business context
- Fuzzy search on unindexed large text fields
- Unbounded full table scans

## XML SQL

Use XML SQL only when wrappers are insufficient, such as complex joins or custom aggregation. Keep XML query names aligned with Mapper method names.

## Checklist

Before finishing Mapper work, verify:

- [ ] Mapper uses `@Mapper`.
- [ ] Mapper extends `BaseMapperX<DO>`.
- [ ] Dynamic conditions use `LambdaQueryWrapperX`.
- [ ] Optional filters use `xxxIfPresent` helpers.
- [ ] Pagination returns `PageResult<DO>`.
- [ ] Pagination has explicit ordering.
- [ ] Query conditions match available or planned indexes.
- [ ] No N+1 query pattern exists.
- [ ] No unnecessary raw SQL is introduced.
