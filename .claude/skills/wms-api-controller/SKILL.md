---
name: wms-api-controller
description: "Use when creating or reviewing WMS backend Controller APIs. Enforces project conventions for REST paths, OpenAPI docs, permissions, validation, CommonResult responses, and keeping business logic out of controllers."
---

# WMS API Controller Skill

## Overview

Use this skill when creating, modifying, or reviewing backend Controller code in the WMS project.

The current project uses Spring Boot 3, SpringDoc, Sa-Token, Jakarta Validation, and unified response objects. Controller code must stay thin: receive requests, validate parameters, declare permissions, call Service, and return `CommonResult`.

## Project Context

Common paths:

```text
wms-module-*/src/main/java/com/jsmauto/wms/module/*/controller/admin/**
wms-framework/wms-common/src/main/java/com/jsmauto/wms/framework/common/pojo/CommonResult.java
```

Typical example:

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

## Hard Rules

- Controller classes MUST use `@RestController`.
- Controller classes MUST use `@RequestMapping("/模块/资源")`.
- Controller classes MUST use `@Validated`.
- Controller classes MUST use `@Tag`.
- Controller methods MUST use `@Operation`.
- Admin APIs MUST use `@SaCheckPermission` unless there is a clear public-access reason.
- Request body parameters MUST use `@Valid @RequestBody`.
- Responses MUST use `CommonResult<T>`.
- Pagination responses MUST use `CommonResult<PageResult<T>>`.
- Controller MUST NOT directly return DO objects.
- Controller MUST NOT access Mapper directly.
- Controller MUST NOT contain business rules, SQL, Redis key construction, or transaction orchestration.

## HTTP Method Convention

| Operation | Method | Path |
| --- | --- | --- |
| Create | `POST` | `/create` |
| Update | `PUT` | `/update` |
| Delete | `DELETE` | `/delete` |
| Detail | `GET` | `/get` |
| Page | `GET` | `/page` |
| Export | `GET` | `/export-excel` |

## Permission Convention

Permission format:

```text
模块:资源:动作
```

Examples:

```text
system:user:create
system:user:update
system:user:delete
system:user:query
system:user:export
```

The permission in `@SaCheckPermission` MUST match the menu or button permission configured in `system_menu`.

## Good Pattern

```java
@GetMapping("/page")
@Operation(summary = "获得用户分页列表")
@SaCheckPermission("system:user:query")
public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO reqVO) {
    PageResult<AdminUserDO> pageResult = userService.getUserPage(reqVO);
    return success(UserConvert.INSTANCE.convertPage(pageResult));
}
```

## Bad Patterns

Do not write this:

```java
@GetMapping("/list")
public List<AdminUserDO> list() {
    return userMapper.selectList();
}
```

Problems:

- No `CommonResult`.
- Returns DO directly.
- Controller calls Mapper.
- No permission check.
- No pagination.

## Checklist

Before finishing Controller work, verify:

- [ ] Path follows `/模块/资源`.
- [ ] Method path follows project CRUD convention.
- [ ] `@Tag` and `@Operation` are present.
- [ ] Admin API has `@SaCheckPermission`.
- [ ] Permission string matches menu/button permission.
- [ ] Request VO uses validation.
- [ ] Response uses `CommonResult<T>`.
- [ ] Pagination uses `PageResult<T>`.
- [ ] Controller calls Service only.
- [ ] No DO is returned to frontend.
