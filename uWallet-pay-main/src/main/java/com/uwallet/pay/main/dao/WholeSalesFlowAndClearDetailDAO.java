package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.WholeSalesFlowAndClearDetailDTO;
import com.uwallet.pay.main.model.entity.WholeSalesFlowAndClearDetail;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 整体出售清算中间表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 整体出售清算中间表
 * @author: joker
 * @date: Created in 2020-10-22 09:28:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: joker
 */
@Mapper
public interface WholeSalesFlowAndClearDetailDAO extends BaseDAO<WholeSalesFlowAndClearDetail> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<WholeSalesFlowAndClearDetailDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 WholeSalesFlowAndClearDetailDTO
     * @param params
     * @return
     */
    WholeSalesFlowAndClearDetailDTO selectOneDTO(Map<String, Object> params);

}
