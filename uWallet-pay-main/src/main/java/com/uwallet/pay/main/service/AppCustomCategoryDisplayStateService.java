package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayStateDTO;
import com.uwallet.pay.main.model.entity.AppCustomCategoryDisplayState;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * APP首页自定义分类 每个州展示商户、图片信息
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: APP首页自定义分类 每个州展示商户、图片信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 15:09:17
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
public interface AppCustomCategoryDisplayStateService extends BaseService {

   /**
    * 保存一条 AppCustomCategoryDisplayState 数据
    *
    * @param appCustomCategoryDisplayStateDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveAppCustomCategoryDisplayState(AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 AppCustomCategoryDisplayState 数据
     *
     * @param appCustomCategoryDisplayStateList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveAppCustomCategoryDisplayStateList(List<AppCustomCategoryDisplayState> appCustomCategoryDisplayStateList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 AppCustomCategoryDisplayState 数据
     *
     * @param id 数据唯一id
     * @param appCustomCategoryDisplayStateDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void    updateAppCustomCategoryDisplayState(Long id, AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 appCustomCategoryDisplayState
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateAppCustomCategoryDisplayStateSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 AppCustomCategoryDisplayState
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteAppCustomCategoryDisplayState(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 AppCustomCategoryDisplayState
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteAppCustomCategoryDisplayState(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 AppCustomCategoryDisplayState
     *
     * @param id 数据唯一id
     * @return 查询到的 AppCustomCategoryDisplayState 数据
     */
    AppCustomCategoryDisplayStateDTO findAppCustomCategoryDisplayStateById(Long id);

    /**
     * 根据条件查询得到第一条 appCustomCategoryDisplayState
     *
     * @param params 查询条件
     * @return 符合条件的一个 appCustomCategoryDisplayState
     */
    AppCustomCategoryDisplayStateDTO findOneAppCustomCategoryDisplayState(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<AppCustomCategoryDisplayStateDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 获取APP主页自定义分类view all图片信息
     * @author zhangzeyuan
     * @date 2021/4/14 10:02
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    JSONObject listAllAppHomePageCategoryAllImgData(JSONObject requestInfo, HttpServletRequest request) throws BizException;


    /**
     *  收藏变化时更新APP首页商户分类
     * @author zhangzeyuan
     * @date 2021/4/27 19:45
     * @param merchantId
     */
    void updateAppCategoriesWhereFavoriteChange(Long merchantId) throws BizException;


    /**
     *  商户禁用时 更新APP首页商户分类
     * @author zhangzeyuan
     * @date 2021/5/20 15:43
     * @param merchantId
     */
    void updateMerchantDataByMerchantNotAvailable(Long merchantId, HttpServletRequest request);

    /**
     *  商户更新时 更新APP首页商户分类信息
     * @author zhangzeyuan
     * @date 2021/6/8 14:41
     * @param merchantId
     * @param request
     */
    void updateMerchantDataToChangeAppHomepage(Long merchantId, Boolean merchantAvailableStatus, HttpServletRequest request);

    void updatDefinition(@Param("id")Long id, @Param("merchantDisplayType")Integer merchantDisplayType);

    Map getDistanceMerchant(@Param("id")Long id);
}
