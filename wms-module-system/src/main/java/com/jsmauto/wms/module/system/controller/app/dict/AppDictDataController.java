package com.jsmauto.wms.module.system.controller.app.dict;

import com.jsmauto.wms.framework.common.enums.CommonStatusEnum;
import com.jsmauto.wms.framework.common.pojo.CommonResult;
import com.jsmauto.wms.framework.common.util.object.BeanUtils;
import com.jsmauto.wms.module.system.controller.app.dict.vo.AppDictDataRespVO;
import com.jsmauto.wms.module.system.dal.dataobject.dict.DictDataDO;
import com.jsmauto.wms.module.system.service.dict.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.dev33.satoken.annotation.SaIgnore;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static com.jsmauto.wms.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 字典数据")
@RestController
@RequestMapping("/system/dict-data")
@Validated
public class AppDictDataController {

    @Resource
    private DictDataService dictDataService;

    @GetMapping("/type")
    @Operation(summary = "根据字典类型查询字典数据信息")
    @Parameter(name = "type", description = "字典类型", required = true, example = "common_status")
    @SaIgnore
    public CommonResult<List<AppDictDataRespVO>> getDictDataListByType(@RequestParam("type") String type) {
        List<DictDataDO> list = dictDataService.getDictDataList(
                CommonStatusEnum.ENABLE.getStatus(), type);
        return success(BeanUtils.toBean(list, AppDictDataRespVO.class));
    }

}
