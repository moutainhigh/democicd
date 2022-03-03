package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.LoginMissDTO;
import com.uwallet.pay.main.model.entity.LoginMiss;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 登陆错误次数记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 登陆错误次数记录表
 * @author: baixinyue
 * @date: Created in 2020-01-02 13:56:13
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface LoginMissDAO extends BaseDAO<LoginMiss> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<LoginMissDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 LoginMissDTO
     * @param params
     * @return
     */
    LoginMissDTO selectOneDTO(Map<String, Object> params);

    /**
     * 登陆错误记录
     * @param loginMiss
     * @return
     */
    int loginMissRecord(LoginMiss loginMiss);

    /**
     * 修改status
     * @param packModifyBaseProps
     * @return
     */
    int updateLoginMissStatus(LoginMiss packModifyBaseProps);
}
