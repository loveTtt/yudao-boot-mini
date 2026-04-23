package com.jsmauto.wms.framework.security.config;

import cn.dev33.satoken.annotation.SaIgnore;
import com.jsmauto.wms.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.jsmauto.wms.framework.common.biz.system.permission.PermissionCommonApi;
import com.jsmauto.wms.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.jsmauto.wms.framework.common.exception.util.ServiceExceptionUtil;
import com.jsmauto.wms.framework.security.core.filter.TokenAuthenticationFilter;
import com.jsmauto.wms.framework.security.core.service.SecurityFrameworkService;
import com.jsmauto.wms.framework.security.core.service.SecurityFrameworkServiceImpl;
import com.jsmauto.wms.framework.security.core.web.SaTokenAnnotationInterceptor;
import com.jsmauto.wms.framework.security.core.util.SecurityFrameworkUtils;
import com.jsmauto.wms.framework.web.core.handler.GlobalExceptionHandler;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Set;

/**
 * 安全自动配置
 *
 * 第一阶段兼容迁移策略：
 * 1. 保留现有 Token 校验逻辑
 * 2. 由 Sa-Token 拦截器承接 Web 层登录校验
 * 3. 保留 Spring Security 的方法鉴权注解能力，避免一次性改动业务代码
 *
 * @author Codex
 */
@AutoConfiguration
@AutoConfigureOrder(-1)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    @Resource
    private SecurityProperties securityProperties;
    @Value("${spring.boot.admin.context-path:}")
    private String adminContextPath;

    /**
     * 密码加密器继续复用，避免影响现有密码存储与比对逻辑
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(securityProperties.getPasswordEncoderLength());
    }

    @Bean
    public FilterRegistrationBean<TokenAuthenticationFilter> tokenAuthenticationFilterRegistration(
            GlobalExceptionHandler globalExceptionHandler,
            OAuth2TokenCommonApi oauth2TokenApi) {
        FilterRegistrationBean<TokenAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TokenAuthenticationFilter(securityProperties, globalExceptionHandler, oauth2TokenApi));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("tokenAuthenticationFilter");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registrationBean;
    }

    @Bean("ss")
    public SecurityFrameworkService securityFrameworkService(PermissionCommonApi permissionApi) {
        return new SecurityFrameworkServiceImpl(permissionApi);
    }

    @Bean
    public PermitAllUrlCollector permitAllUrlCollector() {
        return new PermitAllUrlCollector();
    }

    @Bean
    public SaTokenAnnotationInterceptor saTokenAnnotationInterceptor(SecurityFrameworkService securityFrameworkService) {
        return new SaTokenAnnotationInterceptor(securityFrameworkService);
    }

    /**
     * 使用 Sa-Token 拦截器接管主业务 Web 登录校验
     */
    @Bean
    public WebMvcConfigurer saTokenWebMvcConfigurer(PermitAllUrlCollector permitAllUrlCollector,
                                                    SaTokenAnnotationInterceptor saTokenAnnotationInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                Set<String> permitAllUrls = permitAllUrlCollector.collect();
                // 兼容 Spring Boot Admin 默认路径，避免第一阶段迁移影响监控模块
                permitAllUrls.add("/login");
                permitAllUrls.add("/logout");
                permitAllUrls.add("/assets/**");
                permitAllUrls.add("/instances");
                permitAllUrls.add("/actuator");
                permitAllUrls.add("/actuator/**");
                permitAllUrls.add("/error");
                if (adminContextPath != null && !adminContextPath.isBlank()) {
                    permitAllUrls.add(adminContextPath + "/**");
                }
                registry.addInterceptor(new HandlerInterceptor() {
                            @Override
                            public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                                                     jakarta.servlet.http.HttpServletResponse response,
                                                     Object handler) {
                                if (isAnonymousHandler(handler)) {
                                    return true;
                                }
                                if (SecurityFrameworkUtils.getLoginUser() == null) {
                                    throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.UNAUTHORIZED);
                                }
                                return true;
                            }
                        })
                        .addPathPatterns("/**")
                        .excludePathPatterns(new ArrayList<>(permitAllUrls));
                registry.addInterceptor(saTokenAnnotationInterceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns(new ArrayList<>(permitAllUrls));
            }
        };
    }

    private boolean isAnonymousHandler(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        return AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), SaIgnore.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), SaIgnore.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), PermitAll.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), PermitAll.class);
    }

}
