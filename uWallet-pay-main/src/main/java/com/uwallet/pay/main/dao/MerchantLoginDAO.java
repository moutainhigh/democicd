package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.MerchantLoginDTO;
import com.uwallet.pay.main.model.entity.MerchantLogin;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户主表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户主表
 * @author: zhoutt
 * @date: Created in 2020-02-21 16:19:27
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface MerchantLoginDAO extends BaseDAO<MerchantLogin> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MerchantLoginDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MerchantLoginDTO
     * @param params
     * @return
     */
    MerchantLoginDTO selectOneDTO(Map<String, Object> params);

    /**
     * 模糊查询email
     * @param params
     * @return
     */
    List<MerchantLoginDTO> findByEmail(Map<String, Object> params);
}
