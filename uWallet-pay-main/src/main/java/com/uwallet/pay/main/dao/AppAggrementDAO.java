package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.AppAggrementDTO;
import com.uwallet.pay.main.model.entity.AppAggrement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * app 协议
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: app 协议
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:23
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface AppAggrementDAO extends BaseDAO<AppAggrement> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AppAggrementDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AppAggrementDTO
     * @param params
     * @return
     */
    AppAggrementDTO selectOneDTO(Map<String, Object> params);


}
