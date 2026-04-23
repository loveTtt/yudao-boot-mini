package com.jsmauto.wms.framework.security.config;

import com.jsmauto.wms.framework.web.config.WebProperties;
import jakarta.annotation.Resource;
import org.springframework.core.Ordered;

import java.util.Set;

/**
 * 自定义的 URL 放行配置
 * 目的：每个 Maven Module 可以补充自己的免登录路径
 *
 * @author jsmauto
 */
public abstract class AuthorizeRequestsCustomizer implements Ordered {

    @Resource
    private WebProperties webProperties;

    protected String buildAdminApi(String url) {
        return webProperties.getAdminApi().getPrefix() + url;
    }

    protected String buildAppApi(String url) {
        return webProperties.getAppApi().getPrefix() + url;
    }

    /**
     * 将当前模块需要放行的路径追加到 permitAllUrls 中
     *
     * @param permitAllUrls 放行路径集合
     */
    public abstract void customize(Set<String> permitAllUrls);

    @Override
    public int getOrder() {
        return 0;
    }

}
