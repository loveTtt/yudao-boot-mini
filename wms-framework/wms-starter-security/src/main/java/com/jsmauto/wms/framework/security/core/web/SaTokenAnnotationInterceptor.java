package com.jsmauto.wms.framework.security.core.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import com.jsmauto.wms.framework.common.exception.util.ServiceExceptionUtil;
import com.jsmauto.wms.framework.security.core.annotation.SaCheckScope;
import com.jsmauto.wms.framework.security.core.service.SecurityFrameworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;

import static com.jsmauto.wms.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;

/**
 * Sa-Token 注解桥接拦截器。
 *
 * 当前阶段仍然沿用原有 OAuth2 Token 的解析与登录上下文，
 * 因此这里直接复用项目已有的权限、角色、scope 校验逻辑，
 * 让控制器层可以逐步切换到 Sa-Token 风格注解，而不影响现有业务行为。
 *
 * @author Codex
 */
@RequiredArgsConstructor
public class SaTokenAnnotationInterceptor implements HandlerInterceptor {

    private final SecurityFrameworkService securityFrameworkService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (hasAnnotation(handlerMethod, SaIgnore.class)) {
            return true;
        }
        checkPermission(handlerMethod);
        checkRole(handlerMethod);
        checkScope(handlerMethod);
        return true;
    }

    private void checkPermission(HandlerMethod handlerMethod) {
        SaCheckPermission annotation = getAnnotation(handlerMethod, SaCheckPermission.class);
        if (annotation == null || annotation.value().length == 0) {
            return;
        }
        boolean passed = annotation.value().length == 1
                ? securityFrameworkService.hasPermission(annotation.value()[0])
                : securityFrameworkService.hasAnyPermissions(annotation.value());
        if (!passed) {
            throw ServiceExceptionUtil.exception(FORBIDDEN);
        }
    }

    private void checkRole(HandlerMethod handlerMethod) {
        SaCheckRole annotation = getAnnotation(handlerMethod, SaCheckRole.class);
        if (annotation == null || annotation.value().length == 0) {
            return;
        }
        boolean passed = annotation.value().length == 1
                ? securityFrameworkService.hasRole(annotation.value()[0])
                : securityFrameworkService.hasAnyRoles(annotation.value());
        if (!passed) {
            throw ServiceExceptionUtil.exception(FORBIDDEN);
        }
    }

    private void checkScope(HandlerMethod handlerMethod) {
        SaCheckScope annotation = getAnnotation(handlerMethod, SaCheckScope.class);
        if (annotation == null || annotation.value().length == 0) {
            return;
        }
        boolean passed = annotation.mode() == SaCheckScope.Mode.OR
                ? securityFrameworkService.hasAnyScopes(annotation.value())
                : Arrays.stream(annotation.value()).allMatch(securityFrameworkService::hasScope);
        if (!passed) {
            throw ServiceExceptionUtil.exception(FORBIDDEN);
        }
    }

    private <A extends Annotation> boolean hasAnnotation(HandlerMethod handlerMethod, Class<A> annotationType) {
        return AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), annotationType)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), annotationType);
    }

    private <A extends Annotation> A getAnnotation(HandlerMethod handlerMethod, Class<A> annotationType) {
        A annotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), annotationType);
        if (annotation != null) {
            return annotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), annotationType);
    }

}
