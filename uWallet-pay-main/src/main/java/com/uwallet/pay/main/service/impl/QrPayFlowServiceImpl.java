package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.SignErrorCode;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.QrPayFlowDAO;
import com.uwallet.pay.main.dao.RefundOrderDAO;
import com.uwallet.pay.main.exception.SignException;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.*;
import com.uwallet.pay.main.util.POIUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 扫码支付交易流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 扫码支付交易流水表
 * @author: zhoutt
 * @date: Created in 2019-12-13 18:00:26
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class QrPayFlowServiceImpl extends BaseServiceImpl implements QrPayFlowService{

    @Autowired
    private QrPayFlowDAO qrPayFlowDAO;

    @Autowired
    private ClearDetailService clearDetailService;

    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    private RefundFlowService refundFlowService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    @Lazy
    private MerchantService merchantService;

    @Autowired
    private AccessMerchantService accessMerchantService;

    @Autowired
    private AccessPlatformService accessPlatformService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ClearBatchService clearBatchService;
    @Autowired
    private ClearFlowDetailService clearFlowDetailService;
    @Autowired
    private WholeSalesFlowService wholeSalesFlowService;
    @Autowired
    private UserMonthlyDataService userMonthlyDataService;

    @Autowired
    private MailTemplateService mailTemplateService;

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private ApiMerchantService apiMerchantService;
    @Autowired
    private MailLogService mailLogService;
    @Autowired
    private RefundService refundService;

    private SimpleDateFormat format = new SimpleDateFormat(Constant.DATE_FORMAT,Locale.US);


    @Value("${uWallet.sysEmail}")
    private String sysEmail;

    @Value("${uWallet.sysEmailPwd}")
    private String sysEmailPwd;

    @Override
    public Long saveQrPayFlow(@NonNull QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        QrPayFlow qrPayFlow = BeanUtil.copyProperties(qrPayFlowDTO, new QrPayFlow());
        qrPayFlow = (QrPayFlow) this.packAddBaseProps(qrPayFlow, request);
        log.info("save QrPayFlow:{}", qrPayFlow);
        if (qrPayFlowDAO.insert(qrPayFlow) != 1) {
            log.error("insert error, data:{}", qrPayFlow);
            throw new BizException("Insert qrPayFlow Error!");
        }
        return qrPayFlow.getId();
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQrPayFlowList(@NonNull List<QrPayFlow> qrPayFlowList, HttpServletRequest request) throws BizException {
        if (qrPayFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = qrPayFlowDAO.insertList(qrPayFlowList);
        if (rows != qrPayFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, qrPayFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateQrPayFlow(@NonNull Long id, @NonNull QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update qrPayFlowDTO:{}", qrPayFlowDTO);
        QrPayFlow qrPayFlow = BeanUtil.copyProperties(qrPayFlowDTO, new QrPayFlow());
        qrPayFlow.setId(id);

        if (request == null) {
            qrPayFlow.setModifiedDate(System.currentTimeMillis());
        } else {
            qrPayFlow = (QrPayFlow) this.packModifyBaseProps(qrPayFlow, request);
        }
        int cnt = qrPayFlowDAO.update(qrPayFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", qrPayFlowDTO);
            throw new BizException("update qrPayFlow Error!");
        }
    }

    @Override
    public void updateQrPayFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        qrPayFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteQrPayFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = qrPayFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteQrPayFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = qrPayFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public QrPayFlowDTO findQrPayFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        QrPayFlowDTO qrPayFlowDTO = qrPayFlowDAO.selectOneDTO(params);
        return qrPayFlowDTO;
    }

    @Override
    public QrPayFlowDTO findOneQrPayFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        QrPayFlow qrPayFlow = qrPayFlowDAO.selectOne(params);
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        if (null != qrPayFlow) {
            BeanUtils.copyProperties(qrPayFlow, qrPayFlowDTO);
        }
        return qrPayFlowDTO;
    }

    @Override
    public List<QrPayFlowDTO> qrPayFlowListDetails(Map<String ,Object> params) {
        params.put("userId", params.get("id"));
        List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowDAO.qrPayFlowListDetails(params);
        return qrPayFlowDTOS;
    }

    @Override
    public JSONObject appQrPayFlowListRecAmountTotal(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        formatMonth(params);
        JSONObject param = new JSONObject();
        BigDecimal bigDecimal = new BigDecimal(0);
        List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowDAO.appQrPayFlowListRecAmountTotal(params);
        param.put("qrPayFlowDTOS",qrPayFlowDTOS);
        if (qrPayFlowDTOS != null && qrPayFlowDTOS.size()>0) {
            QrPayFlowDTO recAmountTotal = qrPayFlowDAO.recAmountTotal(params);
            if (recAmountTotal == null) {
                param.put("recAmountTotal", new BigDecimal("0.00"));
            } else {
                param.put("recAmountTotal",recAmountTotal.getRecAmountTotal());
            }
        } else {
            param.put("recAmountTotal",bigDecimal);
        }
        return param;
    }

    @Override
    public JSONObject appQrPayFlowList(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
//        formatMonth(params);
        JSONObject param = new JSONObject();
        List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowDAO.appQrPayFlowList(params);
        param.put("qrPayFlowDTOS",qrPayFlowDTOS);
        return param;
    }

    @Override
    public void updateQrPayFlowForConcurrency(@NonNull Long id, @NonNull QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update qrPayFlowDTO:{}", qrPayFlowDTO);
        QrPayFlow qrPayFlow = BeanUtil.copyProperties(qrPayFlowDTO, new QrPayFlow());
        qrPayFlow.setId(id);

        if (request == null) {
            qrPayFlow.setModifiedDate(System.currentTimeMillis());
        } else {
            qrPayFlow = (QrPayFlow) this.packModifyBaseProps(qrPayFlow, request);
        }
        int cnt = qrPayFlowDAO.updateForConcurrency(qrPayFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", qrPayFlowDTO);
            throw new BizException("update qrPayFlow Error!");
        }
    }

    @Override
    public int countMerchantClearList(Map<String, Object> params) {
        Integer i = qrPayFlowDAO.countMerchantClearList(params);
        return i==null?0:i;
    }

    @Override
    public List<QrPayFlowDTO> merchantClearList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        if(scs!= null){
            params = getUnionParams(params, scs, pc);
        }
        List<QrPayFlowDTO> resultList = qrPayFlowDAO.merchantClearList(params);
        return resultList;
    }


    @Override
    public QrPayFlowDTO appQrPayFlowUnRecAmountTotal(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("merchantId", id);
        params.put("isNeedClear", StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
        params.put("clearState", StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        params.put("state",StaticDataEnum.TRANS_STATE_31.getCode());
        QrPayFlowDTO recAmountTotal = qrPayFlowDAO.appQrPayFlowUnRecAmountTotal(params);
        return recAmountTotal;
    }

    @Override
    public List<QrPayFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<QrPayFlowDTO> resultList = qrPayFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<QrPayFlowDTO> qrPayFlowList(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        params.put("isNeedClear", StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
        params.put("clearState", StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        params.put("state",StaticDataEnum.TRANS_STATE_31.getCode());
        List<QrPayFlowDTO> resultList = qrPayFlowDAO.qrPayFlowList(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return qrPayFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return qrPayFlowDAO.count(params);
    }

    @Override
    public int countList(@NonNull Map<String, Object> params) {
        formatMonth(params);
        return qrPayFlowDAO.countList(params);
    }

    @Override
    public int countDetails(@NonNull Map<String, Object> params) {
        params = formatMonth(params);
        return qrPayFlowDAO.countDetails(params);
    }

    @Override
    public int countLists(@NonNull Map<String, Object> params) {
//        formatMonth(params);
        return qrPayFlowDAO.countLists(params);
    }

    /**
     * 根据查询的月份，格式化 start，end 查询条件
     * @param params
     * @return
     */
    private Map<String, Object> formatMonth(Map<String, Object> params) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (null != params.get("queryDate") && StringUtils.isNotBlank(params.get("queryDate").toString())) {
            String queryDate = params.get("queryDate").toString();
            String[] split = queryDate.split("-");

            calendar.set(Calendar.YEAR, Integer.parseInt(split[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(split[1]) - 1);

            params.put("start", calendar.getTimeInMillis());

            calendar.add(Calendar.MONTH, 1);
            params.put("end", calendar.getTimeInMillis());
        }
        return params;
    }


    @Override
    public int countQrPayFlowList(@NonNull Map<String, Object> params) {
        params.put("isNeedClear", StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
        params.put("clearState", StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        params.put("state",StaticDataEnum.TRANS_STATE_31.getCode());
        return qrPayFlowDAO.countQrPayFlowList(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = qrPayFlowDAO.groupCount(conditions);
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
        return qrPayFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = qrPayFlowDAO.groupSum(conditions);
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
    public List<PayBorrowDTO> selectPayBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PayBorrowDTO> resultList = qrPayFlowDAO.selectPayBorrow(params);
        return resultList;
    }

    @Override
    public int selectPayBorrowCount(Map<String, Object> params) {
        return qrPayFlowDAO.selectPayBorrowCount(params);
    }

    @Override
    public List<TransferBorrowDTO> selectTransferBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<TransferBorrowDTO> resultList = qrPayFlowDAO.selectTransferBorrow(params);
        return resultList;
    }

    @Override
    public int selectTransferBorrowCount(Map<String, Object> params) {
        return qrPayFlowDAO.selectTransferBorrowCount(params);
    }

    @Override
    public List<PayBorrowDTO> selectBatchBorrow(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PayBorrowDTO> resultList = qrPayFlowDAO.selectPayBorrow(params);
        return resultList;
    }

    @Override
    public int selectBatchBorrowCount(Map<String, Object> params) {
        return qrPayFlowDAO.selectPayBorrowCount(params);
    }

    @Override
    public List<QrPayFlowDTO> findAccountDoubleFlow() {
        return qrPayFlowDAO.findAccountDoubleFlow();
    }

    @Override
    public List<QrPayFlowDTO> findAccountFailFlow() {
        return qrPayFlowDAO.findAccountFailFlow();
    }

    @Override
    public List<QrPayFlowDTO> findThirdDoubtFlow() {
        return qrPayFlowDAO.findThirdDoubtFlow();
    }

    @Override
    public int updateClearBatch(Map<String, Object> map) {
        return  qrPayFlowDAO.updateClearBatch(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQrPayClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        Map<String, Object> updateMap = new HashMap<>();
        int[] stateList = {31 ,1};
        updateMap.put("isNeedClear", "1");
        updateMap.put("orgClearState", "0");
        updateMap.put("clearState", "2");
        updateMap.put("stateList", stateList);
        updateMap.put("start", params.get("start"));
        updateMap.put("end", params.get("end"));
        updateMap.put("batchId",clearBatchDTO.getId());
        updateMap.put("orderSource",StaticDataEnum.ORDER_SOURCE_0.getCode());
        if(params.containsKey("gatewayId")){
            // 老版本清算有gatewayId,需要清算退款
            updateMap.put("gatewayId",params.get("gatewayId"));
            //回退
//            refundFlowService.addQrPayClearBatchId(updateMap);
            //交易
            qrPayFlowDAO.addQrPayClearBatchId(updateMap);
        }else{
            // 新版本清算交易
            updateMap.put("merchantIdList",params.get("merchantIdList"));
            qrPayFlowDAO.addQrPayClearBatchIdNew(updateMap);
        }


    }

    @Override
    public QrPayFlowDTO clearTotal(Map<String, Object> clearMap) {
        return qrPayFlowDAO.clearTotal(clearMap);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQrPayClearBatch(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("state", "0");
        updateMap.put("batchId",clearBatchDTO.getId());
        //查询要清算的交易列表，得到应清算金额和交易信息
        List<ClearDetailDTO> clearList = clearDetailService.getClearBatchNew(updateMap);
        for(ClearDetailDTO clearDetailDTO:clearList ){
            clearDetailDTO.setModifiedBy(clearBatchDTO.getModifiedBy());
            clearDetailDTO.setCreatedBy(clearBatchDTO.getCreatedBy());
            clearDetailService.saveClearDetail(clearDetailDTO,request);
        }
    }

    @Override
    public Map<String, Object> transactionDetails(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        formatMonth(params);
        List<AppTransactionDetailsDTO> resultList = qrPayFlowDAO.transactionDetails(params);
        String code = "payType,showType";
        final String[] codeList = code.split(",");
        Map<String, List<StaticData>> result = staticDataService.findByCodeList(codeList);
        List<JSONObject> list = new ArrayList<>(resultList.size());
        resultList.forEach(item -> {
            JSONObject object = JSONResultHandle.resultHandle(item, AppTransactionDetailsDTO.class);
            list.add(object);
        });
        Map<String, Object> param = new HashMap<>(2);
        param.put("list",list);
        param.put("result",result);
        return param;
    }

    @Override
    public QrPayFlowDTO gathering(@NonNull Long userId) {
        UserDTO userDTO = userService.findUserById(userId);
        Map<String, Object> map = new HashMap<>(4);
        if(userDTO.getRole()==StaticDataEnum.MERCHANT_ROLE_TYPE_0.getCode()){
            MerchantDTO merchantDTO =merchantService.findMerchantById(userDTO.getMerchantId());
            map.put("recUserId", merchantDTO.getUserId());
        }else{
            map.put("recUserId", userId);
        }
        long nowTime = System.currentTimeMillis();
        long todayStartTime = nowTime - ((nowTime + TimeZone.getDefault().getRawOffset()) % (24 * 60 * 60 * 1000L));
        long todayEndTime = todayStartTime + (24 * 60 * 60 * 1000 - 1);
        map.put("start", todayStartTime);
        map.put("end", todayEndTime);
        map.put("state", StaticDataEnum.TRANS_STATE_31.getCode());
        List<QrPayFlowDTO> qrPayFlowDTOList = find(map, null, null);
        BigDecimal bigDecimal = new BigDecimal("0");
        for (QrPayFlowDTO qrPayFlowDTO : qrPayFlowDTOList) {
            bigDecimal = bigDecimal.add(qrPayFlowDTO.getRecAmount());
        }
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setCount(qrPayFlowDTOList.size());
        qrPayFlowDTO.setRecAmount(bigDecimal);
        return qrPayFlowDTO;
    }

    @Override
    public String createApiOrder(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        checkApiOrder(requestInfo, request);
        Long merchantId = requestInfo.getLong("merchantId");
        String orderNo = requestInfo.getString("orderNo");
        String notifyUrl = requestInfo.getString("notifyUrl");
        BigDecimal transAmount = requestInfo.getBigDecimal("transAmount");

        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setMerchantId(merchantId);
        qrPayFlowDTO.setAccessPartyOrderNo(orderNo);
        qrPayFlowDTO.setAccessPartyNotifyUrl(notifyUrl);
        qrPayFlowDTO.setTransAmount(transAmount);
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        qrPayFlowDTO.setOrderSource(StaticDataEnum.ORDER_SOURCE_1.getCode());
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_7.getCode());
        Long id = saveQrPayFlow(qrPayFlowDTO, request);
        // 生成订单token
        AccessMerchantDTO accessMerchantDTO = accessMerchantService.findAccessMerchantById(merchantId);
        return entranceEncryption(Long.valueOf(accessMerchantDTO.getPlatformId()), merchantId, transAmount, orderNo);
    }

    /**
     * 校验api订单信息
     * @param requestInfo
     * @param request
     * @throws Exception
     */
    private void checkApiOrder(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Long merchantId = requestInfo.getLong("merchantId");
        String orderNo = requestInfo.getString("orderNo");
        BigDecimal transAmount = requestInfo.getBigDecimal("transAmount");
        if (merchantId == null) {
            throw new BizException(I18nUtils.get("api.merchantId.isNull", getLang(request)));
        } else {
            AccessMerchantDTO accessMerchantDTO = accessMerchantService.findAccessMerchantById(merchantId);
            if (accessMerchantDTO.getState().intValue() == StaticDataEnum.STATUS_0.getCode()) {
                throw new BizException(I18nUtils.get("merchant.forbidden", getLang(request)));
            }
        }
        if (orderNo.isEmpty()) {
            throw new BizException(I18nUtils.get("api.orderNo.isNull", getLang(request)));
        } else {
            Map<String, Object> params = new HashMap<>(16);
            params.put("accessPartyOrderNo", orderNo);
            params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
            List<QrPayFlowDTO> list = find(params, null, null);
            if (list != null && !list.isEmpty()) {
                throw new BizException(I18nUtils.get("api.orderNo.repeat", getLang(request)));
            }
        }
        if (!RegexUtils.isTransAmt(transAmount.toString())) {
            throw new BizException(I18nUtils.get("api.transAmount.error", getLang(request)));
        }
    }

    /**
     * 生成api订单token
     * @param platFormId
     * @param merchantId
     * @param transactionAmount
     * @param orderNo
     * @return
     * @throws BizException
     */
    private String entranceEncryption(Long platFormId, Long merchantId, BigDecimal transactionAmount, String orderNo) throws BizException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accessSideId", platFormId);
        jsonObject.put("merchantId", merchantId);
        jsonObject.put("transactionAmount", transactionAmount);
        jsonObject.put("tripartiteTransactionNo", orderNo);

        String string = jsonObject.toString();

        HuffmanCode huffmanCode = new HuffmanCode();
        byte[] bytes = huffmanCode.huffmanBuild(string);

        String code = Arrays.toString(bytes);
        String encrypt;
        try {
            encrypt = this.encrypt(code + System.currentTimeMillis());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException(e.getMessage());
        }

        jsonObject.clear();
        jsonObject.put("bytes", code);
        jsonObject.put("length", huffmanCode.getLength());
        jsonObject.put("code", huffmanCode.getHuffmanCodeMap());

        // 暂定十分钟有效期
        redisUtils.hmset(encrypt + "~" + orderNo + "_api_order", jsonObject);

        return encrypt + "~" + orderNo;
    }

    private String encrypt(String dataStr) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(dataStr.getBytes("UTF8"));
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 校验订单金额
     * @param requestInfo
     * @param qrPayFlowDTO
     * @param request
     * @throws Exception
     */
    private void checkAmount(JSONObject requestInfo, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws Exception {
        BigDecimal serviceFee = requestInfo.getBigDecimal("serviceFee");
        BigDecimal discount = requestInfo.getBigDecimal("discount");
        BigDecimal paymentAmount = requestInfo.getBigDecimal("payAmount");

        BigDecimal serviceFeeRate = requestInfo.getBigDecimal("serviceFeeRate").divide(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountRate = requestInfo.getBigDecimal("discountRate").divide(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

        BigDecimal checkServiceFee = qrPayFlowDTO.getTransAmount().multiply(serviceFeeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal checkDiscount = qrPayFlowDTO.getTransAmount().multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal checkPaymentAmount = qrPayFlowDTO.getTransAmount().subtract(checkDiscount).setScale(2, RoundingMode.HALF_UP);
        if (serviceFee.compareTo(checkServiceFee) != 0) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        if (discount.compareTo(checkDiscount) != 0) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        if (paymentAmount.compareTo(checkPaymentAmount) != 0) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
    }

    @Override
    public JSONObject paymentApiOrder(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        String orderToken = request.getHeader("orderToken");
        if (StringUtils.isEmpty(orderToken)) {
            throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
        }
        JSONObject orderInfo = orderTokenDecode(orderToken, request);

        Map<String, Object> params = new HashMap<>(16);
        params.put("accessPartyOrderNo", orderInfo.getString("tripartiteTransactionNo"));

        QrPayFlowDTO qrPayFlowDTO = findOneQrPayFlow(params);
        if (qrPayFlowDTO.getId() == null) {
            throw new BizException(I18nUtils.get("api.order.not.exist", getLang(request)));
        }

        // 校验订单金额
        checkAmount(requestInfo, qrPayFlowDTO, request);

        // 查询商户
        AccessMerchantDTO accessMerchantDTO = accessMerchantService.findAccessMerchantById(qrPayFlowDTO.getMerchantId());

        BigDecimal serviceFee = requestInfo.getBigDecimal("serviceFee");
        BigDecimal discount = requestInfo.getBigDecimal("discount");
        BigDecimal paymentAmount = requestInfo.getBigDecimal("payAmount");

        // 更新订单
        qrPayFlowDTO.setMerchantId(accessMerchantDTO.getId());
        qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_0.getCode());
        qrPayFlowDTO.setPayAmount(paymentAmount);
        qrPayFlowDTO.setPayUserId(getApiUserId(request));
        qrPayFlowDTO.setAccessPartyServerFee(serviceFee);
        qrPayFlowDTO.setAccessPartyDiscount(discount);
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        qrPayFlowDTO.setSaleType(StaticDataEnum.SALE_TYPE_0.getCode());
        updateQrPayFlow(qrPayFlowDTO.getId(), qrPayFlowDTO, request);


        // 同步分期付获取交易结果
        JSONObject creditInfo = new JSONObject();
        creditInfo.put("userId", qrPayFlowDTO.getPayUserId());
        creditInfo.put("merchantId", accessMerchantDTO.getId());
        creditInfo.put("borrowAmount", qrPayFlowDTO.getPayAmount());
        creditInfo.put("serviceFee", serviceFee);
//        creditInfo.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
        creditInfo.put("orderAmount", qrPayFlowDTO.getTransAmount());
        creditInfo.put("redEnvelopeAmount", new BigDecimal("0"));
        JSONObject requestData = new JSONObject();
        requestData.put("accessRate", requestInfo.getBigDecimal("discountRate").divide(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        requestData.put("createBorrowMessageDTO", creditInfo);
        requestData.put("accessSideId", accessMerchantDTO.getPlatformId());
        requestData.put("isShow", StaticDataEnum.STATUS_0.getCode());
        requestData.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
        JSONObject result = null;
        String returnState = "";
        try {
            result = serverService.apiCreditOrder(requestData, request);
        } catch (Exception e) {
            // 请求可疑全部置为失败，并返回通知
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
            creditInfo.clear();
            creditInfo.put("tripartiteTransactionNo", qrPayFlowDTO.getAccessPartyOrderNo());
            creditInfo.put("accessSideId", accessMerchantDTO.getPlatformId());
            serverService.orderStateRollback(creditInfo, request);

            returnState = StaticDataEnum.API_ORDER_STATE_0.getMessage();
            JSONObject notifyInfo = apiOrderResultPackage(qrPayFlowDTO, returnState);
            return notifyInfo;
        }
        // 获取结果跟新订单信息
        Integer orderState = result.getInteger("transactionResult");
        // 分期付交易结果统一处理
        JSONObject notifyInfo = dealCreditOrderResult(result,qrPayFlowDTO,orderState,request);
//        if (orderState.intValue() == StaticDataEnum.API_ORDER_STATE_1.getCode()) {
//            qrPayFlowDTO.setCreditOrderNo(result.getLong("borrowId").toString());
//            qrPayFlowDTO.setClearAmount(result.getBigDecimal("clearAmount"));
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
//            qrPayFlowDTO.setRecAmount(result.getBigDecimal("clearAmount"));
//            if(StringUtils.isNotEmpty(result.getString("clearState"))){
//                Integer clearState = result.getInteger("clearState");
//                if(clearState ==  StaticDataEnum.CLEAR_STATE_TYPE_1.getCode()){
//                    qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
//                    qrPayFlowDTO.setClearTime(System.currentTimeMillis());
//                }
//
//            }
//
//            returnState = StaticDataEnum.API_ORDER_STATE_1.getMessage();
//        } else if (orderState.intValue() == StaticDataEnum.API_ORDER_STATE_0.getCode()) {
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//            returnState = StaticDataEnum.API_ORDER_STATE_0.getMessage();
//        } else {
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
//            returnState = StaticDataEnum.API_ORDER_STATE_3.getMessage();
//        }
//        updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
        // 页面返回结果
//        JSONObject notifyInfo = apiOrderResultPackage(qrPayFlowDTO, returnState);
        notifyInfo.put("discountRate", requestInfo.getBigDecimal("discountRate"));
        notifyInfo.put("creditInfo", result);
        return notifyInfo;
    }

    /**
     * 订单结果返回
     * @param qrPayFlowDTO
     * @param returnState
     * @return
     * @throws Exception
     */
    private JSONObject apiOrderResultPackage(QrPayFlowDTO qrPayFlowDTO, String returnState) throws Exception {
        JSONObject notifyInfo = new JSONObject();
        notifyInfo.put("outOrderNo", qrPayFlowDTO.getAccessPartyOrderNo());
        notifyInfo.put("orderNo", qrPayFlowDTO.getCreditOrderNo());
        notifyInfo.put("amount", qrPayFlowDTO.getTransAmount());
        notifyInfo.put("payAmount", qrPayFlowDTO.getPayAmount());
        notifyInfo.put("totalAmount", qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getAccessPartyServerFee()));
        notifyInfo.put("serviceFee", qrPayFlowDTO.getAccessPartyServerFee());
        notifyInfo.put("discount", qrPayFlowDTO.getAccessPartyDiscount());
        notifyInfo.put("orderState", returnState);
        notifyInfo.put("createdDate", qrPayFlowDTO.getCreatedDate());
        notifyInfo.put("transactionDate", qrPayFlowDTO.getModifiedDate());
        // 通知结果
        if (!StringUtils.isEmpty(qrPayFlowDTO.getAccessPartyNotifyUrl())) {
            try {
                HttpClientUtils.post(qrPayFlowDTO.getAccessPartyNotifyUrl(), notifyInfo.toJSONString());
            } catch (Exception e) {
                log.info("api notify failed, data:{}, error message:{}, e:{}", notifyInfo, e.getMessage(), e);
            }
        }
        return notifyInfo;
    }

    @Override
    public JSONObject orderTokenDecode(String token, HttpServletRequest request) throws Exception {

        Map<Object, Object> map = redisUtils.hmget(token + "_api_order");

        if (null == map || map.isEmpty()) {
            throw new SignException(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
        }

        String decode = this.getHuffmanCode(map.get("bytes").toString(), (Map<String, Object>) map.get("code"), Long.parseLong(map.get("length").toString()));

        return JSONObject.parseObject(decode);
    }

    private String getHuffmanCode(String string, Map<String, Object> code, long length) {
        JSONArray jsonArray = JSONArray.parseArray(string);
        byte[] bytes = new byte[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            bytes[i] = jsonArray.getByte(i);
        }
        Map<Byte, String> huffmanCodeMap = new HashMap<>(JSONResultHandle.getContainerSize(code.size()));
        Set<Map.Entry<String, Object>> entries = code.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            huffmanCodeMap.put(Byte.parseByte(entry.getKey()), entry.getValue().toString());
        }

        byte[] decode = HuffmanCode.huffmanDecode(bytes, length, huffmanCodeMap);

        return new String(decode);
    }

    @Override
    public JSONObject discountRateSearch(HttpServletRequest request) throws Exception {
        String orderToken = request.getHeader("orderToken");
        if (StringUtils.isEmpty(orderToken)) {
            throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
        }
        JSONObject orderInfo = orderTokenDecode(orderToken, request);

        Map<String, Object> params = new HashMap<>(16);
        params.put("accessPartyOrderNo", orderInfo.getString("tripartiteTransactionNo"));

        QrPayFlowDTO qrPayFlowDTO = findOneQrPayFlow(params);
        if (qrPayFlowDTO.getId() == null) {
            throw new BizException(I18nUtils.get("api.order.not.exist", getLang(request)));
        }

        // 计算服务费、折扣费
        AccessMerchantDTO accessMerchantDTO = accessMerchantService.findAccessMerchantById(qrPayFlowDTO.getMerchantId());
        AccessPlatformDTO accessPlatformDTO = accessPlatformService.findAccessPlatformById(Long.valueOf(accessMerchantDTO.getPlatformId()));

        BigDecimal serviceFee = qrPayFlowDTO.getTransAmount().multiply(accessPlatformDTO.getServerFeeRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = qrPayFlowDTO.getTransAmount().multiply(accessPlatformDTO.getDiscountRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal paymentAmount = qrPayFlowDTO.getTransAmount().subtract(discount).setScale(2, RoundingMode.HALF_UP);

        // 获取分期付产品
        JSONObject creditData = new JSONObject();
        creditData.put("userId", getApiUserId(request));
        JSONObject userCreditInfo = serverService.userInfoCredit(creditData, request);

        JSONObject result = new JSONObject();
        result.put("outOrderNo", qrPayFlowDTO.getAccessPartyOrderNo());
        result.put("orderNo", qrPayFlowDTO.getCreditOrderNo());
        result.put("amount", qrPayFlowDTO.getTransAmount());
        result.put("payAmount", paymentAmount);
        result.put("serviceFeeRate", accessPlatformDTO.getServerFeeRate().multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        result.put("discountRate", accessPlatformDTO.getDiscountRate().multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        result.put("serviceFee", serviceFee);
        result.put("discount", discount);
        result.put("totalAmount", paymentAmount.add(serviceFee));
        result.put("products", userCreditInfo.getJSONArray("products"));
        result.put("bankcardNumber", userCreditInfo.getString("bankcardNumber"));
        result.put("accountId", userCreditInfo.getLong("accountId"));
        result.put("bankcardBank", userCreditInfo.getString("bankcardBank"));
        result.put("maxPeriods", userCreditInfo.getString("maxPeriods"));
        result.put("merchantName", accessMerchantDTO.getName());
        return result;
    }

    @Override
    public JSONObject apiOrderRepayment(Long productId, HttpServletRequest request) throws Exception {
        String orderToken = request.getHeader("orderToken");
        if (StringUtils.isEmpty(orderToken)) {
            throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
        }
        JSONObject orderInfo = orderTokenDecode(orderToken, request);

        Map<String, Object> params = new HashMap<>(16);
        params.put("accessPartyOrderNo", orderInfo.getString("tripartiteTransactionNo"));

        QrPayFlowDTO qrPayFlowDTO = findOneQrPayFlow(params);
        if (qrPayFlowDTO.getId() == null) {
            throw new BizException(I18nUtils.get("api.order.not.exist", getLang(request)));
        }

        // 计算服务费、折扣费
        AccessMerchantDTO accessMerchantDTO = accessMerchantService.findAccessMerchantById(qrPayFlowDTO.getMerchantId());
        AccessPlatformDTO accessPlatformDTO = accessPlatformService.findAccessPlatformById(Long.valueOf(accessMerchantDTO.getPlatformId()));

        BigDecimal serviceFee = qrPayFlowDTO.getTransAmount().multiply(accessPlatformDTO.getServerFeeRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = qrPayFlowDTO.getTransAmount().multiply(accessPlatformDTO.getDiscountRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal paymentAmount = qrPayFlowDTO.getTransAmount().subtract(discount);

        // 同步分期付获取交易结果
        JSONObject creditInfo = new JSONObject();
        creditInfo.put("userId", getApiUserId(request));
        creditInfo.put("merchantId", accessMerchantDTO.getId());
        creditInfo.put("borrowAmount", paymentAmount);
        creditInfo.put("serviceFee", serviceFee);
        creditInfo.put("productId", productId);
        creditInfo.put("tripartiteTransactionNo", qrPayFlowDTO.getAccessPartyOrderNo());
        creditInfo.put("orderAmount", qrPayFlowDTO.getTransAmount());
        creditInfo.put("redEnvelopeAmount", new BigDecimal("0"));
        JSONObject requestData = new JSONObject();
        requestData.put("accessRate", accessPlatformDTO.getDiscountRate());
        requestData.put("createBorrowMessageDTO", creditInfo);
        requestData.put("accessSideId", accessPlatformDTO.getId() );
        requestData.put("isShow", StaticDataEnum.STATUS_1.getCode());
        requestData.put("tripartiteTransactionNo", qrPayFlowDTO.getAccessPartyOrderNo());
        JSONObject result = null;
        try {
            result = serverService.apiCreditOrder(requestData, request);
        } catch (Exception e) {
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
        return result;
    }

    @Override
    public void paymentApiOrderDoubleHandle() throws Exception {
        Map<String, Object> params = new HashMap<>(16);
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        params.put("state", StaticDataEnum.TRANS_STATE_3.getCode());
        List<QrPayFlowDTO> qrPayFlowDTOList = find(params, null, null);
        for (QrPayFlowDTO qrPayFlowDTO : qrPayFlowDTOList) {
            Long accessSideId = null;
            if(qrPayFlowDTO.getOrderSource() == StaticDataEnum.ORDER_SOURCE_1.getCode()){
                AccessMerchantDTO accessMerchantDTO = accessMerchantService.findAccessMerchantById(qrPayFlowDTO.getMerchantId());
                AccessPlatformDTO accessPlatformDTO = accessPlatformService.findAccessPlatformById(Long.valueOf(accessMerchantDTO.getPlatformId()));
                accessSideId = accessPlatformDTO.getId();
            }else{
                accessSideId =  Constant.CREDIT_MERCHANT_ID;
            }
            JSONObject requestInfo = new JSONObject();
            requestInfo.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
            requestInfo.put("accessSideId",accessSideId);
            JSONObject result = null;
            try {
                result = serverService.apiCreditOrderSearch(requestInfo, null);
            } catch (Exception e) {
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, null);
                return;
            }
            // 获取结果跟新订单信息
            Integer orderState = result.getInteger("state");
            dealCreditOrderResult(result,qrPayFlowDTO,orderState,null);

        }
    }

    /**
     * 生成api订单token
     * @param platFormId
     * @param merchantId
     * @param transactionAmount
     * @param orderNo
     * @return
     * @throws BizException
     */
    private String entranceEncryptionForSearch(Long platFormId, Long merchantId, BigDecimal transactionAmount, String orderNo) throws BizException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accessSideId", platFormId);
        jsonObject.put("merchantId", merchantId);
        jsonObject.put("transactionAmount", transactionAmount);
        jsonObject.put("tripartiteTransactionNo", orderNo);

        String string = jsonObject.toString();

        HuffmanCode huffmanCode = new HuffmanCode();
        byte[] bytes = huffmanCode.huffmanBuild(string);

        String code = Arrays.toString(bytes);
        String encrypt;
        try {
            encrypt = this.encrypt(code + System.currentTimeMillis());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException(e.getMessage());
        }

        jsonObject.clear();
        jsonObject.put("bytes", code);
        jsonObject.put("length", huffmanCode.getLength());
        jsonObject.put("code", huffmanCode.getHuffmanCodeMap());

        // 暂定十分钟有效期
        redisUtils.hmset(encrypt + "_" + "search_order", jsonObject, 60 * 15);

        return encrypt;
    }

    /**
     * 解密订单token
     * @param token
     * @param request
     * @return
     * @throws Exception
     */
    private JSONObject searchOrderTokenDecode(String token, HttpServletRequest request) throws Exception {

        Map<Object, Object> map = redisUtils.hmget(token + "_" + "search_order");

        if (null == map || map.isEmpty()) {
            throw new SignException(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
        }

        String decode = this.getHuffmanCode(map.get("bytes").toString(), (Map<String, Object>) map.get("code"), Long.parseLong(map.get("length").toString()));

        return JSONObject.parseObject(decode);
    }

    @Override
    public String paymentApiOrderSearchToken(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        checkPaymentApiOrderSearch(requestInfo, request);
        Long merchantId = requestInfo.getLong("merchantId");
        String orderNo = requestInfo.getString("orderNo");
        BigDecimal transAmount = requestInfo.getBigDecimal("transAmount");

        Map<String, Object> params = new HashMap<>(16);
        params.put("accessPartyOrderNo", orderNo);
        QrPayFlowDTO qrPayFlowDTO = findOneQrPayFlow(params);
        if (qrPayFlowDTO.getId() == null) {
            throw new BizException(I18nUtils.get("api.order.not.exist", getLang(request)));
        }

        // 生成订单token
        AccessMerchantDTO accessMerchantDTO = accessMerchantService.findAccessMerchantById(merchantId);
        return entranceEncryptionForSearch(Long.valueOf(accessMerchantDTO.getPlatformId()), merchantId, transAmount, orderNo);
    }

    @Override
    public JSONObject paymentApiOrderSearch(HttpServletRequest request) throws Exception {
        String orderToken = request.getHeader("orderToken");
        if (StringUtils.isEmpty(orderToken)) {
            throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
        }
        JSONObject orderInfo = searchOrderTokenDecode(orderToken, request);


        Map<String, Object> params = new HashMap<>(16);
        params.put("accessPartyOrderNo", orderInfo.getString("tripartiteTransactionNo"));

        //todo 如果没有查到状态，再查分期付
        QrPayFlowDTO qrPayFlowDTO = findOneQrPayFlow(params);

        if (qrPayFlowDTO.getId() == null) {
            throw new BizException(I18nUtils.get("api.order.not.exist", getLang(request)));
        }

        AccessMerchantDTO accessMerchantDTO = accessMerchantService.findAccessMerchantById(qrPayFlowDTO.getMerchantId());
        AccessPlatformDTO accessPlatformDTO = accessPlatformService.findAccessPlatformById(Long.valueOf(accessMerchantDTO.getPlatformId()));

        // 获取订单原状态
        int originalState = qrPayFlowDTO.getState().intValue();

        JSONObject searchInfo = new JSONObject();
        searchInfo.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
        searchInfo.put("accessSideId", accessPlatformDTO.getId());
        JSONObject result = null;
        try {
            result = serverService.apiCreditOrderSearch(searchInfo, null);
        } catch (Exception e) {
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, null);
        }
        // 获取结果跟新订单信息
        Integer orderState = result.getInteger("state");
        String returnState = "";
        if (orderState.intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
            returnState = StaticDataEnum.API_ORDER_STATE_1.getMessage();
            qrPayFlowDTO.setCreditOrderNo(result.getLong("borrowId").toString());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        } else if (orderState.intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            returnState = StaticDataEnum.API_ORDER_STATE_0.getMessage();
        }
        // 若原状态不是成功或失败，则更新订单状态
        if (originalState == StaticDataEnum.TRANS_STATE_3.getCode()) {
            try {
                updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
            } catch (Exception e) {
                log.info("update qr pay flow for concurrency failed, data;{}, error mesaage:{}, e:{}", qrPayFlowDTO, e.getMessage(), e);
            }
        }

        JSONObject notifyInfo = apiOrderResultPackage(qrPayFlowDTO, returnState);
        return notifyInfo;
    }

    /**
     * 校验api订单信息
     * @param requestInfo
     * @param request
     * @throws Exception
     */
    private void checkPaymentApiOrderSearch(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Long merchantId = requestInfo.getLong("merchantId");
        String orderNo = requestInfo.getString("orderNo");
        BigDecimal transAmount = requestInfo.getBigDecimal("transAmount");
        if (merchantId == null) {
            throw new BizException(I18nUtils.get("api.merchantId.isNull", getLang(request)));
        } else {
            AccessMerchantDTO accessMerchantDTO = accessMerchantService.findAccessMerchantById(merchantId);
            if (accessMerchantDTO.getState().intValue() == StaticDataEnum.STATUS_0.getCode()) {
                throw new BizException(I18nUtils.get("merchant.forbidden", getLang(request)));
            }
        }
        if (orderNo.isEmpty()) {
            throw new BizException(I18nUtils.get("api.orderNo.isNull", getLang(request)));
        }
        if (!RegexUtils.isTransAmt(transAmount.toString())) {
            throw new BizException(I18nUtils.get("api.transAmount.error", getLang(request)));
        }
    }

    @Override
    public void addApiPlatformClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) {
        //增加状态控制和批次号
        params.put("orgAccPltFeeClearState", "0");
        params.put("accPltFeeClearState", "2");
        params.put("batchId",clearBatchDTO.getId());
        qrPayFlowDAO.addApiPlatformClearBatchId(params);
    }

    @Override
    public int countApiPlatformClear(Map<String, Object> params) {
        Integer i = qrPayFlowDAO.countApiPlatformClear(params);
        return i==null?0:i;
    }

    @Override
    public void updateApiPlatformClearData(Map<String, Object> clearMap) {
        Map<String,Object> map = new HashMap<>(2);
        map.put("orgBatchId",clearMap.get("batchId"));
        map.put("accPltFeeClearState",StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
        qrPayFlowDAO.addApiPlatformClearBatchId(map);
    }

    @Override
    public List<ClearBatchDTO> apiPlatformClearList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return qrPayFlowDAO.apiPlatformClearList(params);
    }

    @Override
    public List<QrPayFlowDTO> getApiPlatformClearDetail(Map<String, Object> params) {
        List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowDAO.getApiPlatformClearDetail(params);
        return qrPayFlowDTOS;
    }

    @Override
    public void apiOrderClosed(String orderNo) {
        Map<String, Object> params = new HashMap<>(16);
        params.put("accessPartyOrderNo", orderNo);

        QrPayFlowDTO qrPayFlowDTO = findOneQrPayFlow(params);

        if (qrPayFlowDTO.getId() == null) {
            log.info("order not exist, orderNo:{}", orderNo);
        }

        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
        String returnState = StaticDataEnum.API_ORDER_STATE_0.getMessage();
        try {
            updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, null);
        } catch (BizException e) {
            log.info("api order closed failed, data:{}, error message:{}, e:{}", qrPayFlowDTO, e.getMessage(), e);
        }

        if (!StringUtils.isEmpty(qrPayFlowDTO.getAccessPartyNotifyUrl())) {
            JSONObject notifyInfo = null;
            try {
                notifyInfo = apiOrderResultPackage(qrPayFlowDTO, returnState);
            } catch (Exception e) {
                log.info("api order closed notify failed, data:{}, error message:{}, e:{}", notifyInfo, e.getMessage(), e);
            }
        }


    }

    @Override
    public JSONObject dealCreditOrderResultOld(JSONObject result, QrPayFlowDTO qrPayFlowDTO, Integer orderState, HttpServletRequest request) throws Exception {
        String returnState = "";
        if (orderState.intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
            returnState = StaticDataEnum.API_ORDER_STATE_1.getMessage();
            qrPayFlowDTO.setCreditOrderNo(result.getLong("borrowId").toString());
            qrPayFlowDTO.setClearAmount(result.getBigDecimal("clearAmount"));
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            qrPayFlowDTO.setRecAmount(result.getBigDecimal("clearAmount"));
            if(StringUtils.isNotEmpty(result.getString("clearState"))){
                Integer clearState = result.getInteger("clearState");
                if(clearState ==  StaticDataEnum.CREDIT_CLEAR_STATE_1.getCode()){
                    qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
                    qrPayFlowDTO.setClearTime(System.currentTimeMillis());
                }
            }
        } else if (orderState.intValue() == StaticDataEnum.TRANS_STATE_0.getCode()) {
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            returnState = StaticDataEnum.API_ORDER_STATE_0.getMessage();
        }else{
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            returnState = StaticDataEnum.API_ORDER_STATE_3.getMessage();
        }

        JSONObject notifyInfo = null;
        if (qrPayFlowDTO.getState().intValue() == StaticDataEnum.TRANS_STATE_1.getCode()
                || qrPayFlowDTO.getState().intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
            // 更新状态
            updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, null);
            // 通知结果
            if (!StringUtils.isEmpty(qrPayFlowDTO.getAccessPartyNotifyUrl())) {
                notifyInfo = apiOrderResultPackage(qrPayFlowDTO, returnState);
            }
        }
        return  notifyInfo;
    }


    @Override
    public JSONObject dealCreditOrderResult(JSONObject result, QrPayFlowDTO qrPayFlowDTO, Integer orderState, HttpServletRequest request) throws Exception {
        String returnState = "";
        if (orderState.intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
            returnState = StaticDataEnum.API_ORDER_STATE_1.getMessage();
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_11.getCode());
            qrPayFlowDTO.setCreditOrderNo(result.getLong("borrowId").toString());
//            if(StringUtils.isNotEmpty(result.getString("clearState"))){
//                Integer clearState = result.getInteger("clearState");
//                if(clearState ==  StaticDataEnum.CREDIT_CLEAR_STATE_1.getCode()){
//                    qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
//                    qrPayFlowDTO.setClearTime(System.currentTimeMillis());
//                }
//            }
        } else if (orderState.intValue() == StaticDataEnum.TRANS_STATE_0.getCode()) {
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            returnState = StaticDataEnum.API_ORDER_STATE_0.getMessage();
        }else{
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_13.getCode());
            returnState = StaticDataEnum.API_ORDER_STATE_3.getMessage();
        }

        JSONObject notifyInfo = null;
        if (qrPayFlowDTO.getState().intValue() == StaticDataEnum.TRANS_STATE_11.getCode()
                || qrPayFlowDTO.getState().intValue() == StaticDataEnum.TRANS_STATE_12.getCode()) {
            // 更新状态
            updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, null);
            // 通知结果
            if (!StringUtils.isEmpty(qrPayFlowDTO.getAccessPartyNotifyUrl())) {
                notifyInfo = apiOrderResultPackage(qrPayFlowDTO, returnState);
            }
        }
        return  notifyInfo;
    }

    @Override
    public void updateClearState(List<OneMerchantClearDataDTO> list, HttpServletRequest request) {
        if(list == null || list.size() == 0){
            return;
        }
        Map<String,Object> params = new HashMap<>();
        for(OneMerchantClearDataDTO oneMerchantClearDataDTO : list){
            try {
                if(oneMerchantClearDataDTO.getMerchantId() == null || oneMerchantClearDataDTO.getClearFlow() == null){
                    log.info("updateClearState merchantId:"+oneMerchantClearDataDTO.getMerchantId()+"data error");
                    continue;
                }

                //查询原流水
                params.clear();
                params.put("id",oneMerchantClearDataDTO.getClearFlow());
                params.put("merchantId",oneMerchantClearDataDTO.getMerchantId());
                params.put("state",StaticDataEnum.CLEAR_BATCH_STATE_1.getCode());
                ClearDetailDTO clearDetailDTO = clearDetailService.findOneClearDetail(params);
                if(clearDetailDTO == null || clearDetailDTO.getId() == null ){
                    log.info("updateClearState clearFlow:"+oneMerchantClearDataDTO.getClearFlow()+"flow is not cleared");
                    continue;
                }

                // 当前时间
                Long now = System.currentTimeMillis();
                // 查询商户信息
                MerchantDTO merchantDTO = merchantService.findMerchantById(oneMerchantClearDataDTO.getMerchantId());
                // 查询清算流水
                AccountFlowDTO accountFlowDTO = accountFlowService.selectLatestByFlowId(clearDetailDTO.getId(),StaticDataEnum.ACC_FLOW_TRANS_TYPE_5.getCode());
                //余额回滚
                int result = clearBatchService.doClearRollback(accountFlowDTO,clearDetailDTO,request);
                if(result == StaticDataEnum.TRANS_STATE_1.getCode() ){
//                params.clear();
//                params.put("clearBatchId",clearDetailDTO.getClearBatchId());
//                params.put("recUserId",merchantDTO.getUserId());
//                List<ClearFlowDetailDTO> flowList = clearFlowDetailService.find(params,null,null);

                    //交易流水清算状态回退
//                this.rollbackClearDetail(clearDetailDTO.getClearBatchId(),merchantDTO.getUserId());
//                //清算流水明细状态回退
//                clearDetailDTO.setState(StaticDataEnum.CLEAR_BATCH_STATE_2.getCode());
//                clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);
//
//                //清算明细状态回退
//                params.clear();
//                params.put("modifiedDate",now);
//                params.put("clearBatchId",clearDetailDO.getClearBatchId());
//                params.put("recUserId",merchantDTO.getUserId());
//                clearFlowDetailService.updateClearBatchToFail(params);
//
//                //清算批次总金额修改
//                clearDetailDTO.setModifiedDate(now);
                    //查询清算成功的总比数总金额
                    params.clear();
                    params.put("clearBatchId",clearDetailDTO.getClearBatchId());
                    ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailService.clearTotal(params);
                    ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
                    clearBatchDTO.setTotalNumber(clearFlowDetailDTO.getCount().longValue());

                    ClearDetailDTO  clearDetailDTO1 = clearDetailService.clearTotal(params);

                    clearBatchDTO.setTotalAmount(clearDetailDTO1 == null ? BigDecimal.ZERO :clearDetailDTO1.getTransAmount());
                    clearBatchDTO.setBorrowAmount(clearDetailDTO1 == null ? BigDecimal.ZERO :clearDetailDTO1.getBorrowAmount());
                    clearBatchDTO.setClearAmount(clearDetailDTO1 == null ? BigDecimal.ZERO :clearDetailDTO1.getClearAmount());

                    clearBatchService.updateClearBatch(clearDetailDTO.getClearBatchId(),clearBatchDTO,request);

                }else if(result == StaticDataEnum.TRANS_STATE_2.getCode() ){
                    throw new Exception("余额回退失败");
                }
            }catch (Exception e){
                log.error("creditClear merchantId:"+oneMerchantClearDataDTO.getMerchantId()+",clearFlow:"+oneMerchantClearDataDTO.getClearFlow()+",Eexception+"+e.getMessage(),e);
                continue;
            }


        }
    }

    @Override
    public int rollbackClearDetail(Long clearBatchId, Long userId) {
        Long now = System.currentTimeMillis();
        return qrPayFlowDAO.rollbackClearDetail(clearBatchId,userId,now);
    }

    @Override
    public Map<String,Object> wealthPageQuery(HttpServletRequest request, Long merchantId) throws Exception {
        Map<String,Object> result = new HashMap<>(6);
        MerchantDTO merchantDTO =  merchantService.findMerchantById(merchantId);
        if (merchantDTO == null || merchantDTO.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.exist", getLang(request)));
        }
        result.put("merchantState",merchantDTO.getState());
        result.put("merchantWholeSaleState",merchantDTO.getWholeSaleApproveState());
        result.put("marketingDiscount",merchantDTO.getMarketingDiscount() == null ? BigDecimal.ZERO : merchantDTO.getMarketingDiscount());
        BigDecimal balance = userService.getBalance(merchantDTO.getUserId(),StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
        result.put("wholeSaleBalance",balance);
        //当前时间为查询截止时间
        Long endTime = System.currentTimeMillis();

        //获取当日起始时间
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        Long todayStart=  now.getTimeInMillis();
        now.set(Calendar.DAY_OF_MONTH,1);
        Long monthStart = now.getTimeInMillis();
        //查询本日交易
        Map<String,Object> params = new HashMap<>(5);
        params.put("merchantId",merchantDTO.getId());
        params.put("start",todayStart);
        params.put("end",endTime);
        int[] stateList = {StaticDataEnum.TRANS_STATE_1.getCode(),StaticDataEnum.TRANS_STATE_31.getCode()};
        params.put("stateList",stateList);
        List<QrPayFlowDTO> dayList =  this.find(params,null,null);
        if(dayList != null && dayList.size()>0 ){
            result.put("dayNum",dayList.size());
            BigDecimal dayAmount = dayList.stream().map(QrPayFlowDTO::getTransAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            result.put("dayTransAmount",dayAmount);
        }else{
            result.put("dayNum",0);
            result.put("dayTransAmount",BigDecimal.ZERO);
        }
        //查询本月交易
        params.put("start",monthStart);
        List<QrPayFlowDTO> monthList =  this.find(params,null,null);

        params.clear();
        params.put("start",monthStart);
        params.put("end",endTime);
        params.put("merchantId",merchantDTO.getId());
        params.put("state",StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
        List<ClearDetailDTO> clearDetailDTOS = clearDetailService.find(params,null,null);

        if(monthList != null && monthList.size()>0 ){
            BigDecimal monthTransAmount = monthList.stream().map(QrPayFlowDTO::getTransAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            result.put("monthTransAmount",monthTransAmount);
            BigDecimal monthClearAmount = clearDetailDTOS.stream().map(ClearDetailDTO::getClearAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            result.put("monthClearAmount",monthClearAmount);
        }else{
            result.put("monthTransAmount",BigDecimal.ZERO);
            result.put("monthClearAmount",BigDecimal.ZERO);
        }
        return result;
    }

    @Override
    public Map<String, Object> orderPageHeadQuery(HttpServletRequest request, Long merchantId) throws Exception {
        Map<String,Object> result = new HashMap<>(6);
        Map<String,Object> params = new HashMap<>();
        MerchantDTO merchantDTO =  merchantService.findMerchantById(merchantId);
        if (merchantDTO == null || merchantDTO.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.exist", getLang(request)));
        }
        Long todayEnd= System.currentTimeMillis();
        //获取当日起始时间
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        Long todayStart=  now.getTimeInMillis();
        Long yesterdayEnd=  todayStart - 1;
        Long yesterdayStart = todayStart - 24 * 60*60*1000;
        params.put("merchantId",merchantDTO.getId());
        params.put("start",yesterdayStart);
        params.put("end",yesterdayEnd);
        int[] stateList = {StaticDataEnum.TRANS_STATE_1.getCode(),StaticDataEnum.TRANS_STATE_31.getCode()};
        params.put("stateList",stateList);

        List<QrPayFlowDTO> todayList = this.find(params,null,null);
        params.clear();
        params.put("merchantId",merchantDTO.getId());
        params.put("passTimeStart",yesterdayStart);
        params.put("passTimeEnd",yesterdayEnd);
        params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
        List<WholeSalesFlowDTO> wholeSalesFlowDTOS = wholeSalesFlowService.find(params,null,null);
        BigDecimal todayClearAmount = BigDecimal.ZERO;
        BigDecimal todayWholeSaleAmount = BigDecimal.ZERO;
        if(todayList != null && todayList.size() > 0){
            todayClearAmount = todayList.stream().map(QrPayFlowDTO::getClearAmount).reduce(BigDecimal.ZERO,BigDecimal::add);

        }
        if(wholeSalesFlowDTOS != null && wholeSalesFlowDTOS.size() > 0){
            todayWholeSaleAmount = wholeSalesFlowDTOS.stream().map(WholeSalesFlowDTO::getSettlementAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        }

        result.put("todayClearAmount",todayClearAmount.add(todayWholeSaleAmount));
        params.clear();
        params.put("merchantId",merchantDTO.getId());
        params.put("start",yesterdayStart);
        params.put("end",yesterdayEnd);
        params.put("state",StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
        List<ClearDetailDTO> yesterdayList = clearDetailService.find(params,null,null);
        if(yesterdayList != null && yesterdayList.size() > 0){
            BigDecimal yesterdayAmount = yesterdayList.stream().map(ClearDetailDTO::getClearAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            result.put("yesterdayClearAmount",yesterdayAmount);
        }else{
            result.put("yesterdayClearAmount",BigDecimal.ZERO);
        }
        return  result;
    }

    @Override
    public  List<Map<String,Object>> getOrderPageGroupByMonthList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc){
        List<QrPayFlowDTO> list = this.find(params,scs,pc);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM. yyyy", Locale.US);
        SimpleDateFormat simpleDateFormat_ = new SimpleDateFormat("MMM.dd HH:mm", Locale.US);
        String month = null;

        String format = null;
        Map<String ,Object> monthMap = new HashMap<>();
        List<QrPayFlowDTO> monthList = new ArrayList<>();
        Boolean dateFlag = false;
        if(params.containsKey("clearState") &&  Integer.parseInt(params.get("clearState").toString()) == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode() ){
            dateFlag = true;
        }

        List<Map<String,Object>> result = new ArrayList<>();
        for(QrPayFlowDTO qrPayFlowDTO:list){

            if(qrPayFlowDTO.getSaleType().compareTo(StaticDataEnum.SALE_TYPE_1.getCode()) == 0){
                //整体出售订单 清算状态改为已清算
                qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
            }

            if(dateFlag){
                format = simpleDateFormat.format(qrPayFlowDTO.getClearTime());
                qrPayFlowDTO.setTransTime(simpleDateFormat_.format(qrPayFlowDTO.getClearTime()));
            }else{
                format = simpleDateFormat.format(qrPayFlowDTO.getCreatedDate());
                qrPayFlowDTO.setTransTime(simpleDateFormat_.format(qrPayFlowDTO.getCreatedDate()));
            }

            if(!format.equals(month)){
                if(month != null){
                    //非首次
                    monthMap.put("list",monthList);
                    result.add(monthMap);
                    monthList = new ArrayList<>();
                    monthMap = new HashMap<>();

                }
                monthList.add(qrPayFlowDTO);
                month = format;
                //新月份新累计
                monthMap.put("month",month);
                //计算本月总金额和总笔数
                //获得本月起止时间


                if(dateFlag){
                    Long now = qrPayFlowDTO.getClearTime();
                    Map<String,Long> timeMap = getMonthTime(now);
                    params.put("clearStart",timeMap.get("start"));
                    params.put("clearEnd",timeMap.get("end"));
                }else{
                    Long now = qrPayFlowDTO.getCreatedDate();
                    Map<String,Long> timeMap = getMonthTime(now);
                    params.put("start",timeMap.get("start"));
                    params.put("end",timeMap.get("end"));
                }


                List<QrPayFlowDTO> totalList = this.find(params,null,null);

                BigDecimal totalTransAmount =  totalList.stream().map(QrPayFlowDTO::getTransAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal totalClearAmount =  totalList.stream().map(QrPayFlowDTO::getClearAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                monthMap.put("totalTransAmount",totalTransAmount);
                monthMap.put("totalClearAmount",totalClearAmount);
                monthMap.put("totalNum",totalList.size());
            }else{
                monthList.add(qrPayFlowDTO);
            }
        }
        monthMap.put("month",month);
        monthMap.put("list",monthList);
        result.add(monthMap);

        return result;
    }

    @Override
    public  List<Map<String,Object>> orderPageGroupByDayList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc){
        List<QrPayFlowDTO> list = this.find(params,scs,pc);


        List<Map<String,Object>> result = dealDateList(params ,list);
//        for(QrPayFlowDTO qrPayFlowDTO:list){
//            if(dateFlag){
//                format = simpleDateFormat.format(qrPayFlowDTO.getClearTime());
//            }else{
//                format = simpleDateFormat.format(qrPayFlowDTO.getCreatedDate());
//            }
//
//            if(!format.equals(date)){
//                if(date != null){
//                    //非首次
//                    dateMap.put("list",dateList);
//                    result.add(dateMap);
//                    dateList = new ArrayList<>();
//                    dateMap = new HashMap<>();
//
//                }
//                dateList.add(qrPayFlowDTO);
//                date = format;
//                //新日期新累计
//                dateMap.put("date",date);
//
//                //计算本日总金额和总笔数
//                //获得本日起止时间
//                if(dateFlag){
//                    Long now = qrPayFlowDTO.getClearTime();
//                    Map<String,Long> timeMap = getDayStartTime(now);
//                    params.put("clearStart",timeMap.get("start"));
//                    params.put("clearEnd",timeMap.get("end"));
//                }else{
//                    Long now = qrPayFlowDTO.getCreatedDate();
//                    Map<String,Long> timeMap = getDayStartTime(now);
//                    params.put("start",timeMap.get("start"));
//                    params.put("end",timeMap.get("end"));
//                }
//
//
//                List<QrPayFlowDTO> totalList = this.find(params,null,null);
//
//                BigDecimal totalTransAmount =  totalList.stream().map(QrPayFlowDTO::getTransAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
//                BigDecimal totalClearAmount =  totalList.stream().map(QrPayFlowDTO::getClearAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
//                dateMap.put("totalTransAmount",totalTransAmount);
//                dateMap.put("totalClearAmount",totalClearAmount);
//            }else{
//                dateList.add(qrPayFlowDTO);
//            }
//        }
//        dateMap.put("date",date);
//        dateMap.put("list",dateList);
//        result.add(dateMap);

        return result;
    }

    private List<Map<String, Object>> dealDateList(Map<String, Object> params, List<QrPayFlowDTO> list) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM.dd ,yyyy", Locale.US);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MMM.dd HH:mm", Locale.US);
        String date = null;

        String format = null;
        Map<String ,Object> dateMap = new HashMap<>();
        List<QrPayFlowDTO> dateList = new ArrayList<>();
        Boolean dateFlag = false;
        if(params.containsKey("clearState") &&  Integer.parseInt(params.get("clearState").toString()) == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode() ){
            dateFlag = true;
        }
        List<Map<String,Object>> result = new ArrayList<>();
        for(QrPayFlowDTO qrPayFlowDTO:list){

            if(qrPayFlowDTO.getSaleType().compareTo(StaticDataEnum.SALE_TYPE_1.getCode()) == 0){
                //整体出售订单 清算状态改为已清算
                qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
            }

            if(dateFlag){
                format = simpleDateFormat.format(qrPayFlowDTO.getClearTime());
                qrPayFlowDTO.setTransTime(simpleDateFormat2.format(qrPayFlowDTO.getClearTime()));
            }else{
                format = simpleDateFormat.format(qrPayFlowDTO.getCreatedDate());
                qrPayFlowDTO.setTransTime(simpleDateFormat2.format(qrPayFlowDTO.getCreatedDate()));
            }

            if(!format.equals(date)){
                if(date != null){
                    //非首次
                    dateMap.put("list",dateList);
                    result.add(dateMap);
                    dateList = new ArrayList<>();
                    dateMap = new HashMap<>();

                }
                dateList.add(qrPayFlowDTO);
                date = format;
                //新日期新累计
                dateMap.put("date",date);

                //计算本日总金额和总笔数
                //获得本日起止时间
                if(dateFlag){
                    Long now = qrPayFlowDTO.getClearTime();
                    Map<String,Long> timeMap = getDayStartTime(now);
                    params.put("clearStart",timeMap.get("start"));
                    params.put("clearEnd",timeMap.get("end"));
                }else{
                    Long now = qrPayFlowDTO.getCreatedDate();
                    Map<String,Long> timeMap = getDayStartTime(now);
                    params.put("start",timeMap.get("start"));
                    params.put("end",timeMap.get("end"));
                }
                //去除条数查询
                params.put("pc", null);
                List<QrPayFlowDTO> totalList = qrPayFlowDAO.getUnClearedList(params);

                BigDecimal totalTransAmount =  totalList.stream().map(QrPayFlowDTO::getTransAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal totalClearAmount =  totalList.stream().map(QrPayFlowDTO::getClearAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                dateMap.put("totalTransAmount",totalTransAmount);
                dateMap.put("totalClearAmount",totalClearAmount);
                dateMap.put("totalNum",totalList.size());
            }else{
                dateList.add(qrPayFlowDTO);
            }
        }
        dateMap.put("date",date);
        dateMap.put("list",dateList);
        result.add(dateMap);
        return result;
    }

    @Override
    public void creditClear(List<OneMerchantClearDataDTO> list, HttpServletRequest request) throws BizException {
        if(list == null || list.size() == 0){
            return;
        }
        //
        //数据校验
        HashSet<String> idSet = new HashSet<>();
        for(OneMerchantClearDataDTO oneMerchantClearDataDTO : list){
            List<UpdateClearStateListDTO> orderList = oneMerchantClearDataDTO.getBorrowList();

            if(oneMerchantClearDataDTO.getMerchantId() == null ||orderList == null || orderList.size() ==0 ||oneMerchantClearDataDTO.getClearFlow() == null){
                log.info("creditClear merchantId:"+oneMerchantClearDataDTO.getMerchantId()+"data error");
                continue;
            }
            MerchantDTO merchantDTO  = merchantService.findMerchantById(oneMerchantClearDataDTO.getMerchantId());
            if(merchantDTO == null || merchantDTO.getId() == null  ){
                log.info("creditClear merchantId:"+oneMerchantClearDataDTO.getMerchantId()+"merchant not existence");
                continue;
            }
            HashSet<String> borrowIdSet = new HashSet<>();
            for(UpdateClearStateListDTO data : orderList){
                //查询交易状态
                if(data.getTripartiteTransactionNo() == null ){
                    throw new BizException(I18nUtils.get("api.orderNo.isNull", getLang(request)));
                }
                borrowIdSet.add(data.getTripartiteTransactionNo().toString());
            }
            if(borrowIdSet.size() != orderList.size()){
                throw new BizException("creditClear : data error");
            }
            idSet.add(oneMerchantClearDataDTO.getMerchantId().toString());
        }

        if(idSet.size() != list.size()){
            throw new BizException("creditClear : data error");
        }


        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        clearBatchDTO.setClearType(StaticDataEnum.CLEAR_TYPE_3.getCode());
        clearBatchDTO.setState(StaticDataEnum.CLEAR_BATCH_STATE_0.getCode());
        clearBatchDTO.setId(clearBatchService.saveClearBatch(clearBatchDTO,request));

        Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            clearBatchService.doCreditClear(list,clearBatchDTO,request);
                        }catch (Exception e){
                            log.info("批次号：{}，清算异常：",clearBatchDTO.getId(),e.getMessage(),e);
                        }
                    }
                }
        );
        t.start();

    }

    @Override
    public List<Map<String, Object>> getUnClearedList(Map<String, Object> params) {
        List<QrPayFlowDTO> list = qrPayFlowDAO.getUnClearedList(params);
        return  dealDateList(params,list);
    }

    @Override
    public List<QrPayFlowDTO> getAllTransFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {

        params = getUnionParams(params, scs, pc);
        List<QrPayFlowDTO> list = qrPayFlowDAO.getUnClearedList(params);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MMM.dd HH:mm", Locale.US);
        for(QrPayFlowDTO qrPayFlowDTO : list){
            qrPayFlowDTO.setTransTime(simpleDateFormat2.format(qrPayFlowDTO.getCreatedDate()));
        }
        return list;
    }

    @Override
    public Map<String, Long> getDayStartTime(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Map<String,Long> result = new HashMap<>();
        Long startTime = calendar.getTimeInMillis();
        Long endTime = startTime + 24*60*60*1000-1;
        result.put("start",startTime);
        result.put("end",endTime);
        return result;
    }

    @Override
    public Map<String, Object> getOperationData(JSONObject data, HttpServletRequest request) throws BizException {
        Long merchantId = data.getLong("merchantId");
        Integer type = data.getInteger("type");
        String date = data.getString("date");
        //参数校验
        if(merchantId == null || StringUtils.isEmpty(date) || type == null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        //获取商户
        MerchantDTO merchantDTO =  merchantService.findMerchantById(merchantId);
        if (merchantDTO == null || merchantDTO.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.exist", getLang(request)));
        }
        Map<String,Object> resultMap = new HashMap<>(8);
        Long startTime ;
        Long endTime ;
        int count ;
        List<QrPayFlowDTO> transList ;
        List<QrPayFlowDTO> clearList ;
        Map<String,Object> params = new HashMap<>(16);
        params.put("merchantId",merchantId);
        if(StaticDataEnum.DATE_TYPE_YEAR.getCode() == type){
            //按年查询
            if(date.length() != Constant.FOUR){
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
            int year = Integer.parseInt(date);
            startTime = getStartTime(year,1);
            Map<String,Long> time = getYearTime(startTime);
            endTime = time.get("end");
            params.put("format","%m");
            params.put("start",startTime);
            params.put("end",endTime);
            //查询交易数据
            transList = qrPayFlowDAO.getOperationTransData(params);
            //查询清算数据
            clearList = qrPayFlowDAO.getOperationClearData(params);
            //12个月
            count = 12;

        }else if(StaticDataEnum.DATE_TYPE_MONTH.getCode() == type){
            //按月查询
            if(date.length() != Constant.SIX){
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
            int year = Integer.parseInt(date.substring(0,4));
            int month = Integer.parseInt(date.substring(4));
            startTime = getStartTime(year,month);
            Map<String,Long> time = getMonthTime(startTime);
            endTime = time.get("end");
            params.put("format","%d");
            params.put("start",startTime);
            params.put("end",endTime);
            transList = qrPayFlowDAO.getOperationTransData(params);
            //查询清算数据
            clearList = qrPayFlowDAO.getOperationClearData(params);
            //本月天数
            count = (BigDecimal.valueOf(endTime+1).subtract(BigDecimal.valueOf(startTime))).divide(BigDecimal.valueOf(24*60*60*1000)).intValue();


        }else{
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        List<Map<String,Object>> transData = new ArrayList<>();
        List<Map<String,Object>> clearData = new ArrayList<>();
        //计算每个日期单位的数据
        for(int i = 1;i <= count;i++){
            Map<String,Object> oneTransData = new HashMap<>(4);
            Map<String,Object> oneClearData = new HashMap<>(4);
            int dateline = i;
            //计算交易金额
            if(transList != null && transList.size() > 0){
                BigDecimal transAmount = transList.stream().filter(list -> dateline == Integer.parseInt(list.getTransTime())).map(QrPayFlowDTO::getTransAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                oneTransData.put("transAmount",transAmount == null ? BigDecimal.ZERO : transAmount);
            }else{
                oneTransData.put("transAmount",BigDecimal.ZERO);
            }
            //计算清算金额
            if(clearList != null && clearList.size() > 0){
                BigDecimal clearAmount = clearList.stream().filter(list -> dateline == Integer.parseInt(list.getTransTime())).map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                oneClearData.put("clearAmount",clearAmount == null ? BigDecimal.ZERO : clearAmount);
            }else{
                oneClearData.put("clearAmount",BigDecimal.ZERO);
            }
            //数据处理
            oneTransData.put("date",dateline);
            oneClearData.put("date",dateline);
            transData.add(oneTransData);
            clearData.add(oneClearData);
        }
        //计算交易总金额和清算总金额
        if(transList != null && transList.size() > 0){
            BigDecimal transAmount = transList.stream().map(QrPayFlowDTO::getTransAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            BigDecimal clearAmount = clearList.stream().map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            resultMap.put("clearAmount",clearAmount);
            resultMap.put("transAmount",transAmount);
        }else{
            resultMap.put("clearAmount",BigDecimal.ZERO);
            resultMap.put("transAmount",BigDecimal.ZERO);
        }

        resultMap.put("clearData",clearData);
        resultMap.put("transData",transData);
        return resultMap;
    }

    @Override
    public List<QrPayFlowDTO> findBatchAmountOutDoubtFlow() {
        return qrPayFlowDAO.findBatchAmountOutDoubtFlow();
    }

    @Override
    public void changeClearState(ChangeClearStateDTO req, HttpServletRequest request) throws Exception {
        if(req.getIdList() == null || req.getIdList().size() == 0){
            return;
        }
        Map<String,Object> params = new HashMap<>();
        if(req.getType() == StaticDataEnum.CHANGE_CLEAR_STATE_TYPE_0.getCode()){
            params.put("oldState",StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
            params.put("newState",StaticDataEnum.CLEAR_STATE_TYPE_3.getCode());
        }else if(req.getType() == StaticDataEnum.CHANGE_CLEAR_STATE_TYPE_1.getCode()){
            params.put("oldState",StaticDataEnum.CLEAR_STATE_TYPE_3.getCode());
            params.put("newState",StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        }else{
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        params.put("idList",req.getIdList());
        params.put("time",System.currentTimeMillis());
        qrPayFlowDAO.changeClearState(params);
    }

    @Override
    public  List<Map<String ,Object>> merchantClearMessageList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, HttpServletRequest request) {
        params = getUnionParams(params, scs, pc);
        // 查询商户信息
        List<MerchantDTO> merchantList = merchantService.getClearMerchantList(params);
        List<Long> merchantIdList = new ArrayList<>();
        for(MerchantDTO merchantDTO:merchantList){
            merchantIdList.add(merchantDTO.getId());
        }


        params.put("merchantIdList",merchantIdList);
        // 查询商户的所有订单信息
        List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowDAO.merchantClearMessageList(params);
        List<Map<String ,Object>> resultList = new ArrayList<>();
        // 处理数据
        for(MerchantDTO merchantDTO : merchantList){
            // 一条数据信息
            Map<String ,Object> oneResult = new HashMap<>(16);
            // 商户信息
            oneResult.put("merchantId",merchantDTO.getId().toString());
            oneResult.put("merchantName",merchantDTO.getPracticalName());
            oneResult.put("merchantState",merchantDTO.getState());
            oneResult.put("abn",merchantDTO.getAbn());
            oneResult.put("city",merchantDTO.getCity());
            if(qrPayFlowDTOS != null && qrPayFlowDTOS.size() > 0){
                // 筛选商户所有订单
                List<QrPayFlowDTO> merchantData = qrPayFlowDTOS.stream().filter(flow->flow.getMerchantId().equals(merchantDTO.getId())).collect(Collectors.toList());
                if(merchantData != null && merchantData.size() > 0){
                    oneResult.put("totalNumber",merchantData.size());
                    BigDecimal totalAmount = merchantData.stream().map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                    oneResult.put("totalAmount",totalAmount);
                    // 筛选未结算订单
                    List<QrPayFlowDTO> unclearedData = merchantData.stream().filter(flow->flow.getClearState() == StaticDataEnum.CLEAR_STATE_TYPE_0.getCode()).collect(Collectors.toList());
                    if(unclearedData != null && unclearedData.size() >0){
                        oneResult.put("unclearedNumber",unclearedData.size());
                        BigDecimal unclearedAmount = unclearedData.stream().map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                        oneResult.put("unclearedAmount",unclearedAmount);
                    }else{
                        oneResult.put("unclearedNumber",0);
                        oneResult.put("unclearedAmount",BigDecimal.ZERO);
                    }
                    // 筛选已结算订单
                    List<QrPayFlowDTO> clearedData = merchantData.stream().filter(flow->flow.getClearState() == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode()).collect(Collectors.toList());
                    if(clearedData != null && clearedData.size() >0){
                        oneResult.put("clearedNumber",clearedData.size());
                        BigDecimal clearedAmount = clearedData.stream().map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                        oneResult.put("clearedAmount",clearedAmount);
                    }else{
                        oneResult.put("clearedNumber",0);
                        oneResult.put("clearedAmount",BigDecimal.ZERO);
                    }
                    // 筛选延迟结算订单
                    List<QrPayFlowDTO> delayClearData = merchantData.stream().filter(flow->flow.getClearState() == StaticDataEnum.CLEAR_STATE_TYPE_3.getCode()).collect(Collectors.toList());
                    if(delayClearData != null && delayClearData.size() >0){
                        oneResult.put("delayClearNumber",delayClearData.size());
                        BigDecimal delayClearAmount = delayClearData.stream().map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                        oneResult.put("delayClearAmount",delayClearAmount);
                    }else{
                        oneResult.put("delayClearNumber",0);
                        oneResult.put("delayClearAmount",BigDecimal.ZERO);
                    }
                }else{
                    oneResult.put("totalNumber",0);
                    oneResult.put("totalAmount",BigDecimal.ZERO);
                    oneResult.put("unclearedNumber",0);
                    oneResult.put("unclearedAmount",BigDecimal.ZERO);
                    oneResult.put("clearedNumber",0);
                    oneResult.put("clearedAmount",BigDecimal.ZERO);
                    oneResult.put("delayClearNumber",0);
                    oneResult.put("delayClearAmount",BigDecimal.ZERO);
                }
            }else{
                oneResult.put("totalNumber",0);
                oneResult.put("totalAmount",BigDecimal.ZERO);
                oneResult.put("unclearedNumber",0);
                oneResult.put("unclearedAmount",BigDecimal.ZERO);
                oneResult.put("clearedNumber",0);
                oneResult.put("clearedAmount",BigDecimal.ZERO);
                oneResult.put("delayClearNumber",0);
                oneResult.put("delayClearAmount",BigDecimal.ZERO);
            }
            resultList.add(oneResult);
        }
        return resultList;
    }

    @Override
    public int merchantUnclearDetailCount(Map<String, Object> params) {
        return qrPayFlowDAO.merchantUnclearDetailCount(params);
    }

    @Override
    public  List<QrPayFlowDTO> merchantUnclearDetailList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return qrPayFlowDAO.merchantUnclearDetailList(params);
    }

    @Override
    public List<QrPayFlowDTO> clearedDetailTransFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return qrPayFlowDAO.clearedDetailTransFlowList(params);
    }

    @Override
    public Integer clearedDetailTransFlowCount(Long id) {
        return qrPayFlowDAO.clearedDetailTransFlowCount(id);
    }


    @Override
    public int countMerchantClearListNew(Map<String, Object> params) {
        return qrPayFlowDAO.countMerchantClearListNew(params);
    }

    @Override
    public List<ExcelCardPayDTO> repackExportPayFlowByCard(List<QrPayFlowCardPayDTO> list, HttpServletRequest request) {
        List<ExcelCardPayDTO> res = new ArrayList<>(list.size());
        for (QrPayFlowCardPayDTO payDTO : list) {
            ExcelCardPayDTO dto = new ExcelCardPayDTO();
            dto.setOrder_no(payDTO.getTransNo());
            dto.setCustomer_no(payDTO.getPayUserId().toString());
            dto.setMerchant_trading_name(payDTO.getPracticalName());
            dto.setOrder_amount(payDTO.getTransAmount().toString());
            dto.setActual_payment(payDTO.getPayAmount().toString());
            dto.setDiscount_amount(payDTO.getDiscountAmount().toString());
            dto.setPocket_money(payDTO.getRedEnvelopeAmount().toString());
            dto.setService_fee(null!=payDTO.getPlatformFee() ? payDTO.getPlatformFee().toString():"");
            dto.setMerchant_settlement(payDTO.getRecAmount().toString());
            dto.setOrder_time(this.formatDate(payDTO.getCreatedDate()));
            dto.setClear_time(this.formatDate(payDTO.getCheckTime()));
            //业务模式 0：正常销售 1：整体销售 2：混合销售
            Integer saleType = payDTO.getSaleType();
            if (null!=saleType) {
                dto.setBusiness_type(saleType == 0 ? "Basic Sales" : (saleType == 1 ? "Wholesales" : "Mixed Sales"));
            }
            //付款方式 2：卡支付，22：分期付
            dto.setPayment_method(payDTO.getTransType()==2?"Bank Card":"Installment");
            //0：不需要  1：需要
            dto.setNeed_clear_to_merchant(payDTO.getIsNeedClear()==1?"Yes":"No");
            dto.setPayment_channel(payDTO.getChannelName());
            //对账状态 0：未对账 1：已对账 2：无需对账
            Integer checkState = payDTO.getCheckState();
            if (null!=checkState) {
                dto.setReconciliation_status(checkState == 0 ? "Not yet" : (checkState == 1 ? "Done" : "Not needed"));
            }
            Long checkTime = payDTO.getCheckTime();
            dto.setReconciliation_time(this.formatDate(checkTime));
            res.add(dto);
        }
        return res;
    }

    @Override
    public List<ExcelInstPayDTO> repackExportInstPayFlow(List<QrPayFlowInstalmentPayDTO> list, HttpServletRequest request) {
        List<ExcelInstPayDTO> res = new ArrayList<>(list.size());
        for (QrPayFlowInstalmentPayDTO payDTO : list) {
            ExcelInstPayDTO dto = new ExcelInstPayDTO();
            dto.setOrder_no(payDTO.getTransNo());
            Long payUserId = payDTO.getPayUserId();
            dto.setCustomer_no(null!=payUserId?payUserId.toString():"");
            dto.setMerchant_trading_name(payDTO.getPracticalName());
            dto.setOrder_amount(payDTO.getTransAmount().toString());
            dto.setActual_payment(payDTO.getPayAmount().toString());
            dto.setDiscount_amount(payDTO.getDiscountAmount().toString());
            dto.setPocket_money(payDTO.getRedEnvelopeAmount().toString());
            dto.setService_fee(null!=payDTO.getPlatformFee() ? payDTO.getPlatformFee().toString():"");
            dto.setMerchant_settlement(payDTO.getRecAmount().toString());
            dto.setMerchant_settlement(payDTO.getRecAmount().toString());
            dto.setOrder_time(this.formatDate(payDTO.getCreatedDate()));
            dto.setClear_time(this.formatDate(payDTO.getCheckTime()));
            Integer periodQuantity = payDTO.getPeriodQuantity();
            dto.setTerm(null!=periodQuantity?periodQuantity.toString():"0");
            Integer paidPeriodQuantity = payDTO.getPaidPeriodQuantity();
            dto.setCleared_term(null != paidPeriodQuantity ? paidPeriodQuantity.toString():"0");
            BigDecimal paidAmount = payDTO.getPaidAmount();
            dto.setCleared_amount(null!= paidAmount?paidAmount.toString():"0.00");
            //业务模式 0：正常销售 1：整体销售 2：混合销售
            Integer saleType = payDTO.getSaleType();
            if (null!=saleType) {
                dto.setBusiness_type(saleType == 0 ? "Basic Sales" : (saleType == 1 ? "Wholesales" : "Mixed Sales"));
            }
            //付款方式 2：卡支付，22：分期付
            dto.setPayment_method(payDTO.getTransType()==2?"Bank Card":"Installment");
            //0：不需要  1：需要
            dto.setNeed_clear_to_merchant(payDTO.getIsNeedClear()==1?"Yes":"No");
            dto.setPayment_channel("Split Pay");
            /*//对账状态 0：未对账 1：已对账 2：无需对账
            Integer checkState = payDTO.getCheckState();
            if (null!=checkState) {
                dto.setReconciliation_status(checkState == 0 ? "Not yet" : (checkState == 1 ? "Done" : "Not needed"));
            }
            Long checkTime = payDTO.getCheckTime();
            dto.setReconciliation_time(this.formatDate(checkTime));*/
            res.add(dto);
        }
        return  res;
    }

    @Override
    public List<ExcelOrderDTO> repackExcelOrderDTO(List<OrderReportDTO> list) {
        Map<String, List<StaticData>> city = staticDataService.findByCodeList(new String[]{"city"});
        List<ExcelOrderDTO> res = new ArrayList<>(list.size());
        for (OrderReportDTO d : list) {
            ExcelOrderDTO dto = new ExcelOrderDTO();
            dto.setMerchant_id(d.getMerchantId());
            dto.setMerchant_name(d.getMerchantName());
            Long recUserId = d.getRecUserId();
            dto.setRec_user_id(null!=recUserId?recUserId.toString():"");
            dto.setBasic_sales_order_quantity(d.getBasicSalesOrderQuantity().toString());
            dto.setBasic_sales_order_amount(d.getBasicSalesOrderAmount().toString());
            dto.setWhole_sales_order_quantity(d.getWholeSalesOrderQuantity().toString());
            dto.setWhole_sales_order_amount(d.getWholeSalesOrderAmount().toString());
            dto.setMix_sales_order_quantity(d.getMixSalesOrderQuantity().toString());
            dto.setMix_sales_order_amount(d.getMixSalesOrderAmount().toString());
            dto.setRed_envelope_amount(d.getRedEnvelopeAmount().toString());
            dto.setBasic_sales_order_amount_by_card(d.getBasicSalesOrderAmountByCard().toString());
            dto.setBasic_sales_customer_actual_payment_amount_by_card(d.getBasicSalesCustomerActualPaymentAmountByCard().toString());
            dto.setBasic_sales_need_settled_amount_by_card(d.getBasicSalesNeedSettledAmountByCard().toString());
            dto.setBasic_sales_order_amount_by_instalment(d.getBasicSalesOrderAmountByInstalment().toString());
            dto.setBasic_sales_customer_actual_payment_amount_by_instalment(d.getBasicSalesCustomerActualPaymentAmountByInstalment().toString());
            dto.setBasic_sales_need_settled_amount_by_instalment(d.getBasicSalesNeedSettledAmountByInstalment().toString());
            dto.setWhole_sales_order_amount_by_card(d.getWholeSalesOrderAmountByCard().toString());
            dto.setWhole_sales_customer_actual_payment_amount_by_card(d.getWholeSalesCustomerActualPaymentAmountByCard().toString());
            dto.setWhole_sales_order_amount_by_instalment(d.getWholeSalesOrderAmountByInstalment().toString());
            dto.setWhole_sales_customer_actual_payment_amount_by_instalment(d.getWholeSalesCustomerActualPaymentAmountByInstalment().toString());
            dto.setMix_sales_order_amount_by_card(d.getMixSalesOrderAmountByCard().toString());
            dto.setMix_sales_customer_actual_payment_amount_by_card(d.getMixSalesCustomerActualPaymentAmountByCard().toString());
            dto.setMix_sales_needSettled_amount_by_card(d.getMixSalesNeedSettledAmountByCard().toString());
            dto.setMix_sales_order_amount_by_instalment(d.getMixSalesOrderAmountByInstalment().toString());
            dto.setMix_sales_customer_actual_payment_amount_by_instalment(d.getMixSalesCustomerActualPaymentAmountByInstalment().toString());
            dto.setMix_sales_need_settled_amount_by_instalment(d.getMixSalesNeedSettledAmountByInstalment().toString());
            dto.setMerchant_total_whole_sales_amount(d.getMerchantTotalWholeSalesAmount().toString());
            dto.setMerchant_remaining_whole_sales_amount(d.getMerchantRemainingWholeSalesAmount().toString());
            dto.setToday_merchant_whole_sales_amount(d.getTodayMerchantWholeSalesAmount().toString());
            List<StaticData> city1 = city.get("city");
            for (StaticData staticData : city1) {
                if (d.getCity()!=null){
                    if (staticData.getValue().equals(d.getCity().toString())){
                        dto.setCity(staticData.getEnName());
                    }
                }

            }
            res.add(dto);
        }
        return res;
    }

    @Override
    public void genDailyReport() {
        log.info("每日运营统计邮件发送");
        JSONObject param = new JSONObject(3);
        long timeMillis = System.currentTimeMillis();
        String todayStr = new SimpleDateFormat("yyyy-MM-dd").format(timeMillis);
        param.put("today",todayStr);
        List<OrderReportDTO> list = this.getOrderReport(param, null);
        try {
            String fileName = "order_report"+new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(
                    timeMillis)+".xlsx";
            List<ExcelOrderDTO> repack = this.repackExcelOrderDTO(list);
            Workbook workbook = POIUtils.createExcel(repack, ExcelOrderDTO.class, fileName);
            String filePath = POIUtils.writeExcel(workbook, "/Users/aarons/Desktop/SandBox", fileName);
            File file = new File(filePath);

            Session session = MailUtil.getSession(sysEmail);
            MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, "mixuam@sina.com", "sendTitle", "sendMsg" , Collections.singletonList(file), session);
            MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);

        } catch (Exception e) {
            log.error("每日运营统计邮件发送---异常");
            e.printStackTrace();
        }

    }

    @Override
    public int getClearMerchantListCount(Map<String, Object> params) {

        return qrPayFlowDAO.getClearMerchantListCount(params);
    }

    @Override
    public void getMonthlyUserSavedTask() throws BizException {
        // 获取当前时间
        Long now = System.currentTimeMillis();
        // 获得上个月的时间区间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Long end = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, -1);
        Long start =  calendar.getTimeInMillis();
        Map<String,Object> params = new HashMap<>(8);
        params.put("start",start);
        params.put("end",end);
        List<QrPayFlowDTO> resultList = qrPayFlowDAO.getMonthlyUserData(params);

        Long saveTime = end-1;
        if(resultList != null && resultList.size() > 0){
            List<UserMonthlyData> data = new ArrayList<>();
            for(QrPayFlowDTO qrPayFlowDTO:resultList){
                UserMonthlyData oneData = new UserMonthlyData();
                oneData.setId(SnowflakeUtil.generateId());
                oneData.setDate(saveTime);
                oneData.setPayAmount(qrPayFlowDTO.getPayAmount());
                oneData.setTransAmount(qrPayFlowDTO.getTransAmount());
                oneData.setSavedAmount(qrPayFlowDTO.getFirstAmount());
                oneData.setUserId(qrPayFlowDTO.getPayUserId());
                oneData.setCreatedDate(now);
                oneData.setModifiedDate(now);
                oneData.setStatus(1);
                data.add(oneData);
            }
            userMonthlyDataService.saveUserMonthlyDataList(data,null);
        }

    }

    @Override
    public JSONObject getUserSavedAmount(Long userId, HttpServletRequest request) throws BizException {
        // 获取当前时间
        Long now = System.currentTimeMillis();
        Map<String,Long> monthTime = this.getMonthTime(now);
        Map<String,Object> params = new HashMap<>(8);
        params.put("start",monthTime.get("start"));
        params.put("end",monthTime.get("end"));
        params.put("payUserId",userId);
        // 查询本月时间
        List<QrPayFlowDTO> resultList = qrPayFlowDAO.getMonthlyUserData(params);
        BigDecimal savedAmountMonthly = BigDecimal.ZERO;
        if(resultList != null && resultList.size() > 0){
            QrPayFlowDTO dto = resultList.get(0);
            savedAmountMonthly = dto.getTransAmount().subtract(dto.getPayAmount()).add(dto.getFee());
        }
        // 查询6个月前的时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -5);
        params.clear();
        params.put("userId",userId);
        params.put("dateStart",calendar.getTimeInMillis());
        params.put("dateEnd",monthTime.get("start"));

        BigDecimal savedAmount = BigDecimal.valueOf(userMonthlyDataService.sum("saved_amount",params));
        JSONObject result = new JSONObject();
        result.put("savedAmount",savedAmount.add(savedAmountMonthly).toString());
        return  result;
    }




    private String formatDate(Long time) {
        return  null != time ? format.format(new Date(time)) : "";
    }


    private Long getStartTime(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, 1,0,0,0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis() ;
    }



    private Map<String,Long> getYearTime(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Map<String,Long> result = new HashMap<>();
        result.put("start",calendar.getTimeInMillis());
        calendar.add(Calendar.YEAR, 1);
        result.put("end", calendar.getTimeInMillis()-1);
        return result;
    }

    private Map<String,Long> getMonthTime(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Map<String,Long> result = new HashMap<>();
        result.put("start",calendar.getTimeInMillis());
        calendar.add(Calendar.MONTH, 1);
        result.put("end", calendar.getTimeInMillis()-1);
        return result;
    }

    @Override
    public int wholeMerchantOrderSearchCount(Map<String, Object> params) {
        return qrPayFlowDAO.wholeMerchantOrderSearchCount(params);
    }

    @Override
    public List<JSONObject> wholeMerchantOrderSearch(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<JSONObject> list = qrPayFlowDAO.wholeMerchantOrderSearch(params);
        if (list != null &&  !list.isEmpty()) {
            BigDecimal cal = new BigDecimal("1");
            for (JSONObject data : list) {
                if (data.getBigDecimal("wholeSalesDiscount") != null) {
                    data.put("wholeSalesDiscount", MathUtils.multiply(data.getBigDecimal("wholeSalesDiscount"), new BigDecimal("100")));
                }
                data.put("payAmount", data.getBigDecimal("transAmount").subtract(data.getBigDecimal("transAmount").multiply(data.getBigDecimal("orderWholeSalesDiscount")).setScale(2,BigDecimal.ROUND_HALF_UP)));
            }
        }
        return list;
    }

    @Override
    public int countDistinctMerchant(Map<String, Object> params) {
        return qrPayFlowDAO.countDistinctMerchant(params);
    }

    @Override
    public List<OrderReportDTO> getOrderReport(Map<String, Object> params, PagingContext pc) {
        params.put("pc", pc);
        List<OrderReportDTO> reportDTOList = qrPayFlowDAO.getOrderReport(params);
        if (reportDTOList == null || reportDTOList.size() == 0) {
            return new ArrayList<>();
        }

        // 查询账户系统获取账户余额
        JSONArray requestArray = new JSONArray();
        reportDTOList.forEach(item -> {
            JSONObject oneData = new JSONObject();
            oneData.put("userId", item.getRecUserId());
            requestArray.add(oneData);
        });
        JSONArray resultData = new JSONArray();
        JSONObject requestData = new JSONObject();
        requestData.put("list", requestArray);
        requestData.put("type", 1);
        try {
            resultData = serverService.getSubAccountBalanceList(requestData);
        } catch (Exception e) {
            log.error("query account balance error, params: {}, error message: {}", requestData, e.getMessage());
        }
        for (OrderReportDTO orderReportDTO : reportDTOList) {
            for (Object datum : resultData) {
                JSONObject obj = (JSONObject) datum;
                if (orderReportDTO.getRecUserId() != null && orderReportDTO.getRecUserId().equals(obj.getLong("userId"))) {
                    BigDecimal balance = obj.getBigDecimal("balance");
                    if (balance != null) {
                        orderReportDTO.setMerchantRemainingWholeSalesAmount(balance);
                    }
                    break;
                }
            }
        }

        return reportDTOList;
    }

    @Override
    public JSONObject getOrderReportTotal(Map<String, Object> params, PagingContext pc) {
        JSONObject result = new JSONObject();
        List<OrderReportDTO> list = this.getOrderReport(params, pc);
        // 计算汇总数据
        result.put("list", list);
        OrderReportDTO totalDTO = this.calculateTotal(list);
        List<OrderReportDTO> totalList = new ArrayList<>();
        totalList.add(totalDTO);
        result.put("total", totalList);
        return result;
    }

    private OrderReportDTO calculateTotal(List<OrderReportDTO> reportDTOList) {
        BigDecimal zero = BigDecimal.ZERO;
        OrderReportDTO totalDTO = new OrderReportDTO(null, "", null, 0, zero, 0, zero, 0, zero,
                zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero,null);
        BigDecimal redAmountTotal = BigDecimal.ZERO;
        for (OrderReportDTO orderReportDTO : reportDTOList) {
            totalDTO.setBasicSalesOrderQuantity(totalDTO.getBasicSalesOrderQuantity() + orderReportDTO.getBasicSalesOrderQuantity());
            totalDTO.setBasicSalesOrderAmount(totalDTO.getBasicSalesOrderAmount().add(orderReportDTO.getBasicSalesOrderAmount()));
            totalDTO.setWholeSalesOrderQuantity(totalDTO.getWholeSalesOrderQuantity() + orderReportDTO.getWholeSalesOrderQuantity());
            totalDTO.setWholeSalesOrderAmount(totalDTO.getWholeSalesOrderAmount().add(orderReportDTO.getWholeSalesOrderAmount()));
            totalDTO.setMixSalesOrderQuantity(totalDTO.getMixSalesOrderQuantity() + orderReportDTO.getMixSalesOrderQuantity());
            totalDTO.setMixSalesOrderAmount(totalDTO.getMixSalesOrderAmount().add(orderReportDTO.getMixSalesOrderAmount()));
            totalDTO.setRedEnvelopeAmount(totalDTO.getRedEnvelopeAmount().add(orderReportDTO.getRedEnvelopeAmount()));
            redAmountTotal = redAmountTotal.add(orderReportDTO.getRedEnvelopeAmount());
            totalDTO.setBasicSalesOrderAmountByCard(totalDTO.getBasicSalesOrderAmountByCard().add(orderReportDTO.getBasicSalesOrderAmountByCard()));
            totalDTO.setBasicSalesCustomerActualPaymentAmountByCard(totalDTO.getBasicSalesCustomerActualPaymentAmountByCard().add(orderReportDTO.getBasicSalesCustomerActualPaymentAmountByCard()));
            totalDTO.setBasicSalesNeedSettledAmountByCard(totalDTO.getBasicSalesNeedSettledAmountByCard().add(orderReportDTO.getBasicSalesNeedSettledAmountByCard()));
            totalDTO.setBasicSalesOrderAmountByInstalment(totalDTO.getBasicSalesOrderAmountByInstalment().add(orderReportDTO.getBasicSalesOrderAmountByInstalment()));
            totalDTO.setBasicSalesCustomerActualPaymentAmountByInstalment(totalDTO.getBasicSalesCustomerActualPaymentAmountByInstalment().add(orderReportDTO.getBasicSalesCustomerActualPaymentAmountByInstalment()));
            totalDTO.setBasicSalesNeedSettledAmountByInstalment(totalDTO.getBasicSalesNeedSettledAmountByInstalment().add(orderReportDTO.getBasicSalesNeedSettledAmountByInstalment()));
            totalDTO.setWholeSalesOrderAmountByCard(totalDTO.getWholeSalesOrderAmountByCard().add(orderReportDTO.getWholeSalesOrderAmountByCard()));
            totalDTO.setWholeSalesCustomerActualPaymentAmountByCard(totalDTO.getWholeSalesCustomerActualPaymentAmountByCard().add(orderReportDTO.getWholeSalesCustomerActualPaymentAmountByCard()));
            totalDTO.setWholeSalesOrderAmountByInstalment(totalDTO.getWholeSalesOrderAmountByInstalment().add(orderReportDTO.getWholeSalesOrderAmountByInstalment()));
            totalDTO.setWholeSalesCustomerActualPaymentAmountByInstalment(totalDTO.getWholeSalesCustomerActualPaymentAmountByInstalment().add(orderReportDTO.getWholeSalesCustomerActualPaymentAmountByInstalment()));
            totalDTO.setMixSalesOrderAmountByCard(totalDTO.getMixSalesOrderAmountByCard().add(orderReportDTO.getMixSalesOrderAmountByCard()));
            totalDTO.setMixSalesCustomerActualPaymentAmountByCard(totalDTO.getMixSalesCustomerActualPaymentAmountByCard().add(orderReportDTO.getMixSalesCustomerActualPaymentAmountByCard()));
            totalDTO.setMixSalesNeedSettledAmountByCard(totalDTO.getMixSalesNeedSettledAmountByCard().add(orderReportDTO.getMixSalesNeedSettledAmountByCard()));
            totalDTO.setMixSalesOrderAmountByInstalment(totalDTO.getMixSalesOrderAmountByInstalment().add(orderReportDTO.getMixSalesOrderAmountByInstalment()));
            totalDTO.setMixSalesCustomerActualPaymentAmountByInstalment(totalDTO.getMixSalesCustomerActualPaymentAmountByInstalment().add(orderReportDTO.getMixSalesCustomerActualPaymentAmountByInstalment()));
            totalDTO.setMixSalesNeedSettledAmountByInstalment(totalDTO.getMixSalesNeedSettledAmountByInstalment().add(orderReportDTO.getMixSalesNeedSettledAmountByInstalment()));
            totalDTO.setMerchantTotalWholeSalesAmount(totalDTO.getMerchantTotalWholeSalesAmount().add(orderReportDTO.getMerchantTotalWholeSalesAmount()));
            totalDTO.setMerchantRemainingWholeSalesAmount(totalDTO.getMerchantRemainingWholeSalesAmount().add(orderReportDTO.getMerchantRemainingWholeSalesAmount()));
            totalDTO.setTodayMerchantWholeSalesAmount(totalDTO.getTodayMerchantWholeSalesAmount().add(orderReportDTO.getTodayMerchantWholeSalesAmount()));
            totalDTO.setCity(orderReportDTO.getCity());
        }
        totalDTO.setRedEnvelopeAmount(redAmountTotal);
        totalDTO.setMerchantName(reportDTOList.size() + "");
        return totalDTO;
    }

    @Override
    public int countPayFlowByCard(Map<String, Object> params) {
        return qrPayFlowDAO.countPayFlowByCard(params);
    }

    @Override
    public List<QrPayFlowCardPayDTO> getPayFlowByCard(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        this.getUnionParams(params, scs, pc);
        List<QrPayFlowCardPayDTO> payFlowList = qrPayFlowDAO.getPayFlowByCard(params);
        if (payFlowList == null || payFlowList.size() == 0) {
            return new ArrayList<>();
        }

        // 查询账户系统获取用户姓名
//        this.getUserNameByIdList(payFlowList);

        return payFlowList;
    }

    private <P extends QrPayFlowCardPayDTO> void getUserNameByIdList(List<P> payFlowList) {
        JSONArray requestArray = new JSONArray();
        payFlowList.forEach(item -> {
            JSONObject oneData = new JSONObject();
            oneData.put("userId", item.getPayUserId());
            requestArray.add(oneData);
        });
        JSONArray resultData = new JSONArray();
        JSONObject requestData = new JSONObject();
        requestData.put("list", requestArray);
        try {
            resultData = serverService.getUsernameList(requestData);
        } catch (Exception e) {
            log.error("query account username error, params: {}, error message: {}", requestData, e.getMessage());
        }
        for (QrPayFlowCardPayDTO qrPayFlowCardPayDTO : payFlowList) {
            for (Object datum : resultData) {
                JSONObject obj = (JSONObject) datum;
                if (qrPayFlowCardPayDTO.getPayUserId() != null && qrPayFlowCardPayDTO.getPayUserId().equals(obj.getLong("userId"))) {
                    qrPayFlowCardPayDTO.setUserName(obj.getString("userName"));
                    break;
                }
            }
        }
    }

    @Override
    public int countPayFlowByInstalment(Map<String, Object> params) {
        return qrPayFlowDAO.countPayFlowByInstalment(params);
    }

    @Override
    public int countPayFlowByInstalmenth5(Map<String, Object> params) {
        return qrPayFlowDAO.countPayFlowByInstalmenth5(params);
    }

    @Override
    public List<QrPayFlowInstalmentPayDTO> getPayFlowByInstalment(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, int isExcel) {
        this.getUnionParams(params, scs, pc);
        List<QrPayFlowInstalmentPayDTO> payFlowList = qrPayFlowDAO.getPayFlowByInstalment(params);
        if (payFlowList == null || payFlowList.size() == 0) {
            return new ArrayList<>();
        }
        // 查询账户系统获取用户姓名
//        this.getUserNameByIdList(payFlowList);
        // 查询分期付系统获取分期付信息
        this.getInstalmentInfo(payFlowList, isExcel);

        return payFlowList;
    }

    @Override
    public List<QrPayFlowInstalmentPayDTO> getPayFlowByInstalmenth5(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, int isExcel) {
        this.getUnionParams(params, scs, pc);
        List<QrPayFlowInstalmentPayDTO> payFlowList = qrPayFlowDAO.getPayFlowByInstalmenth5(params);
        if (payFlowList == null || payFlowList.size() == 0) {
            return new ArrayList<>();
        }
        this.getInstalmentInfo(payFlowList, isExcel);

        return payFlowList;
    }

    private void getInstalmentInfo(List<QrPayFlowInstalmentPayDTO> payFlowList, int isExcel) {
        Set<Long> borrowIdSet = new HashSet<>();
        payFlowList.forEach(item -> borrowIdSet.add(item.getCreditOrderNo()));
        List<Long> borrowIdList = new ArrayList<>(borrowIdSet);
        JSONArray borrowList = new JSONArray();
        try {
            borrowList = serverService.getBorrowList(borrowIdList, isExcel);
        } catch (Exception e) {
            log.error("get borrow info error, params: {}, error: {}", borrowIdList, e.getMessage());
        }
        for (QrPayFlowInstalmentPayDTO qrPayFlowDTO : payFlowList) {
            for (Object datum : borrowList) {
                JSONObject obj = (JSONObject) datum;
                if (qrPayFlowDTO.getCreditOrderNo() != null && qrPayFlowDTO.getCreditOrderNo().equals(obj.getLong("id"))) {
                    // 分期笔数
                    qrPayFlowDTO.setPeriodQuantity(obj.getInteger("periodQuantity"));
                    // 还款状态
                    qrPayFlowDTO.setRepayState(obj.getInteger("state"));
                    if (isExcel == 0) {
                        int paidPeriodQuantity = 0;
                        BigDecimal paidAmount = BigDecimal.ZERO;
                        JSONArray repayList = obj.getJSONArray("repayList");
                        for (Object o : repayList) {
                            JSONObject item = (JSONObject) o;
                            paidAmount = paidAmount.add(item.getBigDecimal("truelyRepayAmount"));
                            if (item.getInteger("state") == 1) {
                                paidPeriodQuantity += 1;
                            }
                        }
                        // 已还款期数
                        qrPayFlowDTO.setPaidPeriodQuantity(paidPeriodQuantity);
                        // 已还款金额
                        qrPayFlowDTO.setPaidAmount(paidAmount);
                        // 还款计划明细
                        qrPayFlowDTO.setRepayPlan(obj.getJSONArray("repayList"));
                    } else {
                        qrPayFlowDTO.setPaidPeriodQuantity(obj.getInteger("totalTerm"));
                        qrPayFlowDTO.setPaidAmount(obj.getBigDecimal("totalPayed"));
                    }
                    break;
                }
            }
        }
    }


    /**
     * 新用户2个周后还未消费  发送邮件 站内信
     *
     * @author zhangzeyuan
     * @date 2021/5/13 13:41
     */
    @Override
    public void sendMsgUserTwoWeeksNoTransaction() throws Exception {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date zero = calendar.getTime();

        List<UserDTO> noTransactionUserListByTwoWeeksAgo = qrPayFlowDAO.getNoTransactionUserListByTwoWeeksAgo(zero.getTime());
        if(CollectionUtils.isEmpty(noTransactionUserListByTwoWeeksAgo)){
            return;
        }

        //站内信模板
        MailTemplateDTO noticeTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_34.getCode()));

        String noticeTitle = noticeTemplate.getEnMailTheme();
        String noticeContent = noticeTemplate.getEnSendContent();

        //邮件模板
        MailTemplateDTO mailTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_33.getCode()));

        String mailContent = mailTemplate.getEnSendContent();
        String mailTitle = mailTemplate.getEnMailTheme();

        //待保存notice消息实体
        List<Notice> saveNoticeList = new ArrayList<>(noTransactionUserListByTwoWeeksAgo.size());
        //待保存邮件记录
        List<MailLog> mailLogList = new ArrayList<MailLog>(noTransactionUserListByTwoWeeksAgo.size());

        JSONObject tempJsonObj = new JSONObject();

        for(UserDTO user : noTransactionUserListByTwoWeeksAgo){
            //qld only 只发QLD的
            tempJsonObj.clear();
            tempJsonObj.put("userId", user.getId().toString());
            JSONObject userInfo = serverService.findOneUserInfo(tempJsonObj);
            tempJsonObj.put("postcode",userInfo.getString("postcode"));
            JSONObject stateJson = FindStateByPostCodeUtils.processStateInfo(tempJsonObj);
            String stateName = stateJson.getString("state");
            if (!"QLD".equals(stateName)){
                continue;
            }

            //封装站内信发送信息
            Notice saveRecord = new Notice();
            saveRecord.setIsRead(0);
            saveRecord.setUserId(user.getId());
            saveRecord.setTitle(noticeTitle);
            saveRecord.setContent(noticeContent);
            saveRecord.setId(SnowflakeUtil.generateId());
            saveRecord.setStatus(1);
            saveRecord.setCreatedBy(1L);
            saveRecord.setCreatedDate(System.currentTimeMillis());
            saveNoticeList.add(saveRecord);

            if(StringUtils.isBlank(user.getEmail())){
                continue;
            }

            //发送邮件
            try {
                //发送邮件
                Session session = MailUtil.getSession(sysEmail);
                MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, user.getEmail(), mailTitle, mailContent, null, session);
                MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);

                //记录邮件流水
                MailLog saveMailLogRecord = new MailLog();
                saveMailLogRecord.setAddress(user.getEmail());
                saveMailLogRecord.setContent(mailContent);
                saveMailLogRecord.setSendType(0);
                saveMailLogRecord.setIp("0");
                saveMailLogRecord.setCreatedBy(0L);
                saveMailLogRecord.setCreatedDate(System.currentTimeMillis());
                saveMailLogRecord.setId(SnowflakeUtil.generateId());
                mailLogList.add(saveMailLogRecord);
            }catch (Exception e){
                log.error("有过交易但一一个月已上未有交易   发送邮件出错， user：" + user.getId());
            }
        }


        if(CollectionUtils.isNotEmpty(saveNoticeList)){
            noticeService.saveNoticeList(saveNoticeList, null);
        }

        if(CollectionUtils.isNotEmpty(mailLogList)){
            mailLogService.saveMailLogList(mailLogList, null);
        }

    }


    /**
     * 有过交易但一一个月已上未有交易 发送邮件
     *
     * @author zhangzeyuan
     * @date 2021/5/13 13:43
     */
    @Override
    public void sendMailOneMonthNoTransaction() throws BizException {
        //是否执行
        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
        paramMap.put("code", "AMonthNoTrans");
        StaticDataDTO staticData = staticDataService.findOneStaticData(paramMap);
        if(null != staticData && staticData.getValue().equals("1")){
            return;
        }

        //获取三十天前的0点和12点时间戳
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date endTime = calendar.getTime();

        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -31);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startTime = calendar.getTime();

        //查询一个月内没有消费的用户
        List<UserDTO> userList = qrPayFlowDAO.getAMonthNoTransactionUserList(startTime.getTime(), endTime.getTime());

        if(CollectionUtils.isEmpty(userList)){
            return;
        }

        //获取邮件模板
        MailTemplateDTO mailTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_32.getCode()));
        if(Objects.isNull(mailTemplate) || StringUtils.isBlank(mailTemplate.getEnSendContent())){
            log.error("有过交易但一一个月已上未有交易 获取邮件模板出错");
            return;
        }
        //内容 标题
        String enSendContent = mailTemplate.getEnSendContent();
        String enMailTheme = mailTemplate.getEnMailTheme();

        //待保存邮件记录
        List<MailLog> mailLogList = new ArrayList<MailLog>(userList.size());

        for(UserDTO user : userList){
            //用户邮箱
            String email = user.getEmail();
            if(StringUtils.isBlank(email)){
                continue;
            }

            if(StringUtils.isBlank(user.getUserFirstName()) && StringUtils.isBlank(user.getUserLastName())){
                continue;
            }

            //姓名
            String firstName = StringUtils.isBlank(user.getUserFirstName()) ? "" : user.getUserFirstName();
            String lastName = StringUtils.isBlank(user.getUserLastName()) ? "" : user.getUserLastName();
            String userName = firstName +  " " + lastName;

            String sendContentCopy = enSendContent;
            //发送内容
//            String realSendContent = templateContentReplace(contentParam, sendContentCopy);
            String realSendContent = sendContentCopy.replace("{0}", userName);

            try {
                //发送邮件
                Session session = MailUtil.getSession(sysEmail);
                MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, email, enMailTheme, realSendContent, null, session);
                MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);

                //记录邮件流水
                MailLog saveMailLogRecord = new MailLog();
                saveMailLogRecord.setAddress(email);
                saveMailLogRecord.setContent(realSendContent);
                saveMailLogRecord.setSendType(0);
                saveMailLogRecord.setIp("0");
                saveMailLogRecord.setCreatedBy(0L);
                saveMailLogRecord.setCreatedDate(System.currentTimeMillis());
                mailLogList.add(saveMailLogRecord);
            }catch (Exception e){
                log.error("有过交易但一一个月已上未有交易   发送邮件出错， user：" + user.getId());
            }

        }

        if(CollectionUtils.isNotEmpty(mailLogList)){
            mailLogService.saveMailLogList(mailLogList, null);
        }
    }


    public String templateContentReplace(Object[] replaceContentParams, String content) {
        if (replaceContentParams != null && replaceContentParams.length > 0) {
            content = MessageFormat.format(content, replaceContentParams);
        }
        return content;
    }


    /**
     * 查询三方交易可疑流水
     *
     * @return
     */
    @Override
    public List<QrPayFlowDTO> getSuspiciousOrderFlowList() {
        return qrPayFlowDAO.getSuspiciousOrderFlowList();
    }

    /**
     * 查询用户支付成功订单数
     *
     * @param userId
     * @return java.lang.Long
     * @author zhangzeyuan
     * @date 2021/8/26 10:01
     */
    @Override
    public Integer countPaidSuccessByUserId(Long userId, Integer transType) {
        return qrPayFlowDAO.countPaidSuccessByUserId(userId, transType);
    }

    @Override
    public int updateRefundData(Map<String, Object> params, HttpServletRequest request) {
        if(request != null){
            params.put("ip", getIp(request)) ;
            params.put("createdBy",getUserId(request));
        }

        return qrPayFlowDAO.updateRefundData(params);
    }

    @Override
    public int countH5MerchantClearList(Map<String, Object> params) {
        Integer i = qrPayFlowDAO.countH5MerchantClearList(params);
        return i==null?0:i;
    }

    @Override
    public void addH5ClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) {
        Map<String, Object> updateMap = new HashMap<>();
        int[] stateList = {31,104};
        updateMap.put("isNeedClear", "1");
        updateMap.put("orgClearState", "0");
        updateMap.put("clearState", "2");
        updateMap.put("stateList", stateList);
        updateMap.put("start", params.get("start"));
        updateMap.put("end", params.get("end"));
        updateMap.put("batchId",clearBatchDTO.getId());
        updateMap.put("orderSource",StaticDataEnum.ORDER_SOURCE_1.getCode());

        // 新版本清算交易
        updateMap.put("merchantIdList",params.get("merchantIdList"));
        qrPayFlowDAO.addQrPayClearBatchIdNew(updateMap);

    }

    @Override
    public void addClearDetailFlow(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("state", "0");
        updateMap.put("batchId",clearBatchDTO.getId());
        //查询要清算的交易列表，得到应清算金额和交易信息
        List<ClearDetailDTO> clearList = clearDetailService.getH5MerchantClearDetail(updateMap);
        for(ClearDetailDTO clearDetailDTO:clearList ){
            clearDetailDTO.setModifiedBy(clearBatchDTO.getModifiedBy());
            clearDetailDTO.setCreatedBy(clearBatchDTO.getCreatedBy());
            clearDetailService.saveClearDetail(clearDetailDTO,request);
        }
    }

    @Override
    public int getApiMerchantClearListCount(Map<String, Object> params) {
        return qrPayFlowDAO.getApiMerchantClearListCount(params);
    }
    /**
     * 用户详情订单数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/13 16:09
     */
    @Override
    public int countUserDetail(Map<String, Object> params) {
        return qrPayFlowDAO.countUserDetail(params);
    }

    /**
     * 用户详情订单列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.QrPayFlowDTO>
     * @author zhangzeyuan
     * @date 2021/9/13 16:10
     */
    @Override
    public List<UserDetailPayOrderDTO> listUserDetailOrder(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return qrPayFlowDAO.listUserDetailOrder(params);
    }

    @Override
    public QrPayFlowDTO findMaxUserUseById(JSONObject result, HttpServletRequest request) {
        QrPayFlowDTO qrPayFlowDTO=qrPayFlowDAO.findMaxUserUseById(result);
        return qrPayFlowDTO;
    }

    @Override
    public int apiMerchantUnclearDetailCount(Map<String, Object> params) {
        return qrPayFlowDAO.apiMerchantUnclearDetailCount(params);
    }

    @Override
    public List<QrPayFlowDTO> apiMerchantUnclearDetailList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return qrPayFlowDAO.apiMerchantUnclearDetailList(params);
    }


    @Override
    public List<Map<String, Object>> getApiMerchantClearList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, HttpServletRequest request) {
        params = getUnionParams(params, scs, pc);
        // 查询商户信息
        List<ApiMerchantDTO> merchantList = apiMerchantService.getApiClearMerchantList(params);
        List<Long> merchantIdList = new ArrayList<>();
        for(ApiMerchantDTO merchantDTO : merchantList){
            merchantIdList.add(merchantDTO.getId());
        }

        params.put("merchantIdList",merchantIdList);
        // 查询商户的所有订单信息
        List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowDAO.apiMerchantClearMessageList(params);

        //查询商户退款数据
        List<RefundOrderDTO> refundOrderDTOS = refundService.merchantClearMessageList(params);

        List<Map<String ,Object>> resultList = new ArrayList<>();
        // 处理数据
        for(ApiMerchantDTO merchantDTO : merchantList){

            // 一条数据信息
            Map<String ,Object> oneResult = new HashMap<>(16);
            // 商户信息
            oneResult.put("merchantId",merchantDTO.getId().toString());
            oneResult.put("merchantName",merchantDTO.getPracticalName());
            oneResult.put("merchantState",merchantDTO.getState());
            oneResult.put("abn",merchantDTO.getAbn());
            oneResult.put("city",merchantDTO.getCity());
            if(qrPayFlowDTOS != null && qrPayFlowDTOS.size() > 0){
                // 筛选商户所有订单
                List<QrPayFlowDTO> merchantData = qrPayFlowDTOS.stream().filter(flow->flow.getMerchantId().equals(merchantDTO.getId())).collect(Collectors.toList());
                if(merchantData != null && merchantData.size() > 0){
                    oneResult.put("totalNumber",merchantData.size());
                    BigDecimal totalAmount = merchantData.stream().map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                    oneResult.put("totalAmount",totalAmount);
                    // 筛选未结算订单
                    List<QrPayFlowDTO> unclearedData = merchantData.stream().filter(flow->flow.getClearState() == StaticDataEnum.CLEAR_STATE_TYPE_0.getCode()).collect(Collectors.toList());
                    if(unclearedData != null && unclearedData.size() >0){
                        oneResult.put("unclearedNumber",unclearedData.size());
                        BigDecimal unclearedAmount = unclearedData.stream().map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                        oneResult.put("unclearedAmount",unclearedAmount);
                    }else{
                        oneResult.put("unclearedNumber",0);
                        oneResult.put("unclearedAmount",BigDecimal.ZERO);
                    }
                    // 筛选已结算订单
                    List<QrPayFlowDTO> clearedData = merchantData.stream().filter(flow->flow.getClearState() == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode()).collect(Collectors.toList());
                    if(clearedData != null && clearedData.size() >0){
                        oneResult.put("clearedNumber",clearedData.size());
                        BigDecimal clearedAmount = clearedData.stream().map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                        oneResult.put("clearedAmount",clearedAmount);
                    }else{
                        oneResult.put("clearedNumber",0);
                        oneResult.put("clearedAmount",BigDecimal.ZERO);
                    }
                    // 筛选延迟结算订单
                    List<QrPayFlowDTO> delayClearData = merchantData.stream().filter(flow->flow.getClearState() == StaticDataEnum.CLEAR_STATE_TYPE_3.getCode()).collect(Collectors.toList());
                    if(delayClearData != null && delayClearData.size() >0){
                        oneResult.put("delayClearNumber",delayClearData.size());
                        BigDecimal delayClearAmount = delayClearData.stream().map(QrPayFlowDTO::getRecAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                        oneResult.put("delayClearAmount",delayClearAmount);
                    }else{
                        oneResult.put("delayClearNumber",0);
                        oneResult.put("delayClearAmount",BigDecimal.ZERO);
                    }
                }else{
                    oneResult.put("totalNumber",0);
                    oneResult.put("totalAmount",BigDecimal.ZERO);
                    oneResult.put("unclearedNumber",0);
                    oneResult.put("unclearedAmount",BigDecimal.ZERO);
                    oneResult.put("clearedNumber",0);
                    oneResult.put("clearedAmount",BigDecimal.ZERO);
                    oneResult.put("delayClearNumber",0);
                    oneResult.put("delayClearAmount",BigDecimal.ZERO);
                }
            }else{
                oneResult.put("totalNumber",0);
                oneResult.put("totalAmount",BigDecimal.ZERO);
                oneResult.put("unclearedNumber",0);
                oneResult.put("unclearedAmount",BigDecimal.ZERO);
                oneResult.put("clearedNumber",0);
                oneResult.put("clearedAmount",BigDecimal.ZERO);
                oneResult.put("delayClearNumber",0);
                oneResult.put("delayClearAmount",BigDecimal.ZERO);
            }

            if(refundOrderDTOS != null && refundOrderDTOS.size() > 0 ){
                List<RefundOrderDTO >  merchantClearedData = refundOrderDTOS.stream().filter(flow->flow.getMerchantId().equals(merchantDTO.getId()) && flow.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode()).collect(Collectors.toList());

                if(merchantClearedData != null && merchantClearedData.size() > 0){
                    oneResult.put("refundClearedNumber",merchantClearedData.size());
                    BigDecimal refundAmount = merchantClearedData.stream().map(RefundOrderDTO::getRefundAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                    oneResult.put("refundClearedAmount",refundAmount);
                }else{
                    oneResult.put("refundClearedNumber",0);
                    oneResult.put("refundClearedAmount",BigDecimal.ZERO);
                }
                List<RefundOrderDTO >  merchantUnClearedData = refundOrderDTOS.stream().filter(flow->flow.getMerchantId().equals(merchantDTO.getId()) && flow.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_0.getCode()).collect(Collectors.toList());

                if(merchantUnClearedData != null && merchantUnClearedData.size() > 0){
                    oneResult.put("refundUnClearedNumber",merchantUnClearedData.size());
                    BigDecimal refundAmount = merchantUnClearedData.stream().map(RefundOrderDTO::getRefundAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                    oneResult.put("refundUnClearedAmount",refundAmount);
                }else{
                    oneResult.put("refundUnClearedNumber",0);
                    oneResult.put("refundUnClearedAmount",BigDecimal.ZERO);
                }
            }else{
                oneResult.put("refundClearedNumber",0);
                oneResult.put("refundClearedAmount",BigDecimal.ZERO);
                oneResult.put("refundUnClearedNumber",0);
                oneResult.put("refundUnClearedAmount",BigDecimal.ZERO);
            }
            resultList.add(oneResult);
        }
        return resultList;
    }

    @Override
    public List<QrPayFlowDTO> h5ClearTransDetail(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<QrPayFlowDTO> list = qrPayFlowDAO.h5ClearTransDetail(params);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class )
    public void h5MerchantSettleFail(ClearBatchDTO clearBatchDTO, ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws BizException {

        Map<String,Object> params = new HashMap<>();

        //流水不存在
        if(clearDetailDTO == null || clearDetailDTO.getId() == null ){
            log.info("h5MerchantSettleFail detail id:"+clearDetailDTO.getId()+"flow is not cleared");
        }

        // 当前时间
        Long now = System.currentTimeMillis();

        params.clear();
        params.put("clearBatchId",clearDetailDTO.getClearBatchId());
        params.put("merchantId",clearDetailDTO.getMerchantId());
        List<ClearFlowDetailDTO> flowList = clearFlowDetailService.find(params,null,null);

        // 交易流水清算状态回退
        this.rollbackClearDetail2(clearDetailDTO.getClearBatchId(),clearDetailDTO.getMerchantId());
        //清算流水明细状态回退
        clearDetailDTO.setState(StaticDataEnum.CLEAR_BATCH_STATE_2.getCode());
        clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);

        // 退款路上清算状态回退
        refundService.rollbackSettlement(clearDetailDTO.getClearBatchId(),clearDetailDTO.getMerchantId(),request);

        //清算明细状态回退
        params.clear();
        params.put("modifiedDate",now);
        params.put("clearBatchId",clearDetailDTO.getClearBatchId());
        params.put("merchantId",clearDetailDTO.getMerchantId());
        if(request != null) {
            params.put("modifiedDate",getUserId(request));
            params.put("ip",getIp(request));
        }

        clearFlowDetailService.updateClearBatchToFail(params);

        //清算批次总金额修改
        clearDetailDTO.setModifiedDate(now);
        //查询清算成功的总比数总金额
        params.clear();
        params.put("clearBatchId",clearDetailDTO.getClearBatchId());
        ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailService.clearTotal(params);
        ClearBatchDTO updateData = new ClearBatchDTO();
        clearBatchDTO.setTotalNumber(clearFlowDetailDTO.getCount().longValue());

        ClearDetailDTO  clearDetailDTO1 = clearDetailService.clearTotal(params);

        updateData.setTotalAmount(clearDetailDTO1 == null ? BigDecimal.ZERO :clearDetailDTO1.getTransAmount());
        updateData.setBorrowAmount(clearDetailDTO1 == null ? BigDecimal.ZERO :clearDetailDTO1.getBorrowAmount());
        updateData.setClearAmount(clearDetailDTO1 == null ? BigDecimal.ZERO :clearDetailDTO1.getClearAmount());

        clearBatchService.updateClearBatch(clearDetailDTO.getClearBatchId(),updateData,request);


    }

    private int rollbackClearDetail2(Long clearBatchId, Long merchantId) {
        Long now = System.currentTimeMillis();
        return qrPayFlowDAO.rollbackClearDetail2(clearBatchId,merchantId,now);
    }

}


