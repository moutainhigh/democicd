package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.MerchantDAO;
import com.uwallet.pay.main.dao.QrcodeInfoDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.QrcodeInfo;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.*;

/**
 * <p>
 * 二维码信息、绑定
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 二维码信息、绑定
 * @author: baixinyue
 * @date: Created in 2019-12-10 14:39:07
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class QrcodeInfoServiceImpl extends BaseServiceImpl implements QrcodeInfoService {

    @Autowired
    private QrcodeInfoDAO qrcodeInfoDAO;

    @Autowired
    private ServerService serverService;

    @Autowired
    private RechargeRouteService rechargeRouteService;

    @Autowired
    private RouteService routeService;

    @Autowired
    @Lazy
    private UserService userService;

    @Value("${spring.qrCodePath}")
    private String qrCodePath;
    @Value("${spring.qrCodeUrl}")
    private String qrCodeUrl;

    /*@Value("${uWallet.account}")
    private String accountUrl;

    @Autowired
    private StaticDataService staticDataService;*/

    @Autowired
    @Lazy
    private MerchantService merchantService;

    /*@Autowired
    private ParametersConfigService parametersConfigService;

    @Autowired
    private RedisUtils redisUtils;*/
    @Autowired
    private MerchantDAO merchantDAO;

    @Autowired
    private CodeUpdateLogService codeUpdateLogService;

    @Override
    public void saveQrcodeInfo(@NonNull QrcodeInfoDTO qrcodeInfoDTO, HttpServletRequest request) throws BizException {
        QrcodeInfo qrcodeInfo = BeanUtil.copyProperties(qrcodeInfoDTO, new QrcodeInfo());
        log.info("save QrcodeInfo:{}", qrcodeInfo);
        if (qrcodeInfoDAO.insert((QrcodeInfo) this.packAddBaseProps(qrcodeInfo, request)) != 1) {
            log.error("insert error, data:{}", qrcodeInfo);
            throw new BizException("Insert qrcodeInfo Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQrcodeInfoList(@NonNull List<QrcodeInfo> qrcodeInfoList, HttpServletRequest request) throws BizException {
        if (qrcodeInfoList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = qrcodeInfoDAO.insertList(qrcodeInfoList);
        if (rows != qrcodeInfoList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, qrcodeInfoList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateQrcodeInfo(@NonNull Long id, @NonNull QrcodeInfoDTO qrcodeInfoDTO, HttpServletRequest request) throws BizException {
        log.info("full update qrcodeInfoDTO:{}", qrcodeInfoDTO);
        QrcodeInfo qrcodeInfo = BeanUtil.copyProperties(qrcodeInfoDTO, new QrcodeInfo());
        qrcodeInfo.setId(id);
        int cnt = qrcodeInfoDAO.update((QrcodeInfo) this.packModifyBaseProps(qrcodeInfo, request));
        if (cnt != 1) {
            log.error("update error, data:{}", qrcodeInfoDTO);
            throw new BizException("update qrcodeInfo Error!");
        }
    }

    @Override
    public void updateQrcodeInfoSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        qrcodeInfoDAO.updatex(params);
    }

    @Override
    public void logicDeleteQrcodeInfo(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = qrcodeInfoDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteQrcodeInfo(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = qrcodeInfoDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public QrcodeInfoDTO findQrcodeInfoById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        QrcodeInfoDTO qrcodeInfoDTO = qrcodeInfoDAO.selectOneDTO(params);
        return qrcodeInfoDTO;
    }

    @Override
    public QrcodeInfoDTO findOneQrcodeInfo(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        QrcodeInfo qrcodeInfo = qrcodeInfoDAO.selectOne(params);
        QrcodeInfoDTO qrcodeInfoDTO = new QrcodeInfoDTO();
        if (null != qrcodeInfo) {
            BeanUtils.copyProperties(qrcodeInfo, qrcodeInfoDTO);
        }
        return qrcodeInfoDTO;
    }

    @Override
    public List<QrcodeInfoDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<QrcodeInfoDTO> resultList = qrcodeInfoDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return qrcodeInfoDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return qrcodeInfoDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = qrcodeInfoDAO.groupCount(conditions);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("count");
            int count = 0;
            if (StringUtils.isNotBlank(value.toString())) {
                count = Integer.parseInt(value.toString());
            }
            map.put(key, count);
        }
        return map;
    }

    @Override
    public Double sum(String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("sumfield", sumField);
        return qrcodeInfoDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = qrcodeInfoDAO.groupSum(conditions);
        Map<String, Double> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("sum");
            double sum = 0d;
            if (StringUtils.isNotBlank(value.toString())) {
                sum = Double.parseDouble(value.toString());
            }
            map.put(key, sum);
        }
        return map;
    }

    @Override
    public void qrCodeCreate(int createNumber, HttpServletRequest request) throws Exception {
        //生成所传数量的二维码，并保存二维码数据
        List<QrcodeInfo> codeList = new ArrayList<>(createNumber);
        for (int i = 0;i < createNumber;i ++) {
            JSONObject object = qrCodeCreate(qrCodeUrl, null);
            //获取二维码信息
            QrcodeInfo qrcodeInfo = JSONObject.parseObject(object.toJSONString(), QrcodeInfo.class);
            qrcodeInfo.setQrcodeUserType(StaticDataEnum.USER_TYPE_20.getCode());
            qrcodeInfo.setState(StaticDataEnum.QRCODE_STATE_0.getCode());
            codeList.add((QrcodeInfo) this.packAddBaseProps(qrcodeInfo, request));
        }

        //将二维码信息保存到数据库
        this.saveQrcodeInfoList(codeList, request);
    }

    @Override
    public List<String> qrCodeDownLoad(Long[] ids, HttpServletRequest request) {
        List<String> paths = qrcodeInfoDAO.findCodePath(ids);
        List<String> returnMsg = new ArrayList<>(paths.size());
        paths.forEach(path -> {
            returnMsg.add(qrCodePath + path);
        });
        return returnMsg;
    }

    @Override
    public List<QrcodeListDTO> findQRCodeList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<QrcodeListDTO> resultList = qrcodeInfoDAO.findQRCodeList(params);
        return resultList;
    }

    @Override
    public int findQRCodeListCount(Map<String, Object> params) {
        return qrcodeInfoDAO.findQRCodeListCount(params);
    }

    @Override
    public String findQRCodePath(Long merchantId, Long timestamp, String oldPath, HttpServletRequest request) throws Exception {
        //查询用户类型
//        UserDTO userDTO = userService.findUserById(userId);
        //获取二维码
        Map<String, Object> params = new HashMap<>(1);
        params.put("merchantId", merchantId);
        QrcodeInfoDTO qrcodeInfoDTO = findOneQrcodeInfo(params);
        return qrcodeInfoDTO.getPath();
    }

    @Override
    public JSONObject findUserInfoByQRCode(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        //校验
        if(StringUtils.isEmpty(requestInfo.getString("code"))||StringUtils.isEmpty(requestInfo.getString("userId"))){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        QrcodeListDTO qrcodeListDTO = qrcodeInfoDAO.findUserInfoByQRCode(requestInfo.getString("code"));
        // 若为用户，则去账户系统查出用户名
        if (qrcodeListDTO == null) {
            throw new BizException(I18nUtils.get("code.find.user.not.exist", getLang(request)));
        }
        if (qrcodeListDTO.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
            JSONObject userInfo = serverService.userInfoByQRCode(qrcodeListDTO.getUserId());
            String userName = userInfo.getString("userFirstName") + " " + userInfo.getString("userMiddleName") + " " + userInfo.getString("userLastName");
            qrcodeListDTO.setUserName(userName);
            // 返回关于用户的费率:所有用户都用此费率
            Map<String, Object> params = new HashMap<>(1);
            params.put("gatewayType",StaticDataEnum.RECHARGE_ROUTE_TYPE_0);
            RechargeRouteDTO rechargeRouteDTO = rechargeRouteService.findOneRechargeRoute(params);
            qrcodeListDTO.setRate(rechargeRouteDTO.getRate());
            qrcodeListDTO.setRateType(1);

        } else {
            // 返回关于商户的费率
            Map<String, Object> param = new HashMap<>(1);
            param.put("id",qrcodeListDTO.getMerchantId());
            MerchantDTO oneMerchant = merchantService.findOneMerchant(param);
            if (oneMerchant.getId() == null) {
                throw new BizException(I18nUtils.get("merchant not exist", getLang(request)));
            }
            if (oneMerchant.getIsAvailable().intValue() == StaticDataEnum.STATUS_0.getCode()) {
                throw new BizException(I18nUtils.get("merchant.forbidden", getLang(request)));
            }
            Map<String, Object> params = new HashMap<>(2);
            params.put("merchantId",oneMerchant.getId());
            List<RouteDTO> routeDTOList = routeService.findList(params, null, null);
            qrcodeListDTO.setRouteDTOS(routeDTOList);
            // 支付折扣计算 支付让利用户 + 营销折扣 * （1 - 支付折扣率平台占比）+ 额外折扣 * （1 - 额外折扣率平台占比）
            Long today = System.currentTimeMillis();
            Long extraDiscountPeriod = oneMerchant.getExtraDiscountPeriod();
            BigDecimal extraDiscount = null;
            if (today.longValue() < extraDiscountPeriod.longValue()) {
                extraDiscount = oneMerchant.getExtraDiscount();
            } else {
                extraDiscount = new BigDecimal("0.00");
            }
//            BigDecimal payDiscountRate = oneMerchant.getBaseRate().add(extraDiscount).add(oneMerchant.getMarketingDiscount());
//
//            ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
//            BigDecimal payDiscountRate = oneMerchant.getPaySellDiscount()
//                    .add(oneMerchant.getMarketingDiscount()
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getMerchantDiscountRatePlatformProportion()))).setScale(4, RoundingMode.HALF_UP)
//                    .add(extraDiscount
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountPayPlatform()))).setScale(4, RoundingMode.HALF_UP);
            qrcodeListDTO.setBaseRate(oneMerchant.getBaseRate());
            qrcodeListDTO.setExtraDiscount(extraDiscount);
            qrcodeListDTO.setMarketingDiscount(oneMerchant.getMarketingDiscount());
            //获取商户整体出售余额
            BigDecimal wholeSaleBalance = userService.getBalance(qrcodeListDTO.getUserId(),1);
            qrcodeListDTO.setWholeSaleBalance(wholeSaleBalance);
            //商户整体出售用户折扣
            qrcodeListDTO.setWholeSaleUserDiscount(oneMerchant.getWholeSaleUserDiscount());

        }

        //用户钱包余额 ,红包转卡券，余额查询取消
//        BigDecimal userBalance = userService.getBalance(requestInfo.getLong("userId"),null);
        qrcodeListDTO.setBalance(BigDecimal.ZERO);
        //查询该用户分期付状态
        Map<String, Object> params = new HashMap<>(1);
        params.put("id",requestInfo.getString("userId"));
        UserDTO userDTO = userService.findOneUser(params);
        qrcodeListDTO.setInstallmentState(userDTO.getInstallmentState());
        qrcodeListDTO.setStripeState(userDTO.getStripeState() == null ? 0 : userDTO.getStripeState());
        return JSONResultHandle.resultHandle(qrcodeListDTO, QrcodeListDTO.class);
    }

    @Override
    public JSONObject findUserInfoId(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        String userId = requestInfo.getString("userId");
        if (StringUtils.isEmpty(userId)) {
            log.info("user info can not be null, data:{}", requestInfo);
            throw new Exception(I18nUtils.get("user.id.empty", getLang(request)));
        }
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", userId);
        UserDTO userDTO = userService.findOneUser(params);
        log.info("user data:{}", userDTO);
        JSONObject userInfo = serverService.userInfoByQRCode(userDTO.getId());
        userInfo.put("imeiNo", userDTO.getImeiNo());
        if (userInfo != null) {
            userInfo.put("userType",userDTO.getUserType());
            userInfo.put("paymentState",userDTO.getPaymentState());
        }
        return JSONResultHandle.resultHandle(userInfo);
    }

    @Override
    public JSONObject qrCodeCreate(String hopRouting, Long timestamp) throws Exception {
        JSONObject codeData = new JSONObject();
        Long id = SnowflakeUtil.generateId();
        codeData.put("code", id);
        codeData.put("hopRouting", hopRouting+id);
        URI uri = new URI(hopRouting+id);
        BufferedImage image = QRCodeUtil.createQRCode(uri.toString(), 300, 340, null);
//        image = QRCodeUtil.insertWords(image, id.toString());
        JSONObject object = QRCodeUtil.writeFile(image, id.toString(), qrCodePath);
        object.put("hopRouting", hopRouting+id);
        return object;
    }

    @Override
    public void merchantBindingQRCode(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Long merchantId = requestInfo.getLong("merchantId");
        String qrCode = requestInfo.getString("qrCode");
        // 查询出商户和店长
        MerchantDTO merchantDTO = merchantService.findMerchantById(merchantId);
        UserDTO userDTO = userService.findUserById(merchantDTO.getUserId());
        // 查询二维码
        Map<String, Object> params = new HashMap<>(1);
        params.put("code", qrCode);
        QrcodeInfoDTO qrcodeInfoDTO = findOneQrcodeInfo(params);
        if (qrcodeInfoDTO.getId() == null || qrcodeInfoDTO.getState().intValue() == StaticDataEnum.QRCODE_STATE_1.getCode()) {
            throw new BizException(I18nUtils.get("qrcode.not.exist", getLang(request)));
        }
        // 绑定
        qrcodeInfoDTO.setMerchantId(merchantId);
        qrcodeInfoDTO.setUserId(userDTO.getId());
        qrcodeInfoDTO.setCorrelationTime(System.currentTimeMillis());
        qrcodeInfoDTO.setState(StaticDataEnum.STATUS_1.getCode());
        userDTO.setCode(qrCode);
        updateQrcodeInfo(qrcodeInfoDTO.getId(), qrcodeInfoDTO, request);
        userService.updateUser(userDTO.getId(), userDTO, request);

    }

    @Override
    public JSONObject findMerchantByqrCode(String qrCode, HttpServletRequest request) throws Exception {
        if (qrCode==null||StringUtils.isBlank(qrCode)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        JSONObject param=new JSONObject();
        JSONObject result=new JSONObject();
        param.put("code",qrCode);
        QrcodeInfo qrcodeInfo = qrcodeInfoDAO.selectOne(param);
        if (qrcodeInfo==null||qrcodeInfo.getId()==null){
            throw new BizException(I18nUtils.get("qrcode.not.exist", getLang(request)));
        }
        // 判断绑定状态
        result.put("qrCode",qrcodeInfo.getCode());
        if (qrcodeInfo.getState()!=null&&qrcodeInfo.getState()==StaticDataEnum.QRCODE_STATE_0.getCode()){
            result.put("result",false);
            return result;
        }
        //获取商户ID
        if (qrcodeInfo.getMerchantId()==null){
            // 逻辑上不会走
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        // 绑定返回商户信息
        Long merchantId = qrcodeInfo.getMerchantId();
        param.clear();
        param.put("id",merchantId);
        MerchantDTO merchantDTO = merchantDAO.selectOneDTO(param);
        result.put("result",true);
        if (merchantDTO==null){
            result.put("id","");
            result.put("userId","");
            result.put("practicalName","");
        }else {
            result.put("id",merchantDTO.getId());
            result.put("userId",merchantDTO.getUserId());
            result.put("practicalName",merchantDTO.getPracticalName());
        }
        return result;
    }

    @Override
    public JSONObject bindQrCode(String qrCode, Long merchantId, HttpServletRequest request) throws BizException {
        if (qrCode==null||StringUtils.isBlank(qrCode)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (merchantId==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        JSONObject result =new JSONObject();
        // 判断商户是否可用
        JSONObject param=new JSONObject();
        param.put("id",merchantId);
        param.put("isAvailable",StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantDAO.selectOneDTO(param);
        if (merchantDTO==null||merchantDTO.getId()==null){
            throw new BizException(I18nUtils.get("merchant.is.not.exist", getLang(request)));
        }
        // 判断QR码是否可用
        param.clear();
        param.put("code",qrCode);
        QrcodeInfo qrcodeInfoDTO = qrcodeInfoDAO.selectOne(param);
        if (qrcodeInfoDTO==null){
            throw new BizException(I18nUtils.get("qrcode.not.exist", getLang(request)));
        }
        if (qrcodeInfoDTO.getState()==1||qrcodeInfoDTO.getMerchantId()!=null){
            // 逻辑上QR码绑定状态有商户ID
            throw new BizException(I18nUtils.get("qrcode.binded", getLang(request)));
        }
        //绑定QR码
        QrcodeInfo qrcodeInfo = BeanUtil.copyProperties(qrcodeInfoDTO, new QrcodeInfo());
        qrcodeInfo.setMerchantId(merchantId);
        qrcodeInfo.setUserId(merchantDTO.getUserId());
        qrcodeInfo.setCorrelationTime(System.currentTimeMillis());
        qrcodeInfo.setState(StaticDataEnum.QRCODE_STATE_1.getCode());
        QrcodeInfo QrcodeInfoIn = (QrcodeInfo)this.packModifyBaseProps(qrcodeInfo,request);
        int update = qrcodeInfoDAO.update(QrcodeInfoIn);
        if (update!=1){
            throw new BizException(I18nUtils.get("binding.qrcode.failed", getLang(request)));
        }
        //添加操作记录
       try {
           CodeUpdateLogDTO codeUpdateLogDTO=new CodeUpdateLogDTO();
           codeUpdateLogDTO.setCode(qrCode);
           codeUpdateLogDTO.setMerchantId(merchantId);
           codeUpdateLogDTO.setState(StaticDataEnum.BIND_TYPE_BIND.getCode());
           codeUpdateLogDTO.setType(StaticDataEnum.CODE_TYPE_QR.getCode());
           codeUpdateLogService.saveCodeUpdateInfo(codeUpdateLogDTO,request);
       }catch (Exception e){
           log.error("添加操作记录异常,data:{}",e);
       }
        result.put("result",true);
        return result;
    }

    @Override
    public JSONObject unbindQrCode(String qrCode, HttpServletRequest request) throws BizException {
        if (StringUtils.isBlank(qrCode)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        JSONObject param =new JSONObject();
        JSONObject result=new JSONObject();
        CodeUpdateLogDTO codeUpdateLogDTO=new CodeUpdateLogDTO();
        param.put("code",qrCode);
        QrcodeInfo qrcodeInfo = qrcodeInfoDAO.selectOne(param);
        if (qrcodeInfo==null||qrcodeInfo.getId()==null){
            throw new BizException(I18nUtils.get("qrcode.not.exist", getLang(request)));
        }
        codeUpdateLogDTO.setMerchantId(qrcodeInfo.getMerchantId());
        // 判断是否绑定
        if (qrcodeInfo.getState()!=null&&qrcodeInfo.getState().equals(StaticDataEnum.QRCODE_STATE_0.getCode())){
            throw new BizException(I18nUtils.get("qrCode.not.bind", getLang(request)));
        }
        qrcodeInfo.setMerchantId(null);
        qrcodeInfo.setCorrelationTime(null);
        qrcodeInfo.setState(StaticDataEnum.QRCODE_STATE_0.getCode());
        qrcodeInfo.setUserId(null);
        QrcodeInfo qrcodeInfoIn = BeanUtil.copyProperties(qrcodeInfo, new QrcodeInfo());
        int update = qrcodeInfoDAO.removeBindQrCodeInfo(qrcodeInfoIn);
        if (update!=1){
            throw new BizException(I18nUtils.get("qr.remove.error", getLang(request)));
        }
        result.put("result",true);
        //添加操作记录
        try{
            codeUpdateLogDTO.setCode(qrCode);
            codeUpdateLogDTO.setState(StaticDataEnum.BIND_TYPE_UNBIND.getCode());
            codeUpdateLogDTO.setType(StaticDataEnum.CODE_TYPE_QR.getCode());
            codeUpdateLogService.saveCodeUpdateInfo(codeUpdateLogDTO,request);
        }catch (Exception e){
            log.error("添加操作记录异常,data:{}",e);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> findQrList(Map<String, Object> params, HttpServletRequest httpServletRequest) {
        List<Map<String, Object>> qrList = qrcodeInfoDAO.findQrList(params);
        return qrList;
    }

    /**
     * 获取商家二维码列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    @Override
    public List<QrcodeListDTO> listMerchantQrList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return qrcodeInfoDAO.listMerchantQrList(params);
    }

}
