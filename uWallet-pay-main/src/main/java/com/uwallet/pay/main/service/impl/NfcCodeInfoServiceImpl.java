package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.NfcCodeInfoDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.NfcCodeInfo;
import com.uwallet.pay.main.model.entity.User;
import com.uwallet.pay.main.model.excel.Nfc;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.exception.BizException;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * <p>
 * NFC信息、绑定表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: NFC信息、绑定表
 * @author: zhoutt
 * @date: Created in 2020-03-23 14:31:21
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class NfcCodeInfoServiceImpl extends BaseServiceImpl implements NfcCodeInfoService {

    @Autowired
    private NfcCodeInfoDAO nfcCodeInfoDAO;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private ParametersConfigService parametersConfigService ;

    @Autowired
    private CodeUpdateLogService codeUpdateLogService;

    @Value("${uWallet.account}")
    private String accountUrl;


    @Override
    public void saveNfcCodeInfo(@NonNull NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request) throws BizException {
        NfcCodeInfo nfcCodeInfo = BeanUtil.copyProperties(nfcCodeInfoDTO, new NfcCodeInfo());
        log.info("save NfcCodeInfo:{}", nfcCodeInfo);
        if (nfcCodeInfoDAO.insert((NfcCodeInfo) this.packAddBaseProps(nfcCodeInfo, request)) != 1) {
            log.error("insert error, data:{}", nfcCodeInfo);
            throw new BizException("Insert nfcCodeInfo Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNfcCodeInfoList(@NonNull List<NfcCodeInfo> nfcCodeInfoList, HttpServletRequest request) throws BizException {
        if (nfcCodeInfoList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = nfcCodeInfoDAO.insertList(nfcCodeInfoList);
        if (rows != nfcCodeInfoList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, nfcCodeInfoList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateNfcCodeInfo(@NonNull Long id, @NonNull NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request) throws BizException {
        log.info("full update nfcCodeInfoDTO:{}", nfcCodeInfoDTO);
        NfcCodeInfo nfcCodeInfo = BeanUtil.copyProperties(nfcCodeInfoDTO, new NfcCodeInfo());
        nfcCodeInfo.setId(id);
        int cnt = nfcCodeInfoDAO.update((NfcCodeInfo) this.packModifyBaseProps(nfcCodeInfo, request));
        if (cnt != 1) {
            log.error("update error, data:{}", nfcCodeInfoDTO);
            throw new BizException("update nfcCodeInfo Error!");
        }
    }

    @Override
    public void updateNfcCodeInfoSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        nfcCodeInfoDAO.updatex(params);
    }

    @Override
    public void logicDeleteNfcCodeInfo(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = nfcCodeInfoDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteNfcCodeInfo(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = nfcCodeInfoDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public NfcCodeInfoDTO findNfcCodeInfoById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        NfcCodeInfoDTO nfcCodeInfoDTO = nfcCodeInfoDAO.selectOneDTO(params);
        return nfcCodeInfoDTO;
    }

    @Override
    public NfcCodeInfoDTO findOneNfcCodeInfo(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        NfcCodeInfo nfcCodeInfo = nfcCodeInfoDAO.selectOne(params);
        NfcCodeInfoDTO nfcCodeInfoDTO = new NfcCodeInfoDTO();
        if (null != nfcCodeInfo) {
            BeanUtils.copyProperties(nfcCodeInfo, nfcCodeInfoDTO);
        }
        return nfcCodeInfoDTO;
    }

    @Override
    public List<NfcCodeInfoDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<NfcCodeInfoDTO> resultList = nfcCodeInfoDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return nfcCodeInfoDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return nfcCodeInfoDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = nfcCodeInfoDAO.groupCount(conditions);
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
        return nfcCodeInfoDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = nfcCodeInfoDAO.groupSum(conditions);
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
    public List<NfcCodeInfoDTO> findList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<NfcCodeInfoDTO> resultList = nfcCodeInfoDAO.findList(params);
        return resultList;
    }

    @Override
    public NfcCodeInfoDTO checkMerchant(Long id ,NfcCodeInfoDTO nfcCodeInfoDTO ,HttpServletRequest request) throws BizException {
        NfcCodeInfoDTO orgMsg = null;
        //app绑定查询是否已存在
        Map<String, Object> map = new HashMap<>(1);
        map.put("code" , nfcCodeInfoDTO.getCode());
        orgMsg = findOneNfcCodeInfo(map);
        if (orgMsg.getId() == null) {
            throw new BizException(I18nUtils.get("nfc.code.not.exist", getLang(request)));
        }
        if(orgMsg.getId() != null && orgMsg.getState().equals(StaticDataEnum.QRCODE_STATE_1.getCode())){
            throw new BizException(I18nUtils.get("code.inexistence", getLang(request)));
        }
//        if (id != null) {
//            //web绑定查询code是否对应
//            orgMsg = findNfcCodeInfoById(id);
//            if(orgMsg==null || !orgMsg.getCode().equals(nfcCodeInfoDTO.getCode())){
//                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
//            }
//        } else {
//            //app绑定查询是否已经绑过
//            Map<String, Object> map = new HashMap<>();
//            map.put("code" , nfcCodeInfoDTO.getCode());
//            orgMsg = findOneNfcCodeInfo(map);
//            if(orgMsg==null ||orgMsg.getId() == null ){
//                throw new BizException(I18nUtils.get("code.inexistence", getLang(request)));
//            }
//            if(orgMsg.getState()==StaticDataEnum.QRCODE_STATE_1.getCode()){
//                throw new BizException(I18nUtils.get("code.binded", getLang(request)));
//            }
//
//        }

        //商户是否可绑定
        MerchantDTO merchantDTO = merchantService.findMerchantById(nfcCodeInfoDTO.getMerchantId());
        if(merchantDTO == null  || merchantDTO.getId() == null){
            throw new BizException(I18nUtils.get("merchant.unavailable", getLang(request)));
        }
        //获取用户信息
        UserDTO userDTO = userService.findUserById(merchantDTO.getUserId());
        if(merchantDTO == null  || merchantDTO.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.unavailable", getLang(request)));
        }
        nfcCodeInfoDTO.setId(orgMsg.getId());
        nfcCodeInfoDTO.setUserId(userDTO.getId());
        nfcCodeInfoDTO.setMerchantName(merchantDTO.getCorporateName());
        nfcCodeInfoDTO.setCorrelationTime(System.currentTimeMillis());
        nfcCodeInfoDTO.setState(StaticDataEnum.QRCODE_STATE_1.getCode());
        return nfcCodeInfoDTO;
    }

    @Override
    public void importNfc(MultipartFile multipartFile, HttpServletRequest request) throws Exception {
        ImportParams params = new ImportParams();
        StringBuilder sb=new StringBuilder();
        // 验证数据
        params.setNeedVerfiy(true);
        List<Nfc> nfcList = null;
        ExcelImportResult<Nfc> result = ExcelImportUtil.importExcelMore(multipartFile.getInputStream(), Nfc.class, params);
        // 校验是否合格
        if(result.isVerfiyFail()){
            // 不合格的数据
            List<Nfc> errorList = result.getList();
            // 拼凑错误信息,自定义
            for (int i = 0; i < errorList.size(); i++) {
                EasyPoiUtils.getWrongInfo(sb, errorList, i, errorList.get(i), "name", "清算信息不合法");
            }
        }
        nfcList = result.getList();

        for (Nfc nfc : nfcList) {
            Map<String, Object> existParams = new HashMap<>(16);
            existParams.put("code", nfc.getCode());
            NfcCodeInfoDTO nfcCodeInfoDTO = findOneNfcCodeInfo(existParams);
            if (nfcCodeInfoDTO.getId() != null) {
                throw new BizException(I18nUtils.get("nfc.exist", getLang(request), new String[]{nfc.getCode()}));
            }
        }

        //创建nfc数据
        nfcList.stream().forEach(nfc -> {
            NfcCodeInfo nfcCodeInfo = new NfcCodeInfo();
            nfcCodeInfo.setCode(nfc.getCode());
            nfcCodeInfo.setQrCode(nfc.getQrCode());
            nfcCodeInfo = (NfcCodeInfo) this.packAddBaseProps(nfcCodeInfo, request);
            try {
                nfcCodeInfoDAO.insert(nfcCodeInfo);
            } catch (Exception e) {
                log.info("insert nfc failed, nfc code:{}", nfcCodeInfo.getCode());
            }
        });
    }

    @Override
    public JSONObject findUserInfoByNFCCode(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put("code",requestInfo.getString("code"));
        NfcCodeInfoDTO nfcCodeInfoDTO = findOneNfcCodeInfo(params);
         // 判断NFC是否存在
        Long merchantId = nfcCodeInfoDTO.getMerchantId();
        if (merchantId==null){
            throw new BizException(I18nUtils.get("nfc.code.not.exist", getLang(request)));
        }
//        if (nfcCodeInfoDTO.getId() == null) {
//            throw new BizException(I18nUtils.get("code.find.user.not.exist", getLang(request)));
//        }
        // 若为用户，则去账户系统查出用户名
        // 返回关于商户的费率
        MerchantDTO oneMerchant = merchantService.findMerchantById(merchantId);
        if (oneMerchant.getId() == null) {
            throw new BizException(I18nUtils.get("merchant not exist", getLang(request)));
        }
        if (oneMerchant.getIsAvailable().intValue() == StaticDataEnum.STATUS_0.getCode()) {
            throw new BizException(I18nUtils.get("merchant.forbidden", getLang(request)));
        }
        params.clear();
        params.put("merchantId",oneMerchant.getId());
        params.put("code","gatewayType");
        List<RouteDTO> routeDTOList = routeService.findList(params, null, null);
        nfcCodeInfoDTO.setRouteDTOS(routeDTOList);
        // 支付折扣计算 支付让利用户 + 营销折扣 * （1 - 支付折扣率平台占比）+ 额外折扣 * （1 - 额外折扣率平台占比）

        Long today = System.currentTimeMillis();
        Long extraDiscountPeriod = oneMerchant.getExtraDiscountPeriod();
        BigDecimal extraDiscount = null;
        if (today.longValue() < extraDiscountPeriod.longValue()) {
            extraDiscount = oneMerchant.getExtraDiscount();
        } else {
            extraDiscount = new BigDecimal("0.00");
        }
//        BigDecimal payDiscountRate = oneMerchant.getBaseRate().add(extraDiscount).add(oneMerchant.getMarketingDiscount());
//        BigDecimal payDiscountRate = oneMerchant.getPaySellDiscount()
//                .add(oneMerchant.getMarketingDiscount()
//                        .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getMerchantDiscountRatePlatformProportion()))).setScale(4, RoundingMode.HALF_UP)
//                .add(extraDiscount
//                        .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountPayPlatform()))).setScale(4, RoundingMode.HALF_UP);
        nfcCodeInfoDTO.setMerchantName(oneMerchant.getPracticalName());

        nfcCodeInfoDTO.setBaseRate(oneMerchant.getBaseRate());
        nfcCodeInfoDTO.setExtraDiscount(extraDiscount);
        nfcCodeInfoDTO.setMarketingDiscount(oneMerchant.getMarketingDiscount());


        //获取商户整体出售余额
        BigDecimal wholeSaleBalance = userService.getBalance(oneMerchant.getUserId(),1);
        nfcCodeInfoDTO.setWholeSaleBalance(wholeSaleBalance);
        //商户整体出售用户折扣
        nfcCodeInfoDTO.setWholeSaleUserDiscount(oneMerchant.getWholeSaleUserDiscount());
        //用户钱包余额 ,红包转卡券，余额查询取消
//        BigDecimal userBalance = userService.getBalance( requestInfo.getLong("userId"),null);
        nfcCodeInfoDTO.setBalance(BigDecimal.ZERO);
        //查询该用户分期付状态
        params.clear();
        params.put("id",requestInfo.getLong("userId"));
        UserDTO userDTO = userService.findOneUser(params);
        nfcCodeInfoDTO.setInstallmentState(userDTO.getInstallmentState());
        nfcCodeInfoDTO.setStripeState(userDTO.getStripeState() == null ? 0 : userDTO.getStripeState());
        return JSONResultHandle.resultHandle(nfcCodeInfoDTO, NfcCodeInfoDTO.class);
    }

    @Override
    public NfcCodeInfoDTO getRemoveBind(Long merchantId,String code,Long id, HttpServletRequest request) throws BizException {
        Map<String,Object> params = new HashMap<>(3);
        params.put("id",id);
        params.put("merchantId",merchantId);
        params.put("code",code);
        NfcCodeInfoDTO orgMsg = findOneNfcCodeInfo(params);
        if(orgMsg==null || orgMsg.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        NfcCodeInfoDTO nfcCodeInfoDTO = new NfcCodeInfoDTO();
        nfcCodeInfoDTO.setId(orgMsg.getId());
        nfcCodeInfoDTO.setCode(orgMsg.getCode());
        nfcCodeInfoDTO.setState(StaticDataEnum.QRCODE_STATE_0.getCode());
        return nfcCodeInfoDTO;
    }

    @Override
    public int removeBindNfcCodeInfo(Long id, NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request)  throws BizException{
        log.info("removeBindNfcCodeInfo nfcCodeInfoDTO:{}", nfcCodeInfoDTO);
        NfcCodeInfo nfcCodeInfo = BeanUtil.copyProperties(nfcCodeInfoDTO, new NfcCodeInfo());
        nfcCodeInfo.setId(id);
        int cnt = nfcCodeInfoDAO.removeBindNfcCodeInfo((NfcCodeInfo) this.packModifyBaseProps(nfcCodeInfo, request));
        if (cnt != 1) {
            log.error("update error, data:{}", nfcCodeInfoDTO);
            throw new BizException("update nfcCodeInfo Error!");
        }
        return cnt;
    }

    @Override
    public Map<String, Object> findMerchantByNfcCode(String nfcCode, HttpServletRequest request) throws BizException {
        log.info("查询nfc绑定信息 :{}", nfcCode);
        if (nfcCode==null || nfcCode.equals("")){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        JSONObject param=new JSONObject(4);
        Map<String,Object> result=new HashMap<>();
        param.put("code",nfcCode);
        //查询nfc
        NfcCodeInfo nfcCodeInfo = nfcCodeInfoDAO.selectOne(param);
        if (nfcCodeInfo==null){
            throw new BizException(I18nUtils.get("nfc.code.not.exist",getLang(request)));
        }
        Long merchantId = nfcCodeInfo.getMerchantId();
        Integer state = nfcCodeInfo.getState();
        //未关联
        if (merchantId==null||state==StaticDataEnum.NFCCODE_STATE_0.getCode()){
            result.put("nfcCode",nfcCodeInfo.getCode());
            result.put("result",false);
            return result;
        }
        //查询商户信息
        result.clear();
        MerchantDTO merchantById = merchantService.findMerchantById(merchantId);
        result.put("nfcCode",nfcCodeInfo.getCode());
        result.put("result",true);
        if (merchantById==null){
            result.put("id","");
            result.put("userId","");
            result.put("practicalName","");
        }else {
            result.put("id",merchantById.getId());
            result.put("userId",merchantById.getUserId());
            result.put("practicalName",merchantById.getPracticalName());
        }
        return result;
    }

    @Override
    public Boolean unBindNfcCode(String code, HttpServletRequest request) throws BizException {
        if (StringUtils.isBlank(code)){
            throw new BizException(I18nUtils.get("parameters.null",getLang(request)));
        }
        // 获取nfc信息
        JSONObject param=new JSONObject(1);
        param.put("code",code);
        CodeUpdateLogDTO codeUpdateLogDTO=new CodeUpdateLogDTO();
        NfcCodeInfo nfcCodeInfo = nfcCodeInfoDAO.selectOne(param);
        codeUpdateLogDTO.setMerchantId(nfcCodeInfo.getMerchantId());
        if (nfcCodeInfo==null||nfcCodeInfo.getId()==null){
            throw new BizException(I18nUtils.get("nfc.code.not.exist",getLang(request)));
        }
        // 判断绑定状态
        if (nfcCodeInfo.getState()!=null&&nfcCodeInfo.getState().equals(StaticDataEnum.NFCCODE_STATE_0.getCode())){
            throw new BizException(I18nUtils.get("nfcCode.not.bind",getLang(request)));
        }
        NfcCodeInfo nfcCodeInfoIn = BeanUtil.copyProperties(nfcCodeInfo, new NfcCodeInfo());
        nfcCodeInfoIn.setState(StaticDataEnum.NFCCODE_STATE_0.getCode());
        nfcCodeInfoIn.setUserId(null);
        nfcCodeInfoIn.setMerchantId(null);
        nfcCodeInfoIn.setCorrelationTime(null);
        NfcCodeInfo baseEntity = (NfcCodeInfo)this.packModifyBaseProps(nfcCodeInfoIn, request);
        int update = nfcCodeInfoDAO.removeBindNfcCodeInfo(baseEntity);
        if (update!=1){
            throw new BizException(I18nUtils.get("nfc.remove.error",getLang(request)));
        }
        // 添加操作记录
        try{
            codeUpdateLogDTO.setCode(code);
            codeUpdateLogDTO.setState(StaticDataEnum.BIND_TYPE_UNBIND.getCode());
            codeUpdateLogDTO.setType(StaticDataEnum.CODE_TYPE_NFC.getCode());
            codeUpdateLogService.saveCodeUpdateInfo(codeUpdateLogDTO,request);
        }catch (Exception e){
            log.error("记录操作记录异常,data:{}",e);
        }
        return true;
    }


}
