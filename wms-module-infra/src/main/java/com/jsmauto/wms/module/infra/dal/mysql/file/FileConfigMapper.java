package com.jsmauto.wms.module.infra.dal.mysql.file;

import com.jsmauto.wms.framework.common.pojo.PageResult;
import com.jsmauto.wms.framework.mybatis.core.mapper.BaseMapperX;
import com.jsmauto.wms.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.jsmauto.wms.module.infra.controller.admin.file.vo.config.FileConfigPageReqVO;
import com.jsmauto.wms.module.infra.dal.dataobject.file.FileConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileConfigMapper extends BaseMapperX<FileConfigDO> {

    default PageResult<FileConfigDO> selectPage(FileConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileConfigDO>()
                .likeIfPresent(FileConfigDO::getName, reqVO.getName())
                .eqIfPresent(FileConfigDO::getStorage, reqVO.getStorage())
                .betweenIfPresent(FileConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FileConfigDO::getId));
    }

    default FileConfigDO selectByMaster() {
        return selectOne(FileConfigDO::getMaster, true);
    }

}
