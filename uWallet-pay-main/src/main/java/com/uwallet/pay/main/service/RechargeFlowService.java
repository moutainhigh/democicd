package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.RechargeBorrowDTO;
import com.uwallet.pay.main.model.dto.RechargeDTO;
import com.uwallet.pay.main.model.dto.RechargeFlowDTO;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;
import com.uwallet.pay.main.model.entity.RechargeFlow;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.exception.BizException;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 充值交易流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service
 * @description: 充值交易流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:49:32
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
public interface RechargeFlowService extends BaseService {

    /**
     * 保存一条 RechargeFlow 数据
     *
     * @param rechargeFlowDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    Long saveRechargeFlow(RechargeFlowDTO rechargeFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 RechargeFlow 数据
     *
     * @param rechargeFlowList 待保存的数据列表
     * @throws BizException 保存失败异常
     */
    void saveRechargeFlowList(List<RechargeFlow> rechargeFlowList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 RechargeFlow 数据
     *
     * @param id              数据唯一id
     * @param rechargeFlowDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateRechargeFlow(Long id, RechargeFlowDTO rechargeFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 rechargeFlow
     *
     * @param dataMap      需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateRechargeFlowSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 RechargeFlow
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteRechargeFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 RechargeFlow
     *
     * @param id 数据唯一id
     * @throws BizException 物理删除异常
     */
    void deleteRechargeFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 RechargeFlow
     *
     * @param id 数据唯一id
     * @return 查询到的 RechargeFlow 数据
     */
    RechargeFlowDTO findRechargeFlowById(Long id);

    /**
     * 根据条件查询得到第一条 rechargeFlow
     *
     * @param params 查询条件
     * @return 符合条件的一个 rechargeFlow
     */
    RechargeFlowDTO findOneRechargeFlow(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<RechargeFlowDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params  查询条件
     * @param columns 需要查询的字段信息
     * @param scs     排序信息
     * @param pc      分页信息
     * @return 查询结果的数据集合
     * @throws BizException 查询异常
     */
    List<Map> findMap(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException;

    /**
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int count(Map<String, Object> params);

    /**
     * 根据给定字段以及查询条件进行分组查询，并统计id的count
     *
     * @param group      分组的字段
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的统计条数
     */
    Map<String, Integer> groupCount(String group, Map<String, Object> conditions);

    /**
     * 根据给定字段查询统计字段的sum结果
     *
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 返回sum计算的结果值
     */
    Double sum(String sumField, Map<String, Object> conditions);

    /**
     * 根据给定字段以及查询条件进行分组查询，并sum统计Field
     *
     * @param group      分组的字段。
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的求和
     */
    Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions);

    /**
     * latPay充值
     *
     * @param rechargeDTO
     * @param request
     */
    void rechargeByLatPay(RechargeDTO rechargeDTO, HttpServletRequest request) throws Exception;

    /**
     * omiPay充值
     * @param rechargeDTO
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject rechargeByOmiPay(RechargeDTO rechargeDTO, HttpServletRequest request) throws Exception;

    /**
     * 充值三方交易查证
     *
     * @throws Exception
     */
    void rechargeWithholdDoubtHandle() throws Exception;

    /**
     * 充值账户处理阶段可疑交易流程处理
     *
     * @throws Exception
     */
    void rechargeAccountDoubtHandle() throws Exception;

    /**
     * 充值账户处理阶段失败交易流程处理
     *
     * @throws Exception
     */
    void rechargeAccountFailedHandle() throws Exception;

    /**
     * 充值订单查询
     *
     * @param params
     * @param scs
     * @param pc
     * @return
     * @throws BizException
     */
    List<RechargeBorrowDTO> selectRechargeBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws BizException;

    /**
     * 充值订单列表页分页
     *
     * @param params
     * @return
     * @throws BizException
     */
    int selectRechargeBorrowCount(Map<String, Object> params) throws BizException;

    /**
     * 限额组件
     *
     * @param gatewayId
     * @param transAmount
     * @throws BizException
     */
    void channelLimit(Long gatewayId, BigDecimal transAmount, HttpServletRequest request) throws BizException;

    /**
     * 限额回滚组件
     *
     * @param createdDate
     * @param id
     * @param transAmount
     * @param request
     */
    void channelLimitRollback(Long createdDate, Long id, BigDecimal transAmount, HttpServletRequest request);

    /**
     * 更新三方流水
     * @param rechargeFlowDTO
     * @param withholdFlowDTO
     * @param request
     * @throws BizException
     */
    void withholdFlowStateChange(RechargeFlowDTO rechargeFlowDTO, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 更新账务流水
     * @param rechargeFlowDTO
     * @param msg
     * @param state
     * @param request
     * @throws BizException
     */
    public void accountFlowStateChange(RechargeFlowDTO rechargeFlowDTO, JSONObject msg, Integer state, HttpServletRequest request) throws BizException;

}
