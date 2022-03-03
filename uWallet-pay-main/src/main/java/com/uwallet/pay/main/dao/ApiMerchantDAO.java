package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ApiMerchantDTO;
import com.uwallet.pay.main.model.entity.ApiMerchant;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * api商户信息表j
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: api商户信息表j
 * @author: zhoutt
 * @date: Created in 2021-09-02 17:32:33
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface ApiMerchantDAO extends BaseDAO<ApiMerchant> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ApiMerchantDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ApiMerchantDTO
     * @param params
     * @return
     */
    ApiMerchantDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询清算数据
     * @param params
     * @return
     */
    List<ApiMerchantDTO> getApiClearMerchantList(Map<String, Object> params);

    /**
     * 查询一级商户列表
     * @param params
     * @return
     */
    List<Map<String, String>> findSuperMerchantMap(Map<String, Object> params);

    /**
     * 获取当前最大Id
     * @return
     */
    Long getMaxId();
}
