package com.jsmauto.wms.framework.security.core.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.jsmauto.wms.framework.security.core.LoginUser;
import com.jsmauto.wms.framework.security.core.context.LoginUserContextHolder;
import com.jsmauto.wms.framework.web.core.util.WebFrameworkUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全服务工具类
 *
 * @author jsmauto
 */
public class SecurityFrameworkUtils {

    /**
     * HEADER 认证头 value 的前缀
     */
    public static final String AUTHORIZATION_BEARER = "Bearer";

    private SecurityFrameworkUtils() {}

    /**
     * 从请求中，获得认证 Token
     *
     * @param request 请求
     * @param headerName 认证 Token 对应的 Header 名字
     * @param parameterName 认证 Token 对应的 Parameter 名字
     * @return 认证 Token
     */
    public static String obtainAuthorization(HttpServletRequest request,
                                             String headerName, String parameterName) {
        // 1. 获得 Token。优先级：Header > Parameter
        String token = request.getHeader(headerName);
        if (StrUtil.isEmpty(token)) {
            token = request.getParameter(parameterName);
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        // 2. 去除 Token 中带的 Bearer
        int index = token.indexOf(AUTHORIZATION_BEARER + " ");
        return index >= 0 ? token.substring(index + 7).trim() : token;
    }

    /**
     * 获取当前用户
     *
     * @return 当前用户
     */
    @Nullable
    public static LoginUser getLoginUser() {
        return LoginUserContextHolder.get();
    }

    /**
     * 获得当前用户的编号，从上下文中
     *
     * @return 用户编号
     */
    @Nullable
    public static Long getLoginUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getId() : null;
    }

    /**
     * 获得当前用户的昵称，从上下文中
     *
     * @return 昵称
     */
    @Nullable
    public static String getLoginUserNickname() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? MapUtil.getStr(loginUser.getInfo(), LoginUser.INFO_KEY_NICKNAME) : null;
    }

    /**
     * 获得当前用户的部门编号，从上下文中
     *
     * @return 部门编号
     */
    @Nullable
    public static Long getLoginUserDeptId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? MapUtil.getLong(loginUser.getInfo(), LoginUser.INFO_KEY_DEPT_ID) : null;
    }

    /**
     * 设置当前用户
     *
     * @param loginUser 登录用户
     * @param request 请求
     */
    public static void setLoginUser(LoginUser loginUser, HttpServletRequest request) {
        LoginUserContextHolder.set(loginUser);

        // 额外设置到 request 中，用于 ApiAccessLogFilter 可以获取到用户编号
        // 这样即使后续逻辑不再依赖 Spring Security 上下文，也能沿用现有日志链路
        if (request != null) {
            WebFrameworkUtils.setLoginUserId(request, loginUser.getId());
            WebFrameworkUtils.setLoginUserType(request, loginUser.getUserType());
        }
    }

    /**
     * 清理当前登录用户
     */
    public static void clearLoginUser() {
        LoginUserContextHolder.clear();
    }

    /**
     * 是否条件跳过权限校验，包括数据权限、功能权限
     *
     * @return 是否跳过
     */
    public static boolean skipPermissionCheck() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        if (loginUser.getVisitTenantId() == null) {
            return false;
        }
        // 重点：跨租户访问时，无法进行权限校验
        return ObjUtil.notEqual(loginUser.getVisitTenantId(), loginUser.getTenantId());
    }

}
