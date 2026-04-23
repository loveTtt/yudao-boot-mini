package com.jsmauto.wms.framework.security.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.jsmauto.wms.framework.security.core.LoginUser;
import org.springframework.lang.Nullable;

/**
 * 登录用户上下文持有器
 *
 * 使用 TransmittableThreadLocal，保证异步线程场景下也可以透传当前登录用户。
 *
 * @author Codex
 */
public final class LoginUserContextHolder {

    private static final ThreadLocal<LoginUser> LOGIN_USER = new TransmittableThreadLocal<>();

    private LoginUserContextHolder() {
    }

    public static void set(@Nullable LoginUser loginUser) {
        if (loginUser == null) {
            clear();
            return;
        }
        LOGIN_USER.set(loginUser);
    }

    @Nullable
    public static LoginUser get() {
        return LOGIN_USER.get();
    }

    public static void clear() {
        LOGIN_USER.remove();
    }

}
