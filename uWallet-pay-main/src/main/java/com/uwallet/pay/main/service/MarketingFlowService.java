package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.AccountFlowDTO;
import com.uwallet.pay.main.model.dto.MarketingFlowDTO;
import com.uwallet.pay.main.model.entity.MarketingFlow;
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
 * 账户动账交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 账户动账交易流水表
 * @author: baixinyue
 * @date: Created in 2020-11-09 15:30:03
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
public interface MarketingFlowService extends BaseService {

   /**
    * 保存一条 MarketingFlow 数据
    *
    * @param marketingFlowDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    Long saveMarketingFlow(MarketingFlowDTO marketingFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 MarketingFlow 数据
     *
     * @param marketingFlowList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveMarketingFlowList(List<MarketingFlow> marketingFlowList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 MarketingFlow 数据
     *
     * @param id 数据唯一id
     * @param marketingFlowDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateMarketingFlow(Long id, MarketingFlowDTO marketingFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 marketingFlow
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateMarketingFlowSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 MarketingFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteMarketingFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 MarketingFlow
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteMarketingFlow(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 MarketingFlow
     *
     * @param id 数据唯一id
     * @return 查询到的 MarketingFlow 数据
     */
    MarketingFlowDTO findMarketingFlowById(Long id);

    /**
     * 根据条件查询得到第一条 marketingFlow
     *
     * @param params 查询条件
     * @return 符合条件的一个 marketingFlow
     */
    MarketingFlowDTO findOneMarketingFlow(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<MarketingFlowDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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

    /**
     * 失败可以查询
     * @param params
     * @return
     */
    List<MarketingFlowDTO> findAbnormal(Map<String, Object> params);

 /**
  * 校验记录流水
  * @param amountInUserId
  * @param transType
  * @param markingCode
  * @param flowId
  * @param request
  * @return
  * @throws Exception
  */
    MarketingFlowDTO createMarketingFlow(Long amountInUserId, Integer transType, String markingCode, Long flowId, HttpServletRequest request) throws  Exception;

 /**
  * 红包获取结果处理
  * @param accountFlowDTO
  * @param marketingFlowDTO
  * @param code
  * @param request
  * @throws BizException
  */
 void doMarketingAmountInResult(AccountFlowDTO accountFlowDTO, MarketingFlowDTO marketingFlowDTO, int code, HttpServletRequest request) throws BizException;


 /**
  * 获取用户使用payo money金额
  * @author zhangzeyuan
  * @date 2021/9/9 15:36
  * @param userId
  * @param request
  * @return java.math.BigDecimal
  */
 BigDecimal getUseRedAmountByUserId(Long userId, HttpServletRequest request);


 /**
  * 卡券回退可疑查证
  */
 void marketingRollBackDoubtHandle();

 /**
  * 卡券回退失败处理
  */
 void marketingRollBackFailHandle();


 /**
  * 获取营销码使用记录
  * @author zhangzeyuan
  * @date 2021/11/6 15:16
  * @param params
  * @return java.util.List<com.uwallet.pay.main.model.dto.MarketingFlowDTO>
  */
 List<MarketingFlowDTO>  getMarketingCodeUsedLog(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    List<MarketingFlowDTO>  getFlowList(Map<String, Object> params);


 /**
  * 获取营销码使用记录
  * @author zhangzeyuan
  * @date 2021/11/11 16:06
  * @param
  * @param request
  * @return com.alibaba.fastjson.JSONObject
  */
 JSONObject getMarketingUsedLogList(Map<String, Object> params, HttpServletRequest request) throws Exception;

    /**
     * 获取营销码使用数量
     * @author zhangzeyuan
     * @date 2021/11/6 15:16
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.MarketingFlowDTO>
     */
 Integer  countUsedLog(Map<String, Object> params);


 /**
  * 获取在哪里消费的商户名称
  * @author zhangzeyuan
  * @date 2021/11/11 15:01
  * @param params
  * @return java.lang.String
  */
 String  getPaidMerchantName(Map<String, Object> params);

 /**
  * 查询用户可用卡券总额
  * @param userId
  * @param request
  * @return
  */
 BigDecimal getRedAmountByUserId(Long userId, HttpServletRequest request);

 /**
  * 查询用户卡券列表
  * @param param
  * @param request
  * @return
  */
 List<JSONObject> getUseAvailablePromotionByUserId(JSONObject param, HttpServletRequest request);

}
