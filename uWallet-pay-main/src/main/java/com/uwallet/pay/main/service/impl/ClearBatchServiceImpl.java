package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.ResultCode;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.FormatUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.ClearBatchDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ClearBatch;
import com.uwallet.pay.main.model.entity.ClearDetail;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.MailUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * <p>
 * 清算表生成
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 清算表生成
 * @author: zhoutt
 * @date: Created in 2019-12-20 10:49:55
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class ClearBatchServiceImpl extends BaseServiceImpl implements ClearBatchService {

    @Autowired
    private ClearBatchDAO clearBatchDAO;

    @Autowired
    private ClearDetailService clearDetailService;

    @Autowired
    private ServerService accountService;

    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    @Lazy
    private QrPayFlowService qrPayFlowService;
    @Autowired
    @Lazy
    private QrPayService qrPayService;
    @Value("${spring.clearFilePath}")
    private String filePath;

    @Value("${uWallet.sysEmail}")
    private String sysEmail;

    @Value("${uWallet.sysEmailPwd}")
    private String sysEmailPwd;
    @Value("${omipay.bsb}")
    private String OmiPayBsb;
    @Value("${omipay.accountName}")
    private String OmiPayAccountName;
    @Value("${omipay.accountNo}")
    private String OmiPayAccountNo;
    @Value("${omipay.apca}")
    private String OmiPayApca;
    @Value("${omipay.abbreviation}")
    private String OmiPayAbbreviation;
    /**
     * omipay清算文件发送邮箱
     */
    private static final String SEND_EMAIL = "staff@ijcapital.com.au";
    /**
     * omipay清算文件发送邮箱密码
     */
    private static final String SEND_EMAIL_PASSWORD = "IJcapital1";
    /**
     * omipay清算文件接收邮箱
     */
    private static final String SETTLEMENT_FILE_SEND_TO_EMAIL = "account@omipay.com.au";
    /**
     * omipay清算文件抄送邮箱
     */
    private static final String COPY_TO_EMAIL = "daniel@omipay.com.au";

    @Autowired
    private CSVService cSVService;

    @Autowired
    private ClearFlowDetailService clearFlowDetailService;

    @Autowired
    private RefundFlowService refundFlowService;

    @Autowired
    private ServerService serverService;

    /**
     * 自动适配文件路径分隔符
     */
    public static final String SEPARATOR = File.separator;
    /**
     * 邮件模板
     */
    private MailTemplateService mailTemplateService;

    @Autowired
    @Lazy
    private MerchantService merchantService;
    @Autowired
    private  WholeSalesFlowService wholeSalesFlowService;
    @Autowired
    private CreditWebService creditWebService;
    @Resource
    private DonationFlowService donationFlowService;
    @Resource
    private TipFlowService tipFlowService;
    @Autowired
    private RefundService refundService;

    @Override
    public Long saveClearBatch(@NonNull ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        ClearBatch clearBatch = BeanUtil.copyProperties(clearBatchDTO, new ClearBatch());
        if (clearBatchDTO.getCreatedBy() == null) {
            clearBatch =(ClearBatch) this.packAddBaseProps(clearBatch, request);
        }else{
            long now = System.currentTimeMillis();
            clearBatch.setId(SnowflakeUtil.generateId());
            clearBatch.setCreatedDate(now);
            clearBatch.setModifiedDate(now);
            clearBatch.setStatus(1);
            clearBatch.setCreatedBy(clearBatchDTO.getCreatedBy());
            clearBatch.setModifiedBy(clearBatchDTO.getModifiedBy());
        }
        log.info("save ClearBatch:{}", clearBatch);
        if (clearBatchDAO.insert(clearBatch) != 1) {
            log.error("insert error, data:{}", clearBatch);
            throw new BizException("Insert clearBatch Error!");
        }
        return clearBatch.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveClearBatchList(@NonNull List<ClearBatch> clearBatchList, HttpServletRequest request) throws BizException {
        if (clearBatchList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = clearBatchDAO.insertList(clearBatchList);
        if (rows != clearBatchList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, clearBatchList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateClearBatch(@NonNull Long id, @NonNull ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        log.info("full update clearBatchDTO:{}", clearBatchDTO);
        ClearBatch clearBatch = BeanUtil.copyProperties(clearBatchDTO, new ClearBatch());
        clearBatch.setId(id);
        int cnt = clearBatchDAO.update((ClearBatch) this.packModifyBaseProps(clearBatch, request));
        if (cnt != 1) {
            log.error("update error, data:{}", clearBatchDTO);
            throw new BizException("update clearBatch Error!");
        }
    }

    @Override
    public void updateClearBatchSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        clearBatchDAO.updatex(params);
    }

    @Override
    public void logicDeleteClearBatch(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = clearBatchDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteClearBatch(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = clearBatchDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ClearBatchDTO findClearBatchById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ClearBatchDTO clearBatchDTO = clearBatchDAO.selectOneDTO(params);
        return clearBatchDTO;
    }

    @Override
    public ClearBatchDTO findOneClearBatch(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ClearBatch clearBatch = clearBatchDAO.selectOne(params);
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        if (null != clearBatch) {
            BeanUtils.copyProperties(clearBatch, clearBatchDTO);
        }
        return clearBatchDTO;
    }

    @Override
    public List<ClearBatchDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ClearBatchDTO> resultList = clearBatchDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return clearBatchDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return clearBatchDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = clearBatchDAO.groupCount(conditions);
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
        return clearBatchDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = clearBatchDAO.groupSum(conditions);
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
    public ClearBatchDTO clear(Long id , HttpServletRequest request, int type , HttpServletResponse response) throws  BizException{
        ClearBatchDTO clearBatchDTO = findClearBatchById(id);
        Map<String, Object> map = new HashMap<>(2);
        map.put("clearBatchId",id);
        map.put("state",0);
        List<ClearDetailDTO> detailList  = clearDetailService.find(map,null,null);
        for(ClearDetailDTO clearDetailDTO:detailList){
            //实际清算金额
            BigDecimal clearAmount = BigDecimal.ZERO;
            RefundFlowDTO refundList = new RefundFlowDTO();
            if (type == 0){
                //查询退款交易金额
                refundList = refundFlowService.getUnCleared(clearDetailDTO.getUserId(),clearBatchDTO.getGatewayId());
            }

            if(refundList != null && refundList.getNotSettlementAmount() != null && refundList.getNotSettlementAmount().compareTo(BigDecimal.ZERO)==1){
                //如果有退款金额
                //查询所有未清算金额,计算实际清算资金
                Map<String,Object> param = new HashMap<>();
                param.put("userId",clearDetailDTO.getUserId());
                param.put("gatewayId",clearBatchDTO.getGatewayId());
                List<QrPayFlowDTO> list = qrPayFlowService.merchantClearList(param,null,null);

                BigDecimal totalAmt = clearDetailDTO.getTransAmount();
                if(list!=null && list.size()>0 && list.get(0).getRecAmountTotal()!=null){
                    totalAmt = clearDetailDTO.getTransAmount().add(list.get(0).getRecAmountTotal());
                }

                if((totalAmt.subtract(clearDetailDTO.getTransAmount())).compareTo(refundList.getNotSettlementAmount())>=0){
                    //待清算总金额-要清算金额>=退款金额
                    clearAmount = clearDetailDTO.getTransAmount();
                }else{
                    //待清算总金额 -退款金额
                    BigDecimal amt =totalAmt.subtract(refundList.getNotSettlementAmount());
                    if(amt.compareTo(BigDecimal.ZERO)==1){
                        clearAmount = amt;
                    }else {
                        clearAmount = BigDecimal.ZERO;
                    }

                }

            }else{
                //无退款金额
                clearAmount = clearDetailDTO.getTransAmount();
            }
            //记录实际清算金额
            log.info("batchid:"+id);
            log.info("userid:"+clearDetailDTO.getUserId());
            log.info("clearAmount:"+clearAmount);


            clearDetailDTO.setClearAmount(clearAmount);
            clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);

            if(clearAmount.compareTo(BigDecimal.ZERO) ==0){
                doClearSuccessResult(null,clearDetailDTO,request);
                continue;
            }
            JSONObject resObj = null;
            boolean amountOutFlag = false;
            AccountFlowDTO accountFlowDTO = getAmountOutFlow(clearDetailDTO,request);
            try{
                //用户出账
                accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO,request));
                Map<String,Object> amtOutMap = new HashMap<>();
                amtOutMap.put("userId",clearDetailDTO.getUserId());
                amtOutMap.put("subAccountType",accountFlowDTO.getAccountType());
                amtOutMap.put("channel","pay");
                amtOutMap.put("channelTransType",accountFlowDTO.getTransType());
                amtOutMap.put("transAmount",clearDetailDTO.getClearAmount());
                amtOutMap.put("channelSerialnumber",accountFlowDTO.getOrderNo());
                try{
                    resObj = accountService.amountOut(JSONObject.parseObject(JSON.toJSONString(amtOutMap)));
                }catch (Exception ex){
                    log.error("批次号：{}，userId:{},出账交易可疑：",id,clearDetailDTO.getUserId(),ex.getMessage(),ex);
                    //交易可疑
                    continue;
                }
                if (resObj.getString("code").equals(ResultCode.ERROR.getCode())) {
                    //交易失败
                    doClearFailResult(accountFlowDTO,clearDetailDTO,request);
                } else if (resObj.getString("code").equals(ResultCode.OK.getCode())) {
                    //交易成功
                    amountOutFlag = true;
                    doClearSuccessResult(accountFlowDTO,clearDetailDTO,request);
                }else{
                    log.info("批次号：{}，userId:{},出账交易结果可疑：",id,clearDetailDTO.getUserId());
                    //交易可疑
                    continue;
                }
            }catch(Exception e){
                log.info("批次号：{}，userId:{},出账交易失败：",id,clearDetailDTO.getUserId(),e.getMessage(),e);

                try{
                    if(amountOutFlag){
                        //余额回滚
                        doClearRollback(accountFlowDTO,clearDetailDTO,request);
                    }else{
                        if(accountFlowDTO.getId()==null){
                            doClearFailResult(null,clearDetailDTO,request);
                        }else{
                            doClearFailResult(accountFlowDTO,clearDetailDTO,request);
                        }
                    }
                }catch (Exception ex){
                    log.info("批次号：{}，userId:{},出账交易回滚异常：",id,clearDetailDTO.getUserId(),ex.getMessage(),ex);
                }
            }finally {
                continue;
            }
        }

        //查询清算成功的总比数总金额
        Map<String,Object> clearMap = new HashMap<>();
        clearMap.put("clearBatchId",id);
        ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailService.clearTotal(clearMap);
        clearBatchDTO.setTotalNumber(clearFlowDetailDTO.getCount().longValue());

        ClearDetailDTO  clearDetailDTO = clearDetailService.clearTotal(clearMap);
        clearBatchDTO.setTotalAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getTransAmount());
        clearBatchDTO.setBorrowAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getBorrowAmount());
        clearBatchDTO.setClearAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getClearAmount());

        if(type == 1 || 0==clearBatchDTO.getGatewayId()){
            //LatPay商户清算

            //TODO 文明名未定
            String fileName = "Bill_"+id+".csv";
            log.info("开始生成平台清算Bill文件，文件名："+fileName);
            //记录清算状态
            clearBatchDTO.setFileName(fileName);

            clearBatchDTO.setUrl(filePath+fileName);
            updateClearBatch(id,clearBatchDTO,request);
            //生成文件
            List<ClearBillCSVDTO> billList = clearDetailService.getClearBillList(id);
            createClearBillFile(billList,fileName,filePath);

        }else if(1==clearBatchDTO.getGatewayId()||2==clearBatchDTO.getGatewayId()){
            //Omipay微信渠道和支付宝渠道
            //查询文件内容
            List<ClearDetailDTO> abaList = clearDetailService.getClearABAList(id);
            if(abaList==null||abaList.size()==0){
                //若无文件内容
                clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                updateClearBatch(id,clearBatchDTO,request);
                log.info("结算批次"+id+"无需要结算资金，不生成ABA文件");
                return null;
            }
            //文件名
            String date = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            String fileName = date+"_QmiPay"+".xls";

            log.info("开始生成OmiPayAba清算文件，文件名："+fileName);
            //记录清算状态
            clearBatchDTO.setFileName(fileName);
            clearBatchDTO.setUrl(filePath+fileName);
            updateClearBatch(id,clearBatchDTO,request);

            //生成ABA文件
//            createABAFile(id,abaList,fileName);
            //此处改为生成excel文件
            List<Map<String, Object>> excelList = new ArrayList<>(1);
            abaList.forEach(detailDTO -> {
                Map<String, Object> data = new HashMap<>(1);
                data.put("商户名称", detailDTO.getCorporateName());
                data.put("商户ABN", detailDTO.getAbn());
                data.put("付款银行户名", detailDTO.getAccountName());
                data.put("银行账户编号", detailDTO.getAccountNo());
                data.put("BSB编号", detailDTO.getBsb());
                data.put("金额", detailDTO.getTransAmount());
                data.put("备注", "");
                excelList.add(data);
            });
            String gatewayName = "";
            if (1 == clearBatchDTO.getGatewayId()) {
                gatewayName = "Ali";
            } else {
                gatewayName = "Wechat";
            }
            createOmiPayClearExcelFile(excelList, clearBatchDTO.getClearAmount(), fileName, gatewayName);
        }

        //更新批次状态
        clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        updateClearBatch(id,clearBatchDTO,request);
        return clearBatchDTO;
    }

    /**
     * ABA文件生成
     * @param id
     * @param abaList
     * @param fileName
     */
    private void createABAFile(Long id, List<ClearDetailDTO> abaList, String fileName) {

        // 获取文件路径
        StringBuilder sb = new StringBuilder(0);
        Date now = new Date();
        sb.append(filePath);
        sb.append(SEPARATOR);
//        sb.append(FormatUtil.getTodayTimeField(now, "yyyy/MM/dd"));
//        sb.append(SEPARATOR);
        sb.append(fileName);
        log.info("开始生成ABA文件："+sb);
        // 创建路径
        File newFile = new File(sb.toString());
        if (!newFile.exists()) {
            newFile.getParentFile().mkdirs();
            try {
                newFile.createNewFile();
            } catch (IOException e) {
               log.info(fileName+"ABA生成文件创建文件异常："+e.getMessage(),e);
            }
        }


        // header部分
        sb.delete(0, sb.length());
        //第0列：固定填0
        sb.append("0");
        //第18-19列：文档序号，通常为01
        sb.append(StringUtils.leftPad("01", 19, ' '));
        //第20-22列：银行缩写，如CBA
        sb.append(StringUtils.rightPad(OmiPayAbbreviation, 3, ' '));
        //第23-29列
        sb.append(StringUtils.leftPad("", 7, ' '));
        //第30-55列：出款银行账号名称，长度26，剩余部分留空，超长截断
        sb.append(StringUtils.rightPad(OmiPayAccountName, 26, ' '));
        //第56-61列：APCA编码，6位，不够补0
        sb.append(StringUtils.leftPad(OmiPayApca, 6, '0'));
        //第62-73列：备注，12位，剩余留空，通常填日期
        sb.append(StringUtils.rightPad(FormatUtil.getTodayTimeField(now, "yyMMddHHmmss"), 12, ' '));
        //第74-79列：日期，ddMMyy，6位
        sb.append(FormatUtil.getTodayTimeField(now, "ddMMyy"));
        //第80-120列
        sb.append(StringUtils.leftPad("", 40, ' '));
        // 换行
        sb.append("\r\n");

        // transaction部分
        // 放款总金额
        BigDecimal sum = new BigDecimal(0);
        // 放款总笔数
        DecimalFormat df = new DecimalFormat("#.00");
        for (ClearDetailDTO b : abaList) {
            //第0列：固定填1
            sb.append("1");
            //第1-7列：收款账户BSB No，000-000格式，7位
            sb.append(new StringBuffer(b.getBsb()).insert(3,"-"));
            //第8-16列：收款账户Account No，9位，右对齐，左侧不足留空
            sb.append(StringUtils.leftPad(FormatUtil.getStringFormat(b.getAccountNo(), 9),9, ' '));
            //第18-19列：固定填50
            sb.append(" 50");
            String money = df.format(b.getClearAmount());
            BigDecimal decimal = new BigDecimal(money);
            sum = sum.add(decimal);
            money = money.replace(".", "");
            //第20-29列：打款金额，整数，单位为cent，10位，右对齐，左侧位数不足补0，如$123.00应该填0000012300
            sb.append(StringUtils.leftPad(money, 10, "0"));
            //第30-61列：收款账户account name，32位，左对齐，剩余留空
            sb.append(StringUtils.rightPad(FormatUtil.getStringFormat(b.getAccountName(), 32), 32, ' '));
            //第62-79列：备注信息，会显示在商户收款记录中，18位，剩余留空
            sb.append(StringUtils.rightPad("remark", 18, ' '));
            //第80-86列：出款账户BSB No，000-000格式，7位
            sb.append(OmiPayBsb);
            //第87-95列：出款账户Account No，9位，右对齐，剩余留空
            sb.append(StringUtils.leftPad(FormatUtil.getStringFormat(OmiPayAccountNo, 9),9, ' '));
            //第96-111列：出款账户Account Name，16位，剩余留空
            sb.append(StringUtils.rightPad(OmiPayAccountName, 16, ' '));
            //第112-120列：固定8个0：00000000
            sb.append("00000000");
            sb.append("\r\n");
        }

        // transaction合计部分
        //第0列：固定填1
        sb.append("1");
        //第1-7列：出款账户BSB No，000-000格式，7位
        sb.append(OmiPayBsb);
        //第8-16列：出款账户Account No，9位，右对齐，左侧不足留空
        sb.append(StringUtils.leftPad(OmiPayAccountNo, 9, ' '));
        //第18-19列：固定填13
        sb.append(" 13");
        String money = df.format(sum).replace(".", "");
        //第20-29列：总打款金额，整数，单位为cent，10位，右对齐，左侧位数不足补0，如$123.00应该填0000012300
        sb.append(StringUtils.leftPad(money, 10, '0'));
        //第30-61列：出款账户account  ，32位，左对齐，剩余留空
        sb.append(StringUtils.rightPad(OmiPayAccountName, 32, ' '));
        //第62-79列：备注，18位，剩余留空
        sb.append(StringUtils.rightPad("", 18, ' '));
        //第80-86列：出款账户BSB No，000-000格式，7位
        sb.append(OmiPayBsb);
        //第87-95列：出款账户Account No，9位，右对齐，剩余留空
        sb.append(StringUtils.leftPad(FormatUtil.getStringFormat(OmiPayAccountNo, 9), 9, ' '));
        //第96-111列：出款账户Account Name，16位，剩余留空
        sb.append(StringUtils.rightPad(OmiPayAccountName, 16, ' '));
        //第112-119列：固定8个0：00000000
        sb.append("00000000");
        sb.append("\r\n");

        // footer部分
        //第0列：固定填7
        sb.append("7");
        //第1-7列：固定999-999
        sb.append("999-999");
        //第20-29列：固定10个0：0000000000
        sb.append(StringUtils.leftPad("0000000000", 22, ' '));
        //第30-39列：总打款金额，整数，单位为cent，10位，右对齐，左侧位数不足补0，如$123.00应该填0000012300
        sb.append(StringUtils.leftPad(money, 10, '0'));
        //第40-49列：总打款金额，整数，单位为cent，10位，右对齐，左侧位数不足补0，如$123.00应该填0000012300
        sb.append(StringUtils.leftPad(money, 10, '0'));
        //第74-79列：transaction+transaction合计部分总行数，即transaction部分行数+1，长度为6，右对齐，左侧补0，如000012
        String countLength = StringUtils.leftPad(abaList.size()+1+"", 6, '0');
        sb.append(StringUtils.leftPad(countLength, 30, ' '));
        //第80-120列
        sb.append(StringUtils.rightPad("", 40, ' '));

        // 生成 ABA 文件
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(newFile));
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
        } catch (IOException e) {
            log.info(fileName+"ABA生成文件异常："+e.getMessage(),e);
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {

            }
        }
    }

    /**
     * 生成omipay清算excel文件
     */
    private void createOmiPayClearExcelFile(List<Map<String, Object>> excelList, BigDecimal totalAmount, String fileName, String gatewayName) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet1 = workbook.createSheet("应收数据");
        // 设置缺省列高
        sheet1.setDefaultRowHeightInPoints(12);
        // 设置缺省列宽
        sheet1.setDefaultColumnWidth(20);
        HSSFFont font1 = workbook.createFont();
        font1.setFontName("宋体");
        // 字体大小
        font1.setFontHeightInPoints((short)12);
        HSSFCellStyle style1 = workbook.createCellStyle();
        // 单元格居中对齐
        style1.setAlignment(HorizontalAlignment.CENTER);
        style1.setFont(font1);
        for (int i = 0; i < 5; i ++) {
            HSSFRow row = sheet1.createRow(i);
            switch (i) {
                case 0:
                    HSSFCell date = row.createCell(0);
                    HSSFRichTextString text = new HSSFRichTextString("应收日期：");
                    date.setCellValue(text);
                    break;
                case 1:
                    HSSFCell platform = row.createCell(0);
                    HSSFRichTextString text1 = new HSSFRichTextString("支付平台：");
                    platform.setCellValue(text1);
                    break;
                case 2:
                    HSSFCell wechat = row.createCell(0);
                    HSSFRichTextString text2 = new HSSFRichTextString("WechatPay：");
                    wechat.setCellValue(text2);
                    break;
                case 3:
                    HSSFCell aliPay = row.createCell(0);
                    HSSFRichTextString text3 = new HSSFRichTextString("Alipay：");
                    aliPay.setCellValue(text3);
                    break;
                case 4:
                    HSSFCell total = row.createCell(0);
                    total.setCellValue("合计：");
                    break;
                default:
                    break;
            }
        }
        HSSFSheet sheet2 = workbook.createSheet("应付数据");
        // 设置缺省列高
        sheet2.setDefaultRowHeightInPoints(12);
        // 设置缺省列宽
        sheet2.setDefaultColumnWidth(20);
        HSSFFont font2 = workbook.createFont();
        font2.setFontName("宋体");
        // 字体大小
        font2.setFontHeightInPoints((short)12);
        HSSFCellStyle style2 = workbook.createCellStyle();
        // 单元格居中对齐
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setFont(font2);
        //应付日期
        HSSFRow row1 = sheet2.createRow(0);
        HSSFCell aliPay = row1.createCell(0);
        HSSFRichTextString text = new HSSFRichTextString("应付日期：");
        aliPay.setCellValue(text);
        HSSFCell aliPayValue = row1.createCell(1);
        aliPayValue.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        //设置头
        String[] headers = {"商户名称", "商户ABN", "付款银行户名", "银行账户编号", "BSB编号", "金额", "备注"};
        HSSFRow row2 = sheet2.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell createCell = row2.createCell(i);
            createCell.setCellStyle(style2);
            HSSFRichTextString rowText = new HSSFRichTextString(headers[i]);
            createCell.setCellValue(rowText);
        }
        for (int i = 2; i < 2 + excelList.size(); i++) {
            HSSFRow row3 = sheet2.createRow(i);
            Map<String, Object> info = excelList.get(i-2);
            for (int j = 0; j < headers.length; j++) {
                HSSFCell createCell = row3.createCell(j);
                createCell.setCellStyle(style2);
                String content = "";
                if (info.get(headers[j]) != null) {
                    content = info.get(headers[j]).toString();
                }
                HSSFRichTextString data = new HSSFRichTextString(content);
                createCell.setCellValue(data);
            }
            //应付笔数、合计应付金额
            switch (i) {
                case 2:
                    HSSFCell textOut1 = row3.createCell(8);
                    HSSFRichTextString total = new HSSFRichTextString("应付笔数：");
                    textOut1.setCellValue(total);
                    HSSFCell dataOut1 = row3.createCell(9);
                    dataOut1.setCellValue(excelList.size());
                    break;
                case 3:
                    HSSFCell textOut2 = row3.createCell(8);
                    HSSFRichTextString paySum = new HSSFRichTextString("合计应付金额：");
                    textOut2.setCellValue(paySum);
                    HSSFCell dataOut2 = row3.createCell(9);
                    dataOut2.setCellValue(totalAmount.setScale(2, RoundingMode.UP).doubleValue());
                    break;
                default:
                    break;
            }
        }
        if (excelList.size() == 1) {
            HSSFRow row4 = sheet2.createRow(3);
            HSSFCell textOut2 = row4.createCell(8);
            HSSFRichTextString paySum = new HSSFRichTextString("合计应付金额：");
            textOut2.setCellValue(paySum);
            HSSFCell dataOut2 = row4.createCell(9);
            dataOut2.setCellValue(totalAmount.setScale(2, RoundingMode.UP).doubleValue());
        }
        try {
            StringBuilder sb = new StringBuilder(0);
            sb.append(filePath);
            sb.append(SEPARATOR);
            sb.append(fileName);
            FileOutputStream outputStream = new FileOutputStream(sb.toString());
            workbook.write(outputStream);
            outputStream.close();
            // 发送清算文件邮件
            List<String> copyToEmails = new ArrayList<>(1);
            List<File> attachments = new ArrayList<>(1);
            attachments.add(new File(sb.toString()));
            copyToEmails.add(COPY_TO_EMAIL);
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(new Integer(StaticDataEnum.SEND_NODE_18.getCode()).toString());
            MailUtil.sendMail(StaticDataEnum.U_WALLET.getMessage(), SEND_EMAIL, SETTLEMENT_FILE_SEND_TO_EMAIL, SEND_EMAIL, SEND_EMAIL_PASSWORD,
                    mailTemplateDTO.getEnMailTheme() + "_" + gatewayName + fileName.split("_")[0], mailTemplateDTO.getEnSendContent(), attachments, copyToEmails);
        } catch (Exception e) {
            log.info(fileName+"生成文件异常："+e.getMessage(),e);
        }
    }

    @Override
    public void createClearBillFile(List<ClearBillCSVDTO> billList, String fileName, String filePath) {
        cSVService.createClearCsvFile(fileName,filePath,billList);
    }

    /**
     * 清算可疑查证
     */
    @Override
    public void clearDoubtHandle() {
        List<ClearDetailDTO> doubtList = clearDetailService.getClearDoubt(StaticDataEnum.TRANS_STATE_0.getCode());
        for(ClearDetailDTO clearDetailDTO : doubtList){
            try{
                //查询原交易流水
                AccountFlowDTO accountFlowDTO = accountFlowService.selectLatestByFlowId(clearDetailDTO.getId(), 6);
                if (accountFlowDTO == null ||accountFlowDTO.getId() ==null || accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                    //无交易记录或者记录为失败
                    //交易失败
                //    clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                //    updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
                    doClearFailResult(null,clearDetailDTO,null);
                    continue;
                } else if (accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                    //交易成功
                    //回滚操作
                    doClearRollback(accountFlowDTO,clearDetailDTO,null);
                    continue;
                }
                JSONObject data;
                data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());
                if (data != null ) {
                    if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                        //成功
                        //记录交易失败
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
                        //回滚
                        doClearRollback(accountFlowDTO,clearDetailDTO,null);
                    } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
//                        //失败
//                        //记录交易失败
//                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
//                        //记录状态
//                        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                        clearDetailDTO.getId(),clearDetailDTO,null);
                        doClearFailResult(accountFlowDTO,clearDetailDTO,null);
                    }
                }else{
                    // 查询无信息为失败
                    //记录交易失败
//                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                    accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
//                    //记录状态
//                    clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    //updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);

                    doClearFailResult(accountFlowDTO,clearDetailDTO,null);


                }
            }catch (Exception e){
                log.info("批次号：{}，流水：{}，userId:{},可疑查证异常：",clearDetailDTO.getClearBatchId(),
                        clearDetailDTO.getId(),clearDetailDTO.getUserId(),e.getMessage(),e);

                continue;
            }

        }
    }

    /**
     * 清算回滚可疑查证
     */
    @Override
    public void clearRollbackDoubtHandle() {
        //
        List<ClearDetailDTO> doubtList = clearDetailService.getClearDoubt(StaticDataEnum.TRANS_STATE_4.getCode());
        for(ClearDetailDTO clearDetailDTO : doubtList){
            try{
                //查询原交易流水
                AccountFlowDTO accountFlowDTO = accountFlowService.selectLatestByFlowId(clearDetailDTO.getId(),StaticDataEnum.ACC_FLOW_TRANS_TYPE_6.getCode());
                if (accountFlowDTO == null || accountFlowDTO.getId() ==null || accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                    //无交易记录或者记录为失败
                    //交易失败
                    clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_5.getCode());
                    clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
                    continue;
                } else if (accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                    //交易成功
                    doClearFailResult(null,clearDetailDTO,null);
                    continue;
                }
                JSONObject data;
                data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());
                if (data != null ) {
                    if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                        //成功
                        //记录交易失败
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
                        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    //    updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
                        doClearFailResult(null,clearDetailDTO,null);
                    } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                        //失败
                        //记录交易失败
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
                        //记录状态
                        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_5.getCode());
                        clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
                    }
                }else{
                    // 查询无信息为失败
                    //记录交易失败
                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
                    //记录状态
                    clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_5.getCode());
                    clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
                }
            }catch (Exception e){
                log.info("批次号：{}，流水：{}，userId:{},回滚可疑查证异常：",clearDetailDTO.getClearBatchId(),
                        clearDetailDTO.getId(),clearDetailDTO.getUserId(),e.getMessage(),e);

                continue;
            }
        }
    }

    @Override
    public void clearRollbackFailHandle() {
        List<ClearDetailDTO> failList = clearDetailService.getClearDoubt(StaticDataEnum.TRANS_STATE_5.getCode());
        for(ClearDetailDTO clearDetailDTO : failList){
            try {            //查询原交易成功流水
                Map<String,Object> map = new HashMap<>();
                map.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
                map.put("flowId",clearDetailDTO.getId());
                map.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_5.getCode());
                AccountFlowDTO accountFlowDTO = accountFlowService.findOneAccountFlow(map);
                doClearRollback(accountFlowDTO,clearDetailDTO,null);

            }catch (Exception e){
                log.info("批次号：{}，流水：{}，userId:{},回滚失败处理异常：",clearDetailDTO.getClearBatchId(),
                        clearDetailDTO.getId(),clearDetailDTO.getUserId(),e.getMessage(),e);

                continue;
            }

        }

    }

    @Override
    public void apiPlatformClearAction(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws  BizException{
        //需要结算平台服务费的交易类型
        int [] orderSource  = {StaticDataEnum.ORDER_SOURCE_1.getCode()};
        params.put("orderSources",orderSource);
        params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
        params.put("orgAccPltFeeClearState",StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        int total = qrPayFlowService.countApiPlatformClear(params);
        if (total == 0) {
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        try {
            //创建清算批次
            if(params.get("start")!=null){
                clearBatchDTO.setClearEndDate(Long.valueOf(params.get("start").toString()));
            }
            if(params.get("end")!=null){
                clearBatchDTO.setClearStartDate(Long.valueOf(params.get("end").toString()));
            }
            clearBatchDTO.setClearType(StaticDataEnum.CLEAR_TYPE_1.getCode());
            //记录批次流水
            clearBatchDTO.setId(saveClearBatch(clearBatchDTO, request));
            //原交易流水打批次号,更新状态
            qrPayFlowService.addApiPlatformClearBatchId(params, clearBatchDTO, request);
            //清算明细入清算流水明细表
            List<ClearFlowDetail> list = clearFlowDetailService.getApiPltClearDataByBatchId(clearBatchDTO.getId());
            addClearFlowDetail(list, clearBatchDTO, request);
            clearDetailService.createApiPlatformClearData(params,clearBatchDTO,request);
        }catch (Exception e){
            log.info("apiMerchantClearAction 商户结算处理异常",e.getMessage(),e);
            if(clearBatchDTO.getId()!=null){
                clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateClearBatch(clearBatchDTO.getId(), clearBatchDTO, request);
            }
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
        Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            apiPlatformClear(clearBatchDTO.getId(), request, response);
                        }catch (Exception e){
                            log.info("批次号：{}，清算异常：",clearBatchDTO.getId(),e.getMessage(),e);
                        }
                    }
                }
        );
        t.start();
    }

    @Override
    public void apiPlatformClear(Long id, HttpServletRequest request, HttpServletResponse response) throws BizException {
        //查询清算明细
        ClearBatchDTO clearBatchDTO = findClearBatchById(id);
        //只需要生成结算文件。所以直接更新结算金额和状态
        //更新交易流水表
        Map<String,Object> clearMap = new HashMap<>();
        clearMap.put("batchId",id);
        qrPayFlowService.updateApiPlatformClearData(clearMap);
        //更新清算流水明细表
        clearMap.clear();
        clearMap.put("orgBatchId",id);
        clearFlowDetailService.clearData(clearMap);
        //更新清算流水表
        clearMap.clear();
        clearMap.put("orgBatchId",id);
        clearDetailService.clearData(clearMap);

        //查询清算成功的总比数总金额
        clearMap.clear();
        clearMap.put("clearBatchId",id);
        ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailService.clearTotal(clearMap);
        clearBatchDTO.setTotalNumber(clearFlowDetailDTO.getCount().longValue());

        ClearDetailDTO  clearDetailDTO = clearDetailService.clearTotal(clearMap);
        clearBatchDTO.setTotalAmount(clearDetailDTO.getTransAmount());
        clearBatchDTO.setBorrowAmount(clearDetailDTO.getBorrowAmount());
        clearBatchDTO.setClearAmount(clearDetailDTO.getClearAmount());

        //TODO 文明名未定
        String fileName = "Bill_"+id+".csv";
        log.info("开始生成平台清算Bill文件，文件名："+fileName);
        //记录清算状态
        clearBatchDTO.setFileName(fileName);
        clearBatchDTO.setUrl(filePath+fileName);
        updateClearBatch(id,clearBatchDTO,request);
        //生成文件
        List<ClearBillCSVDTO> billList = clearDetailService.getApiPltClearBillList(id);
        createClearBillFile(billList,fileName,filePath);

        //更新批次状态
        clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        updateClearBatch(id,clearBatchDTO,request);
    }

    @Override
    public void doCreditClear(List<OneMerchantClearDataDTO> list, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        Map<String ,Object> params = new HashMap<>(3);
        List<ClearDetail> clearDetails = new ArrayList<>();
        int totalNum = 0;
        for(OneMerchantClearDataDTO oneMerchantClearDataDTO : list){
            try {
                List<UpdateClearStateListDTO> orderList = oneMerchantClearDataDTO.getBorrowList();
                //累加总条数
                totalNum = totalNum + orderList.size();
                MerchantDTO merchantDTO  = merchantService.findMerchantById(oneMerchantClearDataDTO.getMerchantId());

                //查询是否已经同步

                ClearDetailDTO checkDTO = clearDetailService.findClearDetailById(oneMerchantClearDataDTO.getClearFlow());
                if(checkDTO != null && checkDTO.getId()!=null){
                    throw new BizException("clear flow have synchronous");
                }


                //记录交易明细
                ClearDetail clearDetailDTO = new ClearDetail();
                clearDetailDTO.setClearBatchId(clearBatchDTO.getId());
                clearDetailDTO.setState(StaticDataEnum.CLEAR_BATCH_STATE_0.getCode());
                clearDetailDTO.setId(oneMerchantClearDataDTO.getClearFlow());
                clearDetailDTO.setMerchantId(oneMerchantClearDataDTO.getMerchantId());
                clearDetailDTO.setUserId(merchantDTO.getUserId());

                BigDecimal clearAmount = BigDecimal.ZERO;
                BigDecimal transAmount = BigDecimal.ZERO;
                int clearNum = 0;
                //记录清算订单
                for(UpdateClearStateListDTO data : orderList){
                    try {
                        //查询交易状态
                        Long clearTime = System.currentTimeMillis();
                        ClearFlowDetailDTO clearFlowDetailDTO = new ClearFlowDetailDTO();
                        clearFlowDetailDTO.setFlowId(data.getTripartiteTransactionNo());
                        clearFlowDetailDTO.setClearBatchId(clearBatchDTO.getId());
                        clearFlowDetailDTO.setId(clearFlowDetailService.saveClearFlowDetail(clearFlowDetailDTO,request));
                        //查询原交易流水
                        params.clear();
                        params.put("id",data.getTripartiteTransactionNo());
                        params.put("merchantId",oneMerchantClearDataDTO.getMerchantId());
                        params.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
                        QrPayFlowDTO  qrPayFlowDTO = qrPayFlowService.findOneQrPayFlow(params);
                        //如果流水不存在，或者已清算，记录本次清算失败
                        if(qrPayFlowDTO == null || qrPayFlowDTO.getId() == null || qrPayFlowDTO.getClearState() == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode()){
                            log.error("creditClear orderNo："+data.getTripartiteTransactionNo()+ "clearState error");
                            clearFlowDetailDTO.setState(StaticDataEnum.CLEAR_BATCH_STATE_2.getCode());
                            clearFlowDetailService.updateClearFlowDetail(clearFlowDetailDTO.getId(),clearFlowDetailDTO,request);
                            continue;
                        }
                        clearFlowDetailDTO.setClearAmount(qrPayFlowDTO.getClearAmount());
                        clearFlowDetailDTO.setTransAmount(qrPayFlowDTO.getClearAmount());
                        clearFlowDetailDTO.setBorrowAmount(qrPayFlowDTO.getTransAmount());
                        clearFlowDetailDTO.setTransType(qrPayFlowDTO.getTransType());
                        clearFlowDetailDTO.setRecUserId(qrPayFlowDTO.getRecUserId());
                        clearFlowDetailDTO.setState(StaticDataEnum.CLEAR_BATCH_STATE_1.getCode());
                        qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
                        qrPayFlowDTO.setClearTime(clearTime);
                        qrPayFlowDTO.setBatchId(clearBatchDTO.getId());
                        qrPayFlowService.updateQrPayFlow(data.getTripartiteTransactionNo(),qrPayFlowDTO,request);
                        clearFlowDetailService.updateClearFlowDetail(clearFlowDetailDTO.getId(),clearFlowDetailDTO,request);
                        clearAmount = clearAmount.add(qrPayFlowDTO.getClearAmount()) ;
                        transAmount = transAmount.add(qrPayFlowDTO.getTransAmount());
                        clearNum++;
                    }catch (Exception e){
                        log.error("creditClear orderNo："+data.getTripartiteTransactionNo()+"Exception:"+e.getMessage(),e);
                    }finally {
                        continue;
                    }
                }
                clearDetailDTO.setClearAmount(clearAmount);
                clearDetailDTO.setTransAmount(clearAmount);
                clearDetailDTO.setBorrowAmount(transAmount);
                clearDetailDTO.setClearNumber(Long.parseLong(clearNum+""));
                clearDetailDTO.setState(StaticDataEnum.CLEAR_BATCH_STATE_1.getCode());
                long now = System.currentTimeMillis();
                Long currentLoginId = getUserId(request);
                clearDetailDTO.setCreatedBy(currentLoginId);
                clearDetailDTO.setCreatedDate(now);
                clearDetailDTO.setModifiedBy(currentLoginId);
                clearDetailDTO.setModifiedDate(now);
                clearDetailDTO.setIp(getIp(request));
                clearDetailDTO.setStatus(1);
                clearDetails.add(clearDetailDTO);
            }catch (Exception e){
                log.error("creditClear merchantId:"+oneMerchantClearDataDTO.getMerchantId()+",clearFlow:"+oneMerchantClearDataDTO.getClearFlow()+",Eexception+"+e.getMessage(),e);
                continue;
            }
            if(clearDetails.size()>0){
                clearDetailService.saveClearDetailList(clearDetails,request);
            }
        }
        //查询清算成功的总比数总金额
        params.clear();
        params.put("clearBatchId",clearBatchDTO.getId());
        ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailService.clearTotal(params);
        clearBatchDTO.setTotalNumber(clearFlowDetailDTO.getCount().longValue());

        ClearDetailDTO  clearDetailDTO = clearDetailService.clearTotal(params);
        clearBatchDTO.setTotalAmount(clearDetailDTO == null ? BigDecimal.ZERO :clearDetailDTO.getTransAmount());
        clearBatchDTO.setBorrowAmount(clearDetailDTO == null ? BigDecimal.ZERO :clearDetailDTO.getBorrowAmount());
        clearBatchDTO.setClearAmount(clearDetailDTO == null ? BigDecimal.ZERO :clearDetailDTO.getClearAmount());
        clearBatchDTO.setState( StaticDataEnum.CLEAR_BATCH_STATE_1.getCode());
        this.updateClearBatch(clearBatchDTO.getId(),clearBatchDTO,request);
    }

    @Override
    public int getClearCount(Map<String, Object> params, MerchantDTO merchantDTO) throws BizException {
        int clearState = Integer.parseInt(params.get("clearState").toString());
        int total = 0;
        if(clearState == StaticDataEnum.CLEAR_STATE_TYPE_0.getCode()){
            //查询未清算
            //增加查询条件，需要清算的，成功的交易
            params.put("isNeedClear",StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
            int[] stateList = {StaticDataEnum.TRANS_STATE_31.getCode(),StaticDataEnum.TRANS_STATE_1.getCode()};
            params.put("stateList",stateList);
            int transNum = qrPayFlowService.count(params);
            Map<String,Object> map = new HashMap<>();
            params.put("settlementState",params.get("clearState"));
            params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
            int wholeSaleNum = wholeSalesFlowService.count(params);
            total = transNum + wholeSaleNum;
        }else if(clearState == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode()){
            //查询已清算
            params.put("state",StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
            params.put("recUserId",merchantDTO.getUserId());
            total = clearFlowDetailService.count(params);
        }else{
            throw new BizException("params error");
        }
        return total;
    }

    @Override
    public List<Map<String, Object>> getClearList(Map<String, Object> params, MerchantDTO merchantDTO, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        int clearState = Integer.parseInt(params.get("clearState").toString());
        List<Map<String, Object>> reslut = new ArrayList<>();

        if(clearState == StaticDataEnum.CLEAR_STATE_TYPE_0.getCode()){
            //查询未清算
            int[] stateList = {StaticDataEnum.TRANS_STATE_31.getCode(), StaticDataEnum.TRANS_STATE_1.getCode()};
            params.put("stateList", stateList);
            params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
            params.put("isNeedClear",StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
            params.put("settlementState",params.get("clearState"));
            reslut = qrPayFlowService.getUnClearedList(params);
        }else{
            //查询已清算
            reslut = getClearedList(params,scs,pc,merchantDTO);

        }
        return reslut;
    }

    private List<Map<String, Object>> getClearedList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, MerchantDTO merchantDTO) {
        params.put("state",StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
        params.put("recUserId",merchantDTO.getUserId());
        List<ClearFlowDetailDTO> clearFlowDetailDTOS = clearFlowDetailService.find(params, scs, pc);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM.dd ,yyyy", Locale.US);

        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MMM.dd HH:mm", Locale.US);
        String date = null;
        String format = null;
        Map<String ,Object> dateMap = new HashMap<>();
        List<ClearFlowDetailDTO> dateList = new ArrayList<>();
        List<Map<String,Object>> result = new ArrayList<>();
        for(ClearFlowDetailDTO clearFlowDetailDTO:clearFlowDetailDTOS){
            //如果是整体出售的清算
            if(clearFlowDetailDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_24.getCode() ){
                Map<String,Object> map = new HashMap<>();
                map.put("id",clearFlowDetailDTO.getFlowId());
//                WholeSalesFlowDTO wholeSalesFlowDTO = wholeSalesFlowService.findOneWholeSalesFlow(map);
                clearFlowDetailDTO.setTransNo(clearFlowDetailDTO.getFlowId().toString());
                clearFlowDetailDTO.setSaleType(StaticDataEnum.SALE_TYPE_1.getCode());
                String transTime = simpleDateFormat2.format(clearFlowDetailDTO.getCreatedDate());
                clearFlowDetailDTO.setTransTime(transTime);
            }else{
                QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(clearFlowDetailDTO.getFlowId());
                clearFlowDetailDTO.setTransNo(qrPayFlowDTO.getTransNo());
                clearFlowDetailDTO.setSaleType(qrPayFlowDTO.getSaleType());
                String transTime = simpleDateFormat2.format(clearFlowDetailDTO.getCreatedDate());
                clearFlowDetailDTO.setTransTime(transTime);
            }

            format = simpleDateFormat.format(clearFlowDetailDTO.getCreatedDate());
            if(!format.equals(date)){
                if(date != null){
                    //非首次
                    dateMap.put("list",dateList);
                    result.add(dateMap);
                    dateList = new ArrayList<>();
                    dateMap = new HashMap<>();
                }
                dateList.add(clearFlowDetailDTO);
                date = format;
                //新日期新累计
                dateMap.put("date",date);

                //计算本日总金额和总笔数
                //获得本日起止时间
                Long now = clearFlowDetailDTO.getCreatedDate();
                Map<String,Long> timeMap = qrPayFlowService.getDayStartTime(now);
                params.put("start",timeMap.get("start"));
                params.put("end",timeMap.get("end"));

                List<ClearFlowDetailDTO> totalList = clearFlowDetailService.find(params, null, null);
//                List<ClearDetailDTO> totalList = clearDetailService.find(params,null,null);

                BigDecimal totalClearAmount =  totalList.stream().map(ClearFlowDetailDTO::getClearAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                dateMap.put("totalClearAmount",totalClearAmount);
                dateMap.put("totalNum",totalList.size());
            }else{
                dateList.add(clearFlowDetailDTO);
            }
        }
        dateMap.put("date",date);
        dateMap.put("list",dateList);
        result.add(dateMap);
        return  result;
    }

    @Override
    public Map<String, Object> getClearFlowDetailById(Long merchantId, Long clearId, Vector<SortingContext> scs, HttpServletRequest request) throws BizException {
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MMM.dd HH:mm", Locale.US);
        Map<String, Object> result = new HashMap<>();
        ClearDetailDTO clearDetailDTO = clearDetailService.findClearDetailById(clearId);
        if(clearDetailDTO == null || clearDetailDTO.getId() == null || !clearDetailDTO.getMerchantId().equals(merchantId)){
            throw new BizException("params error");
        }
        ClearBatchDTO clearBatchDTO = this.findClearBatchById( clearDetailDTO.getClearBatchId());

        Map<String, Object> params = new HashMap<>();
        params.put("state",StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
        params.put("recUserId",clearDetailDTO.getUserId());
        params.put("clearBatchId",clearDetailDTO.getClearBatchId());

        List<ClearFlowDetailDTO> list = clearFlowDetailService.find(params,scs,null);
        if(clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_0.getCode() || clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_3.getCode()){
            for (ClearFlowDetailDTO clearFlowDetailDTO:list){
                QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(clearFlowDetailDTO.getFlowId());
                clearFlowDetailDTO.setTransNo(qrPayFlowDTO.getTransNo());
                clearFlowDetailDTO.setSaleType(qrPayFlowDTO.getSaleType());
                clearFlowDetailDTO.setTransTime(simpleDateFormat2.format(qrPayFlowDTO.getCreatedDate()));
            }
        }else{
            for (ClearFlowDetailDTO clearFlowDetailDTO:list){
                params.clear();
                params.put("id",clearFlowDetailDTO.getFlowId());

                WholeSalesFlowDTO wholeSalesFlowDTO = wholeSalesFlowService.findOneWholeSalesFlow(params);
                clearFlowDetailDTO.setTransNo(wholeSalesFlowDTO.getId().toString());
                clearFlowDetailDTO.setSaleType(StaticDataEnum.SALE_TYPE_1.getCode());
                clearFlowDetailDTO.setTransTime(simpleDateFormat2.format(wholeSalesFlowDTO.getCreatedDate()));
            }
        }
        BigDecimal clearAmount = list.stream().map(ClearFlowDetailDTO::getClearAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM.dd ,yyyy", Locale.US);

        String date = simpleDateFormat.format(clearDetailDTO.getCreatedDate());
        result.put("date",date);
        result.put("clearAmount",clearAmount);
        result.put("totalNum",list.size());
        result.put("list",list);
        return  result;
    }

    @Override
    public void clearFail(Long id, HttpServletRequest request) throws Exception {
        // 查询清算批次
        ClearDetailDTO clearDetailDTO  = clearDetailService.findClearDetailById(id);

        if(null == clearDetailDTO || null  == clearDetailDTO.getId()){
            throw new BizException(I18nUtils.get("clear.data.not.exist", getLang(request)));
        }
        // 查询清算类型
        ClearBatchDTO clearBatchDTO = this.findClearBatchById(clearDetailDTO.getClearBatchId());
        if(null == clearBatchDTO || null  == clearBatchDTO.getId()){
            throw new BizException(I18nUtils.get("clear.data.not.exist", getLang(request)));
        }
        if(clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_2.getCode()){
            //整体出售
            creditWebService.failedClearDetailInfo(id, request);
        }else if(clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_4.getCode()){
            //捐赠订单
            donationFlowService.settleFailed(clearDetailDTO, clearBatchDTO, request);
        }else if(clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_5.getCode()){
            //小费
            tipFlowService.settleFailed(clearDetailDTO, clearBatchDTO, request);
        }else if(clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_6.getCode()){
            //H5商户清算
            qrPayFlowService.h5MerchantSettleFail(clearBatchDTO,clearDetailDTO,request);
        }else{
            //正常出售
            OneMerchantClearDataDTO oneMerchantClearDataDTO = new OneMerchantClearDataDTO();
            oneMerchantClearDataDTO.setClearFlow(id);
            oneMerchantClearDataDTO.setMerchantId(clearDetailDTO.getMerchantId());

            List<OneMerchantClearDataDTO> req = new ArrayList<>();
            req.add(oneMerchantClearDataDTO);
            qrPayFlowService.updateClearState(req,request);
        }
    }

    @Override
    public ClearBatchDTO settleFileExport(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //创建清算批次
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        if( params.get("start")!=null && StringUtils.isNotEmpty(params.get("start").toString()) ){
            clearBatchDTO.setClearEndDate(Long.valueOf(params.get("start").toString()));
        }else{
            params.remove("start");
        }
        if(params.get("end")!=null &&StringUtils.isNotEmpty(params.get("end").toString())){
            clearBatchDTO.setClearStartDate(Long.valueOf(params.get("end").toString()));
        }else{
            params.remove("end");
        }
        if(params.get("merchantIdList")!=null  ){
            String [] merchantIdList = params.get("merchantIdList").toString().split(",",-1);;
            if(merchantIdList.length < 1){
                params.remove("merchantIdList");
            }else{
                params.put("merchantIdList",merchantIdList);
            }
        }
        int total = qrPayFlowService.countMerchantClearListNew(params);
        if (total == 0) {
            //无清算数据
            log.info("settleFileExport 无清算数据");
            return null;
        }

        try {
            //记录批次流水
            clearBatchDTO.setCreatedBy( Long.valueOf(params.get("optionUserId").toString() ));
            clearBatchDTO.setModifiedBy( Long.valueOf(params.get("optionUserId").toString() ));
            clearBatchDTO.setId(saveClearBatch(clearBatchDTO, request));

            //原交易流水打批次号,更新状态
            qrPayFlowService.addQrPayClearBatchId(params, clearBatchDTO, request);
            //清算明细入清算流水明细表
            List<ClearFlowDetail> list = clearFlowDetailService.getDataByBatchId(clearBatchDTO.getId());
            addClearFlowDetail(list,clearBatchDTO,request);
            //生成清算流水信息
            qrPayFlowService.updateQrPayClearBatch(params, clearBatchDTO, request);
            // 清算 ,type: 0:老清算 1：新清算
            clearBatchDTO = clear(clearBatchDTO.getId(), request, 1, response);
        } catch (Exception e) {
            log.info("settleFileExport 商户结算处理异常",e.getMessage(),e);
            if(clearBatchDTO.getId()!=null){
                clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateClearBatch(clearBatchDTO.getId(), clearBatchDTO, request);
            }
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }

        return clearBatchDTO;

    }

    @Override
    public ClearBatchDTO createNullClearBillFile() {
        String fileName = "clearFile.csv";
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        clearBatchDTO.setUrl(filePath + fileName);
        clearBatchDTO.setFileName(fileName);
        createClearBillFile(null,"clearFile.csv",filePath);
        return  clearBatchDTO;
    }


    /**
     * 回滚操作
     * @param accountFlowDTO
     * @param clearDetailDTO
     * @param request
     * @throws Exception
     */
    @Override
    public int doClearRollback(AccountFlowDTO accountFlowDTO, ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws Exception{
        updateTORollback(accountFlowDTO,clearDetailDTO,request);
        int rollbackResult = amountOutRollback(accountFlowDTO.getId(),request);
        if(StaticDataEnum.TRANS_STATE_1.getCode()==rollbackResult){
//            clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);
            doClearFailResult(null,clearDetailDTO,request);
        }else if(StaticDataEnum.TRANS_STATE_2.getCode()==rollbackResult){
            clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_5.getCode());
            clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);
        }
        return rollbackResult;
    }


    @Override
    public int amountOutRollback(Long id ,HttpServletRequest request) throws  Exception{
        //查询要回退的流水
        AccountFlowDTO accountFlowDTO = accountFlowService.findAccountFlowById(id);
        if(accountFlowDTO == null ||accountFlowDTO.getId() == null){
            throw new BizException();
        }
        //查询是否已经回退
        Map<String,Object> checkMap = new HashMap<>();
        checkMap.put("flowId",accountFlowDTO.getFlowId());
        checkMap.put("accountType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_6.getCode());
        checkMap.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
        AccountFlowDTO rollbackSuccessFlow = accountFlowService.findOneAccountFlow(checkMap);
        if(rollbackSuccessFlow.getId()!=null){
            return StaticDataEnum.TRANS_STATE_1.getCode();
        }
        //查询处理中
        checkMap.put("state",StaticDataEnum.TRANS_STATE_0.getCode());
        AccountFlowDTO rollbackDoubtFlow = accountFlowService.findOneAccountFlow(checkMap);
        if(rollbackDoubtFlow.getId()!=null){
            return StaticDataEnum.TRANS_STATE_3.getCode();
        }

        accountFlowDTO.setOrderNo(SnowflakeUtil.generateId());
        accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_6.getCode());
        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        accountFlowDTO.setCreatedDate(System.currentTimeMillis());
        accountFlowDTO.setModifiedDate(System.currentTimeMillis());
        //记录流水
        accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO,request));
        Map<String, Object> map = qrPayService.getAmountInMap(accountFlowDTO);
        //交易请求
        JSONObject accTransObj = null;
        try {
            accTransObj = accountService.amountIn(JSONObject.parseObject(JSON.toJSONString(map)));
        } catch (Exception e) {
            //交易异常
           return StaticDataEnum.TRANS_STATE_3.getCode();
        }
        //记录返回参数
        accountFlowDTO.setReturnCode(accTransObj.getString("code"));
        accountFlowDTO.setReturnMessage(accTransObj.getString("message"));

        if (accTransObj.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            //交易失败
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,request);
            return StaticDataEnum.TRANS_STATE_2.getCode();
        } else if (accTransObj.getString("code").equals(ResultCode.OK.getCode())) {
            //交易成功
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,request);
            return StaticDataEnum.TRANS_STATE_1.getCode();
        } else {
            //交易可疑
//            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
//            accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,request);
            return StaticDataEnum.TRANS_STATE_3.getCode();
        }

    }


    @Transactional(rollbackFor = Exception.class)
    public void updateTORollback(AccountFlowDTO accountFlowDTO, ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws  BizException{
        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_4.getCode());
        if(accountFlowDTO.getId() != null){
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,request);
        }
        clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);
    }




    @Override
    public void doClearFailResult(AccountFlowDTO accountFlowDTO, ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws  BizException{

        Map<String,Object> map = new HashMap<>();
        Long now = System.currentTimeMillis();
        map.put("orgBatchId",clearDetailDTO.getClearBatchId());
        map.put("recUserId",clearDetailDTO.getUserId());
        map.put("modifiedDate",now);
        map.put("clearState",0);
        if(accountFlowDTO != null &&accountFlowDTO.getId() != null){
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,request);
        }
        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
        clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);
        qrPayFlowService.updateClearBatch(map);
        //改退款回退状态为已回退
        map.clear();

        map.put("batchId",clearDetailDTO.getClearBatchId());
        map.put("settlementState",4);
        map.put("settlementTime",now);
        map.put("modifiedDate",now);
        refundFlowService.updateClearBatch(map);
        map.clear();
        map.put("modifiedDate",now);
        map.put("recUserId",clearDetailDTO.getUserId());
        map.put("clearBatchId",clearDetailDTO.getClearBatchId());
        clearFlowDetailService.updateClearBatchToFail(map);
    }

    @Transactional(rollbackFor = Exception.class)
    public void doClearSuccessResult(AccountFlowDTO accountFlowDTO, ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws  BizException{

        ClearBatchDTO clearBatch = findClearBatchById(clearDetailDTO.getClearBatchId());
        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        Map<String,Object> map = new HashMap<>();
        map.put("modifiedDate",System.currentTimeMillis());
        map.put("orgBatchId",clearDetailDTO.getClearBatchId());
        map.put("recUserId",clearDetailDTO.getUserId());
        map.put("clearTime",System.currentTimeMillis());
        map.put("clearState",StaticDataEnum.CLEAR_BATCH_STATE_1.getCode());
        //修改清算流水表为成功
        clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);
        if(accountFlowDTO!=null){
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,request);
        }

        clearFlowDetailService.clearData(map);
        //改退款回退状态为已回退
        refundFlowService.clearData(map);
        //扫码支付流水改为已清算
        qrPayFlowService.updateClearBatch(map);
        if(clearDetailDTO.getTransAmount().compareTo(clearDetailDTO.getClearAmount())>0){
            //如果清算金额>实际清算金额

            //计算退款资金
            BigDecimal refundAmt = clearDetailDTO.getTransAmount().subtract(clearDetailDTO.getClearAmount());
            map.put("gateWayId",clearBatch.getGatewayId());
            //查询回退交易列表
            List<RefundFlowDTO> refundList = refundFlowService.findAllUnClearedRefundFlow(map);
            //剩余资金
            BigDecimal lastAmount =  refundAmt;
            //削减资金
            BigDecimal toLoseAmount =  BigDecimal.ZERO;
            //结果标志 0：完全清算 1：部分清算 2：未清算
            int  flag ;
            for(RefundFlowDTO refundFlowDTO : refundList) {
                //计算削减金额
                if (lastAmount.compareTo(refundFlowDTO.getNotSettlementAmount()) >= 0) {
                    toLoseAmount = refundFlowDTO.getNotSettlementAmount();
                    flag = 0;
                } else {
                    toLoseAmount = lastAmount;
                    if (toLoseAmount.compareTo(BigDecimal.ZERO) == 0) {
                        flag = 2;
                    } else {
                        flag = 1;
                    }

                }
                if (flag == 0) {
                    map.put("settlementState", 1);
                } else if (flag == 1) {
                    map.put("settlementState", 6);
                } else {
                    //无清算金额，跳出循环
                   break;
                }
                lastAmount = lastAmount.subtract(toLoseAmount);
                //削减退款未清算金额
                map.put("toLoseAmount", toLoseAmount);
                map.put("id",refundFlowDTO.getId());
                int n = refundFlowService.updateAmountOut(map);
                if( n != 1){
                    throw new BizException("update Error!");
                }
                //记录退款清算流水记录
                ClearFlowDetailDTO clearFlowDetailDTO = new ClearFlowDetailDTO();
                clearFlowDetailDTO.setRecUserId(refundFlowDTO.getOrgRecUserId());
                clearFlowDetailDTO.setFlowId(refundFlowDTO.getId());
                clearFlowDetailDTO.setTransType(refundFlowDTO.getTransType());
                clearFlowDetailDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                clearFlowDetailDTO.setClearBatchId(clearDetailDTO.getClearBatchId());
                clearFlowDetailDTO.setTransAmount(BigDecimal.ZERO.subtract(refundFlowDTO.getNotSettlementAmount()));
                clearFlowDetailDTO.setClearAmount(BigDecimal.ZERO.subtract(toLoseAmount));
                clearFlowDetailDTO.setBorrowAmount(BigDecimal.ZERO.subtract(refundFlowDTO.getRefundAmount()));
                clearFlowDetailService.saveClearFlowDetail(clearFlowDetailDTO,request);


            }

//            //无需账户出账，说明退款金额>=交易金额
//            //查询清算金额为正数的交易
//            map.put("batchId",clearDetailDTO.getClearBatchId());
//            BigDecimal inAmount = clearFlowDetailService.findAmountInAmount(map);
//            //修改清算资金为正数的交易为清算成功
//            map.put("amountInFlag",0);
//            int i = clearFlowDetailService.clearData(map);
//            int j = qrPayFlowService.updateClearBatch(map);
//            int k = refundFlowService.clearData(map);
//            //清算流水 = 回退交易+正向交易
//            if (i!= k+j){
//                throw new BizException("update  Error!");
//            }
//            //查询退款交易
//            List<ClearFlowDetailDTO> OutAmountList = clearFlowDetailService.findAmountOutAmount(map);
//            BigDecimal lastAmount =  inAmount ;
//            BigDecimal toLoseAmount =  BigDecimal.ZERO;
//
//            //结果标志 0：完全清算 1：部分清算 2：未清算
//            int  flag = 0;
//            for(ClearFlowDetailDTO clearFlowDetailDTO:OutAmountList){
//                //计算削减金额
//                if (lastAmount.compareTo(clearFlowDetailDTO.getTransAmount())>=0){
//                    toLoseAmount = clearFlowDetailDTO.getTransAmount();
//                    flag = 0;
//                }else{
//                    toLoseAmount = lastAmount;
//                    if(toLoseAmount.compareTo(BigDecimal.ZERO)==0){
//                        flag = 2;
//                    }else{
//                        flag = 1;
//                    }
//
//                }
//                if(flag==0){
//                    map.put("settlementState" ,1);
//                }else if(flag==1){
//                    map.put("settlementState" ,6);
//                }else{
//                    map.put("settlementState" ,0);
//                }
//                //削减
//                map.put("toLoseAmount",toLoseAmount);
//                map.put("clearFlowDetailId",clearFlowDetailDTO.getId());
//                map.put("flowId",clearFlowDetailDTO.getFlowId());
//                int m = clearFlowDetailService.updateAmountOut(map);
//                int n = refundFlowService.updateAmountOut(map);
//                if(m != n || m != 1){
//                    throw new BizException("update Error!");
//                }
//            }
        }


    }

    private AccountFlowDTO getAmountOutFlow(ClearDetailDTO clearDetailDTO, HttpServletRequest request) {
        AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
        accountFlowDTO.setFlowId(clearDetailDTO.getId());
        accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_5.getCode());
        accountFlowDTO.setOrderNo(SnowflakeUtil.generateId());
        accountFlowDTO.setTransAmount(clearDetailDTO.getClearAmount());
        accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        accountFlowDTO.setUserId(clearDetailDTO.getUserId());
        return accountFlowDTO;
    }

    @Override
    public void clearBatchAction(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws BizException {
        int total = qrPayFlowService.countMerchantClearList(params);
        if (total == 0) {
            //无清算数据
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        try {
            //创建清算批次
            if(params.get("start")!=null){
                clearBatchDTO.setClearEndDate(Long.valueOf(params.get("start").toString()));
            }
            if(params.get("end")!=null){
                clearBatchDTO.setClearStartDate(Long.valueOf(params.get("end").toString()));
            }
            clearBatchDTO.setGatewayId(Integer.parseInt(params.get("gatewayId").toString()));
            //记录批次流水
            clearBatchDTO.setId(saveClearBatch(clearBatchDTO, request));
            //原交易流水打批次号,更新状态
            qrPayFlowService.addQrPayClearBatchId(params, clearBatchDTO, request);
            //清算明细入清算流水明细表
            List<ClearFlowDetail> list = clearFlowDetailService.getDataByBatchIdNew(clearBatchDTO.getId());
            addClearFlowDetail(list, clearBatchDTO, request);
            //生成清算流水信息
            qrPayFlowService.updateQrPayClearBatch(params, clearBatchDTO, request);

        } catch (Exception e) {
            log.info("clearBatchAction 商户结算处理异常",e.getMessage(),e);
            if(clearBatchDTO.getId()!=null){
                clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateClearBatch(clearBatchDTO.getId(), clearBatchDTO, request);
            }
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }

        Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            clear(clearBatchDTO.getId(), request,0, response);
                        }catch (Exception e){
                            log.info("批次号：{}，清算异常：",clearBatchDTO.getId(),e.getMessage(),e);
                        }
                    }
                }
        );
        t.start();

    }

    @Override
    public void addClearFlowDetail(List<ClearFlowDetail> list, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException{

        for(ClearFlowDetail clearFlowDetail:list){
            Long now = System.currentTimeMillis();
            clearFlowDetail.setId(SnowflakeUtil.generateId());
//            Long currentLoginId = getUserId(request);
            clearFlowDetail.setCreatedBy(clearBatchDTO.getCreatedBy());
            clearFlowDetail.setCreatedDate(now);
            clearFlowDetail.setModifiedBy(clearBatchDTO.getModifiedBy());
            clearFlowDetail.setModifiedDate(now);
            clearFlowDetail.setIp(getIp(request));
            clearFlowDetail.setStatus(1);
            clearFlowDetail.setClearAmount(BigDecimal.ZERO);
            clearFlowDetail.setState(0);
            clearFlowDetail.setBorrowAmount(clearFlowDetail.getBorrowAmount());
        }
        clearFlowDetailService.saveClearFlowDetailList(list,request);
    }


    @Override
    public ClearBatchDTO H5MerchantSettleFileExport(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //创建清算批次
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        if( params.get("start")!=null && StringUtils.isNotEmpty(params.get("start").toString()) ){
            clearBatchDTO.setClearEndDate(Long.valueOf(params.get("start").toString()));
        }else{
            params.remove("start");
        }
        if(params.get("end")!=null &&StringUtils.isNotEmpty(params.get("end").toString())){
            clearBatchDTO.setClearStartDate(Long.valueOf(params.get("end").toString()));
        }else{
            params.remove("end");
        }
        if(params.get("merchantIdList")!=null  ){
            String [] merchantIdList = params.get("merchantIdList").toString().split(",",-1);;
            if(merchantIdList.length < 1){
                params.remove("merchantIdList");
            }else{
                params.put("merchantIdList",merchantIdList);
            }
        }
        // 查询交易开始时间之前，是否有未清算的退款金额
        if (params.containsKey("start")){
            int refundCount = refundService.getH5MerchantRefundCount(params);
            if(refundCount > 0 ){
                throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
            }
        }

        int total = qrPayFlowService.countH5MerchantClearList(params);
        if (total == 0) {
            //无清算数据
            log.info("settleFileExport 无清算数据");
            return null;
        }

        try {
            //记录批次流水
            clearBatchDTO.setCreatedBy( Long.valueOf(params.get("optionUserId").toString() ));
            clearBatchDTO.setModifiedBy( Long.valueOf(params.get("optionUserId").toString() ));

            clearBatchDTO.setClearType(StaticDataEnum.CLEAR_TYPE_6.getCode());
            clearBatchDTO.setId(saveClearBatch(clearBatchDTO, null));

            // 原交易流水打批次号,更新状态
            qrPayFlowService.addH5ClearBatchId(params, clearBatchDTO, request);
            // 退款订单打批号，更新状态
            refundService.addH5ClearBatchId(params, clearBatchDTO, request);
            //清算明细入清算流水明细表
            List<ClearFlowDetail> list = clearFlowDetailService.getDataByBatchId(clearBatchDTO.getId());
            addClearFlowDetail(list,clearBatchDTO,request);
            //生成清算流水信息
            qrPayFlowService.addClearDetailFlow(params, clearBatchDTO, request);
            // 清算 ,type: 0:老清算 1：新清算
            clearBatchDTO = h5Clear(clearBatchDTO.getId(), request,  response);
        } catch (Exception e) {
            log.info("settleFileExport 商户结算处理异常",e.getMessage(),e);
            if(clearBatchDTO.getId()!=null){
                clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateClearBatch(clearBatchDTO.getId(), clearBatchDTO, request);
            }
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }

        return clearBatchDTO;

    }

    @Override
    public void h5MerchantSettleCheck(Map<String, Object> params, HttpServletRequest request) throws Exception {
        //创建清算批次
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        if( params.get("start")!=null && StringUtils.isNotEmpty(params.get("start").toString()) ){
            clearBatchDTO.setClearEndDate(Long.valueOf(params.get("start").toString()));
        }else{
            params.remove("start");
        }
        if(params.get("end")!=null &&StringUtils.isNotEmpty(params.get("end").toString())){
            clearBatchDTO.setClearStartDate(Long.valueOf(params.get("end").toString()));
        }else{
            params.remove("end");
        }
        if(params.get("merchantIdList")!=null  ){
            String [] merchantIdList = params.get("merchantIdList").toString().split(",",-1);;
            if(merchantIdList.length < 1){
                params.remove("merchantIdList");
            }else{
                params.put("merchantIdList",merchantIdList);
            }
        }
        // 查询交易开始时间之前，是否有未清算的退款金额
        if (params.containsKey("start")){
            int refundCount = refundService.getH5MerchantRefundCount(params);
            if(refundCount > 0 ){
                throw new BizException(I18nUtils.get("refund.uncleared", getLang(request)));
            }
        }

    }

    private ClearBatchDTO h5Clear(Long id, HttpServletRequest request, HttpServletResponse response) throws BizException {
        ClearBatchDTO clearBatchDTO = findClearBatchById(id);
        Map<String, Object> map = new HashMap<>(2);
        map.put("clearBatchId",id);
        map.put("state",0);
        List<ClearDetailDTO> detailList  = clearDetailService.find(map,null,null);
        for(ClearDetailDTO clearDetailDTO:detailList){
            try {
                //实际清算金额
                if(clearDetailDTO.getTransAmount().compareTo(BigDecimal.ZERO) > 0 ){
                    clearDetailDTO.setClearAmount(clearDetailDTO.getTransAmount());
                    doH5ClearSuccessResult(clearDetailDTO,request);
                }else{
                    clearDetailDTO.setClearAmount(BigDecimal.ZERO);
                    doH5ClearFailResult(clearDetailDTO,request);
                }
            }catch ( Exception e){

                log.error("h5Clear clearDetailDTO id:" + clearBatchDTO.getId() + ",exception:" + e.getMessage(),e );
                continue;
            }
        }

        //查询清算成功的总比数总金额
        Map<String,Object> clearMap = new HashMap<>();
        clearMap.put("clearBatchId",id);
        ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailService.clearTotal(clearMap);
        clearBatchDTO.setTotalNumber(clearFlowDetailDTO.getCount().longValue());

        ClearDetailDTO  clearDetailDTO = clearDetailService.clearTotal(clearMap);
        clearBatchDTO.setTotalAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getTransAmount());
        clearBatchDTO.setBorrowAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getBorrowAmount());
        clearBatchDTO.setClearAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getClearAmount());

        //TODO 文明名未定
        String fileName = "Bill_"+id+".csv";
        log.info("开始生成平台清算Bill文件，文件名："+fileName);
        //记录清算状态
        clearBatchDTO.setFileName(fileName);

        clearBatchDTO.setUrl(filePath+fileName);
        updateClearBatch(id,clearBatchDTO,request);
        //生成文件
        List<ClearBillCSVDTO> billList = clearDetailService.getH5ClearBillList(id);
        createClearBillFile(billList,fileName,filePath);


        //更新批次状态
        clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        updateClearBatch(id,clearBatchDTO,request);
        return clearBatchDTO;
    }

    private void doH5ClearFailResult(ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws BizException {

        // 修改清算明细状态
        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
        clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);
        Map<String,Object> map = new HashMap<>();
        Long now = System.currentTimeMillis();
        map.put("orgBatchId",clearDetailDTO.getClearBatchId());
        map.put("merchantId",clearDetailDTO.getMerchantId());
        map.put("modifiedDate",now);
        map.put("clearState",StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        qrPayFlowService.updateClearBatch(map);
        //改退款回退状态为已回退
        refundService.clearData(map);
        map.clear();
        map.put("modifiedDate",now);
        map.put("merchantId",clearDetailDTO.getMerchantId());
        map.put("clearBatchId",clearDetailDTO.getClearBatchId());
        clearFlowDetailService.updateClearBatchToFail(map);

    }

    private void doH5ClearSuccessResult( ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws BizException {

//        ClearBatchDTO clearBatch = findClearBatchById(clearDetailDTO.getClearBatchId());
        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        Map<String,Object> map = new HashMap<>();
        map.put("modifiedDate",System.currentTimeMillis());
        map.put("orgBatchId",clearDetailDTO.getClearBatchId());
        map.put("merchantId",clearDetailDTO.getMerchantId());
        map.put("clearTime",System.currentTimeMillis());
        map.put("clearState",StaticDataEnum.CLEAR_BATCH_STATE_1.getCode());
        //修改清算流水表为成功
        clearDetailService.updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,request);

        clearFlowDetailService.clearData(map);
        //改退款回退状态为已清算
        refundService.clearData(map);
        //扫码支付流水改为已清算
        qrPayFlowService.updateClearBatch(map);

    }
}
