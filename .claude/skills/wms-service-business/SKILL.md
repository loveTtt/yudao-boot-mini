---
name: wms-service-business
description: "Use when creating or reviewing WMS Service layer business logic. Enforces thin controllers, transaction boundaries, unified business exceptions, mapper orchestration, cache interaction, and avoiding misplaced logic."
---

# WMS Service Business Skill

## Overview

Use this skill when implementing or reviewing Service layer logic in the WMS backend.

Service classes own business rules, orchestration, validation against persisted state, transaction boundaries, and interaction with Mapper, RedisDAO, and external services.

## Project Context

Common path:

```text
wms-module-*/src/main/java/com/jsmauto/wms/module/*/service/**
```

Common shape:

```java
public interface DemoService {
    Long createDemo(DemoSaveReqVO reqVO);
}

@Service
@Validated
public class DemoServiceImpl implements DemoService {

    @Resource
    private DemoMapper demoMapper;

    @Override
    public Long createDemo(DemoSaveReqVO reqVO) {
        DemoDO demo = BeanUtils.toBean(reqVO, DemoDO.class);
        demoMapper.insert(demo);
        return demo.getId();
    }
}
```

## Hard Rules

- Controller MUST call Service, not Mapper.
- Service owns business validation and orchestration.
- Multi-table writes MUST use transaction boundaries.
- Business failures MUST use the project unified exception mechanism.
- Service MUST NOT return raw failure states such as `null` or `false` when an exception is appropriate.
- Service MUST NOT swallow exceptions silently.
- Redis key construction SHOULD be encapsulated in RedisDAO, not scattered in Service.
- Service should return only data required by callers.
- Do not add abstractions for hypothetical future use.

## Business Validation Pattern

```java
private DemoDO validateDemoExists(Long id) {
    DemoDO demo = demoMapper.selectById(id);
    if (demo == null) {
        throw exception(DEMO_NOT_EXISTS);
    }
    return demo;
}
```

Use validation helper methods when the same persisted-state check is required by multiple service methods.

## Transaction Pattern

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void updateDemoWithItems(DemoSaveReqVO reqVO) {
    validateDemoExists(reqVO.getId());
    demoMapper.updateById(BeanUtils.toBean(reqVO, DemoDO.class));
    demoItemMapper.deleteByDemoId(reqVO.getId());
    demoItemMapper.insertBatch(buildItems(reqVO));
}
```

Use transactions for multi-step writes that must succeed or fail together.

## Cache Interaction Pattern

If Service changes cached data, it must clear or update cache through the project cache mechanism.

```java
@Override
@CacheEvict(cacheNames = RedisKeyConstants.SMS_TEMPLATE, allEntries = true)
public void updateSmsTemplate(SmsTemplateSaveReqVO updateReqVO) {
    SmsTemplateDO updateObj = BeanUtils.toBean(updateReqVO, SmsTemplateDO.class);
    smsTemplateMapper.updateById(updateObj);
}
```

## Good Service Responsibilities

Service should handle:

- Checking business existence.
- Checking uniqueness.
- Calling Mapper.
- Calling RedisDAO.
- Calling other module APIs.
- Managing transactions.
- Converting request VO to DO when appropriate.

Service should not handle:

- HTTP request parsing.
- OpenAPI annotations.
- Frontend response shape.
- Raw Redis key formatting outside RedisDAO.
- Direct SQL string assembly.

## Checklist

Before finishing Service work, verify:

- [ ] Controller does not call Mapper directly.
- [ ] Business checks are in Service.
- [ ] Multi-table writes are transactional.
- [ ] Business errors use unified exception mechanism.
- [ ] No silent exception swallowing.
- [ ] Cache is evicted or updated after writes.
- [ ] No N+1 query orchestration is introduced.
- [ ] No unnecessary abstraction was added.
- [ ] Method names describe business intent clearly.
