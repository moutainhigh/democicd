package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.UserActionDTO;
import com.uwallet.pay.main.model.entity.UserAction;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 商户端用户-权限关系表
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.mapper
 * @description: 商户端用户-权限关系表
 * @author: baixinyue
 * @date: Created in 2020-02-19 14:02:21
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface UserActionDAO extends BaseDAO<UserAction> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserActionDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 UserActionDTO
     * @param params
     * @return
     */
    UserActionDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据用户id删除权限
     * @param userId
     * @return
     */
    int deleteActionByUserId(Long userId);

}
