package com.jsmauto.wms.framework.security.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 免登录路径收集器
 *
 * 统一收敛静态配置和模块自定义配置，供登录拦截器复用。
 * 注解类放行（如 @SaIgnore / @PermitAll）改为在拦截阶段直接判断，
 * 避免在容器启动阶段提前触发 RequestMappingHandlerMapping 初始化，导致循环依赖。
 *
 * @author Codex
 */
public class PermitAllUrlCollector {

    @Resource
    private SecurityProperties securityProperties;
    @Autowired(required = false)
    private List<AuthorizeRequestsCustomizer> authorizeRequestsCustomizers = Collections.emptyList();

    public Set<String> collect() {
        LinkedHashSet<String> urls = new LinkedHashSet<>();
        // 静态资源
        urls.add("/*.html");
        urls.add("/*.css");
        urls.add("/*.js");
        // 配置文件定义
        securityProperties.getPermitAllUrls().stream()
                .filter(StrUtil::isNotBlank)
                .filter(url -> !"/**".equals(url))
                .forEach(urls::add);
        // 模块自定义放行
        CollUtil.sort(new ArrayList<>(authorizeRequestsCustomizers),
                        (left, right) -> Integer.compare(left.getOrder(), right.getOrder()))
                .forEach(customizer -> customizer.customize(urls));
        return urls;
    }

}
