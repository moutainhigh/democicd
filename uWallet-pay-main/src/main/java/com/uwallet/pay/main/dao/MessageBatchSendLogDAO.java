package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.MessageBatchSendLogDTO;
import com.uwallet.pay.main.model.dto.UserDTO;
import com.uwallet.pay.main.model.entity.MessageBatchSendLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 批量发送消息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 批量发送消息表
 * @author: xucl
 * @date: Created in 2021-05-11 14:18:13
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface MessageBatchSendLogDAO extends BaseDAO<MessageBatchSendLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MessageBatchSendLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MessageBatchSendLogDTO
     * @param params
     * @return
     */
    MessageBatchSendLogDTO selectOneDTO(Map<String, Object> params);

    /**
     * 未绑卡也未开通分期付用户个数
     * @param params
     * @return
     */
    int getNoCardNoCreditUserCount(Map<String, Object> params);

    /**
     * 未开申请分期付用户个数
     * @param params
     * @return
     */
    int getNoCreditUserCount(Map<String, Object> params);

    /**
     * 未绑卡用户个数
     * @param params
     * @returnd
     */
    int getNoCardUserCount(Map<String, Object> params);

    /**
     * 分期付开通拒绝
     * @param params
     * @return
     */
    int getCreditRefuseUserCount(Map<String, Object> params);

    /**
     * 用户未获取到illion报告
     * @param params
     * @return
     */
    int getNoIllionUserCount(Map<String, Object> params);

    /**
     *  有红包的用户
     * @param params
     * @return
     */
    int getHaveRedEnvelopeUserCount(Map<String, Object> params);

    /**
     * 无交易的用户
     * @param params
     * @return
     */
    int getNoTradeUserCount(Map<String, Object> params);

    /**
     * 消费过但是一段时间未消费的用户
     * @param params
     * @return
     */
    int getNoTradeLongTimeUserCount(Map<String, Object> params);

    /**
     * KYC拒绝的用户
     * @param params
     * @return
     */
    int getKYCRefuseUserCount(Map<String, Object> params);

    /**
     * 分期付已逾期一周以上的用户
     * @param params
     * @return
     */
    int getOverdueLongTimeUserCount(Map<String, Object> params);

    /**
     * 已产生逾期费的用户
     * @param params
     * @return
     */
    int getHaveDemurrageUserCount(Map<String, Object> params);
    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MessageBatchSendLogDTO> selectDTONew(Map<String, Object> params);
    /**
     * 未绑卡用户列表
     * @param params
     * @returnd
     */
    List<UserDTO> getNoCardUserList(Map<String, Object> params);
    /**
     * 未绑卡也未开通分期付用户列表
     * @param params
     * @return
     */
    List<UserDTO> getNoCardNoCreditUserList(Map<String, Object> params);
    /**
     * 未开申请分期付用户列表
     * @param params
     * @return
     */
    List<UserDTO> getNoCreditUserList(Map<String, Object> params);
    /**
     * 分期付开通列表
     * @param params
     * @return
     */
    List<UserDTO> getCreditRefuseUserList(Map<String, Object> params);
    /**
     * KYC拒绝的用户
     * @param params
     * @return
     */
    List<UserDTO> getKYCRefuseUserList(Map<String, Object> params);
    /**
     * 用户未获取到illion报告
     * @param params
     * @return
     */
    List<UserDTO> getNoIllionUserList(Map<String, Object> params);

    /**
     *  有红包的用户列表
     * @param params
     * @return
     */
    List<UserDTO> getHaveRedEnvelopeUserList(Map<String, Object> params);
    /**
     * 无交易的用户列表
     * @param params
     * @return
     */
    List<UserDTO> getNoTradeUserList(Map<String, Object> params);
    /**
     * 消费过但是一段时间未消费的用户列表
     * @param params
     * @return
     */
    List<UserDTO> getNoTradeLongTimeUserList(Map<String, Object> params);
    /**
     * 分期付已逾期一周以上的用户列表
     * @param params
     * @return
     */
    List<UserDTO> getOverdueLongTimeUserList(Map<String, Object> params);
    /**
     * 已产生逾期费的用户列表
     * @param params
     * @return
     */
    List<UserDTO> getHaveDemurrageUserList(Map<String, Object> params);

    /**
     * 修改发送成功数量
     * @param param
     * @return
     */
    int updateBatchNumber(JSONObject param);
}
