package com.uwallet.pay.main.service;


import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.PosQrPayFlowDTO;
import com.uwallet.pay.main.model.dto.PosTransactionRecordDTO;
import com.uwallet.pay.main.model.entity.PosQrPayFlow;
import io.swagger.models.auth.In;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 *
 * </p>
 *
 * @package: com.fenmi.generator.service
 * @description:
 * @author: zhangzeyuan
 * @date: Created in 2021-03-22 15:46:35
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
public interface PosQrPayFlowService extends BaseService {

    /**
     * 保存一条 PosQrPayFlow 数据
     *
     * @param posQrPayFlowDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    void savePosQrPayFlow(PosQrPayFlowDTO posQrPayFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 PosQrPayFlow 数据
     *
     * @param posQrPayFlowList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void savePosQrPayFlowList(List<PosQrPayFlow> posQrPayFlowList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 PosQrPayFlow 数据
     *
     * @param id              数据唯一id
     * @param posQrPayFlowDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updatePosQrPayFlow(Long id, PosQrPayFlowDTO posQrPayFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 posQrPayFlow
     *
     * @param dataMap      需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updatePosQrPayFlowSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 PosQrPayFlow
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeletePosQrPayFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 PosQrPayFlow
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deletePosQrPayFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 PosQrPayFlow
     *
     * @param id 数据唯一id
     * @return 查询到的 PosQrPayFlow 数据
     */
    PosQrPayFlowDTO findPosQrPayFlowById(Long id);

    /**
     * 根据条件查询得到第一条 posQrPayFlow
     *
     * @param params 查询条件
     * @return 符合条件的一个 posQrPayFlow
     */
    PosQrPayFlowDTO findOnePosQrPayFlow(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<PosQrPayFlowDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     *  pos 扫描二维码获取订单金额
     * @param jsonObject
     * @param request
     * @return
     */
    JSONObject showPosTransAmount(JSONObject jsonObject, HttpServletRequest request) throws BizException;


    /**
     *  获取POS订单历史记录列表
     * @author zhangzeyuan
     * @date 2021/3/23 13:54
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.PosQrPayFlowDTO>
     */
    List<PosTransactionRecordDTO> listPosTransaction(Map<String, Object> params);

    /**
     * 获取POS订单历史记录列表
     * @author zhangzeyuan
     * @date 2021/3/23 13:57
     * @param params
     * @return java.lang.Integer
     */
    Integer countPosTransaction(Map<String, Object> params);



    /**
     * 根据三方订单号 更新系统订单号
     * @author zhangzeyuan
     * @date 2021/3/25 15:50
     * @param sysTransNo
     * @param thirdNo
     */
    void updateSysTransNoByThirdNo(String sysTransNo, String thirdNo, Long userId, Long modifyTime);



    /**
     * 根据系统订单号更新流水状态
     * @author zhangzeyuan
     * @date 2021/3/30 10:32
     * @param sysTransNo
     * @param orderStatus
     * @param userId
     * @param modifyTime
     */
    void updateOrderStatusBySysTransNo(String sysTransNo, Integer orderStatus, Long userId, Long modifyTime) throws BizException;


}
