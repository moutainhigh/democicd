package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 商户信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 商户信息表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:22:53
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Mapper
public interface MerchantDAO extends BaseDAO<Merchant> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MerchantDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MerchantDTO
     * @param params
     * @return
     */
    MerchantDTO selectOneDTO(Map<String, Object> params);

    /**
     * 统计符合条件的商户审核条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countMerchantApprove(Map<String, Object> params);

    /**
     * 根据查询条件得到商户审核列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    List<MerchantDetailDTO> listMerchantApprove(Map<String, Object> params);

    /**
     * 统计符合条件的商户列表条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countMerchant(Map<String, Object> params);

    /**
     * H5统计符合条件的商户列表条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countMerchantH5(Map<String, Object> params);

    /**
     * H5根据查询条件得到商户列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    List<MerchantDetailDTO> listMerchant(Map<String, Object> params);
    /**
     * 根据查询条件得到商户列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    List<ApiMerchantDTO> listMerchantH5(Map<String, Object> params);
    ApiMerchantDTO apiMerchantH5(@Param("id") Long id);
    /**
     *通过id列表查询商户列表
     * @param merchantIdList
     * @return
     */
    List<ContactsFileDTO> selectMerchantListByIdList(List<Long> merchantIdList);

    List<MerchantDTO> findMerchantLogInList(Map<String, Object> params);

    /**
     * 重置商户信息
     * @param merchantDTO
     * @return
     */
    int reSetMerchantInfo(MerchantDTO merchantDTO);

    /**
     * app商户发现列表分页计数
     * @param params
     * @return
     */
    int countAppFindList(Map<String, Object> params);

    /**
     * app商户发现列表
     * @param params
     * @return
     */
    List<MerchantDTO> appFindList(Map<String, Object> params);

    /**
     * 查询最新商户推荐排序
     * @return
     */
    Integer latestTopSort();

    /**
     * 将传入的顺序以后的上移
     * @param topSort
     */
    int topSortUp(Integer topSort);

    /**
     * 推荐列表页分页计数
     * @return
     */
    int topCount();

    /**
     * 推荐列表分页
     * @param params
     * @return
     */
    List<MerchantDTO> topList(Map<String, Object> params);

    /**
     * 查询上一级或下一级
     * @param id
     * @param upOrDown
     * @return
     */
    MerchantDTO shiftUpOrDown(@Param("id") Long id, @Param("upOrDown") Integer upOrDown);

    /**
     * 商户合同管理分页
     * @param params
     * @return
     */
    int merchantContractManageCount(Map<String, Object> params);

    /**
     * 商户合同管理
     * @param params
     * @param
     * @param
     * @return
     */
    List<MerchantDetailDTO> merchantContractManage(Map<String, Object> params);

    /**
     * 查询审核商户信息
     * @param merchantId
     * @return
     */
    MerchantDetailDTO findApproveMerchantDetail(@Param("merchantId") Long merchantId);
    /**
     * H5查询审核商户信息
     * @param merchantId
     * @return
     */
    MerchantDetailH5DTO findApproveMerchantDetailH5(@Param("merchantId") Long merchantId);

    int countMerchantChangeApprove(Map<String, Object> params);

    List<MerchantDetailDTO> listMerchantChangeApprove(Map<String, Object> params);

    /**
     * 获取所有推荐商户
     * @param params
     * @return
     */
    List<MerchantDTO> findTopTenNoLocation(Map<String, Object> params);

    /**
     * 获取新增商户列表
     * @param data
     * @return
     */
    List<MerchantDTO> getNewVenus(JSONObject data);

    /**
     * 查询正常出售可清算的商户个数
     * @return
     */
    int getMerchantClearMessageCount(Map<String, Object> params);

    /**
     * 查询正常出售可清算的商户
     * @param params
     * @return
     */
    List<MerchantDTO> merchantClearMessageList(Map<String, Object> params);

    /**
     * 查询整体出售可清算的商户个数
     * @param params
     * @return
     */
    List<MerchantDTO> getClearMerchantList(Map<String, Object> params);

    /**
     * 查询整体出售可清算的商户
     * @param params
     * @return
     */
    List<MerchantDTO> getWholeSaleClearMerchantList(Map<String, Object> params);

    /**
     * 根据店铺名称模糊查询
     * @param params
     * @return
     */
    List<Map<String,Object>> getMerchantListByPracticalName(Map<String, Object> params);

    /**
     * 根据店铺ID查询详细信息(店铺名称，店铺id，店铺状态)
     * @param params
     * @return
     */
    MerchantDTO getMerchantInfo(Map<String, Object> params);

    /**
     * 根据店铺名称模糊匹配时间最大的一条数据
     * @param params
     * @return
     */
    Long getMaxMerchantListByPracticalName(Map<String, Object> params);


    /**
     * 根据输入关键字 模糊匹配 商户列表
     * @param keywords
     * @return
     */

    List<MerchantAppHomePageDTO> getMerchantListByKeywords(@Param("categoryType") String categoryType, @Param("keywords")String keywords, @Param("stateName")String stateName);


    /**
     * 根据anb获取商户列表
     * @author zhangzeyuan
     * @date 2021/3/23 13:27
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.MerchantDTO>
     */
    List<MerchantDTO> getMerchantListByABN(Map<String, Object> params);



    /**
     * 根据ID获取商户列表信息
     * @author zhangzeyuan
     * @date 2021/4/8 17:58
     * @param ids
     * @return java.util.List<com.uwallet.pay.main.model.dto.MerchantDTO>
     */
    List<MerchantAppHomePageDTO> getMerchantListByIds(@Param("ids") String ids, @Param("userId") Long userId);

    /**
     * 商户展示类型类 距离 获取商户列表信息
     * @author zhangzeyuan
     * @date 2021/4/8 17:58
     * @param
     * @return java.util.List<com.uwallet.pay.main.model.dto.MerchantDTO>
     */
    List<MerchantAppHomePageDTO> getMerchantListDistance(@Param("merchantState") String merchantState,@Param("categoryType") Integer categoryType, @Param("userId") Long userId);
    /**
     * 根据商户ID获取回调通知地址
     * @author zhangzeyuan
     * @date 2021/3/25 13:21
     * @param merchantId
     * @return java.lang.String
     */
    String getMerchantNotifyUrl(Long merchantId);

    /**
     * 获取商户列表
     * @param data
     * @return
     */
    List<JSONObject> findMerchantListJson(JSONObject data);

    /**
     * 通过商户id集合获取商户商户信息
     * @param idListStr
     * @return
     */
    List<JSONObject> getMerchantListInfo(@Param("idListStr") String idListStr);


    List<MerchantAppHomePageDTO> listCustomStateMerchantDataByIds(@Param("ids")String ids);

    /**
     * 计算搜索页面是否有符合条件的数据
     * @param queryParam
     * @return
     */
    int countForList(JSONObject queryParam);
}
