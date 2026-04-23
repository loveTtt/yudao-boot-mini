package com.jsmauto.wms.module.infra.dal.mysql.db;

import com.jsmauto.wms.framework.mybatis.core.mapper.BaseMapperX;
import com.jsmauto.wms.module.infra.dal.dataobject.db.DataSourceConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源配置 Mapper
 *
 * @author jsmauto
 */
@Mapper
public interface DataSourceConfigMapper extends BaseMapperX<DataSourceConfigDO> {
}
