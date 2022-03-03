package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.UserDetailRepaymentDTO;
import com.uwallet.pay.main.model.dto.UserStepLogDTO;
import com.uwallet.pay.main.model.entity.UserStepLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户权限阶段记录
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户权限阶段记录
 * @author: baixinyue
 * @date: Created in 2020-06-30 16:52:45
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface UserStepLogDAO extends BaseDAO<UserStepLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserStepLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 UserStepLogDTO
     * @param params
     * @return
     */
    UserStepLogDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询最新日志记录
     * @param stepId
     * @return
     */
    UserStepLogDTO findLatestStepLog(Long stepId);

    /**
     * 获取用户认证阶段记录
     * @param stepId
     * @return
     */
    List<UserStepLogDTO> findStepLog(Long stepId);

    /**
     * 获取用户认证阶段记录
     * @param params
     * @return
     */
    List<UserStepLogDTO> findStepLogNew(Map<String, Object> params);

    /**
     * 查询用户提交KYC记录
     * @param params
     * @return
     */
    int findKycLogListCount(Map<String, Object> params);

    /**
     * 查询用户提交KYC数据
     * @param params
     * @return
     */
    List<UserStepLogDTO> findKycLogList(Map<String, Object> params);
}
