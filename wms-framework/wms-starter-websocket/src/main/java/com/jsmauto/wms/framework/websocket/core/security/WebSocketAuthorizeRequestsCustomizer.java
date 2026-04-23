package com.jsmauto.wms.framework.websocket.core.security;

import com.jsmauto.wms.framework.security.config.AuthorizeRequestsCustomizer;
import com.jsmauto.wms.framework.websocket.config.WebSocketProperties;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * WebSocket 的权限自定义
 *
 * @author jsmauto
 */
@RequiredArgsConstructor
public class WebSocketAuthorizeRequestsCustomizer extends AuthorizeRequestsCustomizer {

    private final WebSocketProperties webSocketProperties;

    @Override
    public void customize(Set<String> permitAllUrls) {
        permitAllUrls.add(webSocketProperties.getPath());
    }

}
