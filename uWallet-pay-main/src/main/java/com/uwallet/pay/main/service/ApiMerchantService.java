package com.uwallet.pay.main.service;

import com.uwallet.pay.main.model.dto.ApiMerchantDTO;
import com.uwallet.pay.main.model.dto.MerchantDetailH5DTO;
import com.uwallet.pay.main.model.entity.ApiMerchant;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.exception.BizException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * api商户信息表j
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: api商户信息表j
 * @author: zhoutt
 * @date: Created in 2021-09-02 17:35:15
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
public interface ApiMerchantService extends BaseService {

   /**
    * 保存一条 ApiMerchant 数据
    *
    * @param apiMerchantDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    Long saveApiMerchant(ApiMerchantDTO apiMerchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 ApiMerchant 数据
     *
     * @param apiMerchantList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveApiMerchantList(List<ApiMerchant> apiMerchantList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 ApiMerchant 数据
     *
     * @param id 数据唯一id
     * @param apiMerchantDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateApiMerchant(Long id, ApiMerchantDTO apiMerchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 apiMerchant
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateApiMerchantSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 ApiMerchant
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteApiMerchant(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 ApiMerchant
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteApiMerchant(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 ApiMerchant
     *
     * @param id 数据唯一id
     * @return 查询到的 ApiMerchant 数据
     */
    ApiMerchantDTO findApiMerchantById(Long id);

    /**
     * 根据条件查询得到第一条 apiMerchant
     *
     * @param params 查询条件
     * @return 符合条件的一个 apiMerchant
     */
    ApiMerchantDTO findOneApiMerchant(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<ApiMerchantDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 查询商户清算数据
     * @param params
     * @return
     */
    List<ApiMerchantDTO> getApiClearMerchantList(Map<String, Object> params);

    /**
     * 查询所有的一级商户
     * @return
     */
    List<Map<String, String>>  findSuperMerchant();


   /**
    * 记录信息
    * @param merchantData
    * @param request
    */
   void replenishDirectorAndOwner(ApiMerchantDTO merchantData, HttpServletRequest request) throws BizException;

   /**
    * 拒绝
    * @param id
    * @param remark
    * @param request
    * @throws BizException
    */
   void refuseMerchant(Long id, String remark, HttpServletRequest request) throws BizException;

   /**
    * 通过
    * @param id
    * @param merchantDetailDTO
    * @param request
    * @throws BizException
    */
   void passMerchant(Long id, MerchantDetailH5DTO merchantDetailDTO, HttpServletRequest request) throws Exception;
}
