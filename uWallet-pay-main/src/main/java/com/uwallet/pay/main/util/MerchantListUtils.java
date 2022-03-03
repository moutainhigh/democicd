package com.uwallet.pay.main.util;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.GeodesyUtil;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.MerchantAppHomePageDTO;
import com.uwallet.pay.main.model.dto.MerchantDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GlobalCoordinates;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 通过地理位置 获取商户折扣列表工具类
 * @Author aarons
 */
@Slf4j
public class MerchantListUtils {

    private static final String mapGeoLocationAPI = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";

    private static final String googleApiKey = "AIzaSyDV2B73Io1vf8EpvMSprvtOrfcLTOR_Ey8";
    private static final String ONE_KM = "1000";

    /**
     * 通过用户当前经纬度,向MerchantDTO中封装距离该商户的距离信息
     * @param merchantDTO 商户信息
     * @param lat 当前用户经度
     * @param lng 当前用户纬度
     * @return
     * @throws BizException
     */
    public static BigDecimal calDistance(MerchantDTO merchantDTO,String lat,String lng) {
        try {
            if (StringUtils.isNoneEmpty(new String[]{merchantDTO.getLat(),merchantDTO.getLng()})) {
                GlobalCoordinates source = new GlobalCoordinates(new Double(lat), new Double(lng));
                GlobalCoordinates target = new GlobalCoordinates(new Double(merchantDTO.getLat()), new Double(merchantDTO.getLng()));
                //商户用户距离, 单位--> m
                double distance = GeodesyUtil.getDistanceMeter(source, target, Ellipsoid.Sphere);
                //计算到当前商户的距离, 单位--> km
                return new BigDecimal(distance).divide(new BigDecimal(ONE_KM)).setScale(2, RoundingMode.HALF_UP);
            }
        }catch (Exception e){
            log.error("获取用户到当前商户距离信息异常, 商户DTO:{},用户lat:{},用户lng:{}",merchantDTO,lat,lng);
        }
        return null;
    }
    /**
     * 通过用户当前经纬度,向MerchantDTO中封装距离该商户的距离信息
     * @param merchantDTO 商户信息
     * @param lat 当前用户经度
     * @param lng 当前用户纬度
     * @return
     * @throws BizException
     */
    public static BigDecimal calDistanceAppHome(MerchantAppHomePageDTO merchantDTO, String lat, String lng) {
        try {
            if (StringUtils.isNoneEmpty(new String[]{merchantDTO.getLat(),merchantDTO.getLng(),lat,lng})) {
                GlobalCoordinates userSource = new GlobalCoordinates(new Double(lat), new Double(lng));
                GlobalCoordinates target = new GlobalCoordinates(new Double(merchantDTO.getLat()), new Double(merchantDTO.getLng()));
                //商户用户距离, 单位--> m
                double distance = GeodesyUtil.getDistanceMeter(userSource, target, Ellipsoid.Sphere);
                //计算到当前商户的距离, 单位--> km
                return new BigDecimal(distance).divide(new BigDecimal(ONE_KM)).setScale(2, RoundingMode.HALF_UP);
            }
        }catch (Exception e){
            log.error("获取用户到当前商户距离信息异常, 商户DTO:{},用户lat:{},用户lng:{}",merchantDTO,lat,lng);
        }
        return null;
    }

    public static Object calDistanceJson(JSONObject dto, String userLat, String userLng) {
        try {
            String lat = dto.getString("lat");
            String lng = dto.getString("lng");
            if (StringUtils.isNoneEmpty(new String[]{lat,lng,userLat,userLng})) {
                GlobalCoordinates source = new GlobalCoordinates(new Double(userLat), new Double(userLng));
                GlobalCoordinates target = new GlobalCoordinates(new Double(lat), new Double(lng));
                //商户用户距离, 单位--> m
                double distance = GeodesyUtil.getDistanceMeter(source, target, Ellipsoid.Sphere);
                //计算到当前商户的距离, 单位--> km
                return new BigDecimal(distance).divide(new BigDecimal(ONE_KM)).setScale(2, RoundingMode.HALF_UP);
            }
        }catch (Exception e){
            log.error("获取用户到当前商户距离信息异常, 商户DTO:{},用户lat:{},用户lng:{}",dto,userLat,userLng);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取 距离区间内的商户列表
     * @param merchantDTOList
     * @param biggerThan 大于该距离
     * @param smallerThan 小于该距离
     * @return
     */
    public static List<MerchantDTO> getRangeMerchantList(List<MerchantDTO> merchantDTOList, String biggerThan, String smallerThan) {
        if (CollectionUtil.isNotEmpty(merchantDTOList)) {
            return merchantDTOList.stream()
                    .filter(mer -> mer.getDistance().compareTo(new BigDecimal(biggerThan)) > 0 && mer.getDistance().compareTo(new BigDecimal(smallerThan)) <= 0)
                    .sorted(Comparator.comparing(MerchantDTO::getUserDiscount).reversed().thenComparing(MerchantDTO::getDistance))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return new ArrayList<>();
    }

    /**
     * 返回 小于/大于 该距离的 商户列表
     * @param merchantDTOList
     * @param range 距离
     * @param isBiggerThan true->大于, false->小于
     * @return
     */
    public static List<MerchantDTO> getDistanceMerchant(List<MerchantDTO> merchantDTOList, String range, boolean isBiggerThan) {
        if (CollectionUtil.isNotEmpty(merchantDTOList)) {
            Stream<MerchantDTO> stream = merchantDTOList.stream();
            Stream<MerchantDTO> merchantDTOStream;

            merchantDTOStream = isBiggerThan ?
                    stream.filter(mer -> mer.getDistance().compareTo(new BigDecimal(range)) > 0)
                  : stream.filter(mer -> mer.getDistance().compareTo(new BigDecimal(range)) <= 0);

            return merchantDTOStream
                    .sorted(Comparator.comparing(MerchantDTO::getUserDiscount).reversed().thenComparing(MerchantDTO::getDistance))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return new ArrayList<>();
    }


    /**
     * 如果有城市信息,大于6公里 整个城市的商家
     * @param merchantDTOList
     * @param range
     * @param city
     * @return
     */
    public static Collection<? extends MerchantDTO> withinSameCityMerchant(List<MerchantDTO> merchantDTOList, String range, String city) {
        if (CollectionUtil.isNotEmpty(merchantDTOList)) {
            return merchantDTOList.stream()
                    .filter(mer -> mer.getDistance().compareTo(new BigDecimal(range)) > 0 && city.equals(mer.getCity()))
                    .sorted(Comparator.comparing(MerchantDTO::getUserDiscount).reversed().thenComparing(MerchantDTO::getDistance))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return new ArrayList<>();
    }

    /**
     * 不在该城市,全澳洲的商家,折扣排序
     * @param merchantDTOList
     * @param city
     * @return
     */
    public static Collection<? extends MerchantDTO> noInSameCityMerchant(List<MerchantDTO> merchantDTOList, String city) {
        if (CollectionUtil.isNotEmpty(merchantDTOList)) {
            return merchantDTOList.stream()
                    .filter(mer -> !city.equals(mer.getCity()))
                    .sorted(Comparator.comparing(MerchantDTO::getUserDiscount).reversed())
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return new ArrayList<>();
    }

    /**
     * 经纬度获取地址
     * 返回参数样例: https://docs.qq.com/doc/DT05Eb1hRY2hjdFNt
     * @param lat
     * @param lng
     * @return
     * @throws Exception
     */
    public static JSONObject getCityStreet(String lat, String lng){

        JSONObject result = new JSONObject(8);
        String cityName = " ";
        String street = " ";
        String stateName = " ";
        JSONArray address = new JSONArray();
        try {
            String latLng = lat + "," + lng + "&key=" + googleApiKey;
            String mapsApi = mapGeoLocationAPI + latLng;
            String resStr = HttpClientUtils.sendGet(mapsApi);
            if (StringUtils.isNotBlank(resStr)) {
                JSONObject location = JSONObject.parseObject(resStr);
                JSONArray addressComponents = location.getJSONArray("results").getJSONObject(0).getJSONArray("address_components");
                address = addressComponents;
                if (CollectionUtil.isNotEmpty(addressComponents)) {
                    for (int i = 0; i < addressComponents.size(); i++) {
                        JSONObject node = addressComponents.getJSONObject(i);
                        String type = node.getJSONArray("types").getString(0);
                        if ("route".equals(type)) {
                            street = node.getString("short_name");
                        } else if ("administrative_area_level_2".equals(type)) {
                            cityName = node.getString("short_name");
                        }else if ("administrative_area_level_1".equals(type)) {
                            stateName = node.getString("short_name");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("MerchantListUtils===> get city street failed,lng-lat:{},error:{},errorMsg:{}",lng + "-" +lat,e,e.getMessage());
        }
        result.put("street", street);
        result.put("cityName",cityName);
        //2021-04-22 加入当前经纬度的'州'信息
        result.put("stateName",stateName);
        //2021-04-22
        result.put("fullAddress",address);
        result.put("lat",lat);
        result.put("lng",lng);
        return result;
    }

    /**
     * 设置 用户 : 卡支付/分期付支付 在是否有整体出售下 分别的折扣
     *     用户最大折扣
     * @param dto
     * @return
     */
    public static MerchantDTO setCardInstallmentDiscount(MerchantDTO dto) {
        BigDecimal cardPayDiscount ;
        BigDecimal installmentDiscount ;
        if (dto.getHaveWholeSell().equals(StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode())){
            //有整体出售额度 折扣只有一个
            //卡支付
            cardPayDiscount = dto.getWholeSaleUserPayDiscount();
            //分期付
            installmentDiscount = dto.getWholeSaleUserDiscount();
        }else {
            //无整体出售额度 base+marketing+extra
            //固定折扣
            BigDecimal marketingDiscount = dto.getMarketingDiscount();
            //计算额外折扣
            Long extraDiscountPeriod = dto.getExtraDiscountPeriod();
            BigDecimal extraDiscount;
            if (null!= extraDiscountPeriod) {
                extraDiscount = System.currentTimeMillis() < extraDiscountPeriod ? dto.getExtraDiscount() : BigDecimal.ZERO;
            }else {
                extraDiscount = BigDecimal.ZERO;
            }
            //base折扣-卡
            BigDecimal basePayRate = dto.getBasePayRate();
            //base折扣-分期付
            BigDecimal baseRate = dto.getBaseRate();

            BigDecimal first = marketingDiscount.add(extraDiscount);

            cardPayDiscount = basePayRate.add(first);
            installmentDiscount = baseRate.add(first);
        }

        dto.setCardPayDiscount(cardPayDiscount);
        dto.setInstallmentDiscount(installmentDiscount);
        // 设置用户折扣==> 折扣一致,随便返回一个, 不一致 返回最大
        dto.setUserDiscount(cardPayDiscount.compareTo(installmentDiscount)==0 ?
                  cardPayDiscount
                : cardPayDiscount.compareTo(installmentDiscount) > 0 ? cardPayDiscount:installmentDiscount);
        return dto;
    }


    /**
     * 设置 用户 : 卡支付/分期付支付 在是否有整体出售下 分别的折扣
     * @author zhangzeyuan
     * @date 2021/4/22 16:55
     * @param dto
     * @return com.uwallet.pay.main.model.dto.MerchantAppHomePageDTO
     */

    /**
     * 根据传过来的DTO 计算 折扣信息
     * @author zhangzeyuan
     * @date 2021/4/22 18:18
     * @param dto
     * @return com.uwallet.pay.main.model.dto.MerchantAppHomePageDTO
     */
    public static String setCardInstallmentDiscountByAppHomePageDTO(MerchantAppHomePageDTO dto) {
        BigDecimal cardPayDiscount ;
        BigDecimal installmentDiscount ;
        if (dto.getHaveWholeSell().equals(StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode())){
            //有整体出售额度 折扣只有一个
            //卡支付
            cardPayDiscount = dto.getWholeSaleUserPayDiscount();
            //分期付
            installmentDiscount = dto.getWholeSaleUserDiscount();
        }else {
            //无整体出售额度 base+marketing+extra
            //固定折扣
            BigDecimal marketingDiscount = dto.getMarketingDiscount();
            //计算额外折扣
            Long extraDiscountPeriod = dto.getExtraDiscountPeriod();
            BigDecimal extraDiscount;
            if (null!= extraDiscountPeriod) {
                extraDiscount = System.currentTimeMillis() < extraDiscountPeriod ? dto.getExtraDiscount() : BigDecimal.ZERO;
            }else {
                extraDiscount = BigDecimal.ZERO;
            }
            //base折扣-卡
            BigDecimal basePayRate = dto.getBasePayRate();
            //base折扣-分期付
            BigDecimal baseRate = dto.getBaseRate();

            BigDecimal first = marketingDiscount.add(extraDiscount);

            cardPayDiscount = basePayRate.add(first);
            installmentDiscount = baseRate.add(first);
        }

//        dto.setCardPayDiscount(cardPayDiscount);
//        dto.setInstallmentDiscount(installmentDiscount);
        // 设置用户折扣==> 折扣一致,随便返回一个, 不一致 返回最大
        String userDiscount = "";
        BigDecimal userDiscountDecimal = cardPayDiscount.compareTo(installmentDiscount) == 0 ?
                cardPayDiscount
                : cardPayDiscount.compareTo(installmentDiscount) > 0 ? cardPayDiscount : installmentDiscount;

        if(StringUtils.isNotBlank(userDiscountDecimal.toString())){
            userDiscount =  userDiscountDecimal.toString();
        }

        return userDiscount;
    }

    /**
     * 重新组装 keyword to list, 设置用户折扣,设置距离
     * @param list
     * @param userLat
     * @param userLng
     * @param request
     * @return
     */
    public static List<JSONObject> processData(List<JSONObject> list, String userLat, String userLng, HttpServletRequest request) {
        if (CollectionUtil.isNotEmpty(list)){
            list.forEach(dto->{
                //将keyword字段中','分割的tag封装成 List<String> ,返回最多3个tag(不管有几个)
                String keywords = dto.getString("keyword");
                if (StringUtils.isNotBlank(keywords)){
                    dto.put("tags",Arrays.stream(keywords.split(",",-1)).filter(StringUtils::isNotBlank).limit(3).collect(Collectors.toList()));
                }else {
                    dto.put("tags",new ArrayList<>());
                }
                //设置折扣数据
                MerchantListUtils.setJsonDiscountInfo(dto,request);
                //如果有用户地理位置信息,设置距离,没有则设置为空字符串
                dto.put("distance",MerchantListUtils.calDistanceJson(dto, userLat, userLng));
                //删除前端不用的key
                MerchantListUtils.removeKey(dto,request);
            });
        }
        return list;
    }

    private static JSONObject removeKey(JSONObject dto, HttpServletRequest request) {
        dto.remove("keyword");
        dto.remove("wholeSaleUserPayDiscount");
        dto.remove("marketingDiscount");
        dto.remove("extraDiscountPeriod");
        dto.remove("basePayRate");
        dto.remove("baseRate");
        return dto;
    }

    public static JSONObject setJsonDiscountInfo(JSONObject dto, HttpServletRequest request) {
        BigDecimal cardPayDiscount ;
        BigDecimal installmentDiscount ;
        if (dto.getInteger("haveWholeSell").equals(StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode())){
            //有整体出售额度 折扣只有一个
            //卡支付
            cardPayDiscount = null != dto.getBigDecimal("wholeSaleUserPayDiscount") ? dto.getBigDecimal("wholeSaleUserPayDiscount") : BigDecimal.ZERO;
            //分期付
            installmentDiscount = null!= dto.getBigDecimal("wholeSaleUserDiscount") ? dto.getBigDecimal("wholeSaleUserDiscount") : BigDecimal.ZERO;
        }else {
            //无整体出售额度 base+marketing+extra
            //固定折扣
            BigDecimal marketingDiscount = dto.getBigDecimal("marketingDiscount");
            marketingDiscount = null != marketingDiscount ? marketingDiscount : BigDecimal.ZERO;
            //计算额外折扣
            Long extraDiscountPeriod = dto.getLong("extraDiscountPeriod");
            BigDecimal extraDiscount;
            if (null!= extraDiscountPeriod) {
                extraDiscount = System.currentTimeMillis() < extraDiscountPeriod ? dto.getBigDecimal("extraDiscount") : BigDecimal.ZERO;
                extraDiscount = extraDiscount != null ? extraDiscount : BigDecimal.ZERO;
            }else {
                extraDiscount = BigDecimal.ZERO;
            }
            //base折扣-卡
            BigDecimal basePayRate = null != dto.getBigDecimal("basePayRate") ? dto.getBigDecimal("basePayRate"): BigDecimal.ZERO;
            //base折扣-分期付
            BigDecimal baseRate = null != dto.getBigDecimal("baseRate") ? dto.getBigDecimal("baseRate"): BigDecimal.ZERO;

            BigDecimal first = marketingDiscount.add(extraDiscount);

            cardPayDiscount = basePayRate.add(first);
            installmentDiscount = baseRate.add(first);
        }
        cardPayDiscount = null != cardPayDiscount ? cardPayDiscount : BigDecimal.ZERO;
        installmentDiscount = null != installmentDiscount ? installmentDiscount : BigDecimal.ZERO;
        dto.put("cardPayDiscount",cardPayDiscount);
        dto.put("installmentDiscount",installmentDiscount);
        // 设置用户折扣==> 折扣一致,随便返回一个, 不一致 返回最大
        dto.put("userDiscount",cardPayDiscount.compareTo(installmentDiscount)==0 ?
                cardPayDiscount
                : cardPayDiscount.compareTo(installmentDiscount) > 0 ? cardPayDiscount : installmentDiscount);
        return dto;
    }
}
