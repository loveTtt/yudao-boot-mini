package com.jsmauto.wms.module.system.api.logger;

import com.jsmauto.wms.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.jsmauto.wms.module.system.service.logger.LoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 登录日志的 API 实现类
 *
 * @author jsmauto
 */
@Service
@Validated
public class LoginLogApiImpl implements LoginLogApi {

    @Resource
    private LoginLogService loginLogService;

    @Override
    public void createLoginLog(LoginLogCreateReqDTO reqDTO) {
        loginLogService.createLoginLog(reqDTO);
    }

}
