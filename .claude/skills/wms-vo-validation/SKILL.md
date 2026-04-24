---
name: wms-vo-validation
description: "Use when creating or reviewing WMS request and response VO classes. Enforces SaveReqVO, PageReqVO, RespVO separation, OpenAPI Schema annotations, Jakarta Validation, and avoiding DO leakage."
---

# WMS VO Validation Skill

## Overview

Use this skill when designing, creating, or reviewing request and response VO classes in the WMS backend.

VO classes define the API contract. They are not database entities and must not be treated as DO objects.

## Project Context

Common paths:

```text
wms-module-*/src/main/java/com/jsmauto/wms/module/*/controller/admin/**/vo/**
```

Common VO types:

| Type | Purpose |
| --- | --- |
| `SaveReqVO` | Create and update request body |
| `PageReqVO` | Pagination query request |
| `RespVO` | API response body |
| `SimpleRespVO` | Lightweight dropdown or reference response |
| `ExcelVO` | Import/export structure when applicable |

## Hard Rules

- Request VO MUST use `@Schema` on fields.
- Required fields MUST use Jakarta Validation annotations.
- Controller request bodies MUST validate VO with `@Valid`.
- `PageReqVO` MUST extend the project pagination base class when applicable.
- `RespVO` MUST NOT expose sensitive fields such as password, token, secret, or credential values.
- VO classes MUST NOT use MyBatis annotations like `@TableName` or `@TableId`.
- DO classes MUST NOT be reused as request or response bodies.

## Validation Rules

Use these annotations consistently:

| Case | Annotation |
| --- | --- |
| Required string | `@NotBlank` |
| Required object/id/number | `@NotNull` |
| Length limit | `@Size` |
| Email | `@Email` |
| Format rule | `@Pattern` |
| Nested object | `@Valid` |

## Good Pattern

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

## SaveReqVO Convention

```java
@Schema(description = "管理后台 - 示例保存 Request VO")
@Data
public class DemoSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例")
    @NotBlank(message = "名称不能为空")
    @Size(max = 64, message = "名称长度不能超过 64 个字符")
    private String name;
}
```

Create and update may share one `SaveReqVO` when the fields are genuinely the same. Do not split just for ceremony.

## PageReqVO Convention

```java
@Schema(description = "管理后台 - 示例分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DemoPageReqVO extends PageParam {

    @Schema(description = "名称", example = "示例")
    private String name;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
```

## RespVO Convention

```java
@Schema(description = "管理后台 - 示例 Response VO")
@Data
public class DemoRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例")
    private String name;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
```

## Checklist

Before finishing VO work, verify:

- [ ] VO type matches its purpose: Save, Page, Resp, Simple, or Excel.
- [ ] Fields have `@Schema`.
- [ ] Required fields have validation annotations.
- [ ] String length is constrained where user input is accepted.
- [ ] Format fields use `@Pattern`, `@Email`, or date formatting.
- [ ] Response VO does not expose sensitive fields.
- [ ] DO annotations are not used in VO.
- [ ] Controller uses `@Valid` for request VO.
