package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.ReconciliationDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.Reconciliation;
import com.uwallet.pay.main.model.excel.Transaction;
import com.uwallet.pay.main.model.excel.TransactionRefund;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.AmazonAwsUploadUtil;
import com.uwallet.pay.main.util.I18nUtils;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * ??????
 * </p>
 *
 * @package: com.uwallet.pay.main.main.service.impl
 * @description: ??????
 * @author: baixinyue
 * @date: Created in 2020-02-17 09:59:08
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class ReconciliationServiceImpl extends BaseServiceImpl implements ReconciliationService {

    @Autowired
    private QrPayFlowService qrPayFlowService;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Autowired
    private RefundFlowService refundFlowService;

    @Resource
    private ReconciliationDAO reconciliationDAO;

    @Value("${spring.reconciliationFilePath}")
    private String RECON_FILE_PATH;
    @Resource
    private ReconciliationBatchService reconciliationBatchService;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void saveReconciliation(@NonNull ReconciliationDTO reconciliationDTO, HttpServletRequest request) throws BizException {
        Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
        log.info("save Reconciliation:{}", reconciliation);
        if (reconciliationDAO.insert((Reconciliation) this.packAddBaseProps(reconciliation, request)) != 1) {
            log.error("insert error, data:{}", reconciliation);
            throw new BizException("Insert reconciliation Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReconciliationList(@NonNull List<Reconciliation> reconciliationList, HttpServletRequest request) throws BizException {
        if (CollectionUtil.isNotEmpty(reconciliationList)) {
            if (reconciliationList.size() == 0) {
                throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
            }
            Map<String, Object> params = new HashMap<>(16);
            List<Reconciliation> copy = new ArrayList<>(reconciliationList.size());
            copy.addAll(reconciliationList);
            for (int i = 0; i < copy.size(); i++) {
                params.put("orderNo", copy.get(i).getOrderNo());
                if (findOneReconciliation(params).getId() != null) {
                    copy.remove(i);
                    i--;
                }

            }
            if (CollectionUtil.isNotEmpty(copy)) {
                int rows = reconciliationDAO.insertList(copy);
                if (rows != copy.size()) {
                    log.error("??????????????????????????????({})????????????({})?????????", rows, copy.size());
                    throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
                }
            }
        }
    }

    @Override
    public void updateReconciliation(@NonNull Long id, @NonNull ReconciliationDTO reconciliationDTO, HttpServletRequest request) throws BizException {
        log.info("full update reconciliationDTO:{}", reconciliationDTO);
        Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
        reconciliation.setId(id);
        if (request != null) {
            reconciliation = (Reconciliation) this.packModifyBaseProps(reconciliation, request);
        } else {
            reconciliation.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = reconciliationDAO.update(reconciliation);
        if (cnt != 1) {
            log.error("update error, data:{}", reconciliationDTO);
            throw new BizException("update reconciliation Error!");
        }
    }

    @Override
    public void updateReconciliationSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        reconciliationDAO.updatex(params);
    }

    @Override
    public void logicDeleteReconciliation(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("?????????????????????id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = reconciliationDAO.delete(params);
        if (rows != 1) {
            log.error("??????????????????, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteReconciliation(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("????????????, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = reconciliationDAO.pdelete(params);
        if (rows != 1) {
            log.error("????????????, ???????????????{}?????????", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ReconciliationDTO findReconciliationById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ReconciliationDTO reconciliationDTO = reconciliationDAO.selectOneDTO(params);
        return reconciliationDTO;
    }

    @Override
    public ReconciliationDTO findOneReconciliation(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Reconciliation reconciliation = reconciliationDAO.selectOne(params);
        ReconciliationDTO reconciliationDTO = new ReconciliationDTO();
        if (null != reconciliation) {
            BeanUtils.copyProperties(reconciliation, reconciliationDTO);
        }
        return reconciliationDTO;
    }

    @Override
    public List<ReconciliationDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ReconciliationDTO> resultList = reconciliationDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns???????????????0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return reconciliationDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return reconciliationDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = reconciliationDAO.groupCount(conditions);
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
        return reconciliationDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = reconciliationDAO.groupSum(conditions);
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
    @Transactional(rollbackFor = Exception.class)
    public void importReconciliation(Integer type, MultipartFile file, HttpServletRequest request) throws Exception {
        log.info("??????????????????????????????");
        /* 2021???1???25??? ????????????
              1.??????????????????????????????
              2.??????????????????: u_reconciliation_batch ???
              3.??????????????? ?????? ????????????,??????,
              4.??? ?????????????????? ????????? u_reconciliation??? ??????????????????
         */
        if (null == type || file.isEmpty()){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        //???????????????
//        String originalFilename = "duizhangceshi.csv";//file.getName();
        String originalFilename = file.getOriginalFilename();
        //????????????
        String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
        //25-Jan-2021 ???????????????????????????
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        //????????????
        String newFileName = SnowflakeUtil.generateId() + sfx;
        //???????????????->?????????????????????
        String uploadPath =  RECON_FILE_PATH+ "/" + simpleDateFormat.format(new Date(System.currentTimeMillis())) + "/"+ newFileName;
        //?????????????????????????????????(?????????)
        String fileServerPath = AmazonAwsUploadUtil.upload(file,"/"+uploadPath);
        Long reconBatchId = reconciliationBatchService.saveReconciliationBatch(
                ReconciliationBatchDTO.builder()
                        .thirdFileName(originalFilename)
                        .path(fileServerPath)
                        .fileName(newFileName)
                        .type(type)
                        .state(StaticDataEnum.CHECK_PROCESSING.getCode())
                        .build()
                , request);

        //????????????
        JSONObject countRes = new JSONObject();

        /*if (type == StaticDataEnum.GATEWAY_TYPE_1.getCode() || type ==  StaticDataEnum.GATEWAY_TYPE_2.getCode()) {
            countRes = omiPayReconciliationImport(file, reconBatchId, request);
        } else if */
        if (type == StaticDataEnum.GATEWAY_TYPE_0.getCode()) {
            countRes = latPayReconciliationImport(file, reconBatchId, request);
        }else if (type == StaticDataEnum.GATEWAY_TYPE_8.getCode()) {
            countRes = stripeReconciliationImport(file, reconBatchId, request);
        } /*else if (type == StaticDataEnum.GATEWAY_TYPE_4.getCode()) {
            countRes = integratPayReconciliationImport(file, reconBatchId, request);
        } else if (type == StaticDataEnum.GATEWAY_TYPE_3.getCode()) {
            countRes = latPayDDReconciliationImport(file, reconBatchId, request);
        } else if (type == StaticDataEnum.GATEWAY_TYPE_6.getCode()) {
            countRes = splitReconciliationImport(file, reconBatchId, request);
        }*/
        //??????????????????
        reconciliationBatchService.updateReconciliationBatch(reconBatchId
                ,ReconciliationBatchDTO.builder()
                        .totalNumber(countRes.getInteger("total"))
                        .failNumber(countRes.getInteger("failed"))
                        .state(StaticDataEnum.CHECK_SUCCESS.getCode()).build()
                ,request);
    }

    /**
     * omipay??????????????????
     * @param file
     * @param request
     */
    public JSONObject omiPayReconciliationImport(MultipartFile file, Long reconBatchId, HttpServletRequest request) throws Exception {
        //??????file??????Workbook,????????????????????????????????????,????????????excel?????????sheet???
        List<Transaction> transactionList = new ArrayList<>(1);
        List<TransactionRefund> transactionRefundList = new ArrayList<>(1);
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = null;
        for (int i = 0; i < workbook.getNumberOfSheets(); i ++) {
            if (i == 0) {
                // ??????sheet
                sheet = workbook.getSheetAt(i);
                for (int j = 0; j < sheet.getPhysicalNumberOfRows(); j ++) {
                    if (j > 1) {
                        // ??????row
                        XSSFRow row = sheet.getRow(j);
                        Transaction transaction = new Transaction();
                        for (int k = 0; k < row.getPhysicalNumberOfCells(); k ++) {
                            XSSFCell cell = row.getCell(k);
                            if (k == 1) {
                                transaction.setTransactionNumber(cell.getStringCellValue());
                            } else if (k == 4) {
                                transaction.setPaymentChannel(cell.getStringCellValue());
                            } else if (k == 7) {
                                transaction.setGrossAmount(new BigDecimal(String.valueOf(cell.getNumericCellValue())));
                            } else if (k == 12) {
                                transaction.setTransactionAmount(new BigDecimal(String.valueOf(cell.getNumericCellValue())));
                            } else if (k == 20) {
                                transaction.setOutOrderNumber(cell.getStringCellValue());
                            } else if (k == 0) {
                                transaction.setTransactionTime(cell.getDateCellValue());
                            }
                        }
                        transactionList.add(transaction);
                    }
                }
            } else {
                // ??????sheet
                sheet = workbook.getSheetAt(i);
                for (int j = 0; j < sheet.getPhysicalNumberOfRows(); j ++) {
                    if (j > 1) {
                        // ??????row
                        XSSFRow row = sheet.getRow(j);
                        TransactionRefund transactionRefund = new TransactionRefund();
                        for (int k = 0; k < row.getPhysicalNumberOfCells(); k ++) {
                            XSSFCell cell = row.getCell(k);
                            if (k == 1) {
                                transactionRefund.setTransactionNumber(cell.getStringCellValue());
                            } else if (k == 6) {
                                transactionRefund.setNetRefundAmount(new BigDecimal(String.valueOf(cell.getNumericCellValue())));
                            } else if (k == 5) {
                                transactionRefund.setRefundAmount(new BigDecimal(String.valueOf(cell.getNumericCellValue())));
                            } else if (k == 7) {
                                transactionRefund.setOutRefundNumber(cell.getStringCellValue());
                            } else if (k == 0) {
                                transactionRefund.setRefundTime(cell.getDateCellValue());
                            }
                        }
                        transactionRefundList.add(transactionRefund);
                    }
                }
            }
        }
        //??????????????????
        int failedCount = omiPayTransactionSave(transactionList, transactionRefundList,reconBatchId, request);
        JSONObject res = new JSONObject(4);
        res.put("failed",failedCount);
        res.put("total",transactionList.size()+transactionRefundList.size());
        return res;
    }

    /**
     *
     * @param file
     * @param request
     * @throws Exception
     */
    public JSONObject latPayReconciliationImport(MultipartFile file, Long reconBatchId, HttpServletRequest request) throws Exception {
        List<String> dataList=new ArrayList<String>(1);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
        }catch (Exception e) {
            throw new BizException(e.getMessage());
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        dataList.remove(0);
        List<Transaction> list = new ArrayList<>(dataList.size());
        for (String data : dataList) {
            String[] dataInfo = data.split(",");
            Transaction transaction = new Transaction();
            transaction.setTransactionNumber(dataInfo[0].replace("\"", ""));
            transaction.setTransactionAmount(new BigDecimal(dataInfo[5].replace("\"", "")));
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy hh:MM");
                transaction.setTransactionTime(sdf.parse(dataInfo[3].replace("\"", "")));
                //???????????? Rejected/Accepted
                String transStatus = dataInfo[14].replace("\"", "");
                int statusIntValue = transStatus.equalsIgnoreCase(StaticDataEnum.LATPAY_CARD_TRANS_STATUS_SUCCESS.getMessage())
                        ? StaticDataEnum.LATPAY_CARD_TRANS_STATUS_SUCCESS.getCode() : StaticDataEnum.LATPAY_CARD_TRANS_STATUS_FAILED.getCode();
                transaction.setStatus(statusIntValue);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            list.add(transaction);
        }
        //??????????????????
        int failedCount = latPayTransactionSave(list,reconBatchId, request);
        JSONObject res = new JSONObject(4);
        res.put("failed",failedCount);
        res.put("total",dataList.size());
        return res;
    }



    /**
     * stripe??????????????????
     * @author zhangzeyuan
     * @date 2022/2/15 13:39
     * @param file
     * @param reconBatchId
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    public JSONObject stripeReconciliationImport(MultipartFile file, Long reconBatchId, HttpServletRequest request) throws Exception {
        CsvReader csvReader = new CsvReader();
        csvReader.setContainsHeader(true);
        CsvContainer csv = csvReader.read(transferToFile(file), StandardCharsets.UTF_8);
        int count = csv.getRowCount();
        List<Transaction> list = new ArrayList<>(count);

        for (CsvRow row : csv.getRows()) {
            Transaction transaction = new Transaction();

            //stripe ??????ID
            transaction.setTransactionNumber(row.getField("id"));
            String amount = null == row.getField("amount") ? row.getField("Amount") : row.getField("amount");
            //????????????
            transaction.setTransactionAmount(StringUtils.isNotBlank(amount) ? new BigDecimal(amount) : BigDecimal.ZERO);
            transaction.setPaymentChannel(StaticDataEnum.GATEWAY_TYPE_8.getCode() + "");
            //withhold flow order_no
            transaction.setOutOrderNumber(row.getField("id (metadata)"));

            String desc = "";
            if(null != row.getField("Description")){
                desc = row.getField("Description");
            }
            if(null != row.getField("description")){
                desc = row.getField("description");
            }
            transaction.setDesc(desc);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String replaceDate = null == row.getField("created") ? row.getField("Created (UTC)") : row.getField("created");
                transaction.setTransactionTime(sdf.parse(replaceDate));
                //???????????? Paid/requires_payment_method/requires_action/Failed
                String transStatus = null == row.getField("status") ? row.getField("Status") : row.getField("status");
                int state = 0;
                if(transStatus.equals(StaticDataEnum.STRIPE_CARD_TRANS_STATUS_SUCCESS.getMessage())){
                    state = StaticDataEnum.STRIPE_CARD_TRANS_STATUS_SUCCESS.getCode();
                }else if(transStatus.equals(StaticDataEnum.STRIPE_CARD_TRANS_STATUS_FAILED.getMessage())){
                    state = StaticDataEnum.STRIPE_CARD_TRANS_STATUS_FAILED.getCode();
                }else if(transStatus.equals(StaticDataEnum.STRIPE_CARD_TRANS_STATUS_3DS.getMessage())){
                    state = StaticDataEnum.STRIPE_CARD_TRANS_STATUS_3DS.getCode();
                }else{
                    state = StaticDataEnum.STRIPE_CARD_TRANS_STATUS_OTHERS.getCode();
                }
                transaction.setStatus(state);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            list.add(transaction);
        }

        JSONObject res = new JSONObject(4);
        int failedCount = 0;

        if(count > 0){
            failedCount = stripeTransactionSave(list,reconBatchId, request);
        }
        res.put("failed",failedCount);
        res.put("total", count);

        return res;
    }



    private File transferToFile(MultipartFile multipartFile) {
        //????????????????????????????????????????????????java ????????????????????? ?????? MultipartFile.transferto()?????? ???
        File file = null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String[] filename = originalFilename.split("\\.");
            file=File.createTempFile(filename[0], filename[1]);
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     *
     * @param file
     * @param request
     * @throws Exception
     */
    public JSONObject latPayDDReconciliationImport(MultipartFile file, Long reconBatchId, HttpServletRequest request) throws Exception {
        List<String> dataList=new ArrayList<String>(1);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
        }catch (Exception e) {
            throw new BizException(e.getMessage());
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        dataList.remove(0);
        List<Transaction> list = new ArrayList<>(dataList.size());
        for (String data : dataList) {
            String[] dataInfo = data.split(",");
            Transaction transaction = new Transaction();
            transaction.setTransactionNumber(dataInfo[2].replace("\"", ""));
            transaction.setTransactionAmount(new BigDecimal(dataInfo[6].replace("\"", "")).setScale(2, RoundingMode.HALF_UP));
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy hh:MM");
                transaction.setTransactionTime(sdf.parse(dataInfo[3].replace("\"", "")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            list.add(transaction);
        }
        //??????????????????
        int failedCount = latPayDDTransactionSave(list,reconBatchId, request);
        JSONObject res = new JSONObject(4);
        res.put("failed",failedCount);
        res.put("total",dataList.size());
        return res;
    }

    /**
     * integraPay ??????????????????
     * @param file
     * @param request
     * @throws Exception
     */
    public JSONObject integratPayReconciliationImport(MultipartFile file, Long reconBatchId, HttpServletRequest request) throws Exception {
        List<String> dataList = new ArrayList<>(1);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
        }catch (Exception e) {
            throw new BizException(e.getMessage());
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        dataList.remove(0);
        List<Transaction> transactionList = new ArrayList<>(1);
        List<TransactionRefund> transactionRefundList = new ArrayList<>(1);
        for (String data : dataList) {
            String[] dataInfo = data.split(",");
            if (dataInfo[5].equals(StaticDataEnum.INTEGRA_PAY_RECONCILIATION_STATUS_0.getMessage())) {
                Transaction transaction = new Transaction();
                transaction.setTransactionNumber(dataInfo[3]);
                transaction.setTransactionAmount(new BigDecimal(dataInfo[4]));
                transaction.setOutOrderNumber(dataInfo[7]);
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy hh:MM");
                    transaction.setTransactionTime(sdf.parse(dataInfo[1]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                transactionList.add(transaction);
            } else {
                TransactionRefund transactionRefund = new TransactionRefund();

                transactionRefundList.add(transactionRefund);
            }
        }
        //??????integraPay????????????
        int failedCount = integraPayTransactionSave(transactionList, transactionRefundList,reconBatchId, request);
        JSONObject res = new JSONObject(4);
        res.put("failed",failedCount);
        res.put("total",dataList.size());
        return res;
    }

    /**
     * split ??????????????????
     * @param file
     * @param request
     * @throws Exception
     */
    public JSONObject splitReconciliationImport(MultipartFile file, Long reconBatchId, HttpServletRequest request) throws Exception {
        List<String> dataList=new ArrayList<String>(1);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
        }catch (Exception e) {
            throw new BizException(e.getMessage());
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        dataList.remove(0);
        List<Transaction> list = new ArrayList<>(dataList.size());
        for (String data : dataList) {
            String[] dataInfo = data.split(",");
            System.out.println(Arrays.asList(dataInfo));
            Transaction transaction = new Transaction();
            transaction.setOutOrderNumber(dataInfo[0]);
            transaction.setTransactionAmount(new BigDecimal(dataInfo[12]));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            transaction.setTransactionTime(sdf.parse(dataInfo[5]));
            list.add(transaction);
        }
        JSONObject res = new JSONObject(4);
        int failedCount = splitTransactionSave(list,reconBatchId, request);
        res.put("failed", failedCount);
        res.put("total",dataList.size());
        return res;
    }

    /**
     * ??????omipay????????????????????????????????????????????????
     * @param transactionList
     * @param transactionRefundList
     * @param request
     */
    public int omiPayTransactionSave(List<Transaction> transactionList, List<TransactionRefund> transactionRefundList, Long reconBatchId, HttpServletRequest request) throws BizException {
        //????????????????????????
        List<Reconciliation> reconciliationList = new ArrayList<>(1);
        if (transactionList != null && transactionList.size() > 0) {
            for (Transaction transaction : transactionList) {
                ReconciliationDTO reconciliationDTO = new ReconciliationDTO();
                //??????????????????
                if (transaction.getPaymentChannel().equals(StaticDataEnum.ALI_PAY.getMessage())) {
                    reconciliationDTO.setType(StaticDataEnum.GATEWAY_TYPE_1.getCode());
                } else if (transaction.getPaymentChannel().equals(StaticDataEnum.WECHAT_PAY.getMessage())) {
                    reconciliationDTO.setType(StaticDataEnum.GATEWAY_TYPE_2.getCode());
                }
                reconciliationDTO.setPaymentTime(transaction.getTransactionTime().getTime());
                reconciliationDTO.setTransactionType(StaticDataEnum.PAY.getCode());
                reconciliationDTO.setOrderNo(transaction.getOutOrderNumber().toString());
                reconciliationDTO.setTripartiteOrderNo(transaction.getTransactionNumber());
                reconciliationDTO.setAmountEntered(transaction.getTransactionAmount());
                reconciliationDTO.setGrossAmount(transaction.getGrossAmount());
                reconciliationDTO.setState(0);
                reconciliationDTO.setBatchId(reconBatchId);
                Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
                reconciliationList.add((Reconciliation) this.packAddBaseProps(reconciliation, request));
            }
        }
        if (transactionRefundList != null && transactionRefundList.size() > 0) {
            for (TransactionRefund transactionRefund : transactionRefundList) {
                ReconciliationDTO reconciliationDTO = new ReconciliationDTO();
                reconciliationDTO.setPaymentTime(transactionRefund.getRefundTime().getTime());
                reconciliationDTO.setType(StaticDataEnum.RECONCILIATION_OMI_PAY_REFUND.getCode());
                reconciliationDTO.setTransactionType(StaticDataEnum.REFUND.getCode());
                reconciliationDTO.setOrderNo(transactionRefund.getOutRefundNumber());
                reconciliationDTO.setTripartiteOrderNo(transactionRefund.getTransactionNumber());
                reconciliationDTO.setAmountEntered(transactionRefund.getNetRefundAmount());
                reconciliationDTO.setGrossAmount(transactionRefund.getRefundAmount());
                reconciliationDTO.setState(0);
                reconciliationDTO.setBatchId(reconBatchId);
                Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
                reconciliationList.add((Reconciliation) this.packAddBaseProps(reconciliation, request));
            }
        }
        //??????
        saveReconciliationList(reconciliationList, request);
        //??????
        int count = 0;
        try {
           count =  accountChecking(reconciliationList);
        } catch (Exception e) {
            log.info("account check failed, data:{}, error message:{}, e:{}", reconciliationList, e.getMessage(), e);
        }
        return count;
    }

    /**
     * latpay??????????????????
     * @param transactionList
     * @param request
     * @throws BizException
     */
    public int latPayTransactionSave(List<Transaction> transactionList, Long reconBatchId, HttpServletRequest request) throws BizException {
        List<Reconciliation> reconciliationList = new ArrayList<>(1);
        if (transactionList != null && transactionList.size() > 0) {
            Map<String, Object> condition = new HashMap<>(2);
            for (Transaction transaction : transactionList) {
                ReconciliationDTO reconciliationDTO = new ReconciliationDTO();
                //????????????????????????
                condition.put("lpsTransactionId", transaction.getTransactionNumber());
                WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(condition);
                if (withholdFlowDTO.getId() != null) {
                    //??????????????????
                    reconciliationDTO.setType(StaticDataEnum.GATEWAY_TYPE_0.getCode());
                    reconciliationDTO.setPaymentTime(transaction.getTransactionTime().getTime());
                    reconciliationDTO.setTransactionType(StaticDataEnum.PAY.getCode());
                    reconciliationDTO.setOrderNo(withholdFlowDTO.getOrdreNo());
                    reconciliationDTO.setTripartiteOrderNo(transaction.getTransactionNumber());
                    reconciliationDTO.setAmountEntered(transaction.getTransactionAmount());
                    reconciliationDTO.setGrossAmount(transaction.getGrossAmount());
                    reconciliationDTO.setState(transaction.getStatus());
                    reconciliationDTO.setBatchId(reconBatchId);
                    Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
                    reconciliationList.add((Reconciliation) this.packAddBaseProps(reconciliation, request));
                }
            }
        }
        //??????
        saveReconciliationList(reconciliationList, request);
        //??????
        int count = 0;
        try {
           count = accountChecking(reconciliationList);
        } catch (Exception e) {
            log.info("account check failed, data:{}, error message:{}, e:{}", reconciliationList, e.getMessage(), e);
        }
        return count;
    }


    public int stripeTransactionSave(List<Transaction> transactionList, Long reconBatchId, HttpServletRequest request) throws BizException {
        //????????????
        List<Reconciliation> reconciliationList = new ArrayList<>(transactionList.size());
        for (Transaction transaction : transactionList) {
            ReconciliationDTO reconciliationDTO = new ReconciliationDTO();
            //??????????????????
            reconciliationDTO.setType(StaticDataEnum.GATEWAY_TYPE_8.getCode());
            reconciliationDTO.setPaymentTime(transaction.getTransactionTime().getTime());
            //??????
            if(transaction.getDesc().equals("repayment")){
                reconciliationDTO.setTransactionType(StaticDataEnum.REPAY.getCode());
            }else{
                reconciliationDTO.setTransactionType(StaticDataEnum.PAY.getCode());
            }
            reconciliationDTO.setOrderNo(transaction.getOutOrderNumber());
            reconciliationDTO.setTripartiteOrderNo(transaction.getTransactionNumber());
            reconciliationDTO.setAmountEntered(transaction.getTransactionAmount());
            reconciliationDTO.setGrossAmount(transaction.getGrossAmount());
            reconciliationDTO.setState(transaction.getStatus());
            reconciliationDTO.setBatchId(reconBatchId);
            Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
            reconciliationList.add((Reconciliation) this.packAddBaseProps(reconciliation, request));
        }
        //??????
        saveReconciliationList(reconciliationList, request);
        //??????
        int count = 0;
        try {
            count = accountCheckingStripe(reconciliationList);
        } catch (Exception e) {
            log.info("account check failed, data:{}, error message:{}, e:{}", reconciliationList, e.getMessage(), e);
        }
        return count;
    }

    /**
     * latpay DD??????????????????
     * @param transactionList
     * @param request
     * @throws Exception
     */
    public int latPayDDTransactionSave(List<Transaction> transactionList, Long reconBatchId, HttpServletRequest request) throws Exception {
        List<Reconciliation> reconciliationList = new ArrayList<>(1);
        if (transactionList != null && transactionList.size() > 0) {
            Map<String, Object> condition = new HashMap<>(2);
            for (Transaction transaction : transactionList) {
                ReconciliationDTO reconciliationDTO = new ReconciliationDTO();
                //????????????????????????
                condition.put("orderNo", transaction.getTransactionNumber());
                WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(condition);
                if (withholdFlowDTO.getId() != null) {
                    //??????????????????
                    reconciliationDTO.setType(StaticDataEnum.GATEWAY_TYPE_3.getCode());
                    reconciliationDTO.setPaymentTime(transaction.getTransactionTime().getTime());
                    reconciliationDTO.setTransactionType(StaticDataEnum.PAY.getCode());
                    reconciliationDTO.setOrderNo(withholdFlowDTO.getOrdreNo());
                    reconciliationDTO.setTripartiteOrderNo(withholdFlowDTO.getLpsTransactionId());
                    reconciliationDTO.setAmountEntered(transaction.getTransactionAmount());
                    reconciliationDTO.setGrossAmount(transaction.getGrossAmount());
                    reconciliationDTO.setState(0);
                    reconciliationDTO.setBatchId(reconBatchId);
                    Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
                    reconciliationList.add((Reconciliation) this.packAddBaseProps(reconciliation, request));
                }
            }
        }
        //??????
        saveReconciliationList(reconciliationList, request);
        //??????
        int count = 0;
        try {
           count = accountChecking(reconciliationList);
        } catch (Exception e) {
            log.info("account check failed, data:{}, error message:{}, e:{}", reconciliationList, e.getMessage(), e);
        }
        return count;
    }

    /**
     * integraPay??????????????????
     * @param transactionList
     * @param transactionRefundList
     * @param request
     * @throws BizException
     */
    public int integraPayTransactionSave(List<Transaction> transactionList, List<TransactionRefund> transactionRefundList, Long reconBatchId, HttpServletRequest request) throws BizException {
        //????????????????????????
        List<Reconciliation> reconciliationList = new ArrayList<>(1);
        if (transactionList != null && transactionList.size() > 0) {
            for (Transaction transaction : transactionList) {
                ReconciliationDTO reconciliationDTO = new ReconciliationDTO();
                //??????????????????
                reconciliationDTO.setType(StaticDataEnum.GATEWAY_TYPE_4.getCode());
                reconciliationDTO.setPaymentTime(transaction.getTransactionTime().getTime());
                reconciliationDTO.setTransactionType(StaticDataEnum.PAY.getCode());
                reconciliationDTO.setOrderNo(transaction.getOutOrderNumber().toString());
                reconciliationDTO.setTripartiteOrderNo(transaction.getTransactionNumber());
                reconciliationDTO.setAmountEntered(transaction.getTransactionAmount());
                reconciliationDTO.setGrossAmount(transaction.getGrossAmount());
                reconciliationDTO.setState(0);
                reconciliationDTO.setBatchId(reconBatchId);
                Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
                reconciliationList.add((Reconciliation) this.packAddBaseProps(reconciliation, request));
            }
        }
        if (transactionRefundList != null && transactionRefundList.size() > 0) {
            for (TransactionRefund transactionRefund : transactionRefundList) {
                ReconciliationDTO reconciliationDTO = new ReconciliationDTO();
                reconciliationDTO.setPaymentTime(transactionRefund.getRefundTime().getTime());
                reconciliationDTO.setType(StaticDataEnum.RECONCILIATION_INTEGRA_PAY_REFUND.getCode());
                reconciliationDTO.setTransactionType(StaticDataEnum.REFUND.getCode());
                reconciliationDTO.setOrderNo(transactionRefund.getOutRefundNumber());
                reconciliationDTO.setTripartiteOrderNo(transactionRefund.getTransactionNumber());
                reconciliationDTO.setAmountEntered(transactionRefund.getNetRefundAmount());
                reconciliationDTO.setGrossAmount(transactionRefund.getRefundAmount());
                reconciliationDTO.setState(0);
                Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
                reconciliationList.add((Reconciliation) this.packAddBaseProps(reconciliation, request));
            }
        }
        //??????
        saveReconciliationList(reconciliationList, request);
        //??????
        int count = 0;
        try {
           count = accountChecking(reconciliationList);
        } catch (Exception e) {
            log.info("account check failed, data:{}, error message:{}, e:{}", reconciliationList, e.getMessage(), e);
        }
        return count;
    }

    /**
     * split????????????
     * @param transactionList
     * @param request
     * @throws BizException
     */
    public int splitTransactionSave(List<Transaction> transactionList, Long reconBatchId, HttpServletRequest request) throws BizException {
        List<Reconciliation> reconciliationList = new ArrayList<>(1);
        if (transactionList != null && transactionList.size() > 0) {
            Map<String, Object> condition = new HashMap<>(2);
            for (Transaction transaction : transactionList) {
                ReconciliationDTO reconciliationDTO = new ReconciliationDTO();
                //????????????????????????
                condition.put("splitNo", transaction.getTransactionNumber());
                WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(condition);
                if (withholdFlowDTO.getId() != null) {
                    //??????????????????
                    reconciliationDTO.setType(StaticDataEnum.GATEWAY_TYPE_6.getCode());
                    reconciliationDTO.setPaymentTime(transaction.getTransactionTime().getTime());
                    reconciliationDTO.setTransactionType(StaticDataEnum.PAY.getCode());
                    reconciliationDTO.setOrderNo(withholdFlowDTO.getOrdreNo());
                    reconciliationDTO.setTripartiteOrderNo(withholdFlowDTO.getSplitNo());
                    reconciliationDTO.setAmountEntered(transaction.getTransactionAmount());
                    reconciliationDTO.setGrossAmount(transaction.getGrossAmount());
                    reconciliationDTO.setState(0);
                    reconciliationDTO.setBatchId(reconBatchId);
                    Reconciliation reconciliation = BeanUtil.copyProperties(reconciliationDTO, new Reconciliation());
                    reconciliationList.add((Reconciliation) this.packAddBaseProps(reconciliation, request));
                }
            }
        }
        //??????
        saveReconciliationList(reconciliationList, request);
        //??????,?????? ????????????????????????????????????????????????
        int count = 0;
        try {
           count = accountChecking(reconciliationList);
        } catch (Exception e) {
            log.info("account check failed, data:{}, error message:{}, e:{}", reconciliationList, e.getMessage(), e);
        }
        return count;
    }

    /**
     * ????????????
     */
    @Async("taskExecutor")
    @Override
    public int accountChecking(List<Reconciliation> reconciliationList) throws Exception {
        log.info("account check");
        // ??????
        int failedCount = 0;
        for (Reconciliation reconciliation : reconciliationList) {
            ReconciliationDTO reconciliationDTO = BeanUtil.copyProperties(reconciliation, new ReconciliationDTO());
            failedCount += doAccountChecking(reconciliationDTO);
        }
        // ????????????????????????
        handleCheckDealReconciliation();
        return failedCount;
    }


    @Override
    public int accountCheckingStripe(List<Reconciliation> reconciliationList) throws Exception {
        // ??????
        int failedCount = 0;
        for (Reconciliation reconciliation : reconciliationList) {
            ReconciliationDTO reconciliationDTO = BeanUtil.copyProperties(reconciliation, new ReconciliationDTO());
            failedCount += doAccountChecking(reconciliationDTO);
        }
        return failedCount;
    }


    /**
     * ????????????
     * @param reconciliationDTO
     */
    public int doAccountChecking(ReconciliationDTO reconciliationDTO) {
        //????????????,???????????????????????? ??????1, ????????????0
        int compareRes =0;
        Map<String, Object> params = new HashMap<>(1);
        params.put("ordreNo", reconciliationDTO.getOrderNo());
        if (reconciliationDTO.getTransactionType() == StaticDataEnum.PAY.getCode()
                || reconciliationDTO.getTransactionType() == StaticDataEnum.REPAY.getCode()) {
            WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
            // ?????????omipay????????????????????????omipay?????????????????????????????????????????????????????????
            boolean hasOmiPayOrderNo = ((reconciliationDTO.getType().equals(StaticDataEnum.GATEWAY_TYPE_1.getCode())
                    || reconciliationDTO.getType().equals(StaticDataEnum.GATEWAY_TYPE_2.getCode()))
                    && StringUtils.isEmpty(withholdFlowDTO.getOmiPayOrderNo()));
            if (hasOmiPayOrderNo) {
                try {
                    withholdFlowDTO.setOmiPayOrderNo(reconciliationDTO.getTripartiteOrderNo());
                    withholdFlowService.updateWithholdFlow(withholdFlowDTO.getId(), withholdFlowDTO, null);
                    reconciliationDTO.setState(StaticDataEnum.CHECK_DEAL.getCode());
                    updateReconciliation(reconciliationDTO.getId(), reconciliationDTO, null);
                } catch (Exception e) {
                    log.info("update fail, withholdFlowDTO:{}, reconciliationDTO:{}, error message:{}", withholdFlowDTO, reconciliationDTO, e.getMessage());
                }
            } else if(reconciliationDTO.getType().equals(StaticDataEnum.GATEWAY_TYPE_8.getCode())){
                //stripe ??????
                compareRes = stripeAccountCheck(withholdFlowDTO, reconciliationDTO);
            }else{
                if (withholdFlowDTO.getTransAmount().compareTo(reconciliationDTO.getAmountEntered()) == 0) {
                    reconciliationDTO.setCheckState(StaticDataEnum.CHECK_SUCCESS.getCode());
                } else {
                    reconciliationDTO.setCheckState(StaticDataEnum.CHECK_FAIL.getCode());
                }
                /*
                 *  ?????????????????????-->?????? 0
                 *  ??????????????? ??????,??????????????????/??????????????????,????????? 1;
                 */
                compareRes = this.compareResultValue(withholdFlowDTO,null,reconciliationDTO);
                Long time = System.currentTimeMillis();
                withholdFlowDTO.setCheckTime(time);
                withholdFlowDTO.setCheckState(StaticDataEnum.IS_CHECK_1.getCode());
                reconciliationDTO.setCheckTime(time);
                try {
                    log.info("update, withholdFlowDTO:{}, reconciliationDTO:{}", withholdFlowDTO, reconciliationDTO);
                    updateReconciliation(reconciliationDTO.getId(), reconciliationDTO, null);
                    withholdFlowService.updateWithholdFlow(withholdFlowDTO.getId(), withholdFlowDTO, null);
                } catch (Exception e) {
                    log.info("update fail, withholdFlowDTO:{}, reconciliationDTO:{}, error message:{}", withholdFlowDTO, reconciliationDTO, e.getMessage());
                }
            }
        } else {
            RefundFlowDTO refundFlowDTO = refundFlowService.findOneRefundFlow(params);
            // ????????????????????????????????????????????????????????????????????????????????????
            if (StringUtils.isEmpty(refundFlowDTO.getOmiPayRefundOrderNo())) {
                try {
                    refundFlowDTO.setOmiPayRefundOrderNo(reconciliationDTO.getTripartiteOrderNo());
                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                    reconciliationDTO.setState(StaticDataEnum.CHECK_DEAL.getCode());
                    updateReconciliation(reconciliationDTO.getId(), reconciliationDTO, null);
                } catch (Exception e) {
                    log.info("update fail, refundFlowDTO:{}, reconciliationDTO:{}, error message:{}, e:{}", refundFlowDTO, reconciliationDTO, e.getMessage(), e);
                }
            } else {
                if (refundFlowDTO.getRefundAmount().compareTo(reconciliationDTO.getAmountEntered()) == 0) {
                    reconciliationDTO.setCheckState(StaticDataEnum.CHECK_SUCCESS.getCode());
                } else {
                    reconciliationDTO.setCheckState(StaticDataEnum.CHECK_FAIL.getCode());
                }
                /*  ?????????????????????-->?????? 0
                 *  ??????????????? ??????,??????????????????/??????????????????,????????? 1;
                 */
                compareRes = this.compareResultValue(null,refundFlowDTO,reconciliationDTO);
                Long time = System.currentTimeMillis();
                refundFlowDTO.setCheckTime(time);
                refundFlowDTO.setCheckState(StaticDataEnum.IS_CHECK_1.getCode());
                reconciliationDTO.setCheckTime(time);
                try {
                    log.info("update, refundDTO:{}, reconciliationDTO:{}", refundFlowDTO, reconciliationDTO);
                    updateReconciliation(reconciliationDTO.getId(), reconciliationDTO, null);
                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                } catch (Exception e) {
                    log.info("update fail, refundDTO:{}, reconciliationDTO:{}, error message:{}", refundFlowDTO, reconciliationDTO, e.getMessage());
                }
            }
        }
        return compareRes;
    }

    private int stripeAccountCheck(WithholdFlowDTO withholdFlowDTO, ReconciliationDTO reconciliationDTO){
        int failedCount = stripeOrderCheck(withholdFlowDTO, reconciliationDTO);

        int checkState = StaticDataEnum.CHECK_SUCCESS.getCode();
        if(failedCount > 0){
            checkState = StaticDataEnum.CHECK_FAIL.getCode();
        }

        reconciliationDTO.setCheckState(checkState);
        Long time = System.currentTimeMillis();
        withholdFlowDTO.setCheckTime(time);
        withholdFlowDTO.setCheckState(StaticDataEnum.IS_CHECK_1.getCode());
        reconciliationDTO.setCheckTime(time);
        try {
            log.info("update, withholdFlowDTO:{}, reconciliationDTO:{}", withholdFlowDTO, reconciliationDTO);
            updateReconciliation(reconciliationDTO.getId(), reconciliationDTO, null);
            withholdFlowService.updateWithholdFlow(withholdFlowDTO.getId(), withholdFlowDTO, null);
        } catch (Exception e) {
            log.info("update fail, withholdFlowDTO:{}, reconciliationDTO:{}, error message:{}", withholdFlowDTO, reconciliationDTO, e.getMessage());
        }
        return failedCount;
    }

    private int stripeOrderCheck(WithholdFlowDTO withholdFlowDTO, ReconciliationDTO reconciliationDTO){
        int res = 0;

        if(null == withholdFlowDTO || null == withholdFlowDTO.getId()){
            res = 1;
            return res;
        }

        //????????????
        if (withholdFlowDTO.getTransAmount().compareTo(reconciliationDTO.getAmountEntered()) != 0) {
            reconciliationDTO.setCheckState(StaticDataEnum.CHECK_FAIL.getCode());
            res = 1;
            return res;
        }

        //????????????
        int state = reconciliationDTO.getState();
        if(state  == StaticDataEnum.STRIPE_CARD_TRANS_STATUS_SUCCESS.getCode()){
            if(!withholdFlowDTO.getState().equals(StaticDataEnum.STRIPE_CARD_TRANS_STATUS_SUCCESS.getCode())){
                reconciliationDTO.setCheckState(StaticDataEnum.CHECK_FAIL.getCode());
                res = 1;
            }
        }else if(state  == StaticDataEnum.STRIPE_CARD_TRANS_STATUS_3DS.getCode()){
            if(!withholdFlowDTO.getState().equals(44)){
                reconciliationDTO.setCheckState(StaticDataEnum.CHECK_FAIL.getCode());
                res = 1;
            }
        }else{
            if(!withholdFlowDTO.getState().equals(StaticDataEnum.STRIPE_CARD_TRANS_STATUS_FAILED.getCode())){
                reconciliationDTO.setCheckState(StaticDataEnum.CHECK_FAIL.getCode());
                res = 1;
            }
        }
        return res;
    }


    /**
     * ?????? ????????????????????? ??????????????????
     *  ?????????????????????-->?????? 0
     *  ??????????????? ??????,??????????????????/??????????????????,????????? 1;
     * @param withholdFlowDTO
     * @param refundFlowDTO
     * @param reconciliationDTO
     * @return
     */
    private int compareResultValue(WithholdFlowDTO withholdFlowDTO, RefundFlowDTO refundFlowDTO, ReconciliationDTO reconciliationDTO) {
        int res = 0;
        //????????????????????? ==> 1??????????????? ???2???????????????
        int localTranState = withholdFlowDTO != null ? withholdFlowDTO.getState() : refundFlowDTO.getState();
        if (localTranState != StaticDataEnum.LOCAL_TRANS_STATE_0.getCode()){
            res = (localTranState == reconciliationDTO.getState()) ? 0 : 1;
        }
        return res;
    }

    /**
     * ????????????????????????
     */
    public void handleCheckDealReconciliation() {
        // ?????????????????????????????????
        Map<String, Object> params = new HashMap<>(1);
        params.put("state", StaticDataEnum.CHECK_DEAL.getCode());
        List<ReconciliationDTO> reconciliationDTOList = find(params, null, null);
        params.clear();
        if (reconciliationDTOList != null && !reconciliationDTOList.isEmpty()) {
            for (ReconciliationDTO reconciliationDTO : reconciliationDTOList) {
                QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(Long.valueOf(reconciliationDTO.getOrderNo()));
                if (qrPayFlowDTO.getState().equals(StaticDataEnum.TRANS_STATE_31.getCode())) {
                    doAccountChecking(reconciliationDTO);
                }
            }
        }
    }

    @Override
    public int countReconciliationDetail(Map<String, Object> params) {
        return reconciliationDAO.countReconciliationDetail(params);
    }

    @Override
    public List<ReconciliationDetailDTO> findReconciliationDetail(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        this.getUnionParams(params, scs, pc);
        return reconciliationDAO.findReconciliationDetail(params);
    }

}
