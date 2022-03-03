package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.RequestAnalysisDTO;
import com.uwallet.pay.main.model.entity.RequestAnalysis;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 接口请求数据统计表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 接口请求数据统计表
 * @author: aaronS
 * @date: Created in 2021-02-06 14:03:58
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaronS
 */
@Mapper
public interface RequestAnalysisDAO extends BaseDAO<RequestAnalysis> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RequestAnalysisDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 RequestAnalysisDTO
     * @param params
     * @return
     */
    RequestAnalysisDTO selectOneDTO(Map<String, Object> params);

}
