package com.jsmauto.wms.module.infra.framework.security.config;

import com.jsmauto.wms.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Infra 模块的 Security 配置
 */
@Configuration(proxyBeanMethods = false, value = "infraSecurityConfiguration")
public class SecurityConfiguration {

    @Bean("infraAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(Set<String> permitAllUrls) {
                // Swagger 接口文档
                permitAllUrls.add("/v3/api-docs/**");
                permitAllUrls.add("/webjars/**");
                permitAllUrls.add("/swagger-ui.html");
                permitAllUrls.add("/swagger-ui/**");
                // Spring Boot Actuator 的安全配置
                permitAllUrls.add("/actuator");
                permitAllUrls.add("/actuator/**");
                // Druid 监控
                permitAllUrls.add("/druid/**");
                // 文件读取
                permitAllUrls.add(buildAdminApi("/infra/file/*/get/**"));
            }

        };
    }

}
