package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.MerchantAppHomePageDTO;
import com.uwallet.pay.main.model.dto.MerchantDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * 通过地理位置查询
 * 商户列表信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service
 * @description: 商户列表信息表
 * @author: aarons
 * @date: Created in 2021-01-10 16:22:53
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: aarons
 */
public interface MerchantListService {

    /**
     * 商户列表页搜索按钮,按照tag/trading name 返回商户列表
     * @param data
     * @param request
     * @return
     */
    JSONObject merchantSearchList(JSONObject data, HttpServletRequest request)  throws BizException;

    /**
     * 商户首页推荐列表
     * @param data
     * @param request
     * @return
     */
    List<MerchantDTO> getTopTenList(JSONObject data, HttpServletRequest request);

    /**
     * 商户首页推荐列表-新增商户
     * @param data
     * @param request
     * @return
     */
    List<MerchantDTO> getNewVenus(JSONObject data, HttpServletRequest request);

    /**
     * 处理merchant list中tag,long值,cityName显示名
     * @param topTenList
     * @return
     */
    List<MerchantDTO> processMerchantList(List<MerchantDTO> topTenList,Boolean mark);

    /**
     * 通过经纬度,获取城市街道
     * @param data
     * @param request
     * @return
     */
    JSONObject findCityStInfo(JSONObject data, HttpServletRequest request) throws BizException;

    /**
     * 获取商家列表,地图显示
     * @param data
     * @param request
     * @return
     */
    List<MerchantDTO> getMerchantLocationList(JSONObject data, HttpServletRequest request) throws BizException ;
    /**
     * 查询ip所属区域
     * @param ip
     * @return
     */
    JSONObject findLocationByIp(String ip,HttpServletRequest request) throws BizException ;

    /**
     * app获取商家详情信息
     * @param merchantId
     * @param request
     * @return
     */
    MerchantDTO getMerchantDetail(Long merchantId, HttpServletRequest request) throws BizException;

    /**
     * 获取商户列表信息
     * @param data
     * @param request
     * @return
     * @throws BizException
     */
    JSONObject merchantList(JSONObject data, HttpServletRequest request) throws BizException;

    /**
     * 向首页商户数据中封装距离信息, 分类显示名信息 用户经纬度可以为null,
     * @param merchantList 商户list
     * @param userLat 用户经纬度 可以为null
     * @param userLng 用户经纬度 可以为null
     * @return
     */
    List<MerchantAppHomePageDTO> packDistanceCategoriesInfo(List<MerchantAppHomePageDTO> merchantList, String userLat,String userLng);

    /**
     * 计算商户搜索页面, 是否有数据, 0:无数据, 1:有数据
     * @param queryParam
     * @param request
     * @return
     */
    Integer merchantListHaveData(JSONObject queryParam, HttpServletRequest request);

}
