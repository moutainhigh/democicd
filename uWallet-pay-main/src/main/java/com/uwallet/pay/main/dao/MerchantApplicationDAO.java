package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.MerchantApplicationDTO;
import com.uwallet.pay.main.model.entity.MerchantApplication;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 商户申请表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 商户申请表
 * @author: zhoutt
 * @date: Created in 2021-04-14 11:28:05
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface MerchantApplicationDAO extends BaseDAO<MerchantApplication> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MerchantApplicationDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MerchantApplicationDTO
     * @param params
     * @return
     */
    MerchantApplicationDTO selectOneDTO(Map<String, Object> params);

    /**
     * 修改一条记录
     * @param params
     */
    void updateState(Map<String, Object> params);
}
