---
name: wms-menu-permission
description: "Use when adding or troubleshooting WMS backend menus, role permissions, button permissions, and frontend route integration. Enforces system_menu/system_role_menu, directory/menu/button model, permission strings, SaCheckPermission alignment, and route component paths."
---

# WMS Menu Permission Skill

## Overview

Use this skill when adding, reviewing, or troubleshooting menus, permissions, buttons, and frontend route integration in the WMS admin system.

The frontend route tree is driven by backend menu data. A page file existing in the frontend is not enough; the menu and role authorization must also be configured.

## Project Context

Key files and paths:

```text
wms-module-system/src/main/java/com/jsmauto/wms/module/system/controller/admin/permission/MenuController.java
wms-module-system/src/main/java/com/jsmauto/wms/module/system/service/permission/MenuService.java
wms-ui/wms-ui-admin-vue3/src/views/system/menu/index.vue
wms-ui/wms-ui-admin-vue3/src/store/modules/user.ts
wms-ui/wms-ui-admin-vue3/src/store/modules/permission.ts
sql/mysql/wms-asrs.sql
```

Related tables:

```text
system_menu
system_role_menu
```

Route data flow:

```text
AuthController
  -> AuthPermissionInfoRespVO
  -> frontend user store
  -> frontend permission store
  -> dynamic routes
```

## Menu Types

| Type | Purpose |
| --- | --- |
| Directory | Groups menus, usually no page component |
| Menu | Page entry, maps to frontend component |
| Button | Operation permission inside a page |

## Hard Rules

- Admin page entry MUST have a backend menu record.
- Button operations MUST have button permissions when backend has `@SaCheckPermission`.
- Permission strings MUST match between `system_menu` and `@SaCheckPermission`.
- Permission format MUST be `模块:资源:动作`.
- Frontend component path MUST match the actual file under `src/views`.
- Role authorization MUST be updated after adding menus/buttons.
- User may need to re-login after permission changes.
- Do not rely on frontend button hiding as the security boundary.

## Permission Convention

```text
模块:资源:动作
```

Examples:

```text
system:user:query
system:user:create
system:user:update
system:user:delete
system:user:export
infra:codegen:query
```

Controller example:

```java
@SaCheckPermission("demo:order:update")
@PutMapping("/update")
@Operation(summary = "更新订单")
public CommonResult<Boolean> updateOrder(@Valid @RequestBody DemoOrderSaveReqVO reqVO) {
    orderService.updateOrder(reqVO);
    return success(true);
}
```

The menu button permission must also be:

```text
demo:order:update
```

## Adding A Page Menu

Recommended steps:

1. Create or generate frontend page under `wms-ui/wms-ui-admin-vue3/src/views`.
2. Create backend Controller and permissions.
3. Add a directory if needed.
4. Add a menu record under the directory.
5. Set route path and component path.
6. Add button permissions for create/update/delete/export/query.
7. Grant menu and button permissions to target roles.
8. Re-login and verify route visibility.

## Component Path Example

If the file is:

```text
wms-ui/wms-ui-admin-vue3/src/views/demo/order/index.vue
```

The component path should follow the current project convention, typically:

```text
demo/order/index
```

Always compare with existing menu records before inventing a new format.

## Button Permission Example

```text
查询：demo:order:query
新增：demo:order:create
修改：demo:order:update
删除：demo:order:delete
导出：demo:order:export
```

## Troubleshooting

### Menu does not show

Check:

- Menu exists in `system_menu`.
- Menu status is enabled.
- Parent directory is enabled.
- Current role has menu permission.
- User has re-logged in.
- Component path is correct.

### API returns 403

Check:

- `@SaCheckPermission` value.
- Button permission in menu management.
- Role authorization.
- User login state after authorization changes.

### Page route is 404 or blank

Check:

- Frontend page file exists.
- Menu component path matches file path.
- Frontend dev server has picked up the new file.
- Dynamic routes were refreshed after login.

## Checklist

Before finishing menu/permission work, verify:

- [ ] Directory exists if needed.
- [ ] Menu points to the right frontend component.
- [ ] Buttons exist for protected operations.
- [ ] Permission strings follow `模块:资源:动作`.
- [ ] `@SaCheckPermission` matches button permission.
- [ ] Target roles are authorized.
- [ ] User re-login has been tested.
- [ ] Frontend route opens successfully.
