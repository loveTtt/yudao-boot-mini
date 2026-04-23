package com.jsmauto.wms.module.system.controller.admin.socail;

import com.jsmauto.wms.framework.common.pojo.CommonResult;
import com.jsmauto.wms.framework.common.pojo.PageResult;
import com.jsmauto.wms.framework.common.util.object.BeanUtils;
import com.jsmauto.wms.module.system.api.social.SocialClientApi;
import com.jsmauto.wms.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import com.jsmauto.wms.module.system.controller.admin.socail.vo.client.SocialClientPageReqVO;
import com.jsmauto.wms.module.system.controller.admin.socail.vo.client.SocialClientRespVO;
import com.jsmauto.wms.module.system.controller.admin.socail.vo.client.SocialClientSaveReqVO;
import com.jsmauto.wms.module.system.dal.dataobject.social.SocialClientDO;
import com.jsmauto.wms.module.system.service.social.SocialClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jsmauto.wms.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 社交客户端")
@RestController
@RequestMapping("/system/social-client")
@Validated
public class SocialClientController {

    @Resource
    private SocialClientService socialClientService;
    @Resource
    private SocialClientApi socialClientApi;

    @PostMapping("/create")
    @Operation(summary = "创建社交客户端")
    @SaCheckPermission("system:social-client:create")
    public CommonResult<Long> createSocialClient(@Valid @RequestBody SocialClientSaveReqVO createReqVO) {
        return success(socialClientService.createSocialClient(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新社交客户端")
    @SaCheckPermission("system:social-client:update")
    public CommonResult<Boolean> updateSocialClient(@Valid @RequestBody SocialClientSaveReqVO updateReqVO) {
        socialClientService.updateSocialClient(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除社交客户端")
    @Parameter(name = "id", description = "编号", required = true)
    @SaCheckPermission("system:social-client:delete")
    public CommonResult<Boolean> deleteSocialClient(@RequestParam("id") Long id) {
        socialClientService.deleteSocialClient(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @Operation(summary = "批量删除社交客户端")
    @SaCheckPermission("system:social-client:delete")
    public CommonResult<Boolean> deleteSocialClientList(@RequestParam("ids") List<Long> ids) {
        socialClientService.deleteSocialClientList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得社交客户端")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @SaCheckPermission("system:social-client:query")
    public CommonResult<SocialClientRespVO> getSocialClient(@RequestParam("id") Long id) {
        SocialClientDO client = socialClientService.getSocialClient(id);
        return success(BeanUtils.toBean(client, SocialClientRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得社交客户端分页")
    @SaCheckPermission("system:social-client:query")
    public CommonResult<PageResult<SocialClientRespVO>> getSocialClientPage(@Valid SocialClientPageReqVO pageVO) {
        PageResult<SocialClientDO> pageResult = socialClientService.getSocialClientPage(pageVO);
        return success(BeanUtils.toBean(pageResult, SocialClientRespVO.class));
    }

    @PostMapping("/send-subscribe-message")
    @Operation(summary = "发送订阅消息") // 用于测试
    @SaCheckPermission("system:social-client:query")
    public void sendSubscribeMessage(@RequestBody SocialWxaSubscribeMessageSendReqDTO reqDTO) {
        socialClientApi.sendWxaSubscribeMessage(reqDTO);
    }

}
