package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.DataEnum;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.IpLocationDAO;
import com.uwallet.pay.main.dao.MerchantDAO;
import com.uwallet.pay.main.dao.TagDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.FavoriteMerchantService;
import com.uwallet.pay.main.service.MerchantListService;
import com.uwallet.pay.main.service.MerchantService;
import com.uwallet.pay.main.service.StaticDataService;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.MerchantListUtils;
import com.uwallet.pay.main.util.TestEnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 通过地理位置查询
 * 商户列表信息表
 * 配套工具类 MerchantListUtils:
 * 1. calDistance==>
 * 通过用户当前经纬度,向MerchantDTO中封装距离该商户的距离信息
 * 2. setUserDiscount==>
 * 通过 整体出售是否>0 标识位,向MerchantDTO设置用户折扣信息
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

@Service
@Slf4j
public class MerchantListServiceImpl extends BaseServiceImpl implements MerchantListService {
    @Resource
    @Lazy
    private FavoriteMerchantService favoriteMerchantService;
    @Resource
    @Lazy
    private RedisUtils redisUtils;
    @Resource
    @Lazy
    private MerchantService merchantService;
    @Resource
    @Lazy
    private MerchantDAO merchantDAO;
    @Value("${server.type}")
    private String serverType;
    @Resource
    private StaticDataService staticDataService;
    @Resource
    private IpLocationDAO ipLocationDAO;
    @Resource
    private TagDAO tagDAO;
    /**
     * 测试环境配置文件type,挡板用
     */
    private static final String TEST_SERVER_TYPE = "test";
    /**
     * 首页推荐商户列表条数
     */
    private static final Integer IS_TOP_LIST_SIZE = 10;
    /**
     * 商户列表展示 默认条数
     */
    private static final Integer DEFAULT_PAGE_SIZ = 10;
    /**
     * 商户列表展示 默认页码
     */
    private static final Integer DEFAULT_PAGE_NUMBER = 1;
    /**
     * 首页新商户 返回数据固定条数
     */
    private static final Integer IS_NEW_VENUES_SIZE = 20;

    /**
     * java代码分页展示示例代码:
     * List<CarerVehInOrOutEntity> entityList = carerVehEntityList.stream()
     * .skip(page.getPageSize() * page.getPageNumber())
     * .limit(page.getPageSize()).collect(Collectors.toList());
     *
     * @param data
     * @param request
     * @return
     * @throws BizException
     */
    @Override
    public JSONObject merchantSearchList(JSONObject data, HttpServletRequest request) throws BizException {
        log.info("获取商户列表,请求参数:{}", data);
        //用户经纬度信息,现在取出, verifyMerchantListParams 方法重新封装请求参数, lat+lng+p+s信息会被抹除
        String userLat = data.getString("lat");
        String userLng = data.getString("lng");
        //初始化 每页显示条数, 当前页数page信息
        Integer pageSize = data.getInteger("s") != null ? data.getInteger("s") : DEFAULT_PAGE_SIZ;
        Integer page = data.getInteger("p") != null ? data.getInteger("p") : DEFAULT_PAGE_NUMBER;
        //排序规则标识位: orderType: 0:折扣排序,1:地理位置排序
        Integer orderType = data.getInteger("orderType");
        //是否是tag,trading name 搜索的首次触发表示位
        Integer tagAdd = data.getInteger("tagAdd");
        //参数校验,参数重新封装
        data = this.verifyMerchantListParams(data, request);
        //结果集,集合+商户列表list
        JSONObject resultJson = new JSONObject(4);
        List<MerchantDTO> result = new LinkedList<>();
        //是否有完全用户地理位置信息表示位
        boolean haveLocationInfo = StringUtils.isNoneEmpty(new String[]{userLat, userLng});
        //通过查询条件获取商户列表,并设置 折扣信息, 距离信息(如果有)
        List<MerchantDTO> merchantDTOList = this.getMerchantList(data, userLat, userLng, haveLocationInfo, request);
        int resultSize = merchantDTOList.size();
        if (CollectionUtil.isNotEmpty(merchantDTOList)) {
            if (orderType.equals(StaticDataEnum.ORDER_BY_DISCOUNT.getCode())) {
                //通过折扣排序
                result = this.processDiscountList(merchantDTOList, result, haveLocationInfo, userLat, userLng, request);
                //只有有地理信息,折扣排序,结果集总大小与查询结果集大小不一致
                resultSize = result.size();
            } else if (orderType.equals(StaticDataEnum.ORDER_BY_SEARCH_KEYWORD.getCode())) {
                //关键字搜索,有位置 距离排序,无折扣排序
                result = this.processSearchByKeyword(merchantDTOList, haveLocationInfo);
                //更新tag popular计数
                if (tagAdd.equals(StaticDataEnum.UPDATE_TAG_POPULAR.getCode())) {
                    this.updateTagPopular(data.getString("searchKeyword"));
                }
            } else if (orderType.equals(StaticDataEnum.ORDER_BY_DISTANCE.getCode())) {
                //地理位置,由近到远排序
                result = merchantDTOList.stream().sorted(Comparator.comparing(MerchantDTO::getDistance).thenComparing(MerchantDTO::getPracticalName)).collect(Collectors.toList());
            } else {
                throw new BizException("orderType: " + I18nUtils.get("parameters.error", getLang(request)));
            }
            /*
            通过 每页显示条数,当前页码(取不到有默认值) 返回该页的信息
            2.  resultSize >= (pageSize * (page - 1)) 获取当前页码的数据
               防止 一共x页, 前端传入的当前页数 > x , stream().skip(跳过的条数) 结果为负数,会报出下标越界异常
             */
            result = resultSize < (pageSize * (page - 1)) ? new LinkedList<>() : result.stream().skip(pageSize * (page - 1)).limit(pageSize).collect(Collectors.toCollection(LinkedList::new));
        }
        //向->结果list数据中封装 城市显示名,tag(3个)信息
        List<MerchantDTO> merchantList = this.processMerchantList(result, false);
        if (redisUtils.hasKey("DISCOUNT_TO_ZERO") && orderType.equals(StaticDataEnum.ORDER_BY_DISTANCE.getCode())) {
            merchantList = this.makeDiscountToZero(merchantList);

        }
        resultJson.put("list", merchantList);
        //封装分页信息
        resultJson.put("pc", this.packPageContextInfo(resultSize, page, pageSize));
        return resultJson;
    }
    @Override
    public Integer merchantListHaveData(JSONObject data, HttpServletRequest request){
        String searchKeyword = data.getString("searchKeyword");
        if (StringUtils.isNotBlank(searchKeyword)){
            JSONObject queryParam = new JSONObject();
            StringUtils.replace(searchKeyword,"/'","/'/'");
            queryParam.put("searchKeyword",StringUtils.strip(searchKeyword));
            queryParam.put("code","merchantCategories");
            //模糊匹配 categories的en_name, 如果匹配上, 触发 mapper categories in 搜索条件
            List<StaticDataDTO> staticDataDTOS = staticDataService.find(queryParam, null, null);
            if (CollectionUtils.isNotEmpty(staticDataDTOS)){
                data.put("categoriesIn",staticDataDTOS.stream().map(StaticDataDTO::getValue).collect(Collectors.joining(",")));
            }
        }
        //获取4个角的经纬度信息, 获取经纬度最大最小值
        JSONObject rangeInfo = data.getJSONObject("rangeInfo");
        if (null != rangeInfo && !rangeInfo.isEmpty()){
            getMerchantIdInRange(data,rangeInfo,request);
        }
        //给app返回结果, 有数据返回1, 无数据返回0
        return merchantDAO.countForList(data) > 0 ? 1 : 0;
    }

    @Override
    public JSONObject merchantList(JSONObject data, HttpServletRequest request) throws BizException {
        this.verifyListParam(data, request);
        //用户经纬度信息
        String userLat = data.getString("lat");
        String userLng = data.getString("lng");
        //2021-07-14添加紧急上线内容, 仅展示用户所在省份的商户信息
        this.getUserProvinceCode(data,getUserId(request),userLat,userLng);
        //初始化 每页显示条数, 当前页数page信息
        Integer pageSize = data.getInteger("s") != null ? data.getInteger("s") : DEFAULT_PAGE_SIZ;
        Integer page = data.getInteger("p") != null ? data.getInteger("p") : DEFAULT_PAGE_NUMBER;
        //排序规则标识位: orderType: 1: Discount,2: nearest, 3: New Venues
        Integer orderType = data.getInteger("orderType");
        //如果没有排序规则, 则默认按照折扣排序
        orderType = null != orderType ? orderType : StaticDataEnum.ORDER_BY_DISCOUNT.getCode();
        orderType = StringUtils.isAnyBlank(new String[]{userLat,userLng}) ? StaticDataEnum.ORDER_BY_DISCOUNT.getCode() : orderType;
        //如果是搜索新商户,按照签约时间排序 DESC
        if (orderType.equals(StaticDataEnum.ORDER_BY_NEW_VENUES.getCode())){ data.put("isNewVenues",true); }
        //是否是tag,trading name 搜索的首次触发表示位
        Integer tagAdd = data.getInteger("tagAdd");
        //更新tag popular计数
        if (null!= tagAdd && tagAdd.equals(StaticDataEnum.UPDATE_TAG_POPULAR.getCode()) && StringUtils.isNotBlank(data.getString("keyword"))) {
            this.updateTagPopular(data.getString("keyword"));
        }
        if(StringUtils.isNotEmpty(data.getString("searchKeyword"))){
            String searchKeyword = data.getString("searchKeyword");
            StringUtils.replace(searchKeyword,"/'","/'/'");
            data.put("searchKeyword",StringUtils.strip(searchKeyword));
        }
        List<JSONObject> jsonList = merchantDAO.findMerchantListJson(data);
        //结果集,集合+商户列表list
        JSONObject resultJson = new JSONObject(4);
        //向查询数据中封装地理位置, 折扣, 卡/分期付折扣信息, 去除无用参数
        MerchantListUtils.processData(jsonList, userLat, userLng, request);
        List<MerchantMiniDTO> result = new LinkedList<>();
        //结果集总大小, 用于分页
        int resultSize = jsonList.size();
        if (CollectionUtil.isNotEmpty(jsonList)) {
            List<MerchantMiniDTO> merchantMiniDTOS = JSONArray.parseArray(JSONArray.toJSONString(jsonList)).toJavaList(MerchantMiniDTO.class);
            if (orderType.equals(StaticDataEnum.ORDER_BY_DISCOUNT.getCode())) {
                //通过折扣排序
                result = this.discountJsonList( merchantMiniDTOS, result, userLat, userLng, request);
                //只有有地理信息,折扣排序,结果集总大小与查询结果集大小不一致
                resultSize = result.size();
            } else if (orderType.equals(StaticDataEnum.ORDER_BY_DISTANCE.getCode())) {
                //地理位置,由近到远排序
                result = merchantMiniDTOS.stream().sorted(Comparator.comparing(MerchantMiniDTO::getDistance).thenComparing(MerchantMiniDTO::getTradingName)).collect(Collectors.toList());
            }else if(orderType.equals(StaticDataEnum.ORDER_BY_NEW_VENUES.getCode())){
                //mapper 中已经排序 order by `merchant_approve_pass_time` DESC
                result = merchantMiniDTOS;
            }
            //根据分页信息, 获取当页展示的内容
            result = resultSize < (pageSize * (page - 1)) ? new LinkedList<>() : result.stream().skip(pageSize * (page - 1)).limit(pageSize).collect(Collectors.toCollection(LinkedList::new));
        }
        resultJson.put("list", result);
        //封装分页信息
        resultJson.put("pc", this.packPageContextInfo(resultSize, page, pageSize));
        return resultJson;
    }

    /**
     * 向data中(查询参数)中封装 用户所在省份对应的
     * @param data
     * @param userId
     * @param userLat
     * @param userLng
     */
    private void getUserProvinceCode(JSONObject data, Long userId, String userLat, String userLng) {
        if (TestEnvUtil.isTestEnv()){
            //测试用挡板 可以指定州信息
            HashMap<String, Object> objectObjectHashMap = Maps.newHashMapWithExpectedSize(1);
            objectObjectHashMap.put("code", "stateTest州测试用");
            StaticDataDTO staticDataById = staticDataService.findOneStaticData(objectObjectHashMap);
            if (null != staticDataById && null!=staticDataById.getId()){
                if (StringUtils.isNotBlank(staticDataById.getValue())) {
                    //人工设置用户所在州测试挡板
                    data.put("merchantState", staticDataById.getValue());
                    return;
                }else if (StringUtils.isNotBlank(staticDataById.getEnName()) && StringUtils.isNotBlank(staticDataById.getName())){
                    //人工设置用户经纬度信息挡板
                    userLat = staticDataById.getEnName();
                    userLng = staticDataById.getName();
                }
            }

        }
        try {
            //根据经纬度获取州-对应的数据字典值 默认为
            String provinceValue = null;
            //redis中州信息缓存的key
            String provinceRedisKey = Constant.getUserProvinceInfo(userId);
            //前端上送了地理位置信息
            if (StringUtils.isNoneEmpty(new String[]{userLat, userLng})) {
                JSONObject cityStreet = MerchantListUtils.getCityStreet(userLat, userLng);
                String tempProvinceStr = cityStreet.getString("stateName");
                if (StringUtils.isNotBlank(tempProvinceStr)) {
                    //获取google返回的州简称的 数据字典值
                    String provinceValueTemp = this.getProvinceStaticData(tempProvinceStr);
                    if (StringUtils.isNotBlank(provinceValueTemp)) {
                        provinceValue = provinceValueTemp;
                        //在redis中缓存用户的上次请求的州信息, 缓存一个星期
                        redisUtils.set(provinceRedisKey, provinceValue, Constant.SEVEN_DAY_SEC);
                        if (TestEnvUtil.isTestEnv()){
                            Object redisInfo = redisUtils.get(provinceRedisKey);
                            log.info("商户列表,redis获取的州value为:"+redisInfo);
                        }
                    }
                    log.info("用户ID: " + userId + ",根据经纬度获取的州名为：" + tempProvinceStr + ",取值为: " + provinceValue);
                }
            }
            //通过地理位置无法获取到用户所在州信息, 则 尝试获取Redis中上次缓存的用户地理位置信息
            if (StringUtils.isBlank(provinceValue) && redisUtils.hasKey(provinceRedisKey)) {
                Object value = redisUtils.get(provinceRedisKey);
                provinceValue = null != value ? value.toString() : null;
            }
            data.put("merchantState", provinceValue);
        }catch (Exception e){
            log.error("获取用户州信息异常,error:{},errorMsg:{}",e,e.getMessage());
        }
    }

    private String getProvinceStaticData(String tempProvinceStr) {
        //查询分类信息
        JSONObject param = new JSONObject(3);
        param.put("code", "merchantState");
        Map<String, String> locationMap = staticDataService.find(param, null, null).stream().collect(Collectors.toMap(StaticDataDTO::getEnName, StaticDataDTO::getValue));
        return locationMap.get(tempProvinceStr);
    }

    private void verifyListParam(JSONObject data, HttpServletRequest request) throws BizException{
        /*//检查地理位置信息完整
        if (StringUtils.isBlank(data.getString("lat")) || StringUtils.isBlank(data.getString("lng"))) {
            throw new BizException(I18nUtils.get("not.complete.location.info", getLang(request)));
        }*/
        String categories = data.getString("categories");
        String orderType = data.getString("orderType");
        String tagAdd = data.getString("tagAdd");
        if ((StringUtils.isNotBlank(orderType) && !StringUtils.isNumeric(orderType))
          ||(StringUtils.isNotBlank(tagAdd) && !StringUtils.isNumeric(tagAdd))
          ||(StringUtils.isNotBlank(categories) && !StringUtils.isNumeric(categories))){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        //关联查询是否被用户收藏用
        data.put("userId",getUserId(request));
        //如果匹配tag, 则必须携带tagAdd参数, 用于更新tag搜索数量的值
        if (StringUtils.isNotBlank(data.getString("keyword")) && null == data.getInteger("tagAdd")){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        String searchKeyword = data.getString("searchKeyword");
        if (StringUtils.isNotBlank(searchKeyword)){
            JSONObject param = new JSONObject();
            param.put("searchKeyword",searchKeyword);
            param.put("code","merchantCategories");
            //模糊匹配 categories的en_name, 如果匹配上, 触发 mapper categories in 搜索条件
            List<StaticDataDTO> staticDataDTOS = staticDataService.find(param, null, null);
            if (CollectionUtils.isNotEmpty(staticDataDTOS)){
                data.put("categoriesIn",staticDataDTOS.stream().map(StaticDataDTO::getValue).collect(Collectors.joining(",")));
            }
        }
        //获取4个角的经纬度信息, 获取经纬度最大最小值
        JSONObject rangeInfo = data.getJSONObject("rangeInfo");
        if (null != rangeInfo && !rangeInfo.isEmpty()){
            getMerchantIdInRange(data,rangeInfo,request);
        }
    }
    private void getMerchantIdInRange(JSONObject data,JSONObject rangeInfo, HttpServletRequest request){

        //左上 经纬度信息
        BigDecimal leftUpLng = rangeInfo.getBigDecimal("leftUpLng");
        BigDecimal leftUpLat = rangeInfo.getBigDecimal("leftUpLat");
        //右上 经纬度信息
        BigDecimal rightUpLng = rangeInfo.getBigDecimal("rightUpLng");
        BigDecimal rightUpLat = rangeInfo.getBigDecimal("rightUpLat");
        //左下 经纬度信息
        BigDecimal leftDownLng = rangeInfo.getBigDecimal("leftDownLng");
        BigDecimal leftDownLat = rangeInfo.getBigDecimal("leftDownLat");
        //右下 经纬度信息
        BigDecimal rightDownLng = rangeInfo.getBigDecimal("rightDownLng");
        BigDecimal rightDownLat = rangeInfo.getBigDecimal("rightDownLat");

        List<BigDecimal> latList = Arrays.asList(leftUpLat, rightUpLat, leftDownLat, rightDownLat);
        List<BigDecimal> lngList = Arrays.asList(leftUpLng, rightUpLng, leftDownLng, rightDownLng);
        JSONObject locationRange = new JSONObject(5);
        locationRange.put("latMax",Collections.max(latList));
        locationRange.put("latMin",Collections.min(latList));
        locationRange.put("lngMax",Collections.max(lngList));
        locationRange.put("lngMin",Collections.min(lngList));
        data.put("locationRange",locationRange);
        data.remove("rangeInfo");
    }

    /**
     * 通过位置(可有可无)查询商户列表->参数检查
     * 参数列表:
     * app传入参数列表:
     * 经纬度: lat (正则格式校验)
     * 经纬度: lng (正则格式校验)
     * trading name/tag 匹配关键字: searchKeyword
     * 城市：city
     * 页数: p
     * 每页显示数量: s
     * 后端添加参数列表:
     * 删除状态：status
     * 可用状态：is_available
     *
     * @param data
     * @param request
     * @return
     * @throws BizException
     */
    private JSONObject verifyMerchantListParams(JSONObject data, HttpServletRequest request) throws BizException {
        JSONObject repackRequestParam = new JSONObject(data.size());
        String lat = data.getString("lat");
        String lng = data.getString("lng");
        //检查地理位置信息完整, 有则全有,无则全无
        if ((StringUtils.isNotBlank(lat) && StringUtils.isEmpty(lng))
                || (StringUtils.isNotBlank(lng) && StringUtils.isEmpty(lat))) {
            throw new BizException(I18nUtils.get("not.complete.location.info", getLang(request)));
        }
        boolean haveLocationInfo = StringUtils.isNoneEmpty(new String[]{lat, lng});
        //排序规则表示位
        if (StringUtils.isEmpty(data.getString("orderType"))) {
            throw new BizException("orderType " + I18nUtils.get("parameters.null", getLang(request)));
        }
        //如果排序为地理位置排序,必须携带 地理位置信息
        Integer orderType = data.getInteger("orderType");
        if (orderType.equals(StaticDataEnum.ORDER_BY_DISTANCE.getCode()) && !haveLocationInfo) {
            throw new BizException(I18nUtils.get("not.complete.location.info", getLang(request)));
        }
        //如果地理位置信息完整,封装 sql搜索条件, 当locationInfo有值,搜索条件加入,merchant数据 lat,lng都不为空
        if (haveLocationInfo) {
            repackRequestParam.put("locationInfo", true);
        }
        //搜索关键字, 模糊匹配 tradingName,tag是否包含关键字
        String searchKeyword = data.getString("searchKeyword");
        if (orderType.equals(StaticDataEnum.ORDER_BY_SEARCH_KEYWORD.getCode()) && (StringUtils.isBlank(searchKeyword) || StringUtils.isBlank(data.getString("tagAdd")))) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        if (StringUtils.isNotBlank(searchKeyword)) {
            repackRequestParam.put("searchKeyword", searchKeyword);
        }
        if (StringUtils.isNotBlank(data.getString("city"))) {
            //前端传入city(可能中文,也可能英文),与数据字典数据比对,如果不包含则报错
            //Map<String, List<StaticData>> byCodeList = staticDataService.findByCodeList(new String[]{"city"});
            repackRequestParam.put("city", data.getString("city"));
        }
        return repackRequestParam;
    }

    @Async("taskExecutor")
    void updateTagPopular(String searchKeyword) {
        JSONObject param = new JSONObject(5);
        param.put("adminId",Constant.ADMIN_ID);
        param.put("time",System.currentTimeMillis());
        param.put("ip",Constant.IP);
        param.put("keyword",searchKeyword);
        tagDAO.updateTagPopular(param);
    }

    /**
     * 关键字搜索,有位置 距离排序,无折扣排序
     *
     * @param merchantDTOList
     * @param haveLocationInfo
     * @return
     */
    private List<MerchantDTO> processSearchByKeyword(List<MerchantDTO> merchantDTOList, boolean haveLocationInfo) {
        if (haveLocationInfo) {
            //地理位置,由近到远排序
            return merchantDTOList.stream().sorted(Comparator.comparing(MerchantDTO::getDistance).thenComparing(MerchantDTO::getPracticalName)).collect(Collectors.toCollection(LinkedList::new));
        } else {
            //无地理位置信息查询,商户折扣由大到小
            return merchantDTOList.stream().sorted(Comparator.comparing(MerchantDTO::getUserDiscount).reversed().thenComparing(MerchantDTO::getPracticalName)).collect(Collectors.toCollection(LinkedList::new));
        }
    }


    /**
     * 距离用户半径距离为5公里的商户按照折扣大小排序后
     * 再将5公里至整个city的商户按照折扣大小排序
     * 如果获取不到city信息,则 >5km 至整个澳洲 折扣排序
     *
     * @param merchantDTOList
     * @param result
     * @param haveLocationInfo
     * @param userLat
     * @param userLng
     * @param request
     * @return
     */
    private List<MerchantDTO> processDiscountList(List<MerchantDTO> merchantDTOList,
                                                  List<MerchantDTO> result,
                                                  boolean haveLocationInfo,
                                                  String userLat,
                                                  String userLng,
                                                  HttpServletRequest request) {
        //折扣排序有地理位置信息,或者按照地理位置信息排序,走该分支
        if (haveLocationInfo) {
            //有用户地理位置信息查询
            JSONObject cityCodeInfo = this.getCityCode(userLat, userLng, haveLocationInfo);
            //数据字典的city对应int值,可能为null,为null 则 >5km 至整个澳洲 折扣排序
            String cityCode = cityCodeInfo.getString("cityCode");

            for (int i = 1; i < 3; i++) {
                if (i == 1) {
                    //第1次筛选,获取3km内的商户,折扣由大到小排序
                    result.addAll(MerchantListUtils.getDistanceMerchant(merchantDTOList, Constant.FIVE_KM, false));
                } else {
                    //第2次筛选, 如果有城市信息,大于5公里 整个城市的商家
                    if (StringUtils.isNotBlank(cityCode)) {
                        result.addAll(MerchantListUtils.withinSameCityMerchant(merchantDTOList, Constant.FIVE_KM, cityCode));
                    } else {
                        //没有城市->整个澳洲(距离>5公里的所有商户)
                        result.addAll(MerchantListUtils.getDistanceMerchant(merchantDTOList, Constant.FIVE_KM, true));
                    }
                }
            }
        } else {
            //无地理位置信息查询,商户折扣由大到小
            return merchantDTOList.stream().sorted(Comparator.comparing(MerchantDTO::getUserDiscount).reversed().thenComparing(MerchantDTO::getPracticalName)).collect(Collectors.toCollection(LinkedList::new));
        }
        return result;
    }
    /**
     * Discount排序---按照距离用户距离为1.5公里的签约商户按照折扣大小排序后，
     * 距离用户距离为1.5公里到6公里的签约商户按照折扣大小排序，继续展示
     * 取6公里至整个city的签约商户，按照折扣大小排序，继续展示
     * City以外至整个澳大利亚签约商户按照折扣大小排序
     * @param merchantMiniDTOS
     * @param result
     * @param userLat
     * @param userLng
     * @param request
     * @return
     */
    private List<MerchantMiniDTO> discountJsonList(List<MerchantMiniDTO> merchantMiniDTOS, List<MerchantMiniDTO> result, String userLat, String userLng, HttpServletRequest request) {
        /*//有用户地理位置信息查询
        JSONObject cityCodeInfo = this.getCityCode(userLat, userLng, true);
        //数据字典的city对应int值,可能为null,为null 则 >5km 至整个澳洲 折扣排序
        String cityCode = cityCodeInfo.getString("cityCode");*/
        for (int i = 1; i < 4; i++) {
            LinkedList<MerchantMiniDTO> collect = new LinkedList<>();
            if (i == 1) {
                //按照距离用户距离为1.5公里的签约商户按照折扣大小排序后，
                 collect = merchantMiniDTOS.stream()
                        .filter(mer -> mer.getDistance().compareTo(new BigDecimal(Constant.ONE_POINT_FIVE_KM)) <= 0)
                        .sorted(Comparator.comparing(MerchantMiniDTO::getUserDiscount).reversed().thenComparing(MerchantMiniDTO::getDistance).thenComparing(MerchantMiniDTO::getTradingName))
                        .collect(Collectors.toCollection(LinkedList::new));
            } else if (i == 2){
                //距离用户距离为1.5公里到6公里的签约商户按照折扣大小排序，继续展示
                 collect = merchantMiniDTOS.stream()
                        .filter(mer -> mer.getDistance().compareTo(new BigDecimal(Constant.ONE_POINT_FIVE_KM)) > 0 && mer.getDistance().compareTo(new BigDecimal(Constant.SIX_KM)) <= 0)
                        .sorted(Comparator.comparing(MerchantMiniDTO::getUserDiscount).reversed().thenComparing(MerchantMiniDTO::getDistance).thenComparing(MerchantMiniDTO::getTradingName))
                        .collect(Collectors.toCollection(LinkedList::new));
            }else {
                    //取6公里至整个city的签约商户，按照折扣大小排序，继续展示-->已经作废
                    //2021-07-14 修改逻辑,大于6公里到整个州的商户(查出来的商户本来就是这个州的, 不用加额外筛选条件)
                    collect = merchantMiniDTOS.stream()
                            .filter(mer -> mer.getDistance().compareTo(new BigDecimal(Constant.SIX_KM)) > 0)
                            .sorted(Comparator.comparing(MerchantMiniDTO::getUserDiscount).reversed().thenComparing(MerchantMiniDTO::getDistance).thenComparing(MerchantMiniDTO::getTradingName))
                            .collect(Collectors.toCollection(LinkedList::new));

            }/*else {
                if (StringUtils.isNotBlank(cityCode)){
                   // City以外至整个澳大利亚签约商户按照折扣大小排序
                    //取6公里至整个city的签约商户，按照折扣大小排序，继续展示
                    collect = merchantMiniDTOS.stream()
                            .filter(mer -> !cityCode.equals(mer.getCity()))
                            .sorted(Comparator.comparing(MerchantMiniDTO::getUserDiscount).reversed().thenComparing(MerchantMiniDTO::getDistance).thenComparing(MerchantMiniDTO::getTradingName))
                            .collect(Collectors.toCollection(LinkedList::new));
                }else {
                    collect = merchantMiniDTOS.stream()
                            .filter(mer -> mer.getDistance().compareTo(new BigDecimal(Constant.SIX_KM)) > 0)
                            .sorted(Comparator.comparing(MerchantMiniDTO::getUserDiscount).reversed().thenComparing(MerchantMiniDTO::getDistance).thenComparing(MerchantMiniDTO::getTradingName))
                            .collect(Collectors.toCollection(LinkedList::new));
                }
            }*/
            result.addAll(collect);
        }
        return result;
    }

    /**
     * 封装分页信息
     *
     * @param dataSize
     * @param page
     * @param pageSize
     * @return
     */
    private Object packPageContextInfo(int dataSize, Integer page, Integer pageSize) {
        PagingContext pagingContext = new PagingContext();
        pagingContext.setPageSize(pageSize);
        pagingContext.setTotal(dataSize);
        //如果列表为空, 总页数为0
        pagingContext.setMaxPages(dataSize > 0 ? (int) Math.ceil(dataSize / pageSize) : Constant.ZERO);
        pagingContext.setPageIndex(page);
        return pagingContext;
    }

    /**
     * 根据条件获取商户列表信息
     * --->  封装用户折扣信息
     * --->  如果有用户地理位置信息,封装商户到用户距离信息
     *
     * @param data             查询参数
     * @param userLat          用户经度
     * @param userLng          用户纬度
     * @param haveLocationInfo 是否有用户地理位置信息
     * @param request
     * @return List<MerchantDTO>
     */
    private List<MerchantDTO> getMerchantList(JSONObject data,
                                              String userLat, String userLng,
                                              Boolean haveLocationInfo,
                                              HttpServletRequest request) {
        //加入条件,商户可用, 2021-03-30加入条件, contract complete
        data.put("isAvailable", StaticDataEnum.MERCHANT_AVAILABLE_1.getCode());
        data.put("docusignHasSigned",StaticDataEnum.DOCUSIGN_COMPLETE.getCode());
        data.put("state",1);
        List<MerchantDTO> merchantDTOS = merchantService.find(data, null, null);
        //封装 折扣+距离位置信息
        merchantDTOS.forEach(dto -> {
            //封装用户折扣信息
            MerchantListUtils.setCardInstallmentDiscount(dto);
            //如果有用户地理位置信息,封装商户到用户距离信息
            if (haveLocationInfo) {
                dto.setDistance(MerchantListUtils.calDistance(dto, userLat, userLng));
            }
        });
        return merchantDTOS;
    }

    /**
     * lat (正则格式校验)
     * lng (正则格式校验)
     *
     * @param data
     * @param request
     * @return
     */
    @Override
    public List<MerchantDTO> getTopTenList(JSONObject data, HttpServletRequest request) {
        log.info("获取首页推荐商户列表,请求参数:{}", data);
        String userLat = data.getString("lat");
        String userLng = data.getString("lng");
        boolean isTestServer = StringUtils.isNoneEmpty(new String[]{userLat, userLng}) && serverType.equals(TEST_SERVER_TYPE);
        /*if (isTestServer){
            //测试挡板,固定
            userLat = "-27.4687";
            userLng = "153.0225";
        }*/
        //是否有完全用户地理位置信息表示位
        boolean haveLocationInfo = StringUtils.isNoneEmpty(new String[]{userLat, userLng});
        List<MerchantDTO> merchantDTOList = merchantDAO.findTopTenNoLocation(data);
        merchantDTOList.forEach(dto -> {
            //设置用户折扣
            MerchantListUtils.setCardInstallmentDiscount(dto);
            //封装商户到用户距离信息
            dto.setDistance(haveLocationInfo ? MerchantListUtils.calDistance(dto, userLat, userLng) : null);
        });
        //有用户信息
        if (haveLocationInfo) {
            //经纬度获取用户城市信息,可能获取不到
            JSONObject cityJson = this.getCityCode(userLat, userLng, isTestServer);
            String cityCode = cityJson.getString("cityCode");
            List<MerchantDTO> result = new LinkedList<>();
            for (int i = 1; i < 5; i++) {
                if (result.size() >= 10) {
                    break;
                }
                if (i == 1) {
                    //第一次筛选,获取1.5km内的商户,折扣由大到小排序
                    result.addAll(MerchantListUtils.getDistanceMerchant(merchantDTOList, Constant.ONE_POINT_FIVE_KM, false));
                } else if (i == 2) {
                    //第2次筛选, 查询 1.5-6 公里的商家
                    result.addAll(MerchantListUtils.getRangeMerchantList(merchantDTOList, Constant.ONE_POINT_FIVE_KM, Constant.SIX_KM));
                } else if (i == 3) {
                    //第3次筛选, 如果有城市信息,大于6公里 整个城市的商家
                    if (StringUtils.isNotBlank(cityCode)) {
                        result.addAll(MerchantListUtils.withinSameCityMerchant(merchantDTOList, Constant.SIX_KM, cityCode));
                    } else {
                        //没有城市->整个澳洲(距离>6公里的所有商户)
                        result.addAll(MerchantListUtils.getDistanceMerchant(merchantDTOList, Constant.SIX_KM, true));
                        break;
                    }
                } else {
                    //第4次筛选,不在该城市,全澳洲的商家
                    result.addAll(MerchantListUtils.noInSameCityMerchant(merchantDTOList, cityCode));
                }
            }
            return result.stream().limit(IS_TOP_LIST_SIZE).collect(Collectors.toList());
        }
        //无地理位置信息,直接按照全澳洲商家折扣排序,返回10条信息
        List<MerchantDTO> collect = merchantDTOList.stream().sorted(Comparator.comparing(MerchantDTO::getUserDiscount).reversed()).limit(IS_TOP_LIST_SIZE).collect(Collectors.toList());
        return collect;

    }

    /**
     * 尝试通过经纬度获取city,street信息
     *
     * @param userLat
     * @param userLng
     * @param isTestServer
     * @return
     */
    private JSONObject getCityCode(String userLat, String userLng, boolean isTestServer) {
        if (isTestServer) {
            JSONObject res = new JSONObject(5);
            /*res.put("cityName", "Brisbane");
            res.put("cityCode", 1);
            res.put("street", "test street");*/
            return res;
        }
        //三方接口获取city,street信息,获取失败返回空 JSONObject
        JSONObject cityStreetResult = MerchantListUtils.getCityStreet(userLat, userLng);
        String city = cityStreetResult.getString("city");
        if (StringUtils.isNotBlank(city)) {
            //获取数据字典中的城市对应的-->INT value
            cityStreetResult.put("cityCode", this.findCityCodeByName(city));
        }
        return cityStreetResult;
    }

    @Override
    public List<MerchantDTO> getNewVenus(JSONObject data, HttpServletRequest request) {
        log.info("获取首页推荐商户列表商户首页推荐列表-新增商户,请求参数:{}", data);
        String userLat = data.getString("lat");
        String userLng = data.getString("lng");
        /*if (StringUtils.isNoneEmpty(new String[]{userLat, userLng}) && serverType.equals(TEST_SERVER_TYPE)){
            //测试挡板,固定
            userLng = "-27.4687";
            userLat = "153.0225";
        }*/
        List<MerchantDTO> merchantDTOList;
        data.put("limit", IS_NEW_VENUES_SIZE);
        boolean isTestServer = StringUtils.isNoneEmpty(new String[]{userLat, userLng}) && serverType.equals(TEST_SERVER_TYPE);
        //是否有完全用户地理位置信息表示位
        boolean haveLocationInfo = StringUtils.isNoneEmpty(new String[]{userLat, userLng});
        if (haveLocationInfo) {
            //经纬度获取用户城市信息,可能获取不到
            JSONObject cityJson = this.getCityCode(userLat, userLng, isTestServer);
            String cityCode = cityJson.getString("cityCode");
            //city取不到不触发搜索条件,默认整个澳洲按照签约完成时间排序
            data.put("city", cityCode);
            merchantDTOList = merchantDAO.getNewVenus(data);
            //规定返回20条,如果用city搜索,且不足20条
            if (merchantDTOList.size() < IS_NEW_VENUES_SIZE && StringUtils.isNotBlank(cityCode)) {
                //搜索不在该city的商户,补全不足20条的数据量
                data.put("noInCity", cityCode);
                data.remove("city");
                data.put("limit", IS_NEW_VENUES_SIZE - merchantDTOList.size());
                merchantDTOList.addAll(merchantDAO.getNewVenus(data));
            }
        } else {
            merchantDTOList = merchantDAO.getNewVenus(data);
        }
        //设置用户折扣
        merchantDTOList.forEach(dto -> {
            if (haveLocationInfo) {
                dto.setDistance(MerchantListUtils.calDistance(dto, userLat, userLng));
            }
            dto = MerchantListUtils.setCardInstallmentDiscount(dto);
        });
        if (redisUtils.hasKey("DISCOUNT_TO_ZERO")) {
            this.makeDiscountToZero(merchantDTOList);
        }
        return merchantDTOList;
    }

    private List<MerchantDTO> makeDiscountToZero(List<MerchantDTO> merchantDTOList) {
        merchantDTOList.forEach(merchantDTO -> merchantDTO.setUserDiscount(BigDecimal.ZERO));
        return merchantDTOList;
    }

    @Override
    public List<MerchantDTO> processMerchantList(List<MerchantDTO> resultList, Boolean mark) {
        if (CollectionUtil.isNotEmpty(resultList)) {
            //获取数据字典city信息,转换成 < city名对应的int值, city名(英文)>
            Map<String, List<StaticData>> byCodeList = staticDataService.findByCodeList(new String[]{"city", "merchantState"});
            Map<String, String> cityMap = byCodeList.get("city").stream().collect(Collectors.toMap(StaticData::getValue, StaticData::getEnName));
            resultList.forEach(dto -> {
                //设置city code对应的显示名
                String cityCoe = dto.getCity();
                String merchantState = dto.getMerchantState();
                dto.setCityName(StringUtils.isNotBlank(cityCoe) ? cityMap.get(cityCoe) : "");

                //将keyword字段中','分割的tag封装成 List<String> ,返回最多3个tag(不管有几个)
                if (StringUtils.isNotBlank(dto.getKeyword())) {
                    dto.setTags(Arrays.stream(dto.getKeyword().split(",")).filter(StringUtils::isNotBlank).limit(3).collect(Collectors.toList()));
                }else {
                    dto.setTags(new ArrayList<>());
                }
                if (mark) {
                    //获取州显示名
                    Map<String, String> merchantStateMap = byCodeList.get("merchantState").stream().collect(Collectors.toMap(StaticData::getValue, StaticData::getEnName));
                    dto.setStateNameStr(StringUtils.isNotBlank(merchantState) ? merchantStateMap.get(merchantState) : "");
                    //设置详情页照片,封装成list
                    List<String> picList = new ArrayList<>();
                    picList.add(dto.getLogoUrl());
                    if (StringUtils.isNotBlank(dto.getDetailPhotoUrl())){
                        picList.addAll(Arrays.stream(dto.getDetailPhotoUrl().split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
                    }
                    dto.setDetailPhotoList(picList);

                }
            });
        }
        return resultList;
    }

    @Override
    public JSONObject findCityStInfo(JSONObject data, HttpServletRequest request) throws BizException {
        String userLat = data.getString("lat");
        String userLng = data.getString("lng");
        JSONObject res;
        if (StringUtils.isNoneEmpty(new String[]{userLat, userLng})) {
            if (serverType.equals(TEST_SERVER_TYPE)) {
                //测试挡板,固定
                return this.returnTestRes();
            }
            res = MerchantListUtils.getCityStreet(userLat, userLat);
            //封装 数据字典表 cityName对应的int值
            res.put("cityCode", StringUtils.isNotBlank(res.getString("cityName")) ? this.findCityCodeByName(res.getString("cityName")) : " ");
            //findStateCodeByName
            res.put("stateCode", StringUtils.isNotBlank(res.getString("stateName")) ? this.findCityCodeByName(res.getString("stateName")) : " ");
        } else {
            res = new JSONObject(2);
            res.put("cityCoe","");
            res.put("street", "");
            res.put("cityName","");
            res.put("stateCode","");
        }
        return res;
    }

    @Override
    public List<MerchantDTO> getMerchantLocationList(JSONObject data, HttpServletRequest request) throws BizException {
        String userLat = data.getString("lat");
        String userLng = data.getString("lng");
        boolean haveLocationInfo = StringUtils.isNoneEmpty(new String[]{userLat, userLng});
        if (haveLocationInfo) {
            JSONObject param = new JSONObject(4);
            //触发mapper,地理位置不为null的搜索条件
            param.put("locationInfo", true);
            List<MerchantDTO> merchantDTOS = this.getMerchantList(param, userLat, userLng, haveLocationInfo, request);
            LinkedList<MerchantDTO> dtoLinkedList = new LinkedList<>();
            if (CollectionUtil.isNotEmpty(merchantDTOS)) {
                dtoLinkedList = merchantDTOS.stream()
                        .filter(dto -> null != dto.getDistance())
                        .sorted(Comparator.comparing(MerchantDTO::getDistance))
                        .collect(Collectors.toCollection(LinkedList::new));
            }
            JSONObject categoryParam = new JSONObject(3);
            categoryParam.put("code","merchantCategories");
            Map<String, String> categoryMap = staticDataService.find(categoryParam, null, null).stream().collect(Collectors.toMap(StaticDataDTO::getValue, StaticDataDTO::getEnName));
            dtoLinkedList.forEach(dto->{
                //将keyword字段中','分割的tag封装成 List<String> ,返回最多3个tag(不管有几个)
                if (StringUtils.isNotBlank(dto.getKeyword())) {
                    dto.setTags(Arrays.stream(dto.getKeyword().split(",")).filter(StringUtils::isNotBlank).limit(3).collect(Collectors.toList()));
                }
                //转换分类名
                if(Objects.nonNull(dto.getCategories())){
                    dto.setCategoriesStr(StringUtils.isNotBlank(categoryMap.get(dto.getCategories().toString())) ? categoryMap.get(dto.getCategories().toString()) : " ");
                }
            });
            return dtoLinkedList;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public JSONObject findLocationByIp(String ip, HttpServletRequest request) throws BizException {
        IpLocationDTO ipLocationDTO = null;
        if (StringUtils.isNotBlank(ip)) {
            //测试挡板->仅测试环境,数据字典中存在code=fakeIpData,且ip有值的时候启用
            if (TestEnvUtil.isTest) {
                //测试挡板,固定
                JSONObject param = new JSONObject(4);
                param.put("code", "fakeIpData");
                StaticDataDTO fakeIpData = staticDataService.findOneStaticData(param);
                if (null != fakeIpData && StringUtils.isNotBlank(fakeIpData.getIp())) {
                    ip = fakeIpData.getIp();
                }
            }
            try {
                String[] ipArray = ip.split("\\.");
                if (ipArray.length == 4) {
                    List<Long> ipNums = new ArrayList<>(4);
                    for (int i = 0; i < 4; ++i) {
                        ipNums.add(Long.parseLong(ipArray[i].trim()));
                    }
                    long finalIpNum = ipNums.get(0) * 256L * 256L * 256L
                            + ipNums.get(1) * 256L * 256L + ipNums.get(2) * 256L
                            + ipNums.get(3);
                    ipLocationDTO = ipLocationDAO.selectIpLocationByIp(finalIpNum);
                }
            } catch (Exception e) {
                log.info("通过ip获取地理位置信息失败, 放行, 不抛出异常");
                ipLocationDTO = null;
            }
        }
        JSONObject result = new JSONObject(6);
        result.put("city", ipLocationDTO != null ? ipLocationDTO.getCity() : "");
        result.put("lng", ipLocationDTO != null ? ipLocationDTO.getLng() : "");
        result.put("lat", ipLocationDTO != null ? ipLocationDTO.getLat() : "");
        return result;
    }

    @Override
    public MerchantDTO getMerchantDetail(Long merchantId, HttpServletRequest request) throws BizException {
        MerchantDTO dto = merchantService.findMerchantById(merchantId);
        if (null == dto || null == dto.getId()) {
            throw new BizException(I18nUtils.get("merchant.not.found", getLang(request)));
        }
        MerchantListUtils.setCardInstallmentDiscount(dto);
        //封装城市显示名,tag信息的接口-之前写的方法,接收参数为list,用于批量处理
        dto = this.processMerchantList(Collections.singletonList(dto),true).get(0);
        String address =      StringUtils.isNotBlank(dto.getAddress())?dto.getAddress():"";
        String cityName =     StringUtils.isNotBlank(dto.getCityName())?dto.getCityName():"";
        String stateNameStr = StringUtils.isNotBlank(dto.getStateNameStr())?dto.getStateNameStr():"";
        String fullAddress = address + " "+ cityName +" "+ stateNameStr;
        String businessPhone = dto.getBusinessPhone();
        dto.setBusinessPhone(StringUtils.isNotBlank(businessPhone) ? businessPhone:"");
        String practicalName = dto.getPracticalName();
        dto.setPracticalName(StringUtils.isNotBlank(practicalName) ? practicalName:"");
        String lat = dto.getLat();
        dto.setLat(StringUtils.isNotBlank(lat) ? lat:"");
        String lng = dto.getLng();
        dto.setLng(StringUtils.isNotBlank(lng) ? lng:"");
        dto.setFullAddress(StringUtils.isNotBlank(fullAddress) ? fullAddress:"");
        if (TestEnvUtil.isTestEnv()) {
            //测试用挡板 可以指定地址信息
            HashMap<String, Object> objectObjectHashMap = Maps.newHashMapWithExpectedSize(1);
            objectObjectHashMap.put("code", "stateTest州测试用");
            StaticDataDTO staticDataById = staticDataService.findOneStaticData(objectObjectHashMap);
            if (null != staticDataById && null != staticDataById.getId()) {
                if (StringUtils.isNotBlank(staticDataById.getIp())) {
                    //todo 商户地址信息挡板
                    dto.setFullAddress(staticDataById.getIp());
                }
            }
        }
        //2021-04-13添加逻辑, 查询客户是否已经收藏该商户
        JSONObject param = new JSONObject(4);
        param.put("userId",getUserId(request));
        param.put("merchantId",merchantId);
        try {
            FavoriteMerchantDTO oneFavoriteMerchant = favoriteMerchantService.findOneFavoriteMerchant(param);
            //如果dto存在即为已经收藏
            boolean favoriteBoolean = oneFavoriteMerchant != null && null != oneFavoriteMerchant.getId();
            dto.setIsFavorite( favoriteBoolean ? DataEnum.IS_FAVORITE.getCode() : DataEnum.IS_NOT_FAVORITE.getCode());
            Integer categories = dto.getCategories();
            dto.setCategoriesStr(this.getCategoriesStr(categories));
        }catch (Exception e){
            log.error("获取商户是否被收藏异常,error:{},error msg:{}",e,e.getMessage());
        }
        log.info("返回数据,:{}",dto);
        return dto;
    }

    /**
     * 获取 商户分类的显示名称
     * @param categories
     * @return
     */
    private String getCategoriesStr(Integer categories) {
            if (null != categories){
                JSONObject param = new JSONObject();
                param.put("value",categories);
                param.put("code","merchantCategories");
                StaticDataDTO staticData = staticDataService.findOneStaticData(param);
                if (null != staticData && null!=staticData.getId()){
                    return staticData.getEnName();
                }
            }
            return " ";
    }
    @Override
    public List<MerchantAppHomePageDTO> packDistanceCategoriesInfo(List<MerchantAppHomePageDTO> merchantList, String userLat, String userLng) {
        if (CollectionUtil.isNotEmpty(merchantList)){
            JSONObject param = new JSONObject(3);
            param.put("code","merchantCategories");
            Map<String, String> collect = staticDataService.find(param, null, null).stream().collect(Collectors.toMap(StaticDataDTO::getValue, StaticDataDTO::getEnName));
            merchantList.forEach(dto->{
                BigDecimal distance = MerchantListUtils.calDistanceAppHome(dto, userLat, userLng);
                dto.setDistance(null != distance ? distance.toString() : " ");
//                dto.setDistance(null != distance ? distance : new BigDecimal(0));
                dto.setLat(null);
                dto.setLng(null);
                Integer categories = dto.getCategories();
                dto.setCategoriesStr(null!=categories && null!=collect.get(categories.toString()) ? collect.get(categories.toString()) : " ");
                dto.setCategories(null);
            });
            return merchantList;
        }
        return null;
    }



    private JSONObject returnTestRes() {
        JSONObject res = new JSONObject(5);
        res.put("cityName", "Brisbane");
        res.put("cityCode", 1);
        res.put("street", "test street");
        return res;
    }

    private Object findCityCodeByName(String cityName) {
        JSONObject param = new JSONObject(4);
        param.put("code", "city");
        param.put("en_name", cityName);
        StaticDataDTO oneStaticData = staticDataService.findOneStaticData(param);
        if (null == oneStaticData || null == oneStaticData.getId()){
            return " ";
        }
        return oneStaticData.getValue();
    }
    private Object findStateCodeByName(String stateName) {
        JSONObject param = new JSONObject(4);
        param.put("code", "merchantState");
        param.put("en_name", stateName);
        StaticDataDTO oneStaticData = staticDataService.findOneStaticData(param);
        if (null == oneStaticData || null == oneStaticData.getId()){
            return " ";
        }
        return oneStaticData.getValue();
    }
}
