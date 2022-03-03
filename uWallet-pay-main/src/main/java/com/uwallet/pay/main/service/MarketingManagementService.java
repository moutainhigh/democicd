package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.MarketingAccountDTO;
import com.uwallet.pay.main.model.dto.MarketingFlowDTO;
import com.uwallet.pay.main.model.dto.MarketingManagementDTO;
import com.uwallet.pay.main.model.dto.UserPromotionDTO;
import com.uwallet.pay.main.model.entity.MarketingManagement;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.exception.BizException;
import lombok.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 商户营销码
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 商户营销码
 * @author: baixinyue
 * @date: Created in 2020-11-09 15:29:48
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
public interface MarketingManagementService extends BaseService {

   /**
    * 保存一条 MarketingManagement 数据
    *
    * @param marketingManagementDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveMarketingManagement(MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 MarketingManagement 数据
     *
     * @param marketingManagementList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveMarketingManagementList(List<MarketingManagement> marketingManagementList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 MarketingManagement 数据
     *
     * @param id 数据唯一id
     * @param marketingManagementDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateMarketingManagement(Long id, MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException;



    /**
     * 更新活动状态
     * @author zhangzeyuan
     * @date 2021/11/10 15:37
     * @param id
     * @param marketingManagementDTO
     * @param request
     */
    void updateMarketingManagementActivityState(Long id, MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 marketingManagement
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateMarketingManagementSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 MarketingManagement
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteMarketingManagement(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 MarketingManagement
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteMarketingManagement(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 MarketingManagement
     *
     * @param id 数据唯一id
     * @return 查询到的 MarketingManagement 数据
     */
    MarketingManagementDTO findMarketingManagementById(Long id);

    /**
     * 根据条件查询得到第一条 marketingManagement
     *
     * @param params 查询条件
     * @return 符合条件的一个 marketingManagement
     */
    MarketingManagementDTO findOneMarketingManagement(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<MarketingManagementDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 累加已用次数
     * @return
     */
    int addUsedNumber(Long id) throws BizException;



    /**
     * 累加已用次数
     * @author zhangzeyuan
     * @date 2021/11/15 16:18
     * @param params
     * @return int
     */
    int addUsedNumber(Map<String, Object> params) throws BizException;

    /**
     * 累加已用次数回滚
     * @param id
     * @throws BizException
     */
    int addUsedNumberRollback(Long id)throws BizException;
    /**
     * 列表页分页计数
     * @param params
     * @return
     */
    int marketingCodeCount(Map<String, Object> params);

    /**
     * 列表页分页
     * @param params
     * @return
     */
    List<MarketingManagementDTO> marketingCodeList(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 新增营销码
     * @param marketingManagementDTO
     * @param request
     */
    void saveMarketingCode(MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws Exception;

    /**
     * 删除营销码
     * @param id
     * @param request
     * @throws Exception
     */
    void deleteMarketingCode(Long id, HttpServletRequest request) throws Exception;

    /**
     * 营销码使用记录
     * @param code
     * @param request
     * @return
     * @throws Exception
     */
    List<JSONObject> codeUseLog(String code, HttpServletRequest request) throws Exception;


    /**
     * 查询营销码
     * @param userId
     * @param requestInfo
     * @param request
     * @return
     */
    JSONObject getCodeMessage(Long userId, JSONObject requestInfo, HttpServletRequest request) throws BizException;


    /**
     * 获取所有的营销码列表
     * @author zhangzeyuan
     * @date 2021/10/27 16:25
     * @param data
     * @param request
     * @return int
     */
    JSONObject getAllPromotionList(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 获取可用的营销码列表
     * @author zhangzeyuan
     * @date 2021/10/27 16:25
     * @param data
     * @param request
     * @return int
     */
    JSONObject getAvailablePromotionList(JSONObject data, HttpServletRequest request) throws Exception;


    /**
     * 根据CODE搜索营销码
     * @author zhangzeyuan
     * @date 2021/10/27 17:02
     * @param code
     * @return java.lang.Object
     */
    MarketingManagementDTO findPromotionCodeByCode(String code);




    /**
     * app promotion 模块 code 搜索
     * @author zhangzeyuan
     * @date 2021/10/29 16:20
     * @param code
     * @return com.uwallet.pay.main.model.dto.MarketingManagementDTO
     */
    MarketingManagementDTO appPromotionSearch(String code, Long userId, HttpServletRequest request) throws Exception;


    /**
     * 添加营销券组件
     * @author zhangzeyuan
     * @date 2021/11/9 20:10
     * @param userId
     * @param marketingManagement
     * @param transType
     * @param notActivatedStatus 不激活状态
     * @param request
     * @return com.uwallet.pay.main.model.dto.MarketingAccountDTO
     */
    MarketingAccountDTO addMarketingPromotionCode(Long userId, MarketingManagementDTO marketingManagement, Integer transType, Boolean notActivatedStatus, HttpServletRequest request)throws Exception;



    /**
     * 添加邀请券
     * @author zhangzeyuan
     * @date 2021/11/15 11:13
     * @param userId
     * @param invitedId
     * @param addType
     * @param marketingManagement
     * @param notActivatedStatus
     * @param request
     * @return com.uwallet.pay.main.model.dto.MarketingAccountDTO
     */
    MarketingAccountDTO addInvitationPromotionCode(Long userId, Long invitedId, Integer addType, Long flowId , MarketingManagementDTO marketingManagement, Boolean notActivatedStatus, HttpServletRequest request)throws Exception;


    MarketingAccountDTO addOldUserInvitationPromotionCode(Long userId, Integer transType, Long flowId, MarketingManagementDTO marketingManagement, Boolean notActivatedStatus, HttpServletRequest request) throws Exception;

        /**
         * app通过搜索添加营销券
         * @author zhangzeyuan
         * @date 2021/11/9 20:12
         * @param userId
         * @param marketingId
         * @param request
         * @return com.alibaba.fastjson.JSONObject
         */
    JSONObject appAddPromotionCode(Long userId, Long marketingId, String inputCode, BigDecimal transAmount, Long merchantId, HttpServletRequest request)throws Exception;



    /**
     * 修改注册邀请码
     * @author zhangzeyuan
     * @date 2021/11/8 17:54
     * @param marketingManagementDTO
     * @param request
     */
    void updateInvitationMarketingManagement(MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException;

    /**
     * 卡券活动开始、终止跑批
     */
    void marketingManagerHandle();

 /**
  * 修改发放个数
  * @param id
  * @param marketingManagementDTO
  * @param request
  */
 void updateNumber(Long id, MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) throws BizException;

   /**
    * 查询用户卡券数量
    * @param params
    * @return
    */
    int userPromotionCount(Map<String, Object> params);

   /**
    * 查询用户卡券数量
    * @param params
    * @return
    */
   List<UserPromotionDTO> userPromotionList(Map<String, Object> params);
}
