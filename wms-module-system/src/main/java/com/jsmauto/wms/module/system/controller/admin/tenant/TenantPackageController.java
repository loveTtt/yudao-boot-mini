package com.jsmauto.wms.module.system.controller.admin.tenant;

import com.jsmauto.wms.framework.common.enums.CommonStatusEnum;
import com.jsmauto.wms.framework.common.pojo.CommonResult;
import com.jsmauto.wms.framework.common.pojo.PageResult;
import com.jsmauto.wms.framework.common.util.object.BeanUtils;
import com.jsmauto.wms.module.system.controller.admin.tenant.vo.packages.TenantPackagePageReqVO;
import com.jsmauto.wms.module.system.controller.admin.tenant.vo.packages.TenantPackageRespVO;
import com.jsmauto.wms.module.system.controller.admin.tenant.vo.packages.TenantPackageSaveReqVO;
import com.jsmauto.wms.module.system.controller.admin.tenant.vo.packages.TenantPackageSimpleRespVO;
import com.jsmauto.wms.module.system.dal.dataobject.tenant.TenantPackageDO;
import com.jsmauto.wms.module.system.service.tenant.TenantPackageService;
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

@Tag(name = "管理后台 - 租户套餐")
@RestController
@RequestMapping("/system/tenant-package")
@Validated
public class TenantPackageController {

    @Resource
    private TenantPackageService tenantPackageService;

    @PostMapping("/create")
    @Operation(summary = "创建租户套餐")
    @SaCheckPermission("system:tenant-package:create")
    public CommonResult<Long> createTenantPackage(@Valid @RequestBody TenantPackageSaveReqVO createReqVO) {
        return success(tenantPackageService.createTenantPackage(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新租户套餐")
    @SaCheckPermission("system:tenant-package:update")
    public CommonResult<Boolean> updateTenantPackage(@Valid @RequestBody TenantPackageSaveReqVO updateReqVO) {
        tenantPackageService.updateTenantPackage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除租户套餐")
    @Parameter(name = "id", description = "编号", required = true)
    @SaCheckPermission("system:tenant-package:delete")
    public CommonResult<Boolean> deleteTenantPackage(@RequestParam("id") Long id) {
        tenantPackageService.deleteTenantPackage(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @Operation(summary = "批量删除租户套餐")
    @SaCheckPermission("system:tenant-package:delete")
    public CommonResult<Boolean> deleteTenantPackageList(@RequestParam("ids") List<Long> ids) {
        tenantPackageService.deleteTenantPackageList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得租户套餐")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @SaCheckPermission("system:tenant-package:query")
    public CommonResult<TenantPackageRespVO> getTenantPackage(@RequestParam("id") Long id) {
        TenantPackageDO tenantPackage = tenantPackageService.getTenantPackage(id);
        return success(BeanUtils.toBean(tenantPackage, TenantPackageRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得租户套餐分页")
    @SaCheckPermission("system:tenant-package:query")
    public CommonResult<PageResult<TenantPackageRespVO>> getTenantPackagePage(@Valid TenantPackagePageReqVO pageVO) {
        PageResult<TenantPackageDO> pageResult = tenantPackageService.getTenantPackagePage(pageVO);
        return success(BeanUtils.toBean(pageResult, TenantPackageRespVO.class));
    }

    @GetMapping({"/get-simple-list", "simple-list"})
    @Operation(summary = "获取租户套餐精简信息列表", description = "只包含被开启的租户套餐，主要用于前端的下拉选项")
    public CommonResult<List<TenantPackageSimpleRespVO>> getTenantPackageList() {
        List<TenantPackageDO> list = tenantPackageService.getTenantPackageListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(BeanUtils.toBean(list, TenantPackageSimpleRespVO.class));
    }

}
