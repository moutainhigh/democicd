package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.UserInfoUpdateLogDTO;
import com.uwallet.pay.main.model.entity.UserInfoUpdateLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户信息修改记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户信息修改记录表
 * @author: xucl
 * @date: Created in 2021-09-10 16:55:37
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface UserInfoUpdateLogDAO extends BaseDAO<UserInfoUpdateLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserInfoUpdateLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 UserInfoUpdateLogDTO
     * @param params
     * @return
     */
    UserInfoUpdateLogDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询列表
     * @param params
     * @return
     */
    List<UserInfoUpdateLogDTO> findUpdateList(Map<String, Object> params);

    /**
     *  查询一条记录
     * @param param
     * @return
     */
    UserInfoUpdateLogDTO findOneUserInfoUpdateLogMax(JSONObject param);
}
