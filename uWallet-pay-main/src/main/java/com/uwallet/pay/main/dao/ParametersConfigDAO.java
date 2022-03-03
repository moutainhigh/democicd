package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ParametersConfigDTO;
import com.uwallet.pay.main.model.entity.ParametersConfig;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 系统配置表增加小额免密金额
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 系统配置表增加小额免密金额
 * @author: zhoutt
 * @date: Created in 2019-12-23 16:55:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface ParametersConfigDAO extends BaseDAO<ParametersConfig> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ParametersConfigDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ParametersConfigDTO
     * @param params
     * @return
     */
    ParametersConfigDTO selectOneDTO(Map<String, Object> params);

}
