package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ApiMerchantApplicationDTO;
import com.uwallet.pay.main.model.entity.ApiMerchantApplication;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * h5 api 商户申请表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: h5 api 商户申请表
 * @author: zhoutt
 * @date: Created in 2021-09-23 10:25:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface ApiMerchantApplicationDAO extends BaseDAO<ApiMerchantApplication> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ApiMerchantApplicationDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ApiMerchantApplicationDTO
     * @param params
     * @return
     */
    ApiMerchantApplicationDTO selectOneDTO(Map<String, Object> params);

}
