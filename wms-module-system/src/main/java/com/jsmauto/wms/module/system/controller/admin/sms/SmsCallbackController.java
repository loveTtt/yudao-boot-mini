package com.jsmauto.wms.module.system.controller.admin.sms;

import com.jsmauto.wms.framework.common.pojo.CommonResult;
import com.jsmauto.wms.framework.common.util.servlet.ServletUtils;
import com.jsmauto.wms.framework.tenant.core.aop.TenantIgnore;
import com.jsmauto.wms.module.system.framework.sms.core.enums.SmsChannelEnum;
import com.jsmauto.wms.module.system.service.sms.SmsSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.servlet.http.HttpServletRequest;

import static com.jsmauto.wms.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 短信回调")
@RestController
@RequestMapping("/system/sms/callback")
public class SmsCallbackController {

    @Resource
    private SmsSendService smsSendService;

    @PostMapping("/aliyun")
    @SaIgnore
    @TenantIgnore
    @Operation(summary = "阿里云短信的回调", description = "参见 https://help.aliyun.com/document_detail/120998.html 文档")
    public CommonResult<Boolean> receiveAliyunSmsStatus(HttpServletRequest request) throws Throwable {
        String text = ServletUtils.getBody(request);
        smsSendService.receiveSmsStatus(SmsChannelEnum.ALIYUN.getCode(), text);
        return success(true);
    }

    @PostMapping("/tencent")
    @SaIgnore
    @TenantIgnore
    @Operation(summary = "腾讯云短信的回调", description = "参见 https://cloud.tencent.com/document/product/382/52077 文档")
    public CommonResult<Boolean> receiveTencentSmsStatus(HttpServletRequest request) throws Throwable {
        String text = ServletUtils.getBody(request);
        smsSendService.receiveSmsStatus(SmsChannelEnum.TENCENT.getCode(), text);
        return success(true);
    }


    @PostMapping("/huawei")
    @SaIgnore
    @TenantIgnore
    @Operation(summary = "华为云短信的回调", description = "参见 https://support.huaweicloud.com/api-msgsms/sms_05_0003.html 文档")
    public CommonResult<Boolean> receiveHuaweiSmsStatus(@RequestBody String requestBody) throws Throwable {
        smsSendService.receiveSmsStatus(SmsChannelEnum.HUAWEI.getCode(), requestBody);
        return success(true);
    }

    @PostMapping("/qiniu")
    @SaIgnore
    @TenantIgnore
    @Operation(summary = "七牛云短信的回调", description = "参见 https://developer.qiniu.com/sms/5910/message-push 文档")
    public CommonResult<Boolean> receiveQiniuSmsStatus(@RequestBody String requestBody) throws Throwable {
        smsSendService.receiveSmsStatus(SmsChannelEnum.QINIU.getCode(), requestBody);
        return success(true);
    }

}