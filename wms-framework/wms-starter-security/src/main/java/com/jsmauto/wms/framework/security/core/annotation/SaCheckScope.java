package com.jsmauto.wms.framework.security.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Scope 校验注解。
 *
 * 由于当前项目仍然复用原有 OAuth2 Token 与 scope 存储结构，
 * 这里先补一层轻量桥接，避免在第二阶段改造里扩大到整套 Token 发放链路。
 *
 * @author Codex
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SaCheckScope {

    /**
     * 需要校验的 scope 列表
     */
    String[] value();

    /**
     * 多个 scope 的组合关系
     */
    Mode mode() default Mode.AND;

    enum Mode {
        /**
         * 全部命中
         */
        AND,
        /**
         * 任一命中
         */
        OR
    }

}
