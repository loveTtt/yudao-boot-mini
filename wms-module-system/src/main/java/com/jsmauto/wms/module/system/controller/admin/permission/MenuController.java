package com.jsmauto.wms.module.system.controller.admin.permission;

import com.jsmauto.wms.framework.common.enums.CommonStatusEnum;
import com.jsmauto.wms.framework.common.pojo.CommonResult;
import com.jsmauto.wms.framework.common.util.object.BeanUtils;
import com.jsmauto.wms.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import com.jsmauto.wms.module.system.controller.admin.permission.vo.menu.MenuRespVO;
import com.jsmauto.wms.module.system.controller.admin.permission.vo.menu.MenuSaveVO;
import com.jsmauto.wms.module.system.controller.admin.permission.vo.menu.MenuSimpleRespVO;
import com.jsmauto.wms.module.system.dal.dataobject.permission.MenuDO;
import com.jsmauto.wms.module.system.service.permission.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

import static com.jsmauto.wms.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 菜单")
@RestController
@RequestMapping("/system/menu")
@Validated
public class MenuController {

    @Resource
    private MenuService menuService;

    @PostMapping("/create")
    @Operation(summary = "创建菜单")
    @SaCheckPermission("system:menu:create")
    public CommonResult<Long> createMenu(@Valid @RequestBody MenuSaveVO createReqVO) {
        Long menuId = menuService.createMenu(createReqVO);
        return success(menuId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改菜单")
    @SaCheckPermission("system:menu:update")
    public CommonResult<Boolean> updateMenu(@Valid @RequestBody MenuSaveVO updateReqVO) {
        menuService.updateMenu(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除菜单")
    @Parameter(name = "id", description = "菜单编号", required= true, example = "1024")
    @SaCheckPermission("system:menu:delete")
    public CommonResult<Boolean> deleteMenu(@RequestParam("id") Long id) {
        menuService.deleteMenu(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除菜单")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @SaCheckPermission("system:menu:delete")
    public CommonResult<Boolean> deleteMenuList(@RequestParam("ids") List<Long> ids) {
        menuService.deleteMenuList(ids);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取菜单列表", description = "用于【菜单管理】界面")
    @SaCheckPermission("system:menu:query")
    public CommonResult<List<MenuRespVO>> getMenuList(MenuListReqVO reqVO) {
        List<MenuDO> list = menuService.getMenuList(reqVO);
        list.sort(Comparator.comparing(MenuDO::getSort));
        return success(BeanUtils.toBean(list, MenuRespVO.class));
    }

    @GetMapping({"/list-all-simple", "simple-list"})
    @Operation(summary = "获取菜单精简信息列表",
            description = "只包含被开启的菜单，用于【角色分配菜单】功能的选项。在多租户的场景下，会只返回租户所在套餐有的菜单")
    public CommonResult<List<MenuSimpleRespVO>> getSimpleMenuList() {
        List<MenuDO> list = menuService.getMenuListByTenant(
                new MenuListReqVO().setStatus(CommonStatusEnum.ENABLE.getStatus()));
        list = menuService.filterDisableMenus(list);
        list.sort(Comparator.comparing(MenuDO::getSort));
        return success(BeanUtils.toBean(list, MenuSimpleRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取菜单信息")
    @SaCheckPermission("system:menu:query")
    public CommonResult<MenuRespVO> getMenu(Long id) {
        MenuDO menu = menuService.getMenu(id);
        return success(BeanUtils.toBean(menu, MenuRespVO.class));
    }

}
