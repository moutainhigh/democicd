package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.IllionInstitutionsDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.IllionInstitutions;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description:
 * @author: xucl
 * @date: Created in 2021-03-19 09:37:47
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class IllionInstitutionsServiceImpl extends BaseServiceImpl implements IllionInstitutionsService {
    @Resource
    private IllionInstitutionsDAO illionInstitutionsDAO;
    @Resource
    private ServerService serverService;
    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private StaticDataService staticDataService;
    @Autowired
    private AliyunSmsService aliyunSmsService;
    @Autowired
    private MailTemplateService mailTemplateService;
    @Autowired
    private IllionSubmitLogService illionSubmitLogService;
    @Autowired
    private IllionSubmitStatusLogService  illionSubmitStatusLogService;
    /**
     * illion 三方请求URL
     */
    @Value("${illion.institutions}")
    private String ILLION_GATEWAY_URL_PREFIX;

    @Value("${illion.referralCode}")
    private String REFERRAL_CODE_PREFIX;

    @Value("${illion.callback}")
    private String DATA_SYSTEM_CALL_BACK_URL;

    @Value("${illion.requestNumDays}")
    private Integer BANK_STATEMENT_MAX_REQUIRED_DAYS_CONFIG;

    @Value("${illion.vsersion}")
    private String vsersion;

    @Value("${illion.fetchAll}")
    private String fetchAll;

    @Value("${illion.appKey}")
    private String ILLION_APP_KEY;
    /**
     * mfa需要验证的type类型 集合
     */
    private static final List<String> MFA_TYPE_CHECK_LIST;
    private static final List<String> NORMAL_LOGIN_TYPE_CHECK_LIST;
    /**
     * ilion记录在redis存储时长
     * */
    private static final Long ILLION_SUBMIT_TIME=48*60*60L;

    static {
        MFA_TYPE_CHECK_LIST = Arrays.asList("input", "password", "set","options");
        NORMAL_LOGIN_TYPE_CHECK_LIST = Arrays.asList("TEXT","password","captcha","select");
    }

    @Autowired
    private UserServiceImpl userService;
    @Override
    public void saveIllionInstitutions(@NonNull IllionInstitutionsDTO illionInstitutionsDTO, HttpServletRequest request) throws BizException {
        IllionInstitutions illionInstitutions = BeanUtil.copyProperties(illionInstitutionsDTO, new IllionInstitutions());
        log.info("save IllionInstitutions:{}", illionInstitutions);
        if (illionInstitutionsDAO.insert((IllionInstitutions) this.packAddBaseProps(illionInstitutions, request)) != 1) {
            log.error("insert error, data:{}", illionInstitutions);
            throw new BizException("Insert illionInstitutions Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveIllionInstitutionsList(@NonNull List<IllionInstitutions> illionInstitutionsList, HttpServletRequest request) throws BizException {
        if (illionInstitutionsList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = illionInstitutionsDAO.insertList(illionInstitutionsList);
        if (rows != illionInstitutionsList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, illionInstitutionsList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateIllionInstitutions(@NonNull Long id, @NonNull IllionInstitutionsDTO illionInstitutionsDTO, HttpServletRequest request) throws BizException {
        log.info("full update illionInstitutionsDTO:{}", illionInstitutionsDTO);
        IllionInstitutions illionInstitutions = BeanUtil.copyProperties(illionInstitutionsDTO, new IllionInstitutions());
        illionInstitutions.setId(id);
        int cnt = illionInstitutionsDAO.update((IllionInstitutions) this.packModifyBaseProps(illionInstitutions, request));
        if (cnt != 1) {
            log.error("update error, data:{}", illionInstitutionsDTO);
            throw new BizException("update illionInstitutions Error!");
        }
    }

    @Override
    public void updateIllionInstitutionsSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        illionInstitutionsDAO.updatex(params);
    }

    @Override
    public void logicDeleteIllionInstitutions(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = illionInstitutionsDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteIllionInstitutions(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = illionInstitutionsDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public IllionInstitutionsDTO findIllionInstitutionsById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        IllionInstitutionsDTO illionInstitutionsDTO = illionInstitutionsDAO.selectOneDTO(params);
        return illionInstitutionsDTO;
    }

    @Override
    public IllionInstitutionsDTO findOneIllionInstitutions(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        IllionInstitutions illionInstitutions = illionInstitutionsDAO.selectOne(params);
        IllionInstitutionsDTO illionInstitutionsDTO = new IllionInstitutionsDTO();
        if (null != illionInstitutions) {
            BeanUtils.copyProperties(illionInstitutions, illionInstitutionsDTO);
        }
        return illionInstitutionsDTO;
    }

    @Override
    public List<IllionInstitutionsDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<IllionInstitutionsDTO> resultList = illionInstitutionsDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return illionInstitutionsDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return illionInstitutionsDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = illionInstitutionsDAO.groupCount(conditions);
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
        return illionInstitutionsDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = illionInstitutionsDAO.groupSum(conditions);
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
    public JSONObject getInstitutions(JSONObject param, HttpServletRequest request) throws Exception {

        // 校验该用户是否处在禁用期，禁用期内不能提交illion
        JSONObject searchInfo = new JSONObject();
        searchInfo.put("userId", param.getLong("userId"));
        JSONObject creditUserInfo = serverService.findCreditUserInfo(searchInfo);
        Integer creditUserStateInteger = creditUserInfo.getInteger("state");
        Integer disableDate = creditUserInfo.getInteger("disableDate");
        if (disableDate!=null&&0!=disableDate.intValue()){
            throw new BizException("Your account credit check is disabled. Please apply for opening BNPL again in "+(disableDate==null?0:disableDate)+" days.");
        }else if (creditUserStateInteger!=null&&21==creditUserStateInteger.intValue()){
            throw new BizException("Your account credit check is disabled. Please apply for opening BNPL again in "+(disableDate==null?0:disableDate)+" days.");
        }

        // 什么不传默认返回8条机构信息
        //todo
        JSONObject result =new JSONObject();
        //分页信息
        Integer pageNo = param.getInteger("pageNo");
        Integer pageSize = param.getInteger("pageSize");
        Boolean isAll = param.getBoolean("isAll");
        if (isAll!=null&&isAll){
            String insResult = HttpClientUtils.sendGetByHeader(ILLION_GATEWAY_URL_PREFIX + "/institutions?region=au",ILLION_APP_KEY);
            JSONObject jsonObjects = JSONObject.parseObject(insResult);
            List<JSONObject> institutionsRes = jsonObjects.getJSONArray("institutions").toJavaList(JSONObject.class);
            List<IllionInstitutions> select = illionInstitutionsDAO.select(null);
            List<JSONObject> collect=new ArrayList<>();
            for (int i = 0; i < institutionsRes.size(); i++) {
                JSONObject in=new JSONObject();
                in.put("slug",institutionsRes.get(i).getString("slug")==null?"":institutionsRes.get(i).getString("slug"));
                in.put("name",institutionsRes.get(i).getString("name")==null?"":institutionsRes.get(i).getString("name"));
                c:for (IllionInstitutions illionInstitutions : select) {
                    if (institutionsRes.get(i).getString("slug").equals(illionInstitutions.getSlug())){
                        in.put("img",illionInstitutions.getImg()==null?"":illionInstitutions.getImg());
                        break c;
                    }else {
                        in.put("img","");
                    }
                }
                collect.add(in);
            }
            result.put("Institutions",collect);
            return result;
        }
        PagingContext pagingContext = new PagingContext();
        if (param.isEmpty()){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        //
        String name = param.getString("name").toLowerCase();
        if (StringUtils.isBlank(name)){
                JSONObject params =new JSONObject();
                params.put("pageNo",(pageNo-1)*pageSize);
                params.put("pageSize",pageSize);
                List<IllionInstitutions> IllionInstitutions = illionInstitutionsDAO.selectDTOs(params);
                // 处理前端null信息
                for (IllionInstitutions illionInstitutionsDTO : IllionInstitutions) {
                    //todo
                    illionInstitutionsDTO.setImg(illionInstitutionsDTO.getImg()==null?"":illionInstitutionsDTO.getImg());
                    illionInstitutionsDTO.setName(illionInstitutionsDTO.getName()==null?"":illionInstitutionsDTO.getName());
                    illionInstitutionsDTO.setSlug(illionInstitutionsDTO.getSlug()==null?"":illionInstitutionsDTO.getSlug());
                }
                int count = illionInstitutionsDAO.count(params);
                pagingContext.setTotal(count);
                pagingContext.setPageSize(pageSize==null?IllionInstitutions.size():pageSize);
                pagingContext.setPageIndex(pageNo==null?1:pageNo);
                result.put("pc",pagingContext);
                result.put("Institutions",IllionInstitutions);
            return result;
        }
        //发送三方请求
        List<JSONObject> collect=new ArrayList<>();
        List<JSONObject> collects=new ArrayList<>();
        try{
            //todo
            if (pageNo==null||pageSize==null){
                pageNo=1;
                pageSize=10;
            }
            //todo
            String insResult = HttpClientUtils.sendGetByHeader(ILLION_GATEWAY_URL_PREFIX + "/institutions?region=au",ILLION_APP_KEY);
            JSONObject jsonObject = JSONObject.parseObject(insResult);
            List<JSONObject> institutionsRes = jsonObject.getJSONArray("institutions").toJavaList(JSONObject.class);
             collects = institutionsRes.stream().filter(jsonObject1 -> ((jsonObject1.getString("name").toLowerCase().contains(name))||(jsonObject1.getString("slug").toLowerCase().contains(name)))).collect(Collectors.toList());
             collect = collects.stream().skip((pageNo - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
        }catch (Exception e){
            log.error("请求三方机构列表报错，error:{},errorMsg:{}",e,e.getMessage());
            throw new BizException(I18nUtils.get("accessor.with.problematic.data", getLang(request)));
        }
        List<IllionInstitutions> select = illionInstitutionsDAO.select(null);
        for (int i = 0; i < collect.size(); i++) {
            JSONObject jsonObject = collect.get(i);
            JSONObject in = new JSONObject();
            in.put("name",jsonObject.getString("name")==null?"":jsonObject.getString("name"));
            in.put("slug",jsonObject.getString("slug")==null?"":jsonObject.getString("slug"));
            JSONObject params=new JSONObject(3);
            params.put("slug",jsonObject.getString("slug"));
            x:for (IllionInstitutions illionInstitutions : select) {
                if (jsonObject.getString("slug").equals(illionInstitutions.getSlug())){
                    in.put("img",illionInstitutions.getImg());
                    break x;
                }else {
                    in.put("img","");
                }
            }
            collect.set(i,in);
        }
        pagingContext.setTotal(collects.size());
        pagingContext.setPageSize(pageSize);
        pagingContext.setPageIndex(pageNo);
        result.put("Institutions",collect);
        result.put("pc",pagingContext);
        return result;
    }

    /**
     * preload样例
     *{
     *             "slug": "bank_of_custom",
     *             "name": "Bank of Custom Data",
     *             "credentials": [
     *                 {
     *                     "name": "Username",
     *                     "fieldID": "username",
     *                     "type": "TEXT",
     *                     "description": "",
     *                     "values": "",
     *                     "validation": {
     *                         "minLength": 0,
     *                         "maxLength": 9999999,
     *                         "chars": "*"
     *                     },
     *                     "keyboardType": "default"
     *                 },
     *                 {
     *                     "name": "Password",
     *                     "fieldID": "password",
     *                     "type": "password",
     *                     "description": "",
     *                     "values": "",
     *                     "validation": {
     *                         "minLength": 0,
     *                         "maxLength": 9999999,
     *                         "chars": "*"
     *                     },
     *                     "keyboardType": "default"
     *                 }
     *             ],
     *             "status": "",
     *             "severity": "0",
     *             "present": "1",
     *             "available": "1",
     *             "searchable": "1",
     *             "display": "0",
     *             "searchVal": "testbankofcustomdatademonstration",
     *             "region": "",
     *             "export_with_password": "0",
     *             "estatements_supported": "0",
     *             "transaction_listings_supported": "0",
     *             "card_validation_supported": "0",
     *             "requires_preload": "0",
     *             "requires_mfa": "0",
     *             "is_business_bank": "0",
     *             "ocr_supported": "0",
     *             "type": "bank",
     *             "do_not_proxy": "0",
     *             "updated_at": "2021-03-19 15:53:46",
     *             "max_days": "365",
     *             "get_address_supported": "0",
     *             "supports_payment_summaries": "0",
     *             "is_supported": "1",
     *             "hide_merged_estatement_privacy_note": "0"
     *         },
     * @param param
     * @param request
     * @return
     * @throws BizException
     */
    @Override
    public JSONObject preload(JSONObject param, HttpServletRequest request) throws BizException {
        if (param.isEmpty() || StringUtils.isBlank(param.getString("slug")) ){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        JSONObject result=new JSONObject();
        JSONObject value = this.invokePreload(param.getString("slug"),request);
        //全量机构信息
        JSONObject institution = value.getJSONObject("institution");
        JSONArray credentials = institution.getJSONArray("credentials");
        result.put("credentials", this.repackCredentials(credentials));
        result.put("name",institution.getString("name"));
        result.put("slug",institution.getString("slug"));
        result.put("userToken",StringUtils.isBlank(value.getString("user_token")) ? "":value.getString("user_token"));
        //该机构登录是否需要2次验证
        result.put("requiresMfa",institution.get("requires_mfa"));
        //获取图片
        IllionInstitutions illionDTO = illionInstitutionsDAO.selectOne(param);
        result.put("img", illionDTO != null && StringUtils.isNotBlank(illionDTO.getImg()) ? illionDTO.getImg() :" ");
        return result;
    }
    /**
     * 为前端设备重新封装mfa信息
     * API文档地址: https://docs.bankstatements.com.au/#mfa-request
     * fields 中的集合数据, 通常会包含:
     *   type(类型)/id(上送参数用的key)/label(友好描述该字段的提示信息)/htmlAttrs(键值对的关联数组，可用作相关html元素中的属性。 通常，这些不是MFA响应的一部分，但在HTML中呈现MFA表单时可能很有用。)
     * mfa格式样例:
     * { "mfa": {
     *     "title": "Additional authentication step",//友好描述
     *     "fields": [{  //需要填写的字段及其描述
     *       "type": "header", //类型
     *       "value": "SMS security code"
     *     }, {
     *       "id": "answer",
     *       "label": "A SMS security code has been sent to your registered mobile phone. Please enter the code to sign in",
     *       "type": "input",
     *       "htmlAttrs": {
     *         "placeholder": "answer" }  }] },
     *   "user_token": "7b...6f38a"  }
     * @param mfaInfo
     * @param request
     * @return
     */
    private Object repackMfaInfo(JSONObject mfaInfo,String userToken, HttpServletRequest request) throws BizException{
        if (!mfaInfo.isEmpty()){
            JSONObject returnValue = new JSONObject(mfaInfo.size());
            returnValue.putAll(mfaInfo);
            returnValue.put("userToken", userToken);
            /*
            "options": { //options 和 下面的hint 在 type = options 出现
                        "1234": "Personal Banking Profile",//后台给你们吧每一行都封装成集合格式
                        "5678": "Business Banking Profile"
                    } 重新封装 options 的数据
             */
            List<JSONObject> fieldsList = mfaInfo.getJSONArray("fields").toJavaList(JSONObject.class);
            fieldsList.forEach(fieldObj->{
                String type = fieldObj.getString("type");
                JSONObject oldOptions = fieldObj.getJSONObject("options");
                if (type.equalsIgnoreCase("options") && CollectionUtil.isNotEmpty(oldOptions)){
                    List<JSONObject> repackOptions = new ArrayList<>(oldOptions.size());
                    for (String key : oldOptions.keySet()) {
                        JSONObject newValueObj = new JSONObject(3);
                        newValueObj.put("keyName",key);
                        newValueObj.put("keyValue",oldOptions.get(key));
                        repackOptions.add(newValueObj);
                    }
                    fieldObj.remove("options");
                    fieldObj.put("options",repackOptions);
                }else {
                    fieldObj.put("options",new ArrayList<>());
                }
                if (CollectionUtil.isEmpty(fieldObj.getJSONArray("subElements"))){
                    fieldObj.put("subElements",new ArrayList<>());
                }
            });
            return returnValue;
        }else {
            throw new BizException("Empty set of MFA info.");
        }
    }

    /**
     * 如果机构有select类型的上送参数, 则将value
     * "values": {"1": 1,"2": 2,"3": 3,"4": 4},
     *     封装成以下格式: app用:
     * {"name":"text","values":[{"keyname":"key","value_name":"value"},{"keyname":"key","value_name":"value"}]}
     * 没有则封装成空list
     * @param credentials
     * @return
     */
    private Object repackCredentials(JSONArray credentials) {
        List<JSONObject> credentialsList = credentials.toJavaList(JSONObject.class);
        credentialsList.forEach(credentialObj->{
            if (credentialObj.getString("type").equalsIgnoreCase("select")){
                JSONObject oldValues = credentialObj.getJSONObject("values");
                List<JSONObject> repack = new ArrayList<>(oldValues.size());
                for (String key : oldValues.keySet()) {
                    JSONObject newValueObj = new JSONObject(3);
                    newValueObj.put("keyName",key);
                    newValueObj.put("keyValue",oldValues.get(key));
                    repack.add(newValueObj);
                }
                credentialObj.remove("values");
                credentialObj.put("values",repack);
            }else {
                credentialObj.put("values",new ArrayList<>());
            }
        });
        return credentialsList;
    }

    /**
     * 提交mfa信息, api文档地址: https://docs.bankstatements.com.au/#mfa-response
     * @param param
     * @param request
     * @throws Exception
     */
    @Override
    public JSONObject mfaInfoSubmit(JSONObject param, HttpServletRequest request) throws Exception {
        //必要参数校验,判断当前用户是否开通分期付, 只有未开通的用户可以, 登录参数 预加载后校验
        this.fetchAllAndMfaParamChecker(param,true,request);
        //从redis中获取mfa要求的信息并校验,返回slug, 登录成功后删除用得到
        String slug = this.checkMfaFieldsInfo(param, request);
        // 获取redis中ilionDto对象
        Object userToken = redisUtils.get(param.getString("userToken"));
        boolean flag=true;
//      添加用户分期付进行状态 2021-6-15 需求
        Long userId = getUserId(request);
        JSONObject stateParam=new JSONObject();
        stateParam.put("userId",userId);
        //请求illion, 提交客户填写的mfa信息
        try{
            this.invokeMfaInfoSubmit(param,request);
        }catch (Exception e){
            // 逻辑上 userToken不为null
            if (userToken!=null){
                String text = userToken.toString();
                try{
                    IllionSubmitLogDTO illionSubmitLogDTO = JSONObject.parseObject(text, IllionSubmitLogDTO.class);
                    illionSubmitLogDTO.setSubmittedStatus(StaticDataEnum.ILLION_SUBMIT_STATUS_0.getCode());
                    illionSubmitLogDTO.setReportStatus(StaticDataEnum.ILLION_STATUS_2.getCode());
                    illionSubmitLogDTO.setSubmittedError(":"+e.getMessage());
                    String accountNumber = illionSubmitLogDTO.getAccountNumber();
                    // 判断是否超过48小时
                    Object o = redisUtils.get(accountNumber);
                    if (o!=null){
                        flag=false;
                    }
                    this.addIllionSubmitLog(flag,illionSubmitLogDTO,request);
                }catch (Exception jsonParseException){
                   log.error("二次提交，添加illion记录异常,e:{}",jsonParseException);
                }
//              添加用户分期付进行状态 2021-6-15 需求
                stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_2.getCode());
                stateParam.put("errorMessage",e.getMessage());
                illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);
                throw e;
            }
        }
//      添加用户分期付进行状态 2021-6-15 需求
        stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_3.getCode());
        illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);
        //请求分期付系统,开启初始化用户分期付数据流程->分期付请求支付, 同步用户User表数据
        serverService.illionService(REFERRAL_CODE_PREFIX+getUserId(request),request);
        //登录成功后清除 redis中的的key mfa验证信息
        this.dealWithRedisIllionData(param.getString("userToken"),slug,false,null,request);
        try{
            if (userToken!=null){
                String text = userToken.toString();
                IllionSubmitLogDTO illionSubmitLogDTO = JSONObject.parseObject(text, IllionSubmitLogDTO.class);
                illionSubmitLogDTO.setReportStatus(StaticDataEnum.ILLION_STATUS_2.getCode());
                illionSubmitLogDTO.setSubmittedStatus(StaticDataEnum.ILLION_SUBMIT_STATUS_1.getCode());
                String accountNumber = illionSubmitLogDTO.getAccountNumber();
                // 判断是否超过48小时
                Object o = redisUtils.get(accountNumber);
                if (o!=null){
                    flag=false;
                }
                this.addIllionSubmitLog(flag,illionSubmitLogDTO,request);
            }
        }catch (Exception e){
            log.error("二次提交illion添加记录异常,e:{}",e);
        }
        return this.initReturnJson();
    }

    /**
     * 清除或者删除 redis中二次验证用校验信息
     * @param userToken
     * @param slug
     * @param isAdd t:向redis 中添加, f: 从redis中删除
     * @param mfaInfo
     * @param request
     */
    private void dealWithRedisIllionData(String userToken, String slug, boolean isAdd,JSONObject mfaInfo, HttpServletRequest request) {
        if (isAdd){
            //添加userToken 和 mfa 验证信息的JSON数据
            redisUtils.set(this.buildRedisKey(userToken,slug,true,request),userToken, Constant.MINUTES_15);
            redisUtils.set(this.buildRedisKey(userToken,slug,false,request),mfaInfo,Constant.MINUTES_15);
        }else {
            //请求成功 删除二次验证信息
            redisUtils.del(this.buildRedisKey(userToken,slug,true,request));
            redisUtils.del(this.buildRedisKey(userToken,slug,false,request));
        }
    }

    /**
     * 获取需要二次验证, fetchAll接口登录中, 返回的mfa信息
     * 对二次验证用户提交的信息进行验证
     * mfa需要的信息需在redis中
     * @param param 二次验证用户提交的信息 包含userToken
     * @param request
     * @throws BizException
     */
    private String checkMfaFieldsInfo(JSONObject param, HttpServletRequest request) throws BizException{
        String slug = param.getString("slug");
        /*String mfaInfoRedisKey = this.buildRedisKey(param.getString("userToken"), slug,false,request);
        if (redisUtils.hasKey(mfaInfoRedisKey)) {
            try {
                //获取从fetchAll接口保存的 session需要验证的mfa上送信息及其规则
                JSONObject mfaRules = JSONObject.parseObject(JSON.toJSONString(redisUtils.get(mfaInfoRedisKey)));
                //获取所有字段, 只校验特殊类型
                List<JSONObject> fieldList = mfaRules.getJSONArray("fields").toJavaList(JSONObject.class);
                for ( JSONObject fieldObj : fieldList ) {
                    String type = fieldObj.getString("type");
                    //对input, password ,set等进行验证
                    if (CollectionUtil.isNotEmpty(MFA_TYPE_CHECK_LIST ) && MFA_TYPE_CHECK_LIST.contains(type)){
                        //todo 进行校验
                        String fieldId = fieldObj.getString("id");
                        if (StringUtils.isNotBlank(fieldId) && !type.equals("set")) {
                            String customerInputValue = param.getString(fieldId);
                            //set类型是集合规则,需要单独逻辑校验
                            if (!type.equals("set")) {
                                //todo input password 校验
                            } else {
                                //todo set 规则集合校验
                            }
                        }
                    }
                }

            }catch (Exception e){
                throw new BizException(I18nUtils.get("login.session.not.exist", getLang(request)));
            }
        }else {
            throw new BizException(I18nUtils.get("login.session.not.exist", getLang(request)));
        }*/
        return slug;
    }

    /**
     * 请求-提交用户登录的二次验证信息
     * @param param 必须包含userToken, 其他参数动态
     * @param request
     * @throws Exception
     */
    private void invokeMfaInfoSubmit(JSONObject param, HttpServletRequest request) throws Exception{
        String userToken = param.getString("userToken");
        JSONObject header= new JSONObject(5);
        header.put("X-API-KEY",ILLION_APP_KEY);
        header.put("X-USER-TOKEN", userToken);
        //请求体中要需要放token 用于illion 确认第一次验证的session
        param.put("user_token",userToken);
        param.remove("slug");
        try{
            String result = HttpClientUtils.postByHeader(ILLION_GATEWAY_URL_PREFIX+"/mfa", header, JSONObject.toJSONString(param));
            JSONObject resJson = JSONObject.parseObject(result);
            // 错误码不为空,报错 信息返回app端
            if (resJson.getInteger("errorCode") != null){
                throw new BizException(resJson.getString("error"));
            }
        }catch (Exception e){
            log.info("Illion 提交mfa信息 接口错误,异常:{},异常信息:{}",e,e.getMessage());
            throw new BizException(e.getMessage());
        }
    }

    /**
     * https://docs.bankstatements.com.au/#login-and-fetch-all-statements
     * @param param
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject fetchAll(JSONObject param, HttpServletRequest request) throws Exception {
        //必要参数校验,判断当前用户是否开通分期付, 只有未开通的用户可以, 登录参数 预加载后校验
        this.fetchAllAndMfaParamChecker(param,false,request);
        String slug = param.getString("slug");
        //请求预加载接口, 获取当前机构-slug 需要的上送参数列表和校验规则集合
        JSONObject value = this.invokePreload(slug,request);
        // 判断三方需要信息并校验, institutionInfo机构登录用全量信息
        JSONObject institutionInfo = value.getJSONObject("institution");
        //需要2次验证的机构, 返回数据中会包含: requires_mfa = 1
        boolean REQUIRES_MFA = institutionInfo.getInteger("requires_mfa").equals(StaticDataEnum.REQUIRES_MFA_YES.getCode());
        //登录用所有字段信息及其验证规则
        List<JSONObject> credentials = institutionInfo.getJSONArray("credentials").toJavaList(JSONObject.class);
        // 声明发往三方登录用客户填写的参数信息
        JSONObject requestParams =new JSONObject(8);
        requestParams.put("institution",param.getString("slug"));
        for (JSONObject credential : credentials) {
            // 当前校验规则对应的三方上送字段名
            String fieldID = credential.getString("fieldID");
            //todo with-second-mfa,with-mfa只有在测试环境才出现
            if (NORMAL_LOGIN_TYPE_CHECK_LIST.contains(credential.getString("type")) && !fieldID.equals("with-mfa") && !fieldID.equals("with-second-mfa")) {
                // 获取用户提交的上送信息
                String requestValue = param.getString(fieldID);
                //根据credentials中返回的校验规则 校验用户体提交的信息
                this.ruleChecker(requestValue, credential, request);
                // 参数校验完成 封装到请求三方的集合中
                requestParams.put(fieldID, requestValue);
            }
        }
        // 记录一条illion消息
        IllionSubmitLogDTO illionSubmitLogDTO=new IllionSubmitLogDTO();
        String encrypt="";
        // 添加/修改记录 48小时算一次记录
        Long userId = getUserId(request);
        boolean flag=true;
        try{
            illionSubmitLogDTO.setBank(slug);
            UserDTO userById=null;
            if (userId!=null) {
                userById= userService.findUserById(userId);
            }
            illionSubmitLogDTO.setPhone(userById==null?null:userById.getPhone());
            illionSubmitLogDTO.setDate(System.currentTimeMillis());
            // 加密银行卡号
            String username = param.getString("username");
            encrypt = EncryptUtil.encrypt(slug + "-" + username + "-" + userId);
            illionSubmitLogDTO.setAccountNumber(encrypt);
            // 当前ReferralCode不需要记录id
            illionSubmitLogDTO.setReferralCode(REFERRAL_CODE_PREFIX+userId);
            illionSubmitLogDTO.setUserId(userId);
            // 查询是否有相同记录，用于计数
            JSONObject params=new JSONObject();
            params.put("accountNumber",encrypt);
            List<IllionSubmitLogDTO> illionSubmitLogDTOS = illionSubmitLogService.find(params, null, null);
            if (illionSubmitLogDTOS!=null){
                illionSubmitLogDTO.setSubmitNumber(illionSubmitLogDTOS.size()==0?1:illionSubmitLogDTOS.size()+1);
            }else {
                illionSubmitLogDTO.setSubmitNumber(1);
            }
        }catch (Exception e){
            log.error("初始化illion记录异常,e:{}",e);
        }
        // 获取redis 48小时内算一次记录 key银行卡value 记录id
        try{
            Object o = redisUtils.get(encrypt);
            if (o!=null){
                // 如果有 取出id
                illionSubmitLogDTO.setId(Long.valueOf(o.toString()));
                // 修改记录
                flag=false;
            }else {
                // 雪花生成id
                illionSubmitLogDTO.setId(SnowflakeUtil.generateId());
            }
        }catch (Exception e){
            log.error("获取redis失败,e:{}",e);
        }
        // 添加记录id
        requestParams.put("submitId",illionSubmitLogDTO.getId());
        // 包装发往三方参数
        JSONObject finalRequestParam=this.packFetchAllParams(requestParams,institutionInfo,REQUIRES_MFA,request);
        // 包装请求头
        JSONObject headerParam= this.packHeader(param,request);

        //发送请求
        JSONObject fetchResult=null;
        JSONObject stateParam=new JSONObject();
        stateParam.put("userId",userId);
        try{
            fetchResult= this.invokeFetchAll(headerParam,finalRequestParam,request);
        }catch (Exception e){
            // 上送失败
            illionSubmitLogDTO.setReportStatus(StaticDataEnum.ILLION_STATUS_2.getCode());
            illionSubmitLogDTO.setSubmittedStatus(StaticDataEnum.ILLION_SUBMIT_STATUS_0.getCode());
            illionSubmitLogDTO.setSubmittedError("error:"+e.getMessage());
            this.addIllionSubmitLog(flag,illionSubmitLogDTO,request);
            // 添加用户分期付进行状态 2021-6-15 需求
            stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_2.getCode());
            stateParam.put("errorMessage",e.getMessage());
            illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);
            throw e;
        }
        // 添加用户分期付进行状态 2021-6-15 需求
        stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_1.getCode());
        illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);

        //如果需要二次验证, illion返回mfa数据
        JSONObject mfaInfo = fetchResult.getJSONObject("mfa");
        JSONObject result= this.initReturnJson();
        if (!REQUIRES_MFA && null == mfaInfo){
            //请求分期付系统,开启初始化用户分期付数据流程->分期付请求支付, 同步用户User表数据
            serverService.illionService(REFERRAL_CODE_PREFIX+getUserId(request),request);
            // 上送成功
            illionSubmitLogDTO.setReportStatus(StaticDataEnum.ILLION_STATUS_2.getCode());
            // 保证修改将异常信息替换
            illionSubmitLogDTO.setSubmittedError("");
            illionSubmitLogDTO.setSubmittedStatus(StaticDataEnum.ILLION_SUBMIT_STATUS_1.getCode());
            this.addIllionSubmitLog(flag,illionSubmitLogDTO,request);
            // 添加用户分期付进行状态 2021-6-15 需求
            stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_3.getCode());
            illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);
            return result;
        }
        /*
           第一次验证成功, 如果mfa数据返回, 则说明需要二次验证, 向app返回需要展示的二次验证字段
           redis中设置 userToken 和mfa验证信息 用于二次验证接口->mfaSubmit接口校验 过期时间15分钟
         */
        String userToken = fetchResult.getString("user_token");
        this.dealWithRedisIllionData(param.getString("userToken"),slug,true,mfaInfo,request);
        result.put("result",false);
        result.put("mfa",this.repackMfaInfo(mfaInfo,userToken,request));
        // 二次验证
        //二次记录需要的json以token做key 三方返回token唯一性保持当前会话同一用户同一银行机构
        try{
            // token为key illionDto对象为value 时间15分钟
            redisUtils.set(userToken,JSONObject.toJSONString(illionSubmitLogDTO),Constant.MINUTES_15);
        }catch (Exception e){
            log.error("存储Redis illion记录对象失败,e:{}",e);
        }
        return result;
    }

    /**
     * 请求Illion preload机构登录信息接口
     * @param slug
     * @param request
     * @return
     */
    private JSONObject invokePreload(String slug, HttpServletRequest request) throws BizException{
        try{
            // 请求三方预加载信息，判断需要字段信息
            String results = HttpClientUtils.sendGetByHeader(ILLION_GATEWAY_URL_PREFIX + "/preload?institution="+slug,ILLION_APP_KEY);
            return JSONObject.parseObject(results);
        }catch (Exception e){
            log.error("请求三方机构预加载异常，error:{},errorMsg:{}",e,e.getMessage());
            throw new BizException(I18nUtils.get("accessor.with.problematic.data", getLang(request)));
        }
    }

    /**
     * 请求illion fetch 接口
     * @param headerParam
     * @param finalRequestParam
     * @param request
     * @return
     * @throws BizException
     */
    private JSONObject invokeFetchAll(JSONObject headerParam, JSONObject finalRequestParam, HttpServletRequest request) throws BizException {
        try{
            String result = HttpClientUtils.postByHeader(fetchAll, headerParam, JSONObject.toJSONString(finalRequestParam));
            JSONObject resultJSON = JSONObject.parseObject(result);
            // 错误码不为空,报错 信息返回app端
            if (resultJSON.getInteger("errorCode") != null){
                throw new BizException(resultJSON.getString("error"));
            }
            return resultJSON;
        }catch (Exception e){
            log.info("Illion FetchAll 接口错误,异常:{},异常信息:{}",e,e.getMessage());
            throw new BizException(e.getMessage());
        }
    }

    /**
     * fetchAll 和 mfa 参数提交 通用参数校验
     * @param param
     * @param checkToken t:校验token
     * @param request
     * @throws BizException
     */
    private UserDTO fetchAllAndMfaParamChecker(JSONObject param,Boolean checkToken, HttpServletRequest request) throws BizException {
        if (param.isEmpty() || StringUtils.isBlank(param.getString("userToken")) ||StringUtils.isBlank(param.getString("slug"))){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        Long userId = getUserId(request);
        if (userId == null || userId == 0){
            // 未登录用户
            throw new BizException(I18nUtils.get("login.rule.no", getLang(request)));
        }
        UserDTO userById = userService.findUserById(userId);
        if (null == userById && null == userById.getId()){
            throw new BizException("User not exist");
        }
        if (checkToken){
            //mfa提交需要验证token 必须为从fetchAll接口返回的user_token 不存在则报错
            String userToken = param.getString("userToken");
            String redisUserTokenKey = this.buildRedisKey(userToken,param.getString("slug"),true,request);
            if (!redisUtils.hasKey(redisUserTokenKey)
             || !userToken.equals(redisUtils.get(redisUserTokenKey.toString()))) {
                throw new BizException(I18nUtils.get("login.session.not.exist", getLang(request)));
            }
        }
        //todo 调研参数取值范围
        /*if (!userById.getInstallmentState().equals(StaticDataEnum.INSTALLMENT_NOT_ACTIVE.getCode())){
            // 当前用户分期付状态为 开通/禁用
            throw new BizException(I18nUtils.get("instalment.service.opened", getLang(request)));
        }*/
        return userById;
    }

    /**
     * 组装redis的key, token验证用和 获取mfa验证信息用
     * @param userToken fetchAll返回的token
     * @param marker t: 验证token用, f: 验证mfa信息用token
     * @param request request对象
     * @return
     */
    private String buildRedisKey(String userToken,String slug,Boolean marker,HttpServletRequest request){
        StringBuilder buffer = new StringBuilder();
        /*
            基础格式 IJCP-10x0(x: 1-生产,2:准生产,3:测试HK)-userId-slug(机构简称)-
            ** eg: IJCP-1030=3264872634287-cba-
         */
        buffer.append(REFERRAL_CODE_PREFIX).append(getUserId(request)).append("-").append(slug).append("-");
        //token 的key f: //mfa验证信息的key referralCodeFinal+"-MFA-"+userToken
        return marker ? buffer.append(userToken).toString() : buffer.append("MFA_RULES-").append(userToken).toString();
    }

    private JSONObject packHeader(JSONObject param, HttpServletRequest request){
        JSONObject headerParam= new JSONObject(5);
        headerParam.put("X-API-KEY",ILLION_APP_KEY);
        headerParam.put("X-USER-TOKEN",param.getString("userToken"));
        headerParam.put("X-OUTPUT-VERSION",vsersion);
        return headerParam;
    }

    /**
     * 包装请求参数,
     * https://docs.bankstatements.com.au/#login-and-fetch-all-statements
     * @param requestParams
     * @param institutionInfo
     * @param request
     * @return
     */
    private JSONObject packFetchAllParams(JSONObject requestParams, JSONObject institutionInfo,Boolean REQUIRES_MFA, HttpServletRequest request) {
        JSONObject putParam=new JSONObject(14);
        //该次报告的唯一标识符: IJCP-10x0(1:生产,2:准生产,3:测试)-UserId-记录ID
        putParam.put("referral_code",REFERRAL_CODE_PREFIX+getUserId(request)+"-"+requestParams.getLong("submitId"));
        //如果该机构要求二次验证, 放入该参数, 如果登录成功, 结果会返回 mfa 信息, 返回给前端做二次登录用
        if (REQUIRES_MFA){
            requestParams.put("with-mfa","with");
        }
        //登录参数
        putParam.put("credentials",requestParams);
        /*
         如果设置为 0->false, 成功会返回用户账户数据, 有法律危险!!!!
         1=true: 为了系统安全, 返回最小结果
         */
        putParam.put("silent",1);
        /*
        This is a boolean value with a default of false (or 0). The other possible value is true (or 1).
        如果设置为true，则此API函数将在登录成功后立即返回帐户列表（即与Login函数提供的响应相同。然后，API将在后台执行其余的导出操作，
        并提交 回调URL的最终数据（即与Retrieve Statement Data函数提供的响应相同）（必须在请求中指定，或作为帐户的默认值）。
         */
        putParam.put("async",1);
        //文件推送地址, 数据系统
        putParam.put("callback",DATA_SYSTEM_CALL_BACK_URL);
        // 获取当前机构允许的最大获取天数
        Integer maxDaysAllowed = institutionInfo.getInteger("max_days");
        //获取报告的天数, 希望获取180天, 也可能机构最大不支持,则请求机构允许的最大天数
        putParam.put("requestNumDays",maxDaysAllowed!= null && maxDaysAllowed.compareTo(BANK_STATEMENT_MAX_REQUIRED_DAYS_CONFIG) >= 0
                ? BANK_STATEMENT_MAX_REQUIRED_DAYS_CONFIG : maxDaysAllowed);
        putParam.put("callbackFormat","json");
        /*
          默认为false。 如果设置为true，则BankStatements将生成一个包含原始银行数据的文件。 然后，您将能够执行“检索文件”请求以获取实际文件。
           文件中数据的格式将由所使用的X-API-KEY的帐户设置确定。 注意：此参数以前称为with_raw_file。 这些参数具有完全相同的效果，如果将其中任何一个设置为true，
           则将其视为设置为true。 较旧的with_raw_file参数名称已弃用，因此不应在新代码中使用。
           **** 配合include_files=true 才会推送.json格式文件!!!!!!!!!!!!
         */
        putParam.put("generate_raw_file",true);
        /*
        默认为false。 如果设置为true，则BankStatements将包含从用户的银行数据生成的所有文件（如果设置了则通过generate_raw_file参数生成的文件）
        将包含在发送给回调URL的数据中。 必须将async设置为true才能应用此功能。 注意：此参数以前称为with_pdf。 这些参数具有完全相同的效果，
        如果将其中任何一个设置为true，则将其视为设置为true。 不推荐使用较早的with_pdf参数名称，因此不应在新代码中使用。
        **** 单独开启该配置, 文件接收接口只会接收到html格式文件!!!!!!!!!!!
         */
        putParam.put("include_files",true);
        return putParam;
    }

    private void ruleChecker(String requestValue, JSONObject credential, HttpServletRequest request) throws BizException{
        //动态请求参数校验,optional=true 为非必填天数, 有可能没有
        Boolean optional = credential.getBoolean("optional") ;
        //如果为null 则为必填
        optional = optional != null ? optional : false;
        if (StringUtils.isBlank(requestValue) && !optional){
            throw new BizException(I18nUtils.get("please.complete.info",getLang(request)));
        }
        if (StringUtils.isNotBlank(requestValue)) {
            JSONObject validationRules = credential.getJSONObject("validation");
            String charsValRules = validationRules.getString("chars");
            Integer minLength = validationRules.getInteger("minLength");
            Integer maxLength = validationRules.getInteger("maxLength");
            if (charsValRules.equals("0-9")) {
                // 判断为数字验证码
                if (!Pattern.matches("[0-9]+", requestValue)) {
                    throw new BizException(I18nUtils.get("login.info.error.please.check", getLang(request)));
                }
                if (requestValue.length() < minLength || requestValue.length() > maxLength) {
                    throw new BizException(I18nUtils.get("login.info.error.please.check", getLang(request)));
                }
            } else if (charsValRules.equals("a-z0-9")) {
                // 判断为数字+英文
                if (!Pattern.matches("^[0-9A-Za-z]{6,24}$", requestValue)) {
                    throw new BizException(I18nUtils.get("login.info.error.please.check", getLang(request)));
                }
                if (requestValue.length() < minLength || requestValue.length() > maxLength) {
                    throw new BizException(I18nUtils.get("login.info.error.please.check", getLang(request)));
                }
            } else if (charsValRules.equals("*")) {
                // 为*
                if (requestValue.length() < minLength || requestValue.length() > maxLength) {
                    throw new BizException(I18nUtils.get("login.info.error.please.check", getLang(request)));
                }
            }
        }
    }

    /**
     * 前端返回需要表示符, 多出使用, 在此初始化返回集合
     * @return
     */
    private JSONObject initReturnJson(){
        JSONObject res = new JSONObject(3);
        res.put("result",true);
        return res;
    }
    @Override
    public void sendIllionMessage(JSONObject param, HttpServletRequest request) throws Exception {
        String userId = param.getString("userId");
        JSONObject inParam=new JSONObject();
        inParam.put("userId",userId);
        inParam.put("state",StaticDataEnum.AUDIT_USER_STATE_WAITING_FOR_REVIEW.getCode());
        // 额度开通失败不需要设置state=40
        Integer isCredit = param.getInteger("isCredit");
        if (isCredit!=null){
            // 50 额度开通失败
            inParam.put("state",50);
            // 插入日志记录开通失败
        }else {
            // 加密报告失败
            serverService.updateFailedIllionUserState(inParam,request);
        }

        // 考虑多个运营人员
        if (StringUtils.isBlank(userId)){
            throw new BizException("userId is not null");
        }
        JSONObject params=new JSONObject(1);
        params.put("code","operatePhone");
        List<StaticDataDTO> staticDataDTOS = staticDataService.find(params,null,null);
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode("26");
        if (!CollectionUtil.isEmpty(staticDataDTOS)){
            for (StaticDataDTO staticDataDTO : staticDataDTOS) {
                String phone = staticDataDTO.getValue();
                String phoneCode = new StringBuilder(phone).substring(0, 2);
                if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
                    JSONArray phoneArray = new JSONArray();
                    phoneArray.add(phone);
                    JSONObject sendParams = new JSONObject();
                    sendParams.put("code", userId);
                    aliyunSmsService.sendChinaSms(phone, mailTemplateDTO.getAliCode(), sendParams);
                } else {
                    //设置模板参数
                    String[] paramSend = {userId + ""};
                    //邮件内容
                    String sendMsg = userService.templateContentReplace(paramSend, mailTemplateDTO.getEnSendContent());
                    aliyunSmsService.sendInternationalSms(phone, sendMsg);
                }
            }

        }

    }
    /**
     * 记录illion报告
     * */

    public void addIllionSubmitLog(boolean flag,IllionSubmitLogDTO illionSubmitLogDTO,HttpServletRequest request){
        try {
            if (flag){
                // 存redis
                redisUtils.set(illionSubmitLogDTO.getAccountNumber(),illionSubmitLogDTO.getId(),ILLION_SUBMIT_TIME);
                illionSubmitLogService.saveIllionSubmitLogNew(illionSubmitLogDTO,request);
            }else {
                this.updateIllionSubmitLog(illionSubmitLogDTO,request);
            }
        }catch (Exception e){
            // todo 异常打印日志
            e.printStackTrace();
        }
    }
    /**
     * 修改记录illion报告
     * */
    @Async("taskExecutor")
    public void updateIllionSubmitLog(IllionSubmitLogDTO illionSubmitLogDTO,HttpServletRequest request){
        try {
            log.info("开始修改illion记录");
            illionSubmitLogService.updateIllionSubmitLog(illionSubmitLogDTO.getId(),illionSubmitLogDTO,request);
            log.info("修改illion记录完成");
        }catch (Exception e){
            log.error("修改illion记录异常,e{}",e);
        }
    }


}
