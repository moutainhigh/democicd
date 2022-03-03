package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.UserMonthlyDataDTO;
import com.uwallet.pay.main.model.entity.UserMonthlyData;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户每月统计表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户每月统计表
 * @author: zhoutt
 * @date: Created in 2021-04-08 16:40:22
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface UserMonthlyDataDAO extends BaseDAO<UserMonthlyData> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserMonthlyDataDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 UserMonthlyDataDTO
     * @param params
     * @return
     */
    UserMonthlyDataDTO selectOneDTO(Map<String, Object> params);

}
