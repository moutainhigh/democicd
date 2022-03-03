package com.uwallet.pay.main.service;


import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO;
import com.uwallet.pay.main.model.dto.MerchantContractFileRecordDTO;
import com.uwallet.pay.main.model.entity.AppExclusiveBanner;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * APP首页banner、市场推广图片配置表
 * </p>
 *
 * @package: com.uwallet.pay.main.generator.service
 * @description: APP首页banner、市场推广图片配置表
 * @author: zhangzeyuan
 * @date: Created in 2021-04-08 13:35:31
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
public interface AppExclusiveBannerService extends BaseService {

    /**
     * 保存一条 AppExclusiveBanner 数据
     *
     * @param appExclusiveBannerDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveAppExclusiveBanner(AppExclusiveBannerDTO appExclusiveBannerDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 AppExclusiveBanner 数据
     *
     * @param appExclusiveBannerList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveAppExclusiveBannerList(List<AppExclusiveBanner> appExclusiveBannerList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 AppExclusiveBanner 数据
     *
     * @param id                    数据唯一id
     * @param appExclusiveBannerDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateAppExclusiveBanner(Long id, AppExclusiveBannerDTO appExclusiveBannerDTO, HttpServletRequest request) throws BizException;

    /**
     * 更改banner 可用 不可用状态
     *
     * @param id
     * @param appExclusiveBannerDTO
     * @param request
     * @author zhangzeyuan
     * @date 2021/4/12 14:49
     */
    void updateBannerEnableStatus(Long id, AppExclusiveBannerDTO appExclusiveBannerDTO, HttpServletRequest request) throws BizException;

    /**
     * 上移、下移
     *
     * @param id
     * @param status
     * @param request
     * @author zhangzeyuan
     * @date 2021/4/12 16:27
     */
    void moveUpOrDown(Long id, Integer status, HttpServletRequest request) throws BizException;


    /**
     * 添加记录时获取下一个排序
     *
     * @param displayType
     * @return java.lang.Integer
     * @author zhangzeyuan
     * @date 2021/4/21 18:24
     */
    Integer getAddNextOrder(Integer displayType);

    /**
     * 根据Id部分更新实体 appExclusiveBanner
     *
     * @param dataMap      需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateAppExclusiveBannerSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 AppExclusiveBanner
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteAppExclusiveBanner(Long id, Integer type, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 AppExclusiveBanner
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteAppExclusiveBanner(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 AppExclusiveBanner
     *
     * @param id 数据唯一id
     * @return 查询到的 AppExclusiveBanner 数据
     */
    AppExclusiveBannerDTO findAppExclusiveBannerById(Long id);

    /**
     * 根据条件查询得到第一条 appExclusiveBanner
     *
     * @param params 查询条件
     * @return 符合条件的一个 appExclusiveBanner
     */
    AppExclusiveBannerDTO findOneAppExclusiveBanner(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<AppExclusiveBannerDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 获取APPbanner数据
     *
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/4/8 15:18
     */
    JSONObject getAppHomePageTopBanner(JSONObject requestInfo, HttpServletRequest request);


    /**
     * 获取APP主页市场推广、自定义分类展示数据
     *
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/4/8 16:27
     */
    JSONObject listAllAppHomePageBottomData(JSONObject requestInfo, HttpServletRequest request);


    /**
     * 分页查询
     * @author zhangzeyuan
     * @date 2021/5/8 16:55
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO>
     */
    List<AppExclusiveBannerDTO> listBanner(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);


}
