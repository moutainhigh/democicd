package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.UserStepDTO;
import com.uwallet.pay.main.model.entity.UserStep;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户权限阶段
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户权限阶段
 * @author: baixinyue
 * @date: Created in 2020-06-30 16:51:35
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface UserStepDAO extends BaseDAO<UserStep> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserStepDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 UserStepDTO
     * @param params
     * @return
     */
    UserStepDTO selectOneDTO(Map<String, Object> params);

}
