package com.jsmauto.wms.module.system.framework.web.config;

import com.jsmauto.wms.framework.swagger.config.SwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的 web 组件的 Configuration
 *
 * @author jsmauto
 */
@Configuration(proxyBeanMethods = false)
public class SystemWebConfiguration {

    /**
     * system 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi systemGroupedOpenApi() {
        return SwaggerAutoConfiguration.buildGroupedOpenApi("system");
    }

}
