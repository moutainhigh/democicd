package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.GatewayDTO;
import com.uwallet.pay.main.model.entity.Gateway;
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
 * 支付渠道信息表

 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 支付渠道信息表

 * @author: Strong
 * @date: Created in 2019-12-12 10:16:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
public interface GatewayService extends BaseService {

   /**
    * 保存一条 Gateway 数据
    *
    * @param gatewayDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveGateway(GatewayDTO gatewayDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 Gateway 数据
     *
     * @param gatewayList 待保存的数据列表
     * @throws BizException 保存失败异常
     */
    void saveGatewayList(List<Gateway> gatewayList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 Gateway 数据
     *
     * @param id 数据唯一id
     * @param gatewayDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateGateway(Long id, GatewayDTO gatewayDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 gateway
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateGatewaySelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 Gateway
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteGateway(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 Gateway
     *
     * @param id 数据唯一id
     * @throws BizException 物理删除异常
     */
    void deleteGateway(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 Gateway
     *
     * @param id 数据唯一id
     * @return 查询到的 Gateway 数据
     */
    GatewayDTO findGatewayById(Long id);

    /**
     * 根据条件查询得到第一条 gateway
     *
     * @param params 查询条件
     * @return 符合条件的一个 gateway
     */
    GatewayDTO findOneGateway(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<GatewayDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * @param group 分组的字段
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的统计条数
     */
    Map<String, Integer> groupCount(String group, Map<String, Object> conditions);

    /**
     * 根据给定字段查询统计字段的sum结果
     *
     * @param sumField sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 返回sum计算的结果值
     */
    Double sum(String sumField, Map<String, Object> conditions);

    /**
     * 根据给定字段以及查询条件进行分组查询，并sum统计Field
     *
     * @param group 分组的字段。
     * @param sumField sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的求和
     */
    Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions);

    List<GatewayDTO> getPayType();

    /**
     * 查询手续费
     * @param data
     * @param request
     * @return
     * @throws Exception
     */
    Map<String,Object> getChannelFee(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 修改可用状态
     * @param id
     * @param request
     */
    void updateState(Long id, HttpServletRequest request) throws Exception;

     /**
      * 计算通道手续费
      * @return
      * @param gatewayDTO
      * @param amount
      */
     BigDecimal getGateWayFee(GatewayDTO gatewayDTO, BigDecimal amount);

    /**
     * 获取支付通道
     * @param request
     * @return
     */
    JSONObject getPayGateWay(HttpServletRequest request) throws BizException;
}
