package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.TrusteeDTO;
import com.uwallet.pay.main.model.entity.Trustee;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 受托人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 受托人信息表
 * @author: baixinyue
 * @date: Created in 2020-04-21 14:25:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface TrusteeDAO extends BaseDAO<Trustee> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<TrusteeDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 TrusteeDTO
     * @param params
     * @return
     */
    TrusteeDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据商户id删除受益人信息
     * @param merchantId
     * @return
     */
    int deleteTrusteeByMerchantId(@Param("merchantId") Long merchantId);

}
