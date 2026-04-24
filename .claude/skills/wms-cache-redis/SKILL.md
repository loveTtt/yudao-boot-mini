---
name: wms-cache-redis
description: "Use when designing or reviewing WMS Redis and Spring Cache usage. Enforces RedisKeyConstants, Cacheable/CacheEvict rules, RedisDAO boundaries, dynamic TTL, cache invalidation, and avoiding premature or unsafe caching."
---

# WMS Cache Redis Skill

## Overview

Use this skill when adding, modifying, or reviewing cache behavior in the WMS project.

The project uses Spring Cache, Redis, Redisson, and `StringRedisTemplate`. Ordinary stable lookups can use Spring Cache. Dynamic TTL or custom serialization should use a RedisDAO.

## Project Context

Common paths:

```text
wms-module-*/src/main/java/com/jsmauto/wms/module/*/dal/redis/**
wms-module-*/src/main/java/com/jsmauto/wms/module/*/dal/redis/RedisKeyConstants.java
```

Spring Cache config:

```yaml
spring:
  cache:
    type: REDIS
    redis:
      time-to-live: 1h
```

## Hard Rules

- Cache keys MUST be centralized in `RedisKeyConstants`.
- Do not scatter hard-coded Redis keys in Service or Controller.
- Data mutations MUST evict or update related cache.
- Do not cache sensitive values unless there is a clear security design.
- Do not cache large objects without a real performance reason.
- Do not add cache as a first response to an unmeasured performance problem.
- Dynamic TTL scenarios SHOULD use RedisDAO.
- Token, captcha, temporary state, and session-like data MUST have expiration.

## Redis Key Convention

```java
public interface RedisKeyConstants {

    String DEPT_CHILDREN_ID_LIST = "dept_children_ids";
    String ROLE = "role";
    String USER_ROLE_ID_LIST = "user_role_ids";
    String OAUTH2_ACCESS_TOKEN = "oauth2_access_token:%s";
    String SMS_TEMPLATE = "sms_template";
}
```

Rules:

- Key names should describe business meaning.
- Variable keys should use format templates like `oauth2_access_token:%s`.
- Use one authoritative constant per key.

## Spring Cache Pattern

Use for stable lookup data:

```java
@Override
@Cacheable(cacheNames = RedisKeyConstants.SMS_TEMPLATE, key = "#code", unless = "#result == null")
public SmsTemplateDO getSmsTemplateByCodeFromCache(String code) {
    return smsTemplateMapper.selectByCode(code);
}
```

Evict after mutation:

```java
@Override
@CacheEvict(cacheNames = RedisKeyConstants.SMS_TEMPLATE, allEntries = true)
public void updateSmsTemplate(SmsTemplateSaveReqVO updateReqVO) {
    SmsTemplateDO updateObj = BeanUtils.toBean(updateReqVO, SmsTemplateDO.class);
    smsTemplateMapper.updateById(updateObj);
}
```

## RedisDAO Pattern

Use for dynamic TTL or custom serialization:

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

## When Not To Cache

Do not cache when:

- Query is not hot.
- Underlying data changes frequently and invalidation is unclear.
- Data contains secrets or user-specific sensitive state.
- Pagination result can explode in key cardinality.
- The same performance problem can be solved with an index or batch query.

## Checklist

Before finishing cache work, verify:

- [ ] Cache is actually justified.
- [ ] Key is defined in `RedisKeyConstants`.
- [ ] Key format includes necessary business dimensions.
- [ ] Write/delete paths evict or update cache.
- [ ] Null result caching is intentional.
- [ ] Sensitive fields are not cached accidentally.
- [ ] Dynamic TTL uses RedisDAO.
- [ ] Expiration exists for temporary state.
- [ ] Cache behavior is tested through update and delete paths.
