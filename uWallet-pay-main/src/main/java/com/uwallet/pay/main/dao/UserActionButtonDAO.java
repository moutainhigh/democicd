package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.UserActionButtonDTO;
import com.uwallet.pay.main.model.entity.UserActionButton;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户冻结表存在该表的用户可以被冻结和解冻
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户冻结表存在该表的用户可以被冻结和解冻
 * @author: xucl
 * @date: Created in 2021-09-10 09:35:21
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface UserActionButtonDAO extends BaseDAO<UserActionButton> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserActionButtonDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 UserActionButtonDTO
     * @param params
     * @return
     */
    UserActionButtonDTO selectOneDTO(Map<String, Object> params);

}
