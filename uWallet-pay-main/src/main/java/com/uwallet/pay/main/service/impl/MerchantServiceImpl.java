package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.*;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 商户信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 商户信息表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:22:53
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Service
@Slf4j
public class MerchantServiceImpl extends BaseServiceImpl implements MerchantService {

    @Resource
    private MerchantDAO merchantDAO;
    @Resource
    private ApiMerchantDAO apiMerchantDAO;

    @Autowired
    private ApproveLogService approveLogService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private ParametersConfigService parametersConfigService;

    @Autowired
    private DirectorService directorService;

    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    private GatewayService gatewayService;

    @Value("${uWallet.credit}")
    private String creditMerchantUrl;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private MailTemplateService mailTemplateService;

    @Value("${uWallet.sysEmail}")
    private String sysEmail;

    @Value("${uWallet.sysEmailPwd}")
    private String sysEmailPwd;

    @Value("${google.mapGeocodingAPI}")
    private String googleMapsApi;

    @Value("${google.mapGeocodingAPIKey}")
    private String googleApiKey;

    @Value("${server.type}")
    private String serverType;

    private final static String RELEASE_SERVER_TYPE = "prod";

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private QrcodeInfoService qrcodeInfoService;

    @Autowired
    private ShareholderService shareholderService;

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private TrusteeService trusteeService;

    @Autowired
    private ContactPersonService contactPersonService;

    @Autowired
    private DocuSignService docuSignService;

    @Autowired
    private DocusignRequestService companyEntityMerchantService;

    @Autowired
    private DocusignRequestService soleAndOtherEntityMerchantService;

    @Autowired
    private DocusignRequestService partnerEntityMerchantService;

    @Autowired
    private DocusignRequestService trusteeEntityMerchantService;

    @Autowired
    private WholeSalesFlowService wholeSalesFlowService;

    @Autowired
    private UserActionService userActionService;
    @Resource
    private TagService tagService;

    @Autowired
    private NfcCodeInfoDAO nfcCodeInfoDAO;

    @Autowired
    private QrcodeInfoDAO qrcodeInfoDAO;
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private CodeUpdateLogService codeUpdateLogService;

    @Autowired
    private MerchantUpdateLogService merchantUpdateLogService;

    @Autowired
    @Lazy
    private MerchantApplicationService merchantApplicationService;

    @Autowired
    private SecondMerchantGatewayInfoService secondMerchantGatewayInfoService;

    @Autowired
    private AppCustomCategoryDisplayStateService appCustomCategoryDisplayStateService;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public Long saveMerchant(@NonNull MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        Merchant merchant = BeanUtil.copyProperties(merchantDTO, new Merchant());
        log.info("save Merchant:{}", merchant);
        Merchant merchant_ = (Merchant) this.packAddBaseProps(merchant, request);
        if (merchantDAO.insert(merchant_) != 1) {
            log.error("insert error, data:{}", merchant);
            throw new BizException("Insert merchant Error!");
        }

        return merchant_.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMerchantList(@NonNull List<Merchant> merchantList, HttpServletRequest request) throws BizException {
        if (merchantList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = merchantDAO.insertList(merchantList);
        if (rows != merchantList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, merchantList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchant(@NonNull Long id, @NonNull MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        log.info("full update merchantDTO:{}", merchantDTO);
        List<JSONObject> files = merchantDTO.getFiles();
        if (CollectionUtils.isNotEmpty(files)) {
            JSONArray filesUrl = new JSONArray();
            files.stream().forEach(jsonObject -> {
                String url = jsonObject.getString("url");
                if (StringUtils.isNotEmpty(url)) {
                    filesUrl.add(url);
                }
            });
            merchantDTO.setFileList(filesUrl.toJSONString());
        } else if (files != null && files.size() == 0) {
            merchantDTO.setFileList(new JSONArray().toJSONString());
        }
        List<JSONObject> trusteeFiles = merchantDTO.getTrusteeFiles();
        if (CollectionUtils.isNotEmpty(trusteeFiles)) {
            JSONArray filesUrl = new JSONArray();
            trusteeFiles.stream().forEach(jsonObject -> {
                String url = jsonObject.getString("url");
                if (StringUtils.isNotEmpty(url)) {
                    filesUrl.add(url);
                }
            });
            merchantDTO.setTrusteeFileList(filesUrl.toJSONString());
        } else if (trusteeFiles != null && trusteeFiles.size() == 0) {
            merchantDTO.setTrusteeFileList(new JSONArray().toJSONString());
        }
        // 联系人添加
        List<ContactPersonDTO> contactPersonDTOList = merchantDTO.getContactPersonDTOList();
        if (contactPersonDTOList != null && !contactPersonDTOList.isEmpty()) {
            List<ContactPerson> contactPersonList = new ArrayList<>(contactPersonDTOList.size());
            contactPersonService.deleteContactPersonByMerchantId(id);
            contactPersonDTOList.forEach(contactPersonDTO -> {
                ContactPerson contactPerson = BeanUtil.copyProperties(contactPersonDTO, new ContactPerson());
                contactPerson = (ContactPerson) this.packAddBaseProps(contactPerson, request);
                contactPerson.setMerchantId(id);
                contactPersonList.add(contactPerson);
            });
            contactPersonService.saveContactPersonList(contactPersonList, request);
        }
        Merchant merchant = BeanUtil.copyProperties(merchantDTO, new Merchant());
        merchant.setId(id);
        if (request != null) {
            merchant = (Merchant) this.packModifyBaseProps(merchant, request);
        } else {
            merchant.setModifiedDate(System.currentTimeMillis());
        }

        //查询旧分类
        Integer nowCategories = merchantDTO.getCategories();

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("id", id);
        MerchantDTO existMerchantDTO = merchantDAO.selectOneDTO(map);
        Integer oldCategories = existMerchantDTO.getCategories();

        int cnt = merchantDAO.update(merchant);
        if (cnt != 1) {
            log.error("update error, data:{}", merchantDTO);
            throw new BizException("update merchant Error!");
        }

        //商户转为不可用时  更新首页banner 自定义分类数据 并通知前端更新 是否可用 0：不可用 1：可用
        /*if(Objects.nonNull(merchantDTO.getIsAvailable()) && merchantDTO.getIsAvailable().intValue() == 0){
            try{
                appCustomCategoryDisplayStateService.updateMerchantDataByMerchantNotAvailable(id, request);
            }catch (Exception e){
                log.error("商户禁用时更新自定义分类商户数据出错", merchantDTO);
            }
        }*/
        //商户更新   更新首页banner 自定义分类数据 并通知前端更新
        boolean merchatAvilableStatus = true;
        if(Objects.nonNull(merchantDTO.getIsAvailable()) && merchantDTO.getIsAvailable().intValue() == 0){
            merchatAvilableStatus = false;
        }

        if(merchatAvilableStatus){
            //分类变动 需要 删除掉首页对应的信息
            if(null != existMerchantDTO && null != oldCategories && null != nowCategories && !oldCategories.equals(nowCategories)){
                //变动分类
                merchatAvilableStatus = false;
            }
        }

        try{
            appCustomCategoryDisplayStateService.updateMerchantDataToChangeAppHomepage(id, merchatAvilableStatus, request);
        }catch (Exception e){
            log.error("商户禁用时更新自定义分类商户数据出错", merchantDTO);
        }

        try {
            Map maps = appCustomCategoryDisplayStateService.getDistanceMerchant(id);
            log.info("管理后台修改商户,距离, map:{}", maps);
            Map map3 = new HashMap();
            map3.put("category_type", maps.get("categories"));
            map3.put("state_name", maps.get("en_name"));
            AppCustomCategoryDisplayStateDTO dto = appCustomCategoryDisplayStateService.findOneAppCustomCategoryDisplayState(map3);
            log.info("管理后台修改商户,距离, dto:{}", dto);
            Integer distance = 2;
            if (distance.equals(dto.getMerchantShowType())) {
                log.info("管理后台修改商户,距离redis, dto:{}", distance.equals(dto.getMerchantShowType()));
                String customCategoriesUpdateTimestampRedisKey = Constant.getCustomCategoriesUpdateTimestampRedisKey(dto.getDisplayOrder(), dto.getStateName());
                redisUtils.set(customCategoriesUpdateTimestampRedisKey, System.currentTimeMillis());
            }

        }catch (Exception e){
            log.error("管理后台修改商户,距离出错", e.getMessage());
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchanth5(@NonNull Long id, @NonNull ApiMerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        if(merchantDTO.getMerchantClass() != null && merchantDTO.getMerchantClass() == StaticDataEnum.H5_MERCHANT_CLASS_0.getCode()){
            merchantDTO.setSuperMerchantId(id);
        }
        log.info("full update merchantDTO:{}", merchantDTO);
        List<JSONObject> files = merchantDTO.getFiles();
        if (CollectionUtils.isNotEmpty(files)) {
            JSONArray filesUrl = new JSONArray();
            files.stream().forEach(jsonObject -> {
                String url = jsonObject.getString("url");
                if (StringUtils.isNotEmpty(url)) {
                    filesUrl.add(url);
                }
            });
            merchantDTO.setFileList(filesUrl.toJSONString());
        } else if (files != null && files.size() == 0) {
            merchantDTO.setFileList(new JSONArray().toJSONString());
        }
        List<JSONObject> trusteeFiles = merchantDTO.getTrusteeFiles();
        if (CollectionUtils.isNotEmpty(trusteeFiles)) {
            JSONArray filesUrl = new JSONArray();
            trusteeFiles.stream().forEach(jsonObject -> {
                String url = jsonObject.getString("url");
                if (StringUtils.isNotEmpty(url)) {
                    filesUrl.add(url);
                }
            });
            merchantDTO.setTrusteeFileList(filesUrl.toJSONString());
        } else if (trusteeFiles != null && trusteeFiles.size() == 0) {
            merchantDTO.setTrusteeFileList(new JSONArray().toJSONString());
        }
        // 联系人添加
        List<ContactPersonDTO> contactPersonDTOList = merchantDTO.getContactPersonDTOList();
        if (contactPersonDTOList != null && !contactPersonDTOList.isEmpty()) {
            List<ContactPerson> contactPersonList = new ArrayList<>(contactPersonDTOList.size());
            contactPersonService.deleteContactPersonByMerchantId(id);
            contactPersonDTOList.forEach(contactPersonDTO -> {
                ContactPerson contactPerson = BeanUtil.copyProperties(contactPersonDTO, new ContactPerson());
                contactPerson = (ContactPerson) this.packAddBaseProps(contactPerson, request);
                contactPerson.setMerchantId(id);
                contactPersonList.add(contactPerson);
            });
            contactPersonService.saveContactPersonList(contactPersonList, request);
        }


        ApiMerchant merchant = BeanUtil.copyProperties(merchantDTO, new ApiMerchant());
        merchant.setId(id);
        if (request != null) {
            merchant = (ApiMerchant) this.packModifyBaseProps(merchant, request);
        } else {
            merchant.setModifiedDate(System.currentTimeMillis());
        }


        int cnt = apiMerchantDAO.update(merchant);
        if (cnt != 1) {
            log.error("update error, data:{}", merchantDTO);
            throw new BizException("update merchant Error!");
        }

//        boolean merchatAvilableStatus = true;
//        if(Objects.nonNull(merchantDTO.getIsAvailable()) && merchantDTO.getIsAvailable().intValue() == 0){
//            merchatAvilableStatus = false;
//        }
//
//        try{
//            appCustomCategoryDisplayStateService.updateMerchantDataToChangeAppHomepage(id, merchatAvilableStatus, request);
//        }catch (Exception e){
//            log.error("商户禁用时更新自定义分类商户数据出错", merchantDTO);
//        }

    }

    @Override
    public void updateMerchantSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        merchantDAO.updatex(params);
    }

    @Override
    public void logicDeleteMerchant(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = merchantDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMerchant(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = merchantDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MerchantDTO findMerchantById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MerchantDTO merchantDTO = merchantDAO.selectOneDTO(params);
        return merchantDTO;
    }

    @Override
    public MerchantDTO findOneMerchant(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Merchant merchant = merchantDAO.selectOne(params);
        MerchantDTO merchantDTO = new MerchantDTO();
        if (null != merchant) {
            BeanUtils.copyProperties(merchant, merchantDTO);
        }
        return merchantDTO;
    }

    @Override
    public List<MerchantDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantDTO> resultList = merchantDAO.selectDTO(params);
        // 判断此处是否为APP端请求数据
        if (params.get("app") != null && resultList != null) {
            ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
            BigDecimal discountRate = parametersConfigDTO.getDiscountRate();
            for (MerchantDTO merchantDTO : resultList) {
                if (discountRate != null) {
                    BigDecimal bigDecimal = discountRate;
                    BigDecimal sellDiscount = merchantDTO.getSellDiscount();
                    BigDecimal marketingDiscount = merchantDTO.getMarketingDiscount();
                    if (sellDiscount != null && marketingDiscount != null) {
                        BigDecimal payDiscountRate = merchantDTO.getPaySellDiscount()
                                .add(merchantDTO.getMarketingDiscount()
                                        .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getMerchantDiscountRatePlatformProportion()))).setScale(4, RoundingMode.HALF_UP);
                        merchantDTO.setUserDiscount(payDiscountRate);
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return merchantDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return merchantDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = merchantDAO.groupCount(conditions);
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
        return merchantDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = merchantDAO.groupSum(conditions);
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
    public int countMerchantApprove(Map<String, Object> params) {
        return merchantDAO.countMerchantApprove(params);
    }

    @Override
    public List<MerchantDetailDTO> listMerchantApprove(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantDetailDTO> resultList = merchantDAO.listMerchantApprove(params);
        return resultList;
    }

    @Override
    public int countMerchant(Map<String, Object> params) {
        return merchantDAO.countMerchant(params);
    }

    @Override
    public int countMerchantH5(Map<String, Object> params) {
        return merchantDAO.countMerchantH5(params);
    }

    @Override
    public List<MerchantDetailDTO> listMerchant(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantDetailDTO> resultList = merchantDAO.listMerchant(params);
        return resultList;
    }

    @Override
    public List<ApiMerchantDTO> listMerchantH5(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ApiMerchantDTO> resultList = merchantDAO.listMerchantH5(params);
        for(ApiMerchantDTO dto:resultList){
            if(1 == dto.getMerchantClass()){
                ApiMerchantDTO dt = merchantDAO.apiMerchantH5(dto.getSuperMerchantId());
                dto.setSuperMerchantName(dt.getPracticalName());
            }
        }
        return resultList;
    }
    @Override
    public MerchantDetailDTO selectMerchantApproveById(Map<String, Object> params) {
        MerchantDetailDTO merchantDetailDTO = merchantDAO.findApproveMerchantDetail(Long.valueOf(params.get("id").toString()));
        if (merchantDetailDTO != null) {
            Map<String, Object> map = new HashMap<>(1);
            map.put("merchantId", params.get("id"));
            List<ContactPersonDTO> contactPersonDTOList = contactPersonService.find(map, null, null);
            merchantDetailDTO.setContactPersonDTOList(contactPersonDTOList);
            params.clear();
            params.put("merchantId", merchantDetailDTO.getId());
            if (merchantDetailDTO.getEntityType()!=null){
                if (merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {
                    List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
                    List<BeneficiaryDTO> beneficiaryDTOList = beneficiaryService.find(params, null, null);
                    List<DirectorDTO> directorDTOList = directorService.find(map, null, null);
                    merchantDetailDTO.setDirectorDTOList(directorDTOList);
                    merchantDetailDTO.setShareholderDTOList(shareholderDTOList);
                    merchantDetailDTO.setBeneficiaryDTOList(beneficiaryDTOList);
                } else if (merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {
                    List<PartnerDTO> partnerDTOList = partnerService.find(params, null, null);
                    merchantDetailDTO.setPartnerDTOList(partnerDTOList);
                } else if (merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {
                    List<DirectorDTO> directorDTOList = directorService.find(map, null, null);
                    merchantDetailDTO.setDirectorDTOList(directorDTOList);
                    List<TrusteeDTO> trusteeDTOList = trusteeService.find(params, null, null);
                    List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
                    merchantDetailDTO.setTrusteeDTOList(trusteeDTOList);
                    merchantDetailDTO.setShareholderDTOList(shareholderDTOList);
                } else if (merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {
                    List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
                    merchantDetailDTO.setShareholderDTOList(shareholderDTOList);
                }
            }
            if (!StringUtils.isEmpty(merchantDetailDTO.getDocusignFiles())) {
                JSONObject docusignFiles = JSONObject.parseObject(merchantDetailDTO.getDocusignFiles());
                List<JSONObject> docusignFileList = new ArrayList<>(3);
                docusignFiles.keySet().forEach(key -> {
                    if (!key.startsWith("envelopId")) {
                        JSONObject file = new JSONObject();
                        file.put("name", key);
                        file.put("url", docusignFiles.getString(key));
                        docusignFileList.add(file);
                    }
                });
                merchantDetailDTO.setDocusignFileList(docusignFileList);
            }
            // 商户信息二十个文件
            if (!StringUtils.isEmpty(merchantDetailDTO.getFileList())) {
                List<String> urls = JSONArray.parseArray(merchantDetailDTO.getFileList(), String.class);
                if (urls != null && urls.size() > 0) {
                    List<JSONObject> urlFile = new ArrayList<>(urls.size());
                    urls.forEach(s -> {
                        JSONObject file = new JSONObject();
                        String a = SnowflakeUtil.getEncryptionId();
                        String[] url = s.split("/");
                        file.put("uid", a);
                        file.put("name", url[url.length - 1]);
                        file.put("url", s);
                        file.put("status", "done");
                        urlFile.add(file);
                    });
                    merchantDetailDTO.setFiles(urlFile);
                }
            }
            // 信托文件
            if (!StringUtils.isEmpty(merchantDetailDTO.getTrusteeFileList())) {
                List<String> trusteeUrls = JSONArray.parseArray(merchantDetailDTO.getTrusteeFileList(), String.class);
                if (trusteeUrls != null && trusteeUrls.size() > 0) {
                    List<JSONObject> trusteeUrlFile = new ArrayList<>(trusteeUrls.size());
                    trusteeUrls.forEach(s -> {
                        JSONObject file = new JSONObject();
                        String a = SnowflakeUtil.getEncryptionId();
                        String[] url = s.split("/");
                        file.put("uid", a);
                        file.put("name", url[url.length - 1]);
                        file.put("url", s);
                        file.put("status", "done");
                        trusteeUrlFile.add(file);
                    });
                    merchantDetailDTO.setTrusteeFiles(trusteeUrlFile);
                } else {
                    merchantDetailDTO.setTrusteeFiles(new ArrayList<>());
                }
            }
            // 让利用户、平台所得返回
            CreditMerchantDTO creditMerchantDTO = new CreditMerchantDTO();
            BigDecimal bigDecimal = new BigDecimal("100");
            creditMerchantDTO.setPercentageToUser(merchantDetailDTO.getSellDiscount().multiply(bigDecimal));
            creditMerchantDTO.setPercentageToPlatform(merchantDetailDTO.getRebateDiscount().multiply(bigDecimal));
            merchantDetailDTO.setCreditMerchantDTO(creditMerchantDTO);
            //返回支付平台所得、支付用户让利
            merchantDetailDTO.setPayPercentageToUser(merchantDetailDTO.getPaySellDiscount().multiply(bigDecimal));
            merchantDetailDTO.setPayPercentageToPlatform(merchantDetailDTO.getPayRebateDiscount().multiply(bigDecimal));

            // 费率修改
            int appChargeChoice = MathUtils.multiply(merchantDetailDTO.getAppChargeRate(), new BigDecimal("100")).intValue();
            merchantDetailDTO.setAppChargeChoice(new Integer(appChargeChoice).toString());
            merchantDetailDTO.setBaseRate(merchantDetailDTO.getBaseRate().multiply(new BigDecimal("100")));
            merchantDetailDTO.setBasePayRate(merchantDetailDTO.getBasePayRate().multiply(new BigDecimal("100")));
            merchantDetailDTO.setAppChargeRate(merchantDetailDTO.getAppChargeRate().multiply(new BigDecimal("100")));
            merchantDetailDTO.setAppChargePayRate(merchantDetailDTO.getAppChargePayRate().multiply(new BigDecimal("100")));
            merchantDetailDTO.setExtraDiscount(merchantDetailDTO.getExtraDiscount().multiply(new BigDecimal("100")));
            merchantDetailDTO.setMarketingDiscount(merchantDetailDTO.getMarketingDiscount().multiply(new BigDecimal("100")));

            // 渠道费率查询
            map.clear();
            map.put("merchantId", merchantDetailDTO.getId());
            List<RouteDTO> routeDTOList = routeService.find(map, null, null);
            if (routeDTOList != null) {
                for (RouteDTO routeDTO : routeDTOList) {
                    routeDTO.setRate(Double.valueOf(MathUtils.multiply(Double.toString(routeDTO.getRate()), "100")));
                }
                merchantDetailDTO.setRouteDTOList(routeDTOList);
            }
        }
        return merchantDetailDTO;
    }

    @Override
    public MerchantDetailH5DTO selectMerchantApproveByIdH5(Map<String, Object> params) {
        MerchantDetailH5DTO merchantDetailDTO = merchantDAO.findApproveMerchantDetailH5(Long.valueOf(params.get("id").toString()));
        if (merchantDetailDTO != null) {
            Map<String, Object> map = new HashMap<>(1);
            map.put("merchantId", params.get("id"));
            //查询联系人信息表
            List<ContactPersonDTO> contactPersonDTOList = contactPersonService.find(map, null, null);
            merchantDetailDTO.setContactPersonDTOList(contactPersonDTOList);
            params.clear();
            params.put("merchantId", merchantDetailDTO.getId());
            if (merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {
                List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
                List<BeneficiaryDTO> beneficiaryDTOList = beneficiaryService.find(params, null, null);
                List<DirectorDTO> directorDTOList = directorService.find(map, null, null);
                merchantDetailDTO.setDirectorDTOList(directorDTOList);
                merchantDetailDTO.setShareholderDTOList(shareholderDTOList);
                merchantDetailDTO.setBeneficiaryDTOList(beneficiaryDTOList);
            } else if (merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {
                List<PartnerDTO> partnerDTOList = partnerService.find(params, null, null);
                merchantDetailDTO.setPartnerDTOList(partnerDTOList);
            } else if (merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {
                List<DirectorDTO> directorDTOList = directorService.find(map, null, null);
                merchantDetailDTO.setDirectorDTOList(directorDTOList);
                List<TrusteeDTO> trusteeDTOList = trusteeService.find(params, null, null);
                List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
                merchantDetailDTO.setTrusteeDTOList(trusteeDTOList);
                merchantDetailDTO.setShareholderDTOList(shareholderDTOList);
            } else if (merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantDetailDTO.getEntityType() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {
                List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
                merchantDetailDTO.setShareholderDTOList(shareholderDTOList);
            }
            if (!StringUtils.isEmpty(merchantDetailDTO.getDocusignFiles())) {
                JSONObject docusignFiles = JSONObject.parseObject(merchantDetailDTO.getDocusignFiles());
                List<JSONObject> docusignFileList = new ArrayList<>(3);
                docusignFiles.keySet().forEach(key -> {
                    if (!key.startsWith("envelopId")) {
                        JSONObject file = new JSONObject();
                        file.put("name", key);
                        file.put("url", docusignFiles.getString(key));
                        docusignFileList.add(file);
                    }
                });
                merchantDetailDTO.setDocusignFileList(docusignFileList);
            }
            // 商户信息二十个文件
            if (!StringUtils.isEmpty(merchantDetailDTO.getFileList())) {
                List<String> urls = JSONArray.parseArray(merchantDetailDTO.getFileList(), String.class);
                if (urls != null && urls.size() > 0) {
                    List<JSONObject> urlFile = new ArrayList<>(urls.size());
                    urls.forEach(s -> {
                        JSONObject file = new JSONObject();
                        String a = SnowflakeUtil.getEncryptionId();
                        String[] url = s.split("/");
                        file.put("uid", a);
                        file.put("name", url[url.length - 1]);
                        file.put("url", s);
                        file.put("status", "done");
                        urlFile.add(file);
                    });
                    merchantDetailDTO.setFiles(urlFile);
                }
            }
            // 信托文件
            if (!StringUtils.isEmpty(merchantDetailDTO.getTrusteeFileList())) {
                List<String> trusteeUrls = JSONArray.parseArray(merchantDetailDTO.getTrusteeFileList(), String.class);
                if (trusteeUrls != null && trusteeUrls.size() > 0) {
                    List<JSONObject> trusteeUrlFile = new ArrayList<>(trusteeUrls.size());
                    trusteeUrls.forEach(s -> {
                        JSONObject file = new JSONObject();
                        String a = SnowflakeUtil.getEncryptionId();
                        String[] url = s.split("/");
                        file.put("uid", a);
                        file.put("name", url[url.length - 1]);
                        file.put("url", s);
                        file.put("status", "done");
                        trusteeUrlFile.add(file);
                    });
                    merchantDetailDTO.setTrusteeFiles(trusteeUrlFile);
                } else {
                    merchantDetailDTO.setTrusteeFiles(new ArrayList<>());
                }
            }
            // 让利用户、平台所得返回
//            CreditMerchantDTO creditMerchantDTO = new CreditMerchantDTO();
//            BigDecimal bigDecimal = new BigDecimal("100");
//            creditMerchantDTO.setPercentageToUser(merchantDetailDTO.getSellDiscount().multiply(bigDecimal));
//            creditMerchantDTO.setPercentageToPlatform(merchantDetailDTO.getRebateDiscount().multiply(bigDecimal));
//            merchantDetailDTO.setCreditMerchantDTO(creditMerchantDTO);
            //返回支付平台所得、支付用户让利
//            merchantDetailDTO.setPayPercentageToUser(merchantDetailDTO.getPaySellDiscount().multiply(bigDecimal));
//            merchantDetailDTO.setPayPercentageToPlatform(merchantDetailDTO.getPayRebateDiscount().multiply(bigDecimal));

            // 费率修改
//            int appChargeChoice = MathUtils.multiply(merchantDetailDTO.getAppChargeRate(), new BigDecimal("100")).intValue();
//            merchantDetailDTO.setAppChargeChoice(new Integer(appChargeChoice).toString());
//            merchantDetailDTO.setBaseRate(merchantDetailDTO.getBaseRate().multiply(new BigDecimal("100")));
//            merchantDetailDTO.setBasePayRate(merchantDetailDTO.getBasePayRate().multiply(new BigDecimal("100")));
//            merchantDetailDTO.setAppChargeRate(merchantDetailDTO.getAppChargeRate().multiply(new BigDecimal("100")));
//            merchantDetailDTO.setAppChargePayRate(merchantDetailDTO.getAppChargePayRate().multiply(new BigDecimal("100")));
//            merchantDetailDTO.setExtraDiscount(merchantDetailDTO.getExtraDiscount().multiply(new BigDecimal("100")));
//            merchantDetailDTO.setMarketingDiscount(merchantDetailDTO.getMarketingDiscount().multiply(new BigDecimal("100")));

            // 渠道费率查询
            map.clear();
            map.put("merchantId", merchantDetailDTO.getId());
            List<RouteDTO> routeDTOList = routeService.find(map, null, null);
            if (routeDTOList != null) {
                for (RouteDTO routeDTO : routeDTOList) {
                    routeDTO.setRate(Double.valueOf(MathUtils.multiply(Double.toString(routeDTO.getRate()), "100")));
                }
                merchantDetailDTO.setRouteDTOList(routeDTOList);
            }
        }
        return merchantDetailDTO;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replenishMerchant(@NonNull MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        // 校验参数
        checkMerchant(merchantDTO, request);

        //根据地址获取经纬度
        Map<String, Object> params = new HashMap<>(1);
        params.put("code", "merchantState");
        params.put("value", merchantDTO.getMerchantState());
        StaticDataDTO merchantState = staticDataService.findOneStaticData(params);
        params.clear();
        params.put("code", "city");
        params.put("value", merchantDTO.getMerchantState());
        StaticDataDTO city = staticDataService.findOneStaticData(params);
        String citySuburb = "";
        if (!StringUtils.isEmpty(merchantDTO.getSuburb())) {
            citySuburb = merchantDTO.getSuburb().replaceAll(" ", "+") + "+" + city.getEnName();
        } else {
            citySuburb = city.getEnName();
            citySuburb = StringUtils.isNotBlank(citySuburb) ? citySuburb.replace(" ","-") : citySuburb;
        }
        String addressParam = merchantDTO.getAddress().replaceAll(" ", "+") + ",+" + citySuburb + ",+" + merchantState.getEnName();
        try {
            String mapsApi = googleMapsApi + addressParam + "&key=" + googleApiKey;
            JSONObject location = JSONObject.parseObject(HttpClientUtils.sendGet(mapsApi));
            location = location.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
            merchantDTO.setLat(location.getString("lat"));
            merchantDTO.setLng(location.getString("lng"));
        } catch (Exception e) {
            log.error("replenishMerchant 方法:通过Google Map API 获取地址经纬度失败,addressParam:{},e:{}",addressParam,e.getMessage());
            throw new BizException(I18nUtils.get("location.fail", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getLat())) {
            merchantDTO.setLat("-33.870376");
        }
        if (StringUtils.isEmpty(merchantDTO.getLng())) {
            merchantDTO.setLng("151.210303");
        }

        MerchantDTO oneMerchant;
        if(merchantDTO.getId() != null ){
            oneMerchant = this.findMerchantById(merchantDTO.getId() );
        }else{
            oneMerchant = getMerchant(merchantDTO.getUserId(), request);
        }

        merchantDTO.setAcn(merchantDTO.getAbn().substring(merchantDTO.getAbn().length()-9));
        merchantDTO.setIsAvailable(StaticDataEnum.MERCHANT_AVAILABLE_0.getCode());
        merchantDTO.setState(StaticDataEnum.MERCHANT_STATE_0.getCode());
        // 费率修改
        if (merchantDTO.getAppChargeChoice() != null) {
            merchantDTO.setAppChargeRate(MathUtils.divide(new BigDecimal(merchantDTO.getAppChargeChoice()), new BigDecimal("100"), 4));
            merchantDTO.setAppChargePayRate(merchantDTO.getAppChargeRate());
        }
        if (merchantDTO.getBaseRate() != null) {
            merchantDTO.setBaseRate(MathUtils.divide(merchantDTO.getBaseRate(), new BigDecimal("100"), 4));
        }
        if (merchantDTO.getExtraDiscount() != null) {
            merchantDTO.setExtraDiscount(MathUtils.divide(merchantDTO.getExtraDiscount(), new BigDecimal("100"), 4));
        }
        // 渠道手续费率
//        if (merchantDTO.getRouteDTOList() != null && merchantDTO.getRouteDTOList().size() > 0) {
//            routeService.deleteRouteByMerchantId(merchantDTO.getId());
//            MerchantDetailDTO merchantDetailDTO = new MerchantDetailDTO();
//            merchantDetailDTO.setRouteDTOList(merchantDTO.getRouteDTOList());
//            saveRouteList(merchantDetailDTO, request);
//        }
        // 设置渠道手续费
//        params.clear();
//        params.put("code", "merGatewayType");
//        List<StaticDataDTO> staticDataDTOList = staticDataService.find(params, null, null);
//        List<RouteDTO> routeDTOList = new ArrayList<>(staticDataDTOList.size());
//        staticDataDTOList.stream().forEach(staticData -> {
//            RouteDTO routeDTO = new RouteDTO();
//            routeDTO.setMerchantId(oneMerchant.getId());
//            routeDTO.setGatewayType(new Integer(staticData.getValue()));
//            routeDTO.setRateType(merchantDTO.getChargeMode());
//            routeDTOList.add(routeDTO);
//        });
//        routeService.deleteRouteByMerchantId(oneMerchant.getId());
//        MerchantDetailDTO merchantDetailDTO = new MerchantDetailDTO();
//        merchantDetailDTO.setId(oneMerchant.getId());
//        merchantDetailDTO.setRouteDTOList(routeDTOList);
//        saveRouteList(merchantDetailDTO, request);
        updateMerchant(oneMerchant.getId(), merchantDTO, request);

//        Long merchantId = oneMerchant.getId();
//        // 联系人添加
//        List<ContactPersonDTO> contactPersonDTOList = merchantDTO.getContactPersonDTOList();
//        if (contactPersonDTOList != null && !contactPersonDTOList.isEmpty()) {
//            contactPersonService.deleteContactPersonByMerchantId(merchantId);
//            List<ContactPerson> contactPersonList = new ArrayList<>(contactPersonDTOList.size());
//            contactPersonDTOList.forEach(contactPersonDTO -> {
//                ContactPerson contactPerson = BeanUtil.copyProperties(contactPersonDTO, new ContactPerson());
//                contactPerson = (ContactPerson) this.packAddBaseProps(contactPerson, request);
//                contactPerson.setMerchantId(merchantId);
//                contactPersonList.add(contactPerson);
//            });
//            contactPersonService.saveContactPersonList(contactPersonList, request);
//        }


    }

    @Override
    public void replenishDirectorAndOwner(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        Long merchantId = merchantDTO.getId();
        // 董事、股东、受益人信息添加
        Integer merchantEntityType = merchantDTO.getEntityType();
        // 当为公司类型时，添加董事、股东信息
        if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {

            directorService.deleteDirectorByMerchantId(merchantId);
            shareholderService.deleteShareholderByMerchantId(merchantId);

            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();
            List<DirectorDTO> directorDTOList = merchantDTO.getDirectorDTOList();

            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                shareholderService.deleteShareholderByMerchantId(merchantId);
                List<Shareholder> shareholderList = new ArrayList<>(shareholderDTOList.size());
                shareholderDTOList.forEach(shareholderDTO -> {
                    Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
                    shareholder = (Shareholder) this.packAddBaseProps(shareholder, request);
                    shareholder.setMerchantId(merchantId);
                    shareholderList.add(shareholder);
                });
                shareholderService.saveShareholderList(shareholderList, request);
            }

            if (directorDTOList != null && !directorDTOList.isEmpty()) {
                directorService.deleteDirectorByMerchantId(merchantId);
                List<Director> directorList = new ArrayList<>(directorDTOList.size());
                directorDTOList.forEach(directorDTO -> {
                    Director director = BeanUtil.copyProperties(directorDTO, new Director());
                    director = (Director) this.packAddBaseProps(director, request);
                    director.setMerchantId(merchantId);
                    directorList.add(director);
                });
                directorService.saveDirectorList(directorList, request);
            }

        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {

            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();

            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                shareholderService.deleteShareholderByMerchantId(merchantId);
                List<Shareholder> shareholderList = new ArrayList<>(shareholderDTOList.size());
                shareholderDTOList.forEach(shareholderDTO -> {
                    Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
                    shareholder = (Shareholder) this.packAddBaseProps(shareholder, request);
                    shareholder.setMerchantId(merchantId);
                    shareholderList.add(shareholder);
                });
                shareholderService.saveShareholderList(shareholderList, request);
            }

        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {

            List<PartnerDTO> partnerDTOList = merchantDTO.getPartnerDTOList();

            if (partnerDTOList != null && !partnerDTOList.isEmpty()) {
                partnerService.deletePartnerByMerchantId(merchantId);
                List<Partner> partnerList = new ArrayList<>(partnerDTOList.size());
                partnerDTOList.forEach(partnerDTO -> {
                    Partner partner = BeanUtil.copyProperties(partnerDTO, new Partner());
                    partner = (Partner) this.packAddBaseProps(partner, request);
                    partner.setMerchantId(merchantId);
                    partnerList.add(partner);
                });
                partnerService.savePartnerList(partnerList, request);
            }

        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {

            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();
            List<TrusteeDTO> trusteeDTOList = merchantDTO.getTrusteeDTOList();
            List<DirectorDTO> directorDTOList = merchantDTO.getDirectorDTOList();

            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                shareholderService.deleteShareholderByMerchantId(merchantId);
                List<Shareholder> shareholderList = new ArrayList<>(shareholderDTOList.size());
                shareholderDTOList.forEach(shareholderDTO -> {
                    Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
                    shareholder = (Shareholder) this.packAddBaseProps(shareholder, request);
                    shareholder.setMerchantId(merchantId);
                    shareholderList.add(shareholder);
                });
                shareholderService.saveShareholderList(shareholderList, request);
            }

            if (trusteeDTOList != null && !trusteeDTOList.isEmpty()) {
                trusteeService.deleteTrusteeByMerchantId(merchantId);
                List<Trustee> trusteeList = new ArrayList<>(trusteeDTOList.size());
                trusteeDTOList.forEach(trusteeDTO -> {
                    Trustee trustee = BeanUtil.copyProperties(trusteeDTO, new Trustee());
                    trustee = (Trustee) this.packAddBaseProps(trustee, request);
                    trustee.setMerchantId(merchantId);
                    trusteeList.add(trustee);
                });
                trusteeService.saveTrusteeList(trusteeList, request);
            }

            if (directorDTOList != null && !directorDTOList.isEmpty()) {
                directorService.deleteDirectorByMerchantId(merchantId);
                List<Director> directorList = new ArrayList<>(directorDTOList.size());
                directorDTOList.forEach(directorDTO -> {
                    Director director = BeanUtil.copyProperties(directorDTO, new Director());
                    director = (Director) this.packAddBaseProps(director, request);
                    director.setMerchantId(merchantId);
                    directorList.add(director);
                });
                directorService.saveDirectorList(directorList, request);
            }

        }
        // 二十个文件
        List<JSONObject> files = merchantDTO.getFiles();
        if (files != null && files.size() > 0) {
            JSONArray filesUrl = new JSONArray();
            files.stream().forEach(jsonObject -> {
                String url = jsonObject.getString("url");
                filesUrl.add(url);
            });
            merchantDTO.setFileList(filesUrl.toJSONString());
        }
        // 信托文件
        List<JSONObject> trusteeFiles = merchantDTO.getTrusteeFiles();
        if (trusteeFiles != null && trusteeFiles.size() > 0) {
            JSONArray filesUrl = new JSONArray();
            trusteeFiles.stream().forEach(jsonObject -> {
                String url = jsonObject.getString("url");
                filesUrl.add(url);
            });
            merchantDTO.setTrusteeFileList(filesUrl.toJSONString());
        }
        updateMerchant(merchantDTO.getId(), merchantDTO, request);
    }

    @Override
    public void replenishH5DirectorAndOwner(ApiMerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        Long merchantId = merchantDTO.getId();
        // 董事、股东、受益人信息添加
        Integer merchantEntityType = merchantDTO.getEntityType();
        // 当为公司类型时，添加董事、股东信息
        if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {

            directorService.deleteDirectorByMerchantId(merchantId);
            shareholderService.deleteShareholderByMerchantId(merchantId);

            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();
            List<DirectorDTO> directorDTOList = merchantDTO.getDirectorDTOList();

            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                shareholderService.deleteShareholderByMerchantId(merchantId);
                List<Shareholder> shareholderList = new ArrayList<>(shareholderDTOList.size());
                shareholderDTOList.forEach(shareholderDTO -> {
                    Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
                    shareholder = (Shareholder) this.packAddBaseProps(shareholder, request);
                    shareholder.setMerchantId(merchantId);
                    shareholderList.add(shareholder);
                });
                shareholderService.saveShareholderList(shareholderList, request);
            }

            if (directorDTOList != null && !directorDTOList.isEmpty()) {
                directorService.deleteDirectorByMerchantId(merchantId);
                List<Director> directorList = new ArrayList<>(directorDTOList.size());
                directorDTOList.forEach(directorDTO -> {
                    Director director = BeanUtil.copyProperties(directorDTO, new Director());
                    director = (Director) this.packAddBaseProps(director, request);
                    director.setMerchantId(merchantId);
                    directorList.add(director);
                });
                directorService.saveDirectorList(directorList, request);
            }

        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {

            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();

            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                shareholderService.deleteShareholderByMerchantId(merchantId);
                List<Shareholder> shareholderList = new ArrayList<>(shareholderDTOList.size());
                shareholderDTOList.forEach(shareholderDTO -> {
                    Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
                    shareholder = (Shareholder) this.packAddBaseProps(shareholder, request);
                    shareholder.setMerchantId(merchantId);
                    shareholderList.add(shareholder);
                });
                shareholderService.saveShareholderList(shareholderList, request);
            }

        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {

            List<PartnerDTO> partnerDTOList = merchantDTO.getPartnerDTOList();

            if (partnerDTOList != null && !partnerDTOList.isEmpty()) {
                partnerService.deletePartnerByMerchantId(merchantId);
                List<Partner> partnerList = new ArrayList<>(partnerDTOList.size());
                partnerDTOList.forEach(partnerDTO -> {
                    Partner partner = BeanUtil.copyProperties(partnerDTO, new Partner());
                    partner = (Partner) this.packAddBaseProps(partner, request);
                    partner.setMerchantId(merchantId);
                    partnerList.add(partner);
                });
                partnerService.savePartnerList(partnerList, request);
            }

        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {

            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();
            List<TrusteeDTO> trusteeDTOList = merchantDTO.getTrusteeDTOList();
            List<DirectorDTO> directorDTOList = merchantDTO.getDirectorDTOList();

            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                shareholderService.deleteShareholderByMerchantId(merchantId);
                List<Shareholder> shareholderList = new ArrayList<>(shareholderDTOList.size());
                shareholderDTOList.forEach(shareholderDTO -> {
                    Shareholder shareholder = BeanUtil.copyProperties(shareholderDTO, new Shareholder());
                    shareholder = (Shareholder) this.packAddBaseProps(shareholder, request);
                    shareholder.setMerchantId(merchantId);
                    shareholderList.add(shareholder);
                });
                shareholderService.saveShareholderList(shareholderList, request);
            }

            if (trusteeDTOList != null && !trusteeDTOList.isEmpty()) {
                trusteeService.deleteTrusteeByMerchantId(merchantId);
                List<Trustee> trusteeList = new ArrayList<>(trusteeDTOList.size());
                trusteeDTOList.forEach(trusteeDTO -> {
                    Trustee trustee = BeanUtil.copyProperties(trusteeDTO, new Trustee());
                    trustee = (Trustee) this.packAddBaseProps(trustee, request);
                    trustee.setMerchantId(merchantId);
                    trusteeList.add(trustee);
                });
                trusteeService.saveTrusteeList(trusteeList, request);
            }

            if (directorDTOList != null && !directorDTOList.isEmpty()) {
                directorService.deleteDirectorByMerchantId(merchantId);
                List<Director> directorList = new ArrayList<>(directorDTOList.size());
                directorDTOList.forEach(directorDTO -> {
                    Director director = BeanUtil.copyProperties(directorDTO, new Director());
                    director = (Director) this.packAddBaseProps(director, request);
                    director.setMerchantId(merchantId);
                    directorList.add(director);
                });
                directorService.saveDirectorList(directorList, request);
            }

        }

        ApiMerchant merchant = BeanUtil.copyProperties(merchantDTO, new ApiMerchant());
        merchant.setId(merchantDTO.getId());
        if (request != null) {
            merchant = (ApiMerchant) this.packModifyBaseProps(merchant, request);
        } else {
            merchant.setModifiedDate(System.currentTimeMillis());
        }


        int cnt = apiMerchantDAO.update(merchant);
        if (cnt != 1) {
            log.error("update error, data:{}", merchantDTO);
            throw new BizException("update merchant Error!");
        }
    }

    @Override
    public void replenishBank(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        checkBank(merchantDTO, request);
        MerchantDTO oneMerchant = getMerchant(merchantDTO.getUserId(), request);
        merchantDTO.setIsAvailable(StaticDataEnum.MERCHANT_AVAILABLE_0.getCode());
        merchantDTO.setState(StaticDataEnum.MERCHANT_STATE_0.getCode());
        updateMerchant(oneMerchant.getId(), merchantDTO, request);
    }

    @Override
    public void replenishLogo(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        checkIntro(merchantDTO, request);
        MerchantDTO oneMerchant = getMerchant(merchantDTO.getUserId(), request);
        if(StaticDataEnum.MERCHANT_AVAILABLE_0.getCode()==oneMerchant.getIsAvailable()){
            //如果不可用商户提出修改，审核状态改为未审核
            merchantDTO.setState(StaticDataEnum.MERCHANT_STATE_0.getCode());
        }
        updateMerchant(oneMerchant.getId(), merchantDTO, request);
    }

    /**
     * 校验商家银行参数
     * @param merchantDTO
     */
    private void checkBank(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        // 校验BSB
        String bsb = merchantDTO.getBsb();
        if (StringUtils.isBlank(bsb)) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (bsb.length() > Validator.BSB_NO_LENGTH) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        // 校验银行账号
        String accountNo = merchantDTO.getAccountNo();
        if (null == accountNo) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (accountNo.length() < Validator.BANK_ACCOUNT_NAME_MIN_LENGTH ||
                accountNo.length() > Validator.BANK_ACCOUNT_NAME_MAX_LENGTH) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        // 校验银行名称
        String bankName = merchantDTO.getBankName();
        if (StringUtils.isBlank(bankName)) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (bankName.length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        // 校验开户人姓名
        String accountName = merchantDTO.getAccountName();
        if (StringUtils.isBlank(accountName)) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (accountName.length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
    }

    /**
     * 校验商家简介参数
     * @param merchantDTO
     */
    private void checkIntro(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        // 校验商家简介
        String intro = merchantDTO.getIntro();
        if (StringUtils.isNotBlank(intro) && intro.length() > Validator.TEXT_LENGTH_1000) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
    }

    /**
     * 校验商家基础新消息参数
     * @param merchantDTO
     */
    private void checkMerchant(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {

        // 校验商业名称
        String corporateName = merchantDTO.getCorporateName();
        if (StringUtils.isBlank(corporateName)) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (corporateName.length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        // 校验实用名称
//        String practicalName = merchantDTO.getPracticalName();
//        if (StringUtils.isBlank(practicalName)) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }
//        if (practicalName.length() > Validator.TEXT_LENGTH_100) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }

        // 校验ABN
        String abn = merchantDTO.getAbn();
        if (StringUtils.isBlank(abn)) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (abn.length() > Validator.ABN) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        // 校验详细地址
//        String address = merchantDTO.getAddress();
//        if (StringUtils.isBlank(address)) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }
//        if (address.length() > Validator.TEXT_LENGTH_1000) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }

        // 校验城市
//        String city = merchantDTO.getCity();
//        if (StringUtils.isBlank(city)) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }
//        if (city.length() > Validator.TEXT_LENGTH_1000) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }

        // 校验邮编
//        String postcode = merchantDTO.getPostcode();
//        if (StringUtils.isBlank(postcode)) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }
//        if (postcode.length() != Validator.BANK_ZIP_LENGTH) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }

        // 校验关键联系人名字
//        String liaisonFirstName = merchantDTO.getLiaisonFirstName();
//        if (StringUtils.isBlank(liaisonFirstName)) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }
//        if (liaisonFirstName.length() > Validator.TEXT_LENGTH_80) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }

        // 校验关键联系人 中名
//        String liaisonMiddleName = merchantDTO.getLiaisonMiddleName();
//        if (StringUtils.isNotBlank(liaisonMiddleName) && liaisonMiddleName.length() > Validator.TEXT_LENGTH_80) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }

        // 校验关键联系人 姓
//        String liaisonLastName = merchantDTO.getLiaisonLastName();
//        if (StringUtils.isBlank(liaisonLastName)) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }
//        if (liaisonLastName.length() > Validator.TEXT_LENGTH_80) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }

        // 校验email
//        String email = merchantDTO.getEmail();
//        if (StringUtils.isBlank(email)) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }
//        if (email.length() > Validator.TEXT_LENGTH_100) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }

        // 校验电话
//        String phone = merchantDTO.getPhone();
//        if (StringUtils.isBlank(phone)) {
//            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//        }

        // 根据公司类型，相关docusign签署人信息校验
//        Integer merchantEntityType = merchantDTO.getEntityType();
//        if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {
//
//            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();
//            List<DirectorDTO> directorDTOList = merchantDTO.getDirectorDTOList();
//
//            if (!(merchantDTO.getCompanyType().intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode())) {
//                if (directorDTOList == null || directorDTOList.size() == 0) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//
//                for (DirectorDTO directorDTO : directorDTOList) {
//                    String firstName = directorDTO.getFirstName();
//                    if (StringUtils.isBlank(firstName)) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                    if (firstName.length() > Validator.TEXT_LENGTH_100) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                    String lastName = directorDTO.getLastName();
//                    if (StringUtils.isBlank(lastName)) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                    if (lastName.length() > Validator.TEXT_LENGTH_100) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                }
//            }
//
//            if (shareholderDTOList == null || shareholderDTOList.size() == 0) {
//                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//            }
//
//            for (ShareholderDTO shareholderDTO : shareholderDTOList) {
//                String firstName = shareholderDTO.getFirstName();
//                if (StringUtils.isBlank(firstName)) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//                if (firstName.length() > Validator.TEXT_LENGTH_100) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//                String lastName = shareholderDTO.getLastName();
//                if (StringUtils.isBlank(lastName)) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//                if (lastName.length() > Validator.TEXT_LENGTH_100) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//            }
//
//        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {
//
//            List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();
//
//            if (shareholderDTOList == null || shareholderDTOList.size() == 0) {
//                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//            }
//
//            for (ShareholderDTO shareholderDTO : shareholderDTOList) {
//                String firstName = shareholderDTO.getFirstName();
//                if (StringUtils.isBlank(firstName)) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//                if (firstName.length() > Validator.TEXT_LENGTH_100) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//                String lastName = shareholderDTO.getLastName();
//                if (StringUtils.isBlank(lastName)) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//                if (lastName.length() > Validator.TEXT_LENGTH_100) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//            }
//
//        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {
//
//            List<PartnerDTO> partnerDTOList = merchantDTO.getPartnerDTOList();
//
//            if (partnerDTOList == null || partnerDTOList.size() == 0) {
//                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//            }
//
//            for (PartnerDTO partnerDTO : partnerDTOList) {
//                String firstName = partnerDTO.getFirstName();
//                if (StringUtils.isBlank(firstName)) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//                if (firstName.length() > Validator.TEXT_LENGTH_100) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//                String lastName = partnerDTO.getLastName();
//                if (StringUtils.isBlank(lastName)) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//                if (lastName.length() > Validator.TEXT_LENGTH_100) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//            }
//
//        } else if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode() || merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_5.getCode()) {
//
//            if (merchantEntityType.intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {
//                List<TrusteeDTO> trusteeDTOList = merchantDTO.getTrusteeDTOList();
//
//                if (trusteeDTOList == null || trusteeDTOList.size() == 0) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//
//                for (TrusteeDTO trusteeDTO : trusteeDTOList) {
//                    String firstName = trusteeDTO.getFirstName();
//                    if (StringUtils.isBlank(firstName)) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                    if (firstName.length() > Validator.TEXT_LENGTH_100) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                    String lastName = trusteeDTO.getLastName();
//                    if (StringUtils.isBlank(lastName)) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                    if (lastName.length() > Validator.TEXT_LENGTH_100) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                }
//            } else {
//                List<ShareholderDTO> shareholderDTOList = merchantDTO.getShareholderDTOList();
//
//                if (shareholderDTOList == null || shareholderDTOList.size() == 0) {
//                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                }
//
//                for (ShareholderDTO shareholderDTO : shareholderDTOList) {
//                    String firstName = shareholderDTO.getFirstName();
//                    if (StringUtils.isBlank(firstName)) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                    if (firstName.length() > Validator.TEXT_LENGTH_100) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                    String lastName = shareholderDTO.getLastName();
//                    if (StringUtils.isBlank(lastName)) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                    if (lastName.length() > Validator.TEXT_LENGTH_100) {
//                        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
//                    }
//                }
//            }
//
//        }
    }

    @Override
    public void submitAudit(@NonNull Long userId, HttpServletRequest request) throws BizException {
        MerchantDTO oneMerchant = getMerchant(userId, request);
        MerchantDTO merchantDTO1 = new MerchantDTO();
        if (oneMerchant.getState().intValue() == StaticDataEnum.MERCHANT_STATE_5.getCode()) {
            merchantDTO1.setState(StaticDataEnum.MERCHANT_STATE_6.getCode());
            merchantDTO1.setIsAvailable(StaticDataEnum.AVAILABLE_1.getCode());
        } else {
            merchantDTO1.setState(StaticDataEnum.MERCHANT_STATE_2.getCode());
        }
        updateMerchant(oneMerchant.getId(), merchantDTO1, request);

        UserDTO userDTO = userService.findUserById(userId);
        if (oneMerchant.getState().intValue() != StaticDataEnum.MERCHANT_STATE_5.getCode()){
            //查询模板
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_35.getCode() + "");
            //内容
            String sendMsg = mailTemplateDTO.getEnSendContent();
            //发站内信
            NoticeDTO noticeDTO= new NoticeDTO();
            noticeDTO.setContent(sendMsg);
            noticeDTO.setTitle(mailTemplateDTO.getEnMailTheme());
            noticeDTO.setUserId(userId);
            noticeService.saveNotice(noticeDTO,request);

            //记录邮件流水
            userService.saveMailLog(userDTO.getEmail(),sendMsg,0,request);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuseMerchant(@NonNull Long id, @NonNull MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        if (merchantDTO.getRemark().length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        merchantDTO.setState(StaticDataEnum.MERCHANT_STATE_.getCode());
        audit(id, merchantDTO, request);
        //发送拒绝邮件
        //获取登录信息
        MerchantDTO merchantDTO_ = findMerchantById(id);
        UserDTO userDTO = userService.findUserById(merchantDTO_.getUserId());

        // 查询是否是后台录入的商户
        Map<String ,Object > params = new HashMap<>();
        params.put("merchantId" ,  id);
        params.put("type" ,StaticDataEnum.MERCHANT_APPLICATION_TYPE_1.getCode());
        params.put("state" ,StaticDataEnum.APPROVE_STATE_1.getCode());
        MerchantApplicationDTO merchantApplicationDTO = merchantApplicationService.findOneMerchantApplication(params);
        if(merchantApplicationDTO != null && merchantApplicationDTO.getId() != null ){
            merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_.getCode());
            merchantApplicationService.updateMerchantApplication(merchantApplicationDTO.getId(),merchantApplicationDTO,request);
        }

        //查询模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_10.getCode()+"");
        //邮件内容
        String sendMsg = mailTemplateDTO.getEnSendContent();

        try{
            //发送邮件
//            Session session = MailUtil.getSession(sysEmail);
//            MimeMessage mimeMessage = MailUtil.getMimeMessage(sysEmail, userDTO.getEmail(), mailTemplateDTO.getEnMailTheme(), sendMsg , null, session);
//            MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);
            // 站内信
            NoticeDTO noticeDTO = new NoticeDTO();
            noticeDTO.setUserId(userDTO.getId());
            noticeDTO.setTitle(mailTemplateDTO.getEnMailTheme());
            noticeDTO.setContent(sendMsg);
            noticeService.saveNotice(noticeDTO, request);
            // 站内信
            FirebaseDTO firebaseDTO = new FirebaseDTO();
            firebaseDTO.setAppName("UWallet");
            firebaseDTO.setUserId(userDTO.getId());
            firebaseDTO.setTitle(sendMsg);
            firebaseDTO.setBody(sendMsg);
            firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
            try {
                serverService.pushFirebase(firebaseDTO,request);
            } catch (Exception e) {
                log.info("send message push failed");
            }
            //记录邮件流水
            userService.saveMailLog(userDTO.getEmail(),sendMsg,0,request);
        }catch (Exception e){
            log.info("refuseMerchant.refuseMerchant,发送邮件异常"+e.getMessage(),e);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)

    public void passMerchant(@NonNull MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) throws Exception {
        MerchantDTO merchantDTO = findMerchantById(merchantDetailDTO.getId());
        if (merchantDetailDTO.getState().intValue() == StaticDataEnum.MERCHANT_STATE_7.getCode()) {
            checkMerchantReviewParams(merchantDTO, request);
        }
        // 将审核状态改为审批中并增加审核记录
        merchantDTO.setIsAvailable(StaticDataEnum.AVAILABLE_1.getCode());
        merchantDTO.setState(merchantDetailDTO.getState());
        merchantDTO.setId(merchantDetailDTO.getId());
        merchantDTO.setMerchantApprovePassTime(System.currentTimeMillis());
        boolean ret = (merchantDetailDTO.getState().intValue() == StaticDataEnum.MERCHANT_STATE_5.getCode()
                || merchantDetailDTO.getState().intValue() == StaticDataEnum.MERCHANT_STATE_7.getCode());
        if (ret && merchantDTO.getExtraDiscountPeriod().longValue() == 0L) {
            // 计算额外折扣到期日期
            if (merchantDTO.getExtraDiscountPeriodChoice() != null) {

                Calendar cal = Calendar.getInstance();
                // 取得六个月后时间
                cal.add(Calendar.MONTH, merchantDTO.getExtraDiscountPeriodChoice());
                merchantDTO.setExtraDiscountPeriod(cal.getTimeInMillis());
            }
        }
        MerchantDTO merchantResult = null;
        try {
            merchantResult = audit(merchantDetailDTO.getId(), merchantDTO, request);
        } catch (Exception e) {
            log.info("pass merchant audit failed, data:{}, error message:{}, e:{}", merchantDTO, e.getMessage(), e);
            throw new BizException(I18nUtils.get("pass.merchant.failed", getLang(request)));
        }

        MerchantDTO  merchantDTO_ = findMerchantById(merchantDetailDTO.getId());
        UserDTO userDTO = null;
        // 查询是否是后台申请的商户
        Map<String ,Object> params = new HashMap<>();
        params.put("merchantId",merchantDTO_.getId());
        params.put("type",StaticDataEnum.MERCHANT_APPLICATION_TYPE_1.getCode());
        MerchantApplicationDTO merchantApplicationDTO = merchantApplicationService.findOneMerchantApplication(params);

        if(merchantApplicationDTO != null &&  merchantApplicationDTO.getId() != null) {
            merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_1.getCode());
            merchantApplicationService.updateMerchantApplication(merchantApplicationDTO.getId(),merchantApplicationDTO,request);
            // 创建商户用户再次审核不需要创建用户
            JSONObject param=new JSONObject();
            param.put("merchantId",merchantDTO_.getId());
            param.put("email",merchantApplicationDTO.getEmail());
            Long userId=null;
            UserDTO oneUser = userService.findOneUser(param);
            if (oneUser!=null&&oneUser.getId()!=null){
                userDTO=oneUser;
                userId=userDTO.getId();
            }else {
                userDTO = new UserDTO();
                userDTO.setEmail(merchantApplicationDTO.getEmail());
                userDTO.setUserType(StaticDataEnum.USER_TYPE_20.getCode());
                userDTO.setRole(StaticDataEnum.MERCHANT_ROLE_TYPE_1.getCode());
                userDTO.setMerchantId(merchantDTO_.getId());
                // 创建用户表userid
                userId = userService.userCreate(userDTO, request);
                // 需要更新merchantid
                merchantDTO_.setUserId(userId);
                this.updateMerchant(merchantDTO_.getId(),merchantDTO_,request);

                userDTO.setId(userId);
                // 调用账户系统，开基本账户
                JSONObject accountInfo = new JSONObject();
                accountInfo.put("userId", userId);
                accountInfo.put("phone", userDTO.getPhone());
                accountInfo.put("email", userDTO.getEmail());
                accountInfo.put("accountType", userDTO.getUserType());
                accountInfo.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getCode());
                //
                serverService.saveAccount(accountInfo);

                // 调用账户系统，开整体销售账户
                accountInfo.clear();
                accountInfo.put("userId", userId);
                accountInfo.put("type", 1);
                serverService.createSubAccount(accountInfo);
                // 创建商户权限
                userService.createMerchantUserAction(userDTO.getId(), request);
            }

        }else{
            userDTO = userService.findUserById(merchantDTO_.getUserId());
        }

        // 向分期付系统新增商户
        CreditMerchantDTO creditMerchantDTO = new CreditMerchantDTO();
        creditMerchantDTO.setMerchantId(merchantDTO.getId());
        creditMerchantDTO.setUserId(userDTO.getId());
        creditMerchantDTO.setMerchantName(merchantResult.getPracticalName());
        creditMerchantDTO.setBusinessType(merchantResult.getEntityType());
        creditMerchantDTO.setMainBusiness(merchantResult.getMainBusiness());
        creditMerchantDTO.setAbn(merchantResult.getAbn());
        creditMerchantDTO.setAccountNo(merchantResult.getAccountNo().toString());
        creditMerchantDTO.setPercentageToUser(merchantDTO.getBaseRate());
        creditMerchantDTO.setPercentageToPlatform(merchantDTO.getAppChargeRate());
        creditMerchantDTO.setEmail(merchantResult.getEmail());
        creditMerchantDTO.setAddress(merchantResult.getAddress());
        creditMerchantDTO.setCity(merchantResult.getCity());
        creditMerchantDTO.setRegion(merchantResult.getMerchantState());
        creditMerchantDTO.setPostcode(merchantResult.getPostcode());
        creditMerchantDTO.setCountry(merchantResult.getCounty());
        creditMerchantDTO.setAcn(merchantResult.getAcn());
        creditMerchantDTO.setExtraDiscount(merchantDTO.getExtraDiscount());
        creditMerchantDTO.setExtraDiscountPeriod(merchantDTO.getExtraDiscountPeriod());

        JSONObject merchantInfo = serverService.getMerchantByMerchantId(merchantDTO.getId());
        if (merchantInfo.getJSONObject("data") != null) {
            serverService.updateMerchant(merchantDTO.getId(), JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)), request);
        } else {
            serverService.saveMerchant(JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)));
        }

        Integer sendNode = null;
        long wholeSaleAction = 0L;
        if (merchantDTO.getState().intValue() == StaticDataEnum.MERCHANT_STATE_7.getCode()) {
            sendNode = StaticDataEnum.SEND_NODE_19.getCode();
            wholeSaleAction = 59L;
        } else if (merchantDTO.getState().intValue() == StaticDataEnum.MERCHANT_STATE_5.getCode()) {
            sendNode = StaticDataEnum.SEND_NODE_20.getCode();
            wholeSaleAction = 33L;
        }

        //发邮件
        //查询模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode.intValue() + "");
        //邮件内容
        String sendMsg = mailTemplateDTO.getEnSendContent();
        Map<String, Object> userSearchParams = new HashMap<>(4);
        userSearchParams.put("merchantId", merchantDTO.getId());
        List<UserDTO> userDTOList = userService.find(userSearchParams, null, null);
        if (!CollectionUtils.isEmpty(userDTOList)) {
            userSearchParams.clear();
            for (UserDTO userDTO1 : userDTOList) {
                userSearchParams.put("userId", userDTO1.getId());
                userSearchParams.put("actionId", wholeSaleAction);
                UserActionDTO userActionDTO = userActionService.findOneUserAction(userSearchParams);
                if (userActionDTO.getId() != null) {
                    try{
                        //发站内信
                        NoticeDTO noticeDTO= new NoticeDTO();
                        noticeDTO.setContent(sendMsg);
                        noticeDTO.setTitle(mailTemplateDTO.getEnMailTheme());
                        noticeDTO.setUserId(userDTO1.getId());
                        noticeService.saveNotice(noticeDTO,request);
                        // push
                        FirebaseDTO firebaseDTO = new FirebaseDTO();
                        firebaseDTO.setAppName("UWallet");
                        firebaseDTO.setUserId(userDTO1.getId());
                        firebaseDTO.setTitle(mailTemplateDTO.getEnMailTheme());
                        firebaseDTO.setBody(sendMsg);
                        firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
                        serverService.pushFirebase(firebaseDTO,request);
                        //记录邮件流水
                        userService.saveMailLog(userDTO.getEmail(),sendMsg,0,request);
                    }catch (Exception e){
                        log.info("MerchantServiceImpl.passMerchant,发送消息异常"+e.getMessage(),e);
                    }
                }
            }
        }
    }

    private void checkMerchantReviewParams(MerchantDTO merchantDTO, HttpServletRequest request) throws Exception {
        if (StringUtils.isEmpty(merchantDTO.getLogoUrl())) {
            throw new BizException(I18nUtils.get("addition.info.not.be.null", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getKeyword())) {
            throw new BizException(I18nUtils.get("addition.info.not.be.null", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getIntro())) {
            throw new BizException(I18nUtils.get("addition.info.not.be.null", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getCorporateName())) {
            throw new BizException(I18nUtils.get("docusign.businessName.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getPracticalName())) {
            throw new BizException(I18nUtils.get("docusign.tradingName.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getAddress())) {
            throw new BizException(I18nUtils.get("docusign.address.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getCity())) {
            throw new BizException(I18nUtils.get("docusign.suburb.isNull", getLang(request)));
        }
        if (merchantDTO.getMerchantState() == null) {
            throw new BizException(I18nUtils.get("docusign.state.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getPostcode())) {
            throw new BizException(I18nUtils.get("docusign.postcode.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getAbn())) {
            throw new BizException(I18nUtils.get("docusign.abn.isNull", getLang(request)));
        }
        if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode() && StringUtils.isEmpty(merchantDTO.getOtherEntity())) {
            throw new BizException(I18nUtils.get("docusign.otherEntity.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getDocusignSigner())) {
            throw new BizException(I18nUtils.get("docusign.signer.isNull", getLang(request)));
        }
        if (merchantDTO.getAuthorisedTitle() == null){
            throw new BizException(I18nUtils.get("docusign.signer.title",getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getBankName())) {
            throw new BizException(I18nUtils.get("docusign.bankName.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getAccountNo())) {
            throw new BizException(I18nUtils.get("docusign.accountNo.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getBsb())) {
            throw new BizException(I18nUtils.get("docusign.bsb.isNull", getLang(request)));
        }
        if (merchantDTO.getEstimatedAnnualSales() == null) {
            throw new BizException(I18nUtils.get("docusign.estimatedAnnualSales.isNull", getLang(request)));
        }
        if (merchantDTO.getAvgSalesValue() == null) {
            throw new BizException(I18nUtils.get("docusign.avgSalesValue.isNull", getLang(request)));
        }
        if (merchantDTO.getSalesValueByCard() == null) {
            throw new BizException(I18nUtils.get("docusign.salesValueByCard.isNull", getLang(request)));
        }
        Map<String, Object> params = new HashMap<>(16);
        params.put("merchantId", merchantDTO.getId());
        List<ContactPersonDTO> contactPersonDTOList = contactPersonService.find(params, null, null);
        if (contactPersonDTOList != null && contactPersonDTOList.isEmpty()) {
            throw new BizException(I18nUtils.get("docusign.contactPerson.isNull", getLang(request)));
        } else {
            contactPersonDTOList.stream().forEach(contactPersonDTO -> {
                if (StringUtils.isEmpty(contactPersonDTO.getName())) {
                    throw new RuntimeException(I18nUtils.get("docusign.contactPerson.name.isNull", getLang(request)));
                }
                if (StringUtils.isEmpty(contactPersonDTO.getMobile())) {
                    throw new RuntimeException(I18nUtils.get("docusign.contactPerson.phone.isNull", getLang(request)));
                }
                if (StringUtils.isEmpty(contactPersonDTO.getEmail())) {
                    throw new RuntimeException(I18nUtils.get("docusign.contactPerson.email.isNull", getLang(request)));
                }
                if (StringUtils.isEmpty(contactPersonDTO.getTitle())) {
                    throw new RuntimeException(I18nUtils.get("docusign.contactPerson.title.isNull", getLang(request)));
                }
            });
        }
        if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            List<DirectorDTO> directorDTOList = directorService.find(params, null, null);
            if (merchantDTO.getCompanyType().intValue() == StaticDataEnum.STATUS_0.getCode()) {
                if (directorDTOList == null || directorDTOList.isEmpty()) {
                    throw new BizException(I18nUtils.get("docusign.director.isNull", getLang(request)));
                }
            }
            if (shareholderDTOList == null || shareholderDTOList.isEmpty()) {
                throw new BizException(I18nUtils.get("docusign.owner.isNull", getLang(request)));
            }
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {
            List<PartnerDTO> partnerDTOList = partnerService.find(params, null, null);
            if (partnerDTOList == null || partnerDTOList.isEmpty()) {
                throw new BizException(I18nUtils.get("docusign.owner.isNull", getLang(request)));
            }
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {
            List<DirectorDTO> directorDTOList = directorService.find(params, null, null);
            List<TrusteeDTO> trusteeDTOList = trusteeService.find(params, null, null);
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            if (merchantDTO.getTrusteeType().intValue() == StaticDataEnum.STATUS_0.getCode()) {
                if (trusteeDTOList == null || trusteeDTOList.isEmpty()) {
                    throw new BizException(I18nUtils.get("docusign.director.isNull", getLang(request)));
                }
            } else {
                if (directorDTOList == null || directorDTOList.isEmpty()) {
                    throw new BizException(I18nUtils.get("docusign.director.isNull", getLang(request)));
                }
            }
            if (shareholderDTOList == null || shareholderDTOList.isEmpty()) {
                throw new BizException(I18nUtils.get("docusign.owner.isNull", getLang(request)));
            }
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            if (shareholderDTOList == null || shareholderDTOList.isEmpty()) {
                throw new BizException(I18nUtils.get("docusign.owner.isNull", getLang(request)));
            }
        }
    }

    @Override
    public void reviewUpdateRate(MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) throws BizException {
        BigDecimal bigDecimal = new BigDecimal("100");
        if (merchantDetailDTO.getExtraDiscount() != null && merchantDetailDTO.getAppChargeChoice() != null) {
            Boolean flagPay = checkRate(merchantDetailDTO.getExtraDiscount(), new BigDecimal(merchantDetailDTO.getAppChargeChoice()));
            if (!flagPay) {
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
        }
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setId(merchantDetailDTO.getId());
        merchantDTO.setExtraDiscountPeriodChoice(merchantDetailDTO.getExtraDiscountPeriodChoice());
        merchantDTO.setExtraDiscount(MathUtils.divide(merchantDetailDTO.getExtraDiscount(), bigDecimal, 4));
        merchantDTO.setAppChargeChoice(merchantDetailDTO.getAppChargeChoice());
        merchantDTO.setAppChargeRate(MathUtils.divide(new BigDecimal(merchantDetailDTO.getAppChargeChoice()), bigDecimal, 4));
        merchantDTO.setBaseRate(MathUtils.divide(merchantDetailDTO.getBaseRate(), bigDecimal, 4));
        updateMerchant(merchantDTO.getId(), merchantDTO, request);
    }

    @Override
    public MerchantDetailDTO route(@NonNull Long id) {
        MerchantDetailDTO merchantDetailDTO = new MerchantDetailDTO();
        merchantDetailDTO.setId(id);
        Map<String, Object> map = new HashMap<>(1);
        map.put("merchantId", id);
        List<RouteDTO> routeDTOList = routeService.find(map, null, null);
        if (routeDTOList != null) {
            for (RouteDTO routeDTO : routeDTOList) {
                routeDTO.setRate(Double.valueOf(MathUtils.multiply(Double.toString(routeDTO.getRate()), "100")));
            }
            merchantDetailDTO.setRouteDTOList(routeDTOList);
        }
        JSONObject data = serverService.getMerchantByMerchantId(id);
        MerchantDTO merchantDTO = findMerchantById(id);
        map.put("approveState", StaticDataEnum.WHOLE_SALE_STATE_1.getCode());
        WholeSalesFlowDTO wholeSalesFlowDTO = wholeSalesFlowService.findLatestWholeSaleFlowDTO(map);
        if (data != null) {
//            InstalmentResDTO res = JSONObject.parseObject(data.getJSONObject("data").toJSONString(), InstalmentResDTO.class);
            BigDecimal bigDecimal = new BigDecimal("100");
            //返回支付平台所得、支付用户让利
            merchantDetailDTO.setPayPercentageToUser(merchantDTO.getPaySellDiscount().multiply(bigDecimal));
            merchantDetailDTO.setPayPercentageToPlatform(merchantDTO.getPayRebateDiscount().multiply(bigDecimal));
            //额外折扣
            int appChargeChoice = MathUtils.multiply(merchantDTO.getAppChargeRate(), bigDecimal).intValue();
            merchantDetailDTO.setAppChargeChoice(new Integer(appChargeChoice).toString());
            merchantDetailDTO.setBaseRate(merchantDTO.getBaseRate().multiply(bigDecimal));
            merchantDetailDTO.setExtraDiscount(merchantDTO.getExtraDiscount().multiply(bigDecimal));
            boolean judge = merchantDTO.getExtraDiscountPeriod() != null && merchantDTO.getExtraDiscountPeriod().compareTo(0L) == 1;
            merchantDetailDTO.setExtraDiscountPeriod(judge ? merchantDTO.getExtraDiscountPeriod() : System.currentTimeMillis());
            merchantDetailDTO.setExtraDiscountPeriodChoice(merchantDTO.getExtraDiscountPeriodChoice());
            merchantDetailDTO.setMarketingDiscount(merchantDTO.getMarketingDiscount().multiply(bigDecimal));
            merchantDetailDTO.setWholeSaleUserDiscount(merchantDTO.getWholeSaleUserDiscount().multiply(bigDecimal).intValue());
            merchantDetailDTO.setWholeSaleMerchantDiscount(merchantDTO.getWholeSaleMerchantDiscount().multiply(bigDecimal).intValue());
            if (wholeSalesFlowDTO != null) {
                merchantDetailDTO.setCurrentWholeSaleMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount().multiply(bigDecimal));
            }
            merchantDetailDTO.setModifiedDate(merchantDTO.getModifiedDate());
            merchantDetailDTO.setMerchantApprovePassTime(merchantDTO.getMerchantApprovePassTime() != null ? merchantDTO.getMerchantApprovePassTime() : System.currentTimeMillis());
            merchantDetailDTO.setWholeSaleApproveState(merchantDTO.getWholeSaleApproveState());
            merchantDetailDTO.setWholeSaleUserPayDiscount(merchantDTO.getWholeSaleUserPayDiscount().multiply(bigDecimal));
            merchantDetailDTO.setAppChargeRate(merchantDTO.getAppChargeRate().multiply(bigDecimal));
            merchantDetailDTO.setAppChargePayRate(merchantDTO.getAppChargePayRate().multiply(bigDecimal));
            merchantDetailDTO.setBasePayRate(merchantDTO.getBasePayRate().multiply(bigDecimal));
        }
        return merchantDetailDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRate(@NonNull Long id, @NonNull MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) throws BizException {
        CreditMerchantDTO creditMerchantDTO = merchantDetailDTO.getCreditMerchantDTO();
        BigDecimal payPercentageToUser = merchantDetailDTO.getPayPercentageToUser();
        BigDecimal payPercentageToPlatform = merchantDetailDTO.getPayPercentageToPlatform();
        if (payPercentageToUser != null && payPercentageToPlatform != null) {
            Boolean flagPay = checkRate(merchantDetailDTO.getPayPercentageToUser(), payPercentageToPlatform);
            if (!flagPay)  {
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
        }

        List<RouteDTO> routeDTOList = merchantDetailDTO.getRouteDTOList();
        if (routeDTOList != null && routeDTOList.size() > 0) {
            routeService.deleteRouteByMerchantId(id);
            saveRouteList(merchantDetailDTO, request);
        }

        BigDecimal bigDecimal = new BigDecimal("100");
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setWholeSaleUserPayDiscount(MathUtils.divide(merchantDetailDTO.getWholeSaleUserPayDiscount(), bigDecimal, 4));
        merchantDTO.setMarketingDiscount(MathUtils.divide(merchantDetailDTO.getMarketingDiscount(), bigDecimal, 4));
        merchantDTO.setBaseRate(MathUtils.divide(merchantDetailDTO.getBaseRate(), bigDecimal, 4));
        merchantDTO.setBasePayRate(MathUtils.divide(merchantDetailDTO.getBasePayRate(), bigDecimal, 4));
        merchantDTO.setExtraDiscountPeriodChoice(merchantDetailDTO.getExtraDiscountPeriodChoice());
        merchantDTO.setExtraDiscount(MathUtils.divide(merchantDetailDTO.getExtraDiscount(), bigDecimal, 4));
        merchantDTO.setAppChargeChoice(merchantDetailDTO.getAppChargeChoice());
        merchantDTO.setAppChargeRate(MathUtils.divide(merchantDetailDTO.getAppChargeRate(), bigDecimal, 4));
        merchantDTO.setAppChargePayRate(MathUtils.divide(merchantDetailDTO.getAppChargePayRate(), bigDecimal, 4));
        // merchantDTO.setAppChargeRate(MathUtils.divide(new BigDecimal(merchantDetailDTO.getAppChargeChoice()), bigDecimal, 4));
//        if (merchantDetailDTO.getWholeSaleMerchantDiscount() != null) {
//            merchantDTO.setWholeSaleMerchantDiscount(MathUtils.divide(new BigDecimal(merchantDetailDTO.getWholeSaleMerchantDiscount().toString()), bigDecimal, 4));
//        }
        if (merchantDetailDTO.getWholeSaleUserDiscount() != null) {
            merchantDTO.setWholeSaleUserDiscount(MathUtils.divide(new BigDecimal(merchantDetailDTO.getWholeSaleUserDiscount().toString()), bigDecimal, 4));
            if (creditMerchantDTO != null) {
                creditMerchantDTO.setDiscountPackage(merchantDTO.getWholeSaleUserDiscount());
            }
        }
        // 计算额外折扣到期日期
        if (merchantDetailDTO.getExtraDiscountPeriodChoice() != null) {
            merchantDTO.setMerchantApprovePassTime(System.currentTimeMillis());
            Calendar cal = Calendar.getInstance();
            // 取得六个月后时间
            cal.add(Calendar.MONTH, merchantDetailDTO.getExtraDiscountPeriodChoice());
            merchantDTO.setExtraDiscountPeriod(cal.getTimeInMillis());
        }

        if (creditMerchantDTO != null) {
            creditMerchantDTO.setPercentageToUser(MathUtils.divide(merchantDetailDTO.getBaseRate(), bigDecimal, 4));
            creditMerchantDTO.setPercentageToPlatform(merchantDTO.getAppChargeRate());
            creditMerchantDTO.setMerchantId(id);
            creditMerchantDTO.setExtraDiscount(merchantDTO.getExtraDiscount());
            creditMerchantDTO.setExtraDiscountPeriod(merchantDTO.getExtraDiscountPeriod());
            serverService.updateMerchant(id, JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)), request);
        }
        // 修改支付平台所得、支付用户让利
        if (payPercentageToUser != null) {
            BigDecimal paySellDiscount = MathUtils.divide(payPercentageToUser, bigDecimal, 4);
            merchantDTO.setPaySellDiscount(paySellDiscount);
        }
        if (payPercentageToPlatform != null) {
            BigDecimal payRebateDiscount = MathUtils.divide(payPercentageToPlatform, bigDecimal, 4);
            merchantDTO.setPayRebateDiscount(payRebateDiscount);
        }

        // 修改审核中整体出售订单费率
        MerchantDTO wholeSaleStateMerchantDTO = findMerchantById(id);
        if (wholeSaleStateMerchantDTO.getWholeSaleApproveState() != null  && wholeSaleStateMerchantDTO.getWholeSaleApproveState() == StaticDataEnum.WHOLE_SALE_STATE_1.getCode()) {
            Map<String, Object> params = new HashMap<>(4);
            params.put("merchantId", id);
            params.put("approveState", StaticDataEnum.WHOLE_SALE_STATE_0.getCode());
            WholeSalesFlowDTO wholeSalesFlowDTO = wholeSalesFlowService.findLatestWholeSaleFlowDTO(params);
            if (wholeSalesFlowDTO != null) {
                wholeSalesFlowDTO.setCustomerDiscount(merchantDTO.getWholeSaleUserDiscount());
                wholeSalesFlowService.updateWholeSalesFlow(wholeSalesFlowDTO.getId(), wholeSalesFlowDTO, request);
            }
        }

        // 更新商户信息
        merchantDTO.setId(id);
        updateMethod(id, merchantDTO, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitChange(@NonNull MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        if (merchantDTO.getId() == null) {
            throw new BizException(I18nUtils.get("merchant.id.empty", getLang(request)));
        }
        MerchantDTO merchant = findMerchantById(merchantDTO.getId());
        if (merchant.getIsAvailable() == StaticDataEnum.MERCHANT_AVAILABLE_0.getCode()) {
            throw new BizException(I18nUtils.get("merchant.unavailable", getLang(request)));
        }
//        merchant.setState(StaticDataEnum.MERCHANT_STATE_3.getCode());
        merchant.setAccountApplyState(StaticDataEnum.ACCOUNT_APPLY_STATE_3.getCode());
        updateMerchant(merchant.getId(), merchant, request);

        // 增加审核记录
        merchant.setAccountNo(merchantDTO.getAccountNo());
        merchant.setBankName(merchantDTO.getBankName());
        merchant.setAccountName(merchantDTO.getAccountName());
        merchant.setBsb(merchantDTO.getBsb());

        // 查询记录董事信息
//        Map<String, Object> map = new HashMap<>(1);
//        map.put("merchantId", merchantDTO.getId());
//        List<DirectorDTO> directorDTOList = directorService.find(map, null, null);
//        merchant.setDirectorDTOList(directorDTOList);

        ApproveLogDTO approveLogDTO = new ApproveLogDTO();
        approveLogDTO.setMerchantId(merchantDTO.getId());
        approveLogDTO.setApproveType(StaticDataEnum.APPROVE_LOG_APPROVE_TYPE_1.getCode());
        approveLogDTO.setState(StaticDataEnum.APPROVE_STATE_2.getCode());
        approveLogDTO.setData(JSONObject.toJSONString(merchant));
        approveLogService.saveApproveLog(approveLogDTO, request);
    }

    @Override
    public MerchantDetailDTO selectMerchantChange(Map<String, Object> params) {
        MerchantDetailDTO merchantDetailDTO = selectMerchantApproveById(params);
        if (merchantDetailDTO.getId() != null) {
            Map<String, Object> map = new HashMap<>(3);
            map.put("merchantId", merchantDetailDTO.getId());
            map.put("state", StaticDataEnum.MERCHANT_STATE_2.getCode());
            map.put("approveType", StaticDataEnum.APPROVE_LOG_APPROVE_TYPE_1.getCode());
            ApproveLogDTO approveLogDTO = approveLogService.findOneApproveLog(map);
            String data = approveLogDTO.getData();
            JSONObject jsonObject = JSONObject.parseObject(data);
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.toJSONString(), MerchantDTO.class);
            merchantDetailDTO.setMerchantDTO(merchantDTO);
        }
        return merchantDetailDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditChange(@NonNull MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        Integer state = merchantDTO.getState();
        if (null == state) {
            throw new BizException(I18nUtils.get("merchant.state.isNull", getLang(request)));
        }
        MerchantDTO merchantDTOResult = findMerchantById(merchantDTO.getId());
        Map<String, Object> map = new HashMap<>(3);
        map.put("merchantId", merchantDTOResult.getId());
        map.put("approveType", StaticDataEnum.APPROVE_LOG_APPROVE_TYPE_1.getCode());
        map.put("state", StaticDataEnum.MERCHANT_STATE_2.getCode());
        ApproveLogDTO approveLogDTO = approveLogService.findOneApproveLog(map);

        String sendMsg = "";
        String mailTheme ="";
        if (StaticDataEnum.APPROVE_STATE_1.getCode() == state) {
            merchantDTOResult.setAccountApplyState(StaticDataEnum.ACCOUNT_APPLY_STATE_1.getCode());
            merchantDTOResult.setBsb(merchantDTO.getBsb());
            merchantDTOResult.setAccountNo(merchantDTO.getAccountNo());
            merchantDTOResult.setBankName(merchantDTO.getBankName());
            merchantDTOResult.setAccountName(merchantDTO.getAccountName());
            updateMerchant(merchantDTOResult.getId(), merchantDTOResult, request);

            approveLogDTO.setState(StaticDataEnum.APPROVE_STATE_1.getCode());
            approveLogService.updateApproveLog(approveLogDTO.getId(), approveLogDTO, request);
            //查询模板
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_11.getCode()+"");
            sendMsg = mailTemplateDTO.getEnSendContent();
            mailTheme = mailTemplateDTO.getEnMailTheme();
        } else if (StaticDataEnum.APPROVE_STATE_.getCode() == state) {
            if (merchantDTO.getRemark().length() > Validator.TEXT_LENGTH_100) {
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
            merchantDTOResult.setAccountApplyState(StaticDataEnum.ACCOUNT_APPLY_STATE_4.getCode());
            merchantDTOResult.setRemark(merchantDTO.getRemark());
            updateMerchant(merchantDTOResult.getId(), merchantDTOResult, request);

            approveLogDTO.setState(StaticDataEnum.APPROVE_STATE_.getCode());
            approveLogDTO.setRemark(merchantDTO.getRemark());
            approveLogService.updateApproveLog(approveLogDTO.getId(), approveLogDTO, request);
            //查询模板
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_12.getCode()+"");
            sendMsg = mailTemplateDTO.getEnSendContent();
            mailTheme = mailTemplateDTO.getEnMailTheme();
        }

        try{
            Map<String, Object> userSearchParams = new HashMap<>(4);
            userSearchParams.put("merchantId", merchantDTO.getId());
            List<UserDTO> userDTOList = userService.find(userSearchParams, null, null);
            if (!CollectionUtils.isEmpty(userDTOList)) {
                userSearchParams.clear();
                for (UserDTO userDTO1 : userDTOList) {
                    long wholeSaleAction = 33L;
                    userSearchParams.put("userId", userDTO1.getId());
                    userSearchParams.put("actionId", wholeSaleAction);
                    UserActionDTO userActionDTO = userActionService.findOneUserAction(userSearchParams);
                    if (userActionDTO.getId() != null) {
                        //发站内信
                        NoticeDTO noticeDTO= new NoticeDTO();
                        noticeDTO.setContent(sendMsg);
                        noticeDTO.setTitle(mailTheme);
                        noticeDTO.setUserId(userDTO1.getId());
                        noticeService.saveNotice(noticeDTO,request);
                        //记录邮件流水
                        userService.saveMailLog(userDTO1.getEmail(),sendMsg,0,request);
                    }
                }
            }
        }catch (Exception e){
            log.info("MerchantServiceImpl.auditChange,发送邮件异常"+e.getMessage(),e);
        }
    }

    @Override
    public MerchantDTO getMerchantByUserId(@NonNull Long userId) {
        Map<String, Object> map = new HashMap<>(2);
//        map.put("userId", userId);
//        MerchantDTO merchantDTO = findOneMerchant(map);
        MerchantDTO merchantDTO = findMerchantById(userId);
        if (merchantDTO.getId() != null) {
            map.put("merchantId", merchantDTO.getId());
            List<ContactPersonDTO> contactPersonDTOList = contactPersonService.find(map, null, null);
            merchantDTO.setContactPersonDTOList(contactPersonDTOList);
            if (merchantDTO.getEntityType() != null) {
                if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {
                    List<DirectorDTO> directorDTOList = directorService.find(map, null, null);
                    List<ShareholderDTO> shareholderDTOList = shareholderService.find(map, null, null);
                    if (directorDTOList != null && !directorDTOList.isEmpty()) {
                        directorDTOList.forEach(directorDTO -> {
                            directorDTO.setCreatedDate(null);
                            directorDTO.setModifiedDate(null);
                        });
                    }
                    if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                        shareholderDTOList.forEach(shareholderDTO -> {
                            shareholderDTO.setCreatedDate(null);
                            shareholderDTO.setModifiedDate(null);
                        });
                    }
                    merchantDTO.setDirectorDTOList(directorDTOList);
                    merchantDTO.setShareholderDTOList(shareholderDTOList);
                } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {
                    List<ShareholderDTO> shareholderDTOList = shareholderService.find(map, null, null);
                    if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                        shareholderDTOList.forEach(shareholderDTO -> {
                            shareholderDTO.setCreatedDate(null);
                            shareholderDTO.setModifiedDate(null);
                        });
                    }
                    merchantDTO.setShareholderDTOList(shareholderDTOList);
                } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {
                    List<PartnerDTO> partnerDTOList = partnerService.find(map, null, null);
                    if (partnerDTOList != null && !partnerDTOList.isEmpty()) {
                        partnerDTOList.forEach(partnerDTO -> {
                            partnerDTO.setCreatedDate(null);
                            partnerDTO.setModifiedDate(null);
                        });
                    }
                    merchantDTO.setPartnerDTOList(partnerDTOList);
                } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {
                    List<DirectorDTO> directorDTOList = directorService.find(map, null, null);
                    List<ShareholderDTO> shareholderDTOList = shareholderService.find(map, null, null);
                    List<TrusteeDTO> trusteeDTOList = trusteeService.find(map, null, null);
                    if (directorDTOList != null && !directorDTOList.isEmpty()) {
                        directorDTOList.forEach(directorDTO -> {
                            directorDTO.setCreatedDate(null);
                            directorDTO.setModifiedDate(null);
                        });
                    }
                    if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                        shareholderDTOList.forEach(shareholderDTO -> {
                            shareholderDTO.setCreatedDate(null);
                            shareholderDTO.setModifiedDate(null);
                        });
                    }
                    if (trusteeDTOList != null && !trusteeDTOList.isEmpty()) {
                        trusteeDTOList.forEach(trusteeDTO -> {
                            trusteeDTO.setCreatedDate(null);
                            trusteeDTO.setModifiedDate(null);
                        });
                    }
                    merchantDTO.setDirectorDTOList(directorDTOList);
                    merchantDTO.setShareholderDTOList(shareholderDTOList);
                    merchantDTO.setTrusteeDTOList(trusteeDTOList);
                }
            }
            List<RouteDTO> routeDTOList = routeService.findList(map, null, null);
            merchantDTO.setRouteDTOList(routeDTOList);
            // 商户信息二十个文件
            if (!StringUtils.isEmpty(merchantDTO.getFileList())) {
                List<String> urls = JSONArray.parseArray(merchantDTO.getFileList(), String.class);
                if (urls != null && urls.size() > 0) {
                    List<JSONObject> urlFile = new ArrayList<>(urls.size());
                    urls.stream().forEach(s -> {
                        JSONObject file = new JSONObject();
                        String a = SnowflakeUtil.getEncryptionId();
                        String[] url = s.split("/");
                        file.put("uid", a);
                        file.put("name", url[url.length - 1]);
                        file.put("url", s);
                        file.put("status", "done");
                        urlFile.add(file);
                    });
                    merchantDTO.setFiles(urlFile);
                }
            }
            // 信托文件
            if (!StringUtils.isEmpty(merchantDTO.getTrusteeFileList())) {
                List<String> trusteeUrls = JSONArray.parseArray(merchantDTO.getTrusteeFileList(), String.class);
                if (trusteeUrls != null && trusteeUrls.size() > 0) {
                    List<JSONObject> trusteeUrlFile = new ArrayList<>(trusteeUrls.size());
                    trusteeUrls.stream().forEach(s -> {
                        JSONObject file = new JSONObject();
                        String a = SnowflakeUtil.getEncryptionId();
                        String[] url = s.split("/");
                        file.put("uid", a);
                        file.put("name", url[url.length - 1]);
                        file.put("url", s);
                        file.put("status", "done");
                        trusteeUrlFile.add(file);
                    });
                    merchantDTO.setTrusteeFiles(trusteeUrlFile);
                } else {
                    merchantDTO.setTrusteeFiles(new ArrayList<>());
                }
            }
            if (merchantDTO.getAppChargeRate() != null) {
                int appChargeChoice = MathUtils.multiply(merchantDTO.getAppChargeRate(), new BigDecimal("100")).intValue();
                merchantDTO.setAppChargeChoice(new Integer(appChargeChoice).toString());
            }
            if (merchantDTO.getAppChargePayRate() != null) {
                int appChargeChoice = MathUtils.multiply(merchantDTO.getAppChargePayRate(), new BigDecimal("100")).intValue();
                merchantDTO.setAppChargePayChoice(new Integer(appChargeChoice).toString());
            }
            merchantDTO.setBaseRate(merchantDTO.getBaseRate().multiply(new BigDecimal("100")));
            merchantDTO.setExtraDiscount(merchantDTO.getExtraDiscount().multiply(new BigDecimal("100")));
            QrcodeInfoDTO qrcodeInfoDTO = qrcodeInfoService.findOneQrcodeInfo(map);
            merchantDTO.setQrCode(qrcodeInfoDTO.getCode());

            map.clear();
            map.put("merchantId", merchantDTO.getId());
            WholeSalesFlowDTO wholeSalesFlowDTO = wholeSalesFlowService.findLatestWholeSaleFlowDTO(map);
            if (wholeSalesFlowDTO != null) {
                merchantDTO.setWholeSaleMerchantDiscount(wholeSalesFlowDTO.getMerchantDiscount().multiply(new BigDecimal("100")));
                merchantDTO.setWholeSaleUserDiscount(merchantDTO.getWholeSaleUserDiscount().multiply(new BigDecimal("100")));
            }
        }
        return merchantDTO;
    }

    @Override
    public void isTop(Long id, MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        Map<String, Object> map = new HashMap<>(1);
        map.put("isTop", StaticDataEnum.MERCHANT_IS_TOP_1);
        List<MerchantDTO> merchantDTOList = find(map, null, null);
        if (merchantDTOList != null && merchantDTOList.size() > Constant.MERCHANT_TOP) {
            throw new BizException(I18nUtils.get("", getLang(request)));
        }
        MerchantDTO newMerchantDTO = new MerchantDTO();
        newMerchantDTO.setId(id);
        newMerchantDTO.setIsTop(merchantDTO.getIsTop());
        updateMerchant(id, newMerchantDTO, request);
    }

    @Override
    public MerchantDTO selectMerchantById(@NonNull Long id) throws Exception {
        MerchantDTO merchantDTO = findMerchantById(id);
        Map<String,Object> map  = new HashMap();
        map.put("code","mainBusiness");
        map.put("value",merchantDTO.getMainBusiness());
        StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(map);
        merchantDTO.setMainBusinessEnName(staticDataDTO.getEnName());
        map.clear();
        map.put("code","merchantState");
        map.put("value",merchantDTO.getMerchantState());
        staticDataDTO = staticDataService.findOneStaticData(map);
        merchantDTO.setStateName(staticDataDTO.getEnName());
        map.clear();
        map.put("code","city");
        map.put("value",merchantDTO.getCity());
        staticDataDTO = staticDataService.findOneStaticData(map);
        merchantDTO.setCityName(staticDataDTO.getEnName());
        ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
        BigDecimal discountRate = parametersConfigDTO.getDiscountRate();
        if (discountRate != null) {
            BigDecimal bigDecimal = discountRate;
            BigDecimal sellDiscount = merchantDTO.getSellDiscount();
            BigDecimal marketingDiscount = merchantDTO.getMarketingDiscount();
            if (sellDiscount != null && marketingDiscount != null) {
                Long today = System.currentTimeMillis();
                Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
                BigDecimal extraDiscount = null;
                if (today.longValue() < extraDiscountPeriod.longValue()) {
                    extraDiscount = merchantDTO.getExtraDiscount();
                } else {
                    extraDiscount = new BigDecimal("0.00");
                }
//                BigDecimal payDiscountRate = merchantDTO.getPaySellDiscount()
//                        .add(merchantDTO.getMarketingDiscount()
//                                .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getMerchantDiscountRatePlatformProportion())))
//                        .add(extraDiscount
//                                .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountPayPlatform()))).setScale(4, RoundingMode.HALF_UP);
//                payDiscountRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
//                merchantDTO.setUserDiscount(payDiscountRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
                // 分期付折扣
//                BigDecimal creditDiscountRate = merchantDTO.getSellDiscount()
//                        .add(merchantDTO.getMarketingDiscount()
//                                .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getDiscountRate())))
//                        .add(extraDiscount
//                                .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountCreditPlatform()))).setScale(4, RoundingMode.HALF_UP);
                BigDecimal balance = userService.getBalance(merchantDTO.getUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
                if (balance != null && (balance.compareTo(new BigDecimal("0")) != 0)) {
                    merchantDTO.setDiscoverDiscount(merchantDTO.getWholeSaleUserDiscount().multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
                } else {
                    BigDecimal creditDiscountRate = merchantDTO.getBaseRate().add(extraDiscount).add(merchantDTO.getMarketingDiscount());
                    merchantDTO.setDiscoverDiscount(creditDiscountRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
                }
//                switch (payDiscountRate.compareTo(creditDiscountRate)) {
//                    case 1:
//                        merchantDTO.setDiscoverDiscount(payDiscountRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
//                        break;
//                    case -1:
//                        merchantDTO.setDiscoverDiscount(creditDiscountRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
//                        break;
//                    default:
//                        merchantDTO.setDiscoverDiscount(payDiscountRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
//                        break;
//                }
            }
        }
        //获取二维码
        map.clear();
        map.put("merchantId", merchantDTO.getId());
        QrcodeInfoDTO qrcodeInfoDTO = qrcodeInfoService.findOneQrcodeInfo(map);
        merchantDTO.setQrCode(qrcodeInfoDTO.getCode());
        return merchantDTO;
    }

    @Override
    public List<ContactsFileDTO> selectMerchantListByIdList(List<Long> merchantIdList) {
        return merchantDAO.selectMerchantListByIdList(merchantIdList);
    }

    /**
     * 商户审核并增加审核记录
     * @param id
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    private MerchantDTO audit(Long id, MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        MerchantDTO merchantDTOResult = findMerchantById(id);
        // 补充信息也要记录到申请表中
        merchantDTOResult  = this.getOtherMerchantMessage(merchantDTOResult);
        updateMerchant(id, merchantDTO, request);
//        // 此节点商户信息
//        MerchantDTO merchantDTO1 = findMerchantById(id);
        // 增加审核记录
        ApproveLogDTO approveLogDTO = new ApproveLogDTO();
        approveLogDTO.setMerchantId(id);
        approveLogDTO.setApproveType(StaticDataEnum.APPROVE_LOG_APPROVE_TYPE_0.getCode());
        approveLogDTO.setData(JSONObject.toJSONString(merchantDTOResult));
        approveLogDTO.setState(merchantDTO.getState());
        approveLogDTO.setApprovedBy(getUserId(request));
        approveLogDTO.setRemark(merchantDTO.getRemark());
        approveLogService.saveApproveLog(approveLogDTO, request);
        return merchantDTOResult;
    }

    /**
     * 增加商户渠道中间表
     * @param merchantDetailDTO
     * @param request
     * @throws BizException
     */
    private void saveRouteList(MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) throws BizException {
        List<RouteDTO> routeDTOList = merchantDetailDTO.getRouteDTOList();
        List<Route> list = new ArrayList<>();
        Long currentLoginId = getUserId(request);
        long now = System.currentTimeMillis();
        for (RouteDTO routeDTO : routeDTOList) {
            Route route = new Route();
            route.setId(SnowflakeUtil.generateId());
            route.setCreatedBy(currentLoginId);
            route.setCreatedDate(now);
            route.setModifiedBy(currentLoginId);
            route.setModifiedDate(now);
            route.setIp(getIp(request));
            route.setStatus(StaticDataEnum.STATUS_1.getCode());
            route.setMerchantId(merchantDetailDTO.getId());
            route.setGatewayType(routeDTO.getGatewayType());
            route.setRateType(routeDTO.getRateType());

            if(routeDTO.getRate()==null){
                route.setRate(new Double(0.00));
            } else {
                String s = Double.toString(routeDTO.getRate());
                int i = s.indexOf(".");
                String substring = s.substring(i + 1);
                if (substring.length() > 2) {
                    throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
                }
                String rate = MathUtils.divide(s, "100", 4);
                route.setRate(Double.valueOf(rate));
            }
            list.add(route);
        }
        if (list != null && !list.isEmpty()) {
            routeService.saveRouteList(list, request);
        }
    }

    private MerchantDTO getMerchant(Long userId, HttpServletRequest request) throws BizException {
        if (userId == null) {
            throw new BizException(I18nUtils.get("saving.exception", getLang(request)));
        }
        Map<String, Object> map = new HashMap<>(1);
        map.put("userId", userId);
        return findOneMerchant(map);
    }

    /**
     * 校验小数不能大于2位
     * @param a
     * @param b
     * @return
     */
    private Boolean checkRate(@NonNull BigDecimal a, @NonNull BigDecimal b) {
        BigDecimal zero = new BigDecimal("100");
        BigDecimal add = a.add(b);
        if (add.compareTo(zero) == 0 || add.compareTo(zero) == 1) {
            return false;
        }
        String s = a.toString();
        String s1 = b.toString();
        int i = s.indexOf(".");
        int i1 = s1.indexOf(".");
        String substring = s.substring(i + 1);
        String substring1 = s1.substring(i1 + 1);
        if (substring.length() > 2 || substring1.length() > 2) {
           return false;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(Long id, MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
//        checkMerchant(merchantDTO, request);
        //更新商户信息前先查询出原始数据，判断商户类型是否改变，若改变则清空原商户所有信息
        MerchantDTO oldDTO = findMerchantById(id);
//        if (merchantDTO.getEntityType().intValue() != oldDTO.getEntityType().intValue()) {
//            //重置商户数据
//            try {
//                merchantDAO.reSetMerchantInfo(oldDTO);
//            } catch (Exception e) {
//                log.info("reset merchant data failed, data:{}, error message:{}, e:{}", merchantDTO, e.getMessage(), e);
//                throw new BizException("reset merchant Error!");
//            }
//        }
        // 根据地址获取更新经纬度
        Map<String, Object> params = new HashMap<>(1);
        params.put("code", "merchantState");
        params.put("value", merchantDTO.getMerchantState());
        StaticDataDTO merchantState = staticDataService.findOneStaticData(params);
        params.clear();
        params.put("code", "city");
        params.put("value", merchantDTO.getCity());
        StaticDataDTO city = staticDataService.findOneStaticData(params);
        String citySuburb = "";
        String merchantDTOSuburb = merchantDTO.getSuburb();
        if (!StringUtils.isEmpty(merchantDTOSuburb)) {
            citySuburb = merchantDTOSuburb.replaceAll(" ", "+") + "+" + city.getEnName();
        } else {
            citySuburb = city.getEnName();
            citySuburb = StringUtils.isNotBlank(citySuburb) ? citySuburb.replace(" ","-") : citySuburb;
        }
        String addressParam = merchantDTO.getAddress().replaceAll(" ", "+") + ",+" + citySuburb + ",+" + merchantState.getEnName();
        try {
            String mapsApi = googleMapsApi + addressParam + "&key=" + googleApiKey;
            JSONObject location = JSONObject.parseObject(HttpClientUtils.sendGet(mapsApi));
            location = location.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
            merchantDTO.setLat(location.getString("lat"));
            merchantDTO.setLng(location.getString("lng"));
        } catch (Exception e) {
            log.error("updateDetail: 通过Google Map API 获取地址经纬度失败,addressParam:{},e:{}",addressParam,e.getMessage());
            throw new BizException(I18nUtils.get("location.fail", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getLat())) {
            merchantDTO.setLat("-33.870376");
        }
        if (StringUtils.isEmpty(merchantDTO.getLng())) {
            merchantDTO.setLng("151.210303");
        }
        //更新商户数据
        updateMerchant(id, merchantDTO, request);

        CreditMerchantDTO creditMerchantDTO = new CreditMerchantDTO();
        // 向分期付系统新增商户
        creditMerchantDTO.setMerchantId(id);
        creditMerchantDTO.setUserId(oldDTO.getUserId());
        creditMerchantDTO.setMerchantName(merchantDTO.getPracticalName());
        creditMerchantDTO.setBusinessType(merchantDTO.getEntityType());
        creditMerchantDTO.setMainBusiness(merchantDTO.getMainBusiness());
        creditMerchantDTO.setAbn(merchantDTO.getAbn());
        creditMerchantDTO.setAccountNo(merchantDTO.getAccountNo());
        creditMerchantDTO.setEmail(merchantDTO.getEmail());
        creditMerchantDTO.setAddress(merchantDTO.getAddress());
        creditMerchantDTO.setCity(merchantDTO.getCity());
        creditMerchantDTO.setRegion(merchantDTO.getMerchantState());
        creditMerchantDTO.setPostcode(merchantDTO.getPostcode());
        creditMerchantDTO.setCountry(merchantDTO.getCounty());
        creditMerchantDTO.setAcn(merchantDTO.getAcn());
        JSONObject merchantInfo = serverService.getMerchantByMerchantId(id);
        if (merchantInfo.getJSONObject("data") != null) {
            serverService.updateMerchant(id, JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)), request);
        } else {
            serverService.saveMerchant(JSONObject.parseObject(JSON.toJSONString(creditMerchantDTO)));
        }
        //记录操作日志
        try{
            MerchantUpdateLogDTO merchantUpdateLogDTO = new MerchantUpdateLogDTO();
            merchantUpdateLogDTO.setAfterUpdate(JSON.toJSONString(merchantDTO));
            merchantUpdateLogDTO.setOnUpdate(JSON.toJSONString(oldDTO));
            merchantUpdateLogDTO.setMerchantId(id);
            merchantUpdateLogService.saveMerchantUpdateLog(merchantUpdateLogDTO,request);
        }catch (Exception e){
            log.error("记录商户修改日志异常,data:{}",e);
        }

    }

    @Override
    public List<MerchantDTO> findMerchantLogInList(Map<String, Object> params) {
        return merchantDAO.findMerchantLogInList(params);
    }

    @Override
    public JSONObject getMerchantLoginList(JSONObject data, HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>( 2);
        //获取当前登录用户
//        UserDTO userDTO =userService.findUserById(data.getLong("userId"));
        params.put("email", data.getString("email").toLowerCase());
        //查询商户列表
        List<MerchantDTO> list = findMerchantLogInList(params);
        JSONObject msg = new JSONObject();
        msg.put("email", data.getString("email"));
        msg.put("merList",list);
        return msg;
    }

    @Override
    public HSSFWorkbook exportMerchantExcel(@NonNull Long id, HttpServletRequest request) {
        String[] companyInfoHeaders = {"Business Registered Name", "Trading Name", "Company address", "ABN", "Starting time of transaction", "Business Phone", "Business Email",
                "Company Website", "Type of Entity", "Company public listed on ASX or not?", "Do you have Trust Deed?", "The trust control by the Corporate Trustee?", "Business Type",
                "Estimated Monthly Sales", "Average amount of transactions", "Single transaction amount"};

        String[] contactPersonInfoHeaders = {"Name", "Position", "Contact Number", "Contact Email"};

        String[] bankInfoHeaders = {"BSB", "Bank Account Number", "Bank Name", "Account Name"};

        // 查询导出商户的详细信息
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        MerchantDetailDTO merchantDetailDTO = selectMerchantApproveById(params);

        List<ContactPersonDTO> contactPersonDTOList = contactPersonService.find(params, null, null);

        // 数据字典值处理
        Map<String, Map<String, Object>> enNameMap = getEnName(merchantDetailDTO);
        String city = "";
        String merchantState = "";
        if (merchantDetailDTO.getCity() != null) {
            city = enNameMap.get("city").get(merchantDetailDTO.getCity()).toString();
        }
        if (merchantDetailDTO.getMerchantState() != null) {
            merchantState = enNameMap.get("merchantState").get(merchantDetailDTO.getMerchantState()).toString();
        }

        Map<String, Object> companyInfo = new HashMap<>(16);
        companyInfo.put("Business Registered Name", merchantDetailDTO.getPracticalName());
        companyInfo.put("Trading Name", merchantDetailDTO.getCorporateName());
        companyInfo.put("Company address", merchantDetailDTO.getAddress() + "," + merchantDetailDTO.getSuburb() + "," + city + "," + merchantState + "," + merchantDetailDTO.getPostcode());
        companyInfo.put("ABN", merchantDetailDTO.getAbn());
        if (merchantDetailDTO.getOperationTime() != null) {
            companyInfo.put("Starting time of transaction", enNameMap.get("operationTime").get(merchantDetailDTO.getOperationTime().toString()));
        }
        companyInfo.put("Business Phone", merchantDetailDTO.getBusinessPhone());
        companyInfo.put("Business Email", merchantDetailDTO.getEmail());
        companyInfo.put("Company Website", merchantDetailDTO.getWebsites());
        companyInfo.put("Type of Entity", enNameMap.get("entityType").get(merchantDetailDTO.getEntityType().toString()));
        if (merchantDetailDTO.getTrusteeDeed() != null) {
            companyInfo.put("Do you have Trust Deed?", merchantDetailDTO.getTrusteeDeed().intValue() == 1 ? "yes" : "no");
        }
        if (merchantDetailDTO.getTrusteeCompanyType() != null) {
            companyInfo.put("The trust control by the Corporate Trustee?", merchantDetailDTO.getTrusteeCompanyType().intValue() == 1 ? "corporate" : "personal");
        }
        if (merchantDetailDTO.getCompanyType() != null) {
            companyInfo.put("Company public listed on ASX or not?", enNameMap.get("companyType").get(merchantDetailDTO.getCompanyType().toString()));
        }
        if (merchantDetailDTO.getMainBusiness() != null) {
            companyInfo.put("Business Type", enNameMap.get("mainBusiness").get(merchantDetailDTO.getMainBusiness().toString()));
        }
        if (merchantDetailDTO.getEstimatedAnnualSales() != null) {
            companyInfo.put("Estimated Monthly Sales", enNameMap.get("estimatedAnnualSales").get(merchantDetailDTO.getEstimatedAnnualSales().toString()));
        }
        if (merchantDetailDTO.getAvgSalesValue() != null) {
            companyInfo.put("Average amount of transactions", enNameMap.get("avgSalesValue").get(merchantDetailDTO.getAvgSalesValue().toString()));
        }
        if (merchantDetailDTO.getSalesValueByCard() != null) {
            companyInfo.put("Single transaction amount", enNameMap.get("salesValueByCard").get(merchantDetailDTO.getSalesValueByCard().toString()));
        }

        Map<String, Object> bankInfo = new HashMap<>(16);
        bankInfo.put("BSB", merchantDetailDTO.getBsb());
        bankInfo.put("Bank Account Number", merchantDetailDTO.getAccountNo());
        bankInfo.put("Bank Name", merchantDetailDTO.getBankName());
        bankInfo.put("Account Name", merchantDetailDTO.getAccountName());

        int companyInfoRowsNo = (companyInfoHeaders.length + 1) % 3 == 0 ? (companyInfoHeaders.length + 1) / 3 : ((companyInfoHeaders.length + 1) / 3) + 1;

        int bankInfoRowsNo = (bankInfoHeaders.length + 1) % 3 == 0 ? (bankInfoHeaders.length + 1) / 3 : ((bankInfoHeaders.length + 1) / 3) + 1;

        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet();
        // 设置缺省列高
        sheet.setDefaultRowHeightInPoints(12);
        // 设置缺省列宽
        sheet.setDefaultColumnWidth(20);
        HSSFFont font = workbook.createFont();
        font.setFontName("宋体");
        // 字体大小
        font.setFontHeightInPoints((short)12);
        HSSFCellStyle style = workbook.createCellStyle();
        // 单元格居中对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font);

        HSSFFont headerFont = workbook.createFont();
        headerFont.setFontName("宋体");
        // 字体大小
        headerFont.setFontHeightInPoints((short)12);
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerFont.setBold(true);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        headerStyle.setFont(headerFont);

        int startRowNo = 0;
        int cellNo = 0;

        // 公司信息
        HSSFRow companyTitleRow = sheet.createRow(startRowNo);
        HSSFCell companyTitleCell = companyTitleRow.createCell(0);
        companyTitleCell.setCellStyle(headerStyle);
        companyTitleCell.setCellValue("Company Info:");

        startRowNo += 1;

        for (int i = 0; i < companyInfoRowsNo; i ++) {
            HSSFRow headerRow = sheet.createRow(startRowNo);
            HSSFRow valueRow = sheet.createRow(startRowNo + 1);
            for (int j = 0; j < 3; j ++) {
                if (cellNo > companyInfoHeaders.length - 1) {
                    break;
                }
                HSSFCell headerCell = headerRow.createCell(j);
                headerCell.setCellStyle(headerStyle);
                HSSFRichTextString headerText = new HSSFRichTextString(companyInfoHeaders[cellNo]);
                headerCell.setCellValue(headerText);

                HSSFCell valueCell = valueRow.createCell(j);
                valueCell.setCellStyle(style);
                Object o = companyInfo.get(companyInfoHeaders[cellNo]);
                String s = o != null ? o.toString() : "";
                HSSFRichTextString valueText = new HSSFRichTextString(s);
                valueCell.setCellValue(valueText);
                cellNo += 1;
            }
            startRowNo += 3;
        }

        // 联系人信息
        HSSFRow contactPersonTitleRow = sheet.createRow(startRowNo);
        HSSFCell contactPersonTitleCell = contactPersonTitleRow.createCell(0);
        contactPersonTitleCell.setCellStyle(headerStyle);
        contactPersonTitleCell.setCellValue("ContactPerson Info:");

        startRowNo += 1;

        HSSFRow contactPersonHeaderRow = sheet.createRow(startRowNo);
        for (int i = 0; i < contactPersonInfoHeaders.length; i ++) {
            HSSFCell headerCell = contactPersonHeaderRow.createCell(i);
            headerCell.setCellStyle(headerStyle);
            HSSFRichTextString headerText = new HSSFRichTextString(contactPersonInfoHeaders[i]);
            headerCell.setCellValue(headerText);
        }

        startRowNo += 1;

        if (contactPersonDTOList != null && !contactPersonDTOList.isEmpty()) {
            Map<String, Object> contactPersonInfo = new HashMap<>(16);
            for (ContactPersonDTO contactPersonDTO : contactPersonDTOList) {
                contactPersonInfo.put("Name", contactPersonDTO.getName());
                contactPersonInfo.put("Position", contactPersonDTO.getTitle());
                contactPersonInfo.put("Contact Number", contactPersonDTO.getMobile());
                contactPersonInfo.put("Contact Email", contactPersonDTO.getEmail());
                HSSFRow contactPersonValueRow = sheet.createRow(startRowNo);
                for (int i = 0; i < contactPersonInfoHeaders.length; i ++) {
                    HSSFCell valueCell = contactPersonValueRow.createCell(i);
                    valueCell.setCellStyle(style);
                    Object o = contactPersonInfo.get(contactPersonInfoHeaders[i]);
                    String s = o != null ? o.toString() : "";
                    HSSFRichTextString valueText = new HSSFRichTextString(s);
                    valueCell.setCellValue(valueText);
                }
                startRowNo += 1;
            }
        }

        startRowNo += 1;

        // 银行信息
        HSSFRow bankTitleRow = sheet.createRow(startRowNo);
        HSSFCell bankTitleCell = bankTitleRow.createCell(0);
        bankTitleCell.setCellStyle(headerStyle);
        bankTitleCell.setCellValue("Bank Info:");

        startRowNo += 1;

        cellNo = 0;
        for (int i = 0; i < bankInfoRowsNo; i ++) {
            HSSFRow headerRow = sheet.createRow(startRowNo);
            HSSFRow valueRow = sheet.createRow(startRowNo + 1);
            for (int j = 0; j < 3; j ++) {
                if (cellNo > bankInfoHeaders.length - 1) {
                    break;
                }
                HSSFCell headerCell = headerRow.createCell(j);
                headerCell.setCellStyle(headerStyle);
                HSSFRichTextString headerText = new HSSFRichTextString(bankInfoHeaders[cellNo]);
                headerCell.setCellValue(headerText);

                HSSFCell valueCell = valueRow.createCell(j);
                valueCell.setCellStyle(style);
                Object o = bankInfo.get(bankInfoHeaders[cellNo]);
                String s = o != null ? o.toString() : "";
                HSSFRichTextString valueText = new HSSFRichTextString(s);
                valueCell.setCellValue(valueText);
                cellNo += 1;
            }
            startRowNo += 3;
        }

        // 股东授权人信息

        String[] ownerHeaders = {"First Name", "Last Name", "Date of birth", "Passport Number/Driver Licence Number", "Address", "% of the ownership"};
        String[] directorHeaders = {"First Name", "Last Name", "Date of birth", "Passport Number/Driver Licence Number", "Address"};

        HSSFRow ownerTitleRow = sheet.createRow(startRowNo);
        HSSFCell ownerTitleCell = ownerTitleRow.createCell(0);
        ownerTitleCell.setCellStyle(headerStyle);
        ownerTitleCell.setCellValue("Owner:");

        startRowNo += 1;

        HSSFRow ownerHeaderRow = sheet.createRow(startRowNo);
        for (int i = 0; i < ownerHeaders.length; i ++) {
            HSSFCell headerCell = ownerHeaderRow.createCell(i);
            headerCell.setCellStyle(headerStyle);
            HSSFRichTextString headerText = new HSSFRichTextString(ownerHeaders[i]);
            headerCell.setCellValue(headerText);
        }

        startRowNo += 1;

        boolean judge = merchantDetailDTO.getEntityType().intValue() == 4 && merchantDetailDTO.getTrusteeType().intValue() == 0;
        if (merchantDetailDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()
                || merchantDetailDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode()
                || merchantDetailDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()
        ) {
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            if (shareholderDTOList != null && !shareholderDTOList.isEmpty()) {
                Map<String, Object> ownerInfo = new HashMap<>(16);
                for (ShareholderDTO shareholderDTO : shareholderDTOList) {
                    ownerInfo.put("First Name", shareholderDTO.getFirstName());
                    ownerInfo.put("Last Name", shareholderDTO.getLastName());
                    ownerInfo.put("Date of birth", shareholderDTO.getBirth());
                    ownerInfo.put("Passport Number/Driver Licence Number", shareholderDTO.getIdType().intValue() == 0 ? shareholderDTO.getPassport() : shareholderDTO.getIdNo());
                    ownerInfo.put("Address", shareholderDTO.getAddress());
                    ownerInfo.put("% of the ownership", shareholderDTO.getOwnerShip());
                    HSSFRow ownerValueRow = sheet.createRow(startRowNo);
                    for (int i = 0; i < ownerHeaders.length; i ++) {
                        HSSFCell valueCell = ownerValueRow.createCell(i);
                        valueCell.setCellStyle(style);
                        Object o = ownerInfo.get(ownerHeaders[i]);
                        String s = o != null ? o.toString() : "";
                        HSSFRichTextString valueText = new HSSFRichTextString(s);
                        valueCell.setCellValue(valueText);
                    }
                    startRowNo += 1;
                }
            }
        } else if (merchantDetailDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {
            List<PartnerDTO> partnerDTOList = partnerService.find(params, null, null);
            if (partnerDTOList != null && !partnerDTOList.isEmpty()) {
                Map<String, Object> ownerInfo = new HashMap<>(16);
                for (PartnerDTO partnerDTO : partnerDTOList) {
                    ownerInfo.put("First Name", partnerDTO.getFirstName());
                    ownerInfo.put("Last Name", partnerDTO.getLastName());
                    ownerInfo.put("Date of birth", partnerDTO.getBirth());
                    ownerInfo.put("Passport Number/Driver Licence Number", partnerDTO.getIdType().intValue() == 0 ? partnerDTO.getPassport() : partnerDTO.getIdNo());
                    ownerInfo.put("Address", partnerDTO.getAddress());
                    ownerInfo.put("% of the ownership", partnerDTO.getOwnerShip());
                    HSSFRow ownerValueRow = sheet.createRow(startRowNo);
                    for (int i = 0; i < ownerHeaders.length; i ++) {
                        HSSFCell valueCell = ownerValueRow.createCell(i);
                        valueCell.setCellStyle(style);
                        Object o = ownerInfo.get(ownerHeaders[i]);
                        String s = o != null ? o.toString() : "";
                        HSSFRichTextString valueText = new HSSFRichTextString(s);
                        valueCell.setCellValue(valueText);
                    }
                    startRowNo += 1;
                }
            }
        } else if (judge) {
            List<TrusteeDTO> trusteeDTOList = trusteeService.find(params, null, null);
            if (trusteeDTOList != null && !trusteeDTOList.isEmpty()) {
                Map<String, Object> ownerInfo = new HashMap<>(16);
                for (TrusteeDTO trusteeDTO : trusteeDTOList) {
                    ownerInfo.put("First Name", trusteeDTO.getFirstName());
                    ownerInfo.put("Last Name", trusteeDTO.getLastName());
                    ownerInfo.put("Date of birth", trusteeDTO.getBirth());
                    ownerInfo.put("Passport Number/Driver Licence Number", trusteeDTO.getIdType().intValue() == 0 ? trusteeDTO.getPassport() : trusteeDTO.getIdNo());
                    ownerInfo.put("Address", trusteeDTO.getAddress());
                    ownerInfo.put("% of the ownership", "");
                    HSSFRow ownerValueRow = sheet.createRow(startRowNo);
                    for (int i = 0; i < ownerHeaders.length; i ++) {
                        HSSFCell valueCell = ownerValueRow.createCell(i);
                        valueCell.setCellStyle(style);
                        Object o = ownerInfo.get(ownerHeaders[i]);
                        String s = o != null ? o.toString() : "";
                        HSSFRichTextString valueText = new HSSFRichTextString(s);
                        valueCell.setCellValue(valueText);
                    }
                    startRowNo += 1;
                }
            }
        }

        startRowNo += 1;

        HSSFRow directorTitleRow = sheet.createRow(startRowNo);
        HSSFCell directorTitleCell = directorTitleRow.createCell(0);
        directorTitleCell.setCellStyle(headerStyle);
        directorTitleCell.setCellValue("Director:");

        startRowNo += 1;

        HSSFRow directorHeaderRow = sheet.createRow(startRowNo);
        for (int i = 0; i < directorHeaders.length; i ++) {
            HSSFCell headerCell = directorHeaderRow.createCell(i);
            headerCell.setCellStyle(headerStyle);
            HSSFRichTextString headerText = new HSSFRichTextString(directorHeaders[i]);
            headerCell.setCellValue(headerText);
        }

        boolean judge1 = merchantDetailDTO.getEntityType().intValue() == 1 && merchantDetailDTO.getCompanyType() != null && merchantDetailDTO.getCompanyType().intValue() == 1;
        boolean judge2 = merchantDetailDTO.getEntityType().intValue() == 4 && merchantDetailDTO.getTrusteeType() != null && merchantDetailDTO.getTrusteeType().intValue() == 1;

        if (judge1 || judge2) {
            List<DirectorDTO> directorDTOList = directorService.find(params, null, null);
            if (directorDTOList != null && !directorDTOList.isEmpty()) {
                Map<String, Object> directorInfo = new HashMap<>(16);
                for (DirectorDTO directorDTO : directorDTOList) {
                    directorInfo.put("First Name", directorDTO.getFirstName());
                    directorInfo.put("Last Name", directorDTO.getLastName());
                    directorInfo.put("Date of birth", directorDTO.getBirth());
                    directorInfo.put("Passport Number/Driver Licence Number", Integer.parseInt(directorDTO.getIdType()) == 0 ? directorDTO.getPassport() : directorDTO.getIdNo());
                    directorInfo.put("Address", directorDTO.getAddress());
                    HSSFRow ownerValueRow = sheet.createRow(startRowNo);
                    for (int i = 0; i < directorHeaders.length; i ++) {
                        HSSFCell valueCell = ownerValueRow.createCell(i);
                        valueCell.setCellStyle(style);
                        Object o = directorInfo.get(directorHeaders[i]);
                        String s = o != null ? o.toString() : "";
                        HSSFRichTextString valueText = new HSSFRichTextString(s);
                        valueCell.setCellValue(valueText);
                    }
                    startRowNo += 1;
                }
            }
        }

        startRowNo += 2;

        // 签署人信息

        HSSFRow authorisedTitleRow = sheet.createRow(startRowNo);
        HSSFCell authorisedTitleCell = authorisedTitleRow.createCell(0);
        authorisedTitleCell.setCellStyle(headerStyle);
        authorisedTitleCell.setCellValue("Authorised Officer:");

        String[] signerHeaders = {"Signer Full Name", "Signer Title"};
        Map<String, Object> signerInfo = new HashMap<>(16);
        signerInfo.put("Signer Full Name", merchantDetailDTO.getDocusignSigner());
        if (merchantDetailDTO.getAuthorisedTitle() != null) {
            signerInfo.put("Signer Title", enNameMap.get("authorisedType").get(merchantDetailDTO.getAuthorisedTitle().toString()));
        }

        startRowNo += 1;

        HSSFRow authorisedHeaderRow = sheet.createRow(startRowNo);
        HSSFRow authorisedValueRow = sheet.createRow(startRowNo + 1);
        for (int i = 0; i < signerHeaders.length; i ++) {
            HSSFCell headerCell = authorisedHeaderRow.createCell(i);
            headerCell.setCellStyle(headerStyle);
            HSSFRichTextString headerText = new HSSFRichTextString(signerHeaders[i]);
            headerCell.setCellValue(headerText);

            HSSFCell valueCell = authorisedValueRow.createCell(i);
            valueCell.setCellStyle(style);
            Object o = signerInfo.get(signerHeaders[i]);
            String s = o != null ? o.toString() : "";
            HSSFRichTextString valueText = new HSSFRichTextString(s);
            valueCell.setCellValue(valueText);
        }

        startRowNo += 3;

        // 合同类型
        HSSFRow contractTitleRow = sheet.createRow(startRowNo);
        HSSFCell contractTitleCell = contractTitleRow.createCell(0);
        contractTitleCell.setCellStyle(headerStyle);
        contractTitleCell.setCellValue("Cooperation Type:");

        startRowNo += 1;

        HSSFRow contractHeaderRow = sheet.createRow(startRowNo);
        HSSFRow contractValueRow = sheet.createRow(startRowNo + 1);

        HSSFCell contractHeaderCell = contractHeaderRow.createCell(0);
        contractHeaderCell.setCellStyle(headerStyle);
        HSSFRichTextString contractHeaderText = new HSSFRichTextString("Contract Type");
        contractHeaderCell.setCellValue(contractHeaderText);

        HSSFCell contractValueCell = contractValueRow.createCell(0);
        contractValueCell.setCellStyle(style);
        String contractType = "";
        if (merchantDetailDTO.getContractType() != null) {
            contractType = merchantDetailDTO.getContractType().intValue() == 0 ? "Full Contracts" : "Installment Contracts Only";
        }
        HSSFRichTextString contractValueText = new HSSFRichTextString(contractType);
        contractValueCell.setCellValue(contractValueText);

        startRowNo += 3;

        // 其他
        HSSFRow otherTitleRow = sheet.createRow(startRowNo);
        HSSFCell otherTitleCell = otherTitleRow.createCell(0);
        otherTitleCell.setCellStyle(headerStyle);
        otherTitleCell.setCellValue("Others:");

        String[] otherHeaders = {"Marketing Representative", "Nature of business"};
        Map<String, Object> otherInfo = new HashMap<>(16);
        otherInfo.put("Marketing Representative", merchantDetailDTO.getRepresentativeName());
        otherInfo.put("Nature of business", merchantDetailDTO.getOtherEntity());

        startRowNo += 1;

        HSSFRow otherHeaderRow = sheet.createRow(startRowNo);
        HSSFRow otherValueRow = sheet.createRow(startRowNo + 1);
        for (int i = 0; i < otherHeaders.length; i ++) {
            HSSFCell headerCell = otherHeaderRow.createCell(i);
            headerCell.setCellStyle(headerStyle);
            HSSFRichTextString headerText = new HSSFRichTextString(otherHeaders[i]);
            headerCell.setCellValue(headerText);

            HSSFCell valueCell = otherValueRow.createCell(i);
            valueCell.setCellStyle(style);
            Object o = otherInfo.get(otherHeaders[i]);
            String s = o != null ? o.toString() : "";
            HSSFRichTextString valueText = new HSSFRichTextString(s);
            valueCell.setCellValue(valueText);
        }

        return workbook;
    }


    private Map<String, Map<String, Object>> getEnName(MerchantDetailDTO merchantDetailDTO) {
        Map<String, Map<String, Object>> map = new HashMap<>(16);

        String[] codes = {"entityType", "taxRegistered", "mainBusiness", "operationTime", "merchantState", "estimatedAnnualSales", "avgSalesValue",
                "salesValueByCard", "county", "city", "companyType", "authorised"};
        Map<String, List<StaticData>> codeMap = staticDataService.findByCodeList(codes);
        List<StaticData> entityTypeList = codeMap.get("entityType");
        if (merchantDetailDTO.getEntityType() != null) {
            for (StaticData staticData : entityTypeList) {
                if (staticData.getValue().equals(merchantDetailDTO.getEntityType().toString())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("entityType", hashMap);
                }
            }
        }
//        List<StaticData> taxRegisteredList = codeMap.get("taxRegistered");
//        for (StaticData staticData : taxRegisteredList) {
//            if (staticData.getValue().equals(merchantDetailDTO.getTaxRegistered().toString())) {
//                Map<String, Object> hashMap = new HashMap<>(1);
//                hashMap.put(staticData.getValue(), staticData.getEnName());
//                map.put("taxRegistered", hashMap);
//            }
//        }
        List<StaticData> mainBusinessList = codeMap.get("mainBusiness");
        if (merchantDetailDTO.getMainBusiness() != null) {
            for (StaticData staticData : mainBusinessList) {
                if (staticData.getValue().equals(merchantDetailDTO.getMainBusiness().toString())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("mainBusiness", hashMap);
                }
            }
        }
        List<StaticData> operationTimeList = codeMap.get("operationTime");
        if (merchantDetailDTO.getOperationTime() != null) {
            for (StaticData staticData : operationTimeList) {
                if (staticData.getValue().equals(merchantDetailDTO.getOperationTime().toString())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("operationTime", hashMap);
                }
            }
        }
        List<StaticData> stateList = codeMap.get("merchantState");
        for (StaticData staticData : stateList) {
            if (staticData.getValue().equals(merchantDetailDTO.getMerchantState())) {
                Map<String, Object> hashMap = new HashMap<>(1);
                hashMap.put(staticData.getValue(), staticData.getEnName());
                map.put("merchantState", hashMap);
            }
        }
        List<StaticData> estimatedAnnualSalesList = codeMap.get("estimatedAnnualSales");
        if (merchantDetailDTO.getEstimatedAnnualSales() != null) {
            for (StaticData staticData : estimatedAnnualSalesList) {
                if (merchantDetailDTO.getEstimatedAnnualSales() != null && staticData.getValue().equals(merchantDetailDTO.getEstimatedAnnualSales().toString())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("estimatedAnnualSales", hashMap);
                }
            }
        }
        List<StaticData> avgSalesValueList = codeMap.get("avgSalesValue");
        if (merchantDetailDTO.getAvgSalesValue() != null) {
            for (StaticData staticData : avgSalesValueList) {
                if (merchantDetailDTO.getAvgSalesValue() != null && staticData.getValue().equals(merchantDetailDTO.getAvgSalesValue().toString())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("avgSalesValue", hashMap);
                }
            }
        }
        List<StaticData> salesValueByCardList = codeMap.get("salesValueByCard");
        if (merchantDetailDTO.getSalesValueByCard() != null) {
            for (StaticData staticData : salesValueByCardList) {
                if (merchantDetailDTO.getSalesValueByCard() != null &&staticData.getValue().equals(merchantDetailDTO.getSalesValueByCard().toString())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("salesValueByCard", hashMap);
                }
            }
        }
        List<StaticData> countryList = codeMap.get("county");
        if (merchantDetailDTO.getCounty() != null) {
            for (StaticData staticData : countryList) {
                if (staticData.getValue().equals(merchantDetailDTO.getCounty())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("county", hashMap);
                }
            }
        }
        List<StaticData> cityList = codeMap.get("city");
        if (merchantDetailDTO.getCity() != null) {
            for (StaticData staticData : cityList) {
                if (staticData.getValue().equals(merchantDetailDTO.getCity())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("city", hashMap);
                }
            }
        }
        List<StaticData> companyTypeList = codeMap.get("companyType");
        if (merchantDetailDTO.getCompanyType() != null) {
            for (StaticData staticData : companyTypeList) {
                if (staticData.getValue().equals(merchantDetailDTO.getCompanyType().toString())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("companyType", hashMap);
                }
            }
        }
        List<StaticData> authorisedTypeList = codeMap.get("authorised");
        if (merchantDetailDTO.getAuthorisedTitle() != null) {
            for (StaticData staticData : authorisedTypeList) {
                if (staticData.getValue().equals(merchantDetailDTO.getAuthorisedTitle().toString())) {
                    Map<String, Object> hashMap = new HashMap<>(1);
                    hashMap.put(staticData.getValue(), staticData.getEnName());
                    map.put("authorisedType", hashMap);
                }
            }
        }
        return map;
    }

    @Override
    public List<MerchantDTO> merchantList() {
        Map<String, Object> params = new HashMap<>(1);
        params.put("isAvailable", 1);
     //   params.put("state", 1);
        List<MerchantDTO> resultList = merchantDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public void setDiscountRate(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", merchantDTO.getId());
        MerchantDTO merchant = merchantDAO.selectOneDTO(params);
        merchant.setMarketingDiscount(merchantDTO.getMarketingDiscount());
        params.clear();
        params.put("merchantId",merchantDTO.getId());
        params.put("discountOfMerchant",merchantDTO.getMarketingDiscount());
        //营销折扣+让利用户+平台所得+最大商户手续费<1
        RouteDTO route = routeService.findMaxMerRate(merchantDTO.getId());
        Boolean res = checkDiscountRate(merchant,route);
        if(!res){
            throw new BizException(I18nUtils.get("over.maximum.rate", getLang(request)));
        }
        serverService.updateMerchant(merchantDTO.getId(), JSONObject.parseObject(JSON.toJSONString(params)), request);
        updateMerchant(merchantDTO.getId(),merchant,request);
    }

    private Boolean checkDiscountRate(MerchantDTO merchant, RouteDTO route) {
        Boolean result = false;
        BigDecimal transRate = BigDecimal.ZERO;
        if(!(route == null ||route.getId() == null)){
            transRate = new BigDecimal(route.getRate()+"");
        }
        BigDecimal marketingDiscount = new BigDecimal(merchant.getMarketingDiscount()+"");
        BigDecimal paySellDiscount = new BigDecimal(merchant.getPaySellDiscount()+"");
        BigDecimal payRebateDiscount = new BigDecimal(merchant.getPayRebateDiscount()+"");
        BigDecimal transDiscount = (new BigDecimal("1").subtract(marketingDiscount).subtract(paySellDiscount).subtract(payRebateDiscount)).multiply(transRate).setScale(4, RoundingMode.HALF_UP);

        log.info("transDiscount:",marketingDiscount);

        if(marketingDiscount.add(paySellDiscount).add(payRebateDiscount).add(transDiscount).compareTo(new BigDecimal(1))<0){
            result = true;
        }
        return  result;
    }

    @Override
    public int countAppFindList(Map<String, Object> params) {
        return merchantDAO.countAppFindList(params);
    }

    @Override
    public List<MerchantDTO> appFindList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws Exception {
        params = getUnionParams(params, scs, pc);
        List<MerchantDTO> resultList = merchantDAO.appFindList(params);
        String lat = params.get("lat").toString();
        String lng = params.get("lng").toString();
        // 判断此处是否为APP端请求数据
        if (params.get("app") != null && resultList != null) {
            ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
            Long today = System.currentTimeMillis();
            for (MerchantDTO merchantDTO : resultList) {
                Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
                BigDecimal extraDiscount = null;
                if (today.longValue() < extraDiscountPeriod.longValue()) {
                    extraDiscount = merchantDTO.getExtraDiscount();
                } else {
                    extraDiscount = new BigDecimal("0.00");
                }
                // 对比支付和分期付折扣
//                BigDecimal payDiscountRate = merchantDTO.getPaySellDiscount()
//                        .add(merchantDTO.getMarketingDiscount()
//                                .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getMerchantDiscountRatePlatformProportion())))
//                        .add(extraDiscount
//                                .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountPayPlatform()))).setScale(4, RoundingMode.HALF_UP);
//                BigDecimal creditDiscountRate = merchantDTO.getSellDiscount()
//                        .add(merchantDTO.getMarketingDiscount()
//                                .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getDiscountRate())))
//                        .add(extraDiscount
//                                .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountCreditPlatform()))).setScale(4, RoundingMode.HALF_UP);
                BigDecimal balance = userService.getBalance(merchantDTO.getUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
                if (balance != null && (balance.compareTo(new BigDecimal("0")) != 0)) {
                    merchantDTO.setUserDiscount(merchantDTO.getWholeSaleUserDiscount());
                } else {
                    BigDecimal creditDiscountRate = merchantDTO.getBaseRate().add(extraDiscount).add(merchantDTO.getMarketingDiscount());
                    merchantDTO.setUserDiscount(creditDiscountRate);
                }
//                switch (payDiscountRate.compareTo(creditDiscountRate)) {
//                    case 1:
//                        merchantDTO.setUserDiscount(payDiscountRate);
//                        break;
//                    case -1:
//                        merchantDTO.setUserDiscount(creditDiscountRate);
//                        break;
//                    default:
//                        merchantDTO.setUserDiscount(payDiscountRate);
//                        break;
//                }
                //计算距离
                GlobalCoordinates source = new GlobalCoordinates(new Double(lat), new Double(lng));
                GlobalCoordinates target = new GlobalCoordinates(new Double(merchantDTO.getLat()), new Double(merchantDTO.getLng()));
                double distance = GeodesyUtil.getDistanceMeter(source, target, Ellipsoid.Sphere);
                merchantDTO.setDistance(new BigDecimal(distance).divide(new BigDecimal("1000")).setScale(2, RoundingMode.HALF_UP));
            }
            if (params.get("deals") == null) {
                // 按照折扣排序
                Collections.sort(resultList, (m1, m2) -> m2.getUserDiscount().compareTo(m1.getUserDiscount()));
                // 按照距离排序
                Collections.sort(resultList, Comparator.comparing(MerchantDTO::getDistance));
            } else {
                resultList = resultList.stream().sorted((m1, m2) -> m2.getUserDiscount().compareTo(m1.getUserDiscount())).limit(5).collect(Collectors.toList());
            }
        }
        return resultList;
    }

    @Override
    public void merchantTopChange(Long id, MerchantDTO merchantDTO, HttpServletRequest request) throws Exception {
        MerchantDTO oldMerchant = findMerchantById(id);
        if (!oldMerchant.getIsAvailable().equals(StaticDataEnum.STATUS_1.getCode())) {
            throw new BizException(I18nUtils.get("merchant.recommended", getLang(request)));
        }
        // 设置初始排序位置
        if (merchantDTO.getIsTop().equals(StaticDataEnum.STATUS_1.getCode())) {
            Integer latestSort = merchantDAO.latestTopSort();
            if (latestSort != null && latestSort != 0) {
                merchantDTO.setTopSort(latestSort.intValue() + 1);
            } else {
                merchantDTO.setTopSort(1);
            }
            updateMerchant(id, merchantDTO, request);
        } else {
            Integer topSort = oldMerchant.getTopSort();
            merchantDTO.setTopSort(0);
            updateMerchant(id, merchantDTO, request);
            merchantDAO.topSortUp(topSort);
        }
    }

    @Override
    public int topCount() {
        return merchantDAO.topCount();
    }

    @Override
    public List<MerchantDTO> topList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws Exception {
        params = getUnionParams(params, scs, pc);
        List<MerchantDTO> resultList = merchantDAO.topList(params);
        ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
        Long today = System.currentTimeMillis();
        for (MerchantDTO merchantDTO : resultList) {
            Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
            BigDecimal extraDiscount = null;
            if (today.longValue() < extraDiscountPeriod.longValue()) {
                extraDiscount = merchantDTO.getExtraDiscount();
            } else {
                extraDiscount = new BigDecimal("0.00");
            }
            // 对比支付和分期付折扣
//            BigDecimal payDiscountRate = merchantDTO.getPaySellDiscount()
//                    .add(merchantDTO.getMarketingDiscount()
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getMerchantDiscountRatePlatformProportion())))
//                    .add(extraDiscount
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountPayPlatform()))).setScale(4, RoundingMode.HALF_UP);
//            BigDecimal creditDiscountRate = merchantDTO.getSellDiscount()
//                    .add(merchantDTO.getMarketingDiscount()
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getDiscountRate())))
//                    .add(extraDiscount
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountCreditPlatform()))).setScale(4, RoundingMode.HALF_UP);
            BigDecimal balance=BigDecimal.ZERO;
            try{
                balance = userService.getBalance(merchantDTO.getUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
            }catch (Exception e){
                log.error("查询商户子户异常:e",e);
            }
            if (balance != null && (balance.compareTo(new BigDecimal("0")) != 0)) {
                merchantDTO.setUserDiscount(merchantDTO.getWholeSaleUserDiscount());
            } else {
                BigDecimal creditDiscountRate = merchantDTO.getBaseRate().add(extraDiscount).add(merchantDTO.getMarketingDiscount());
                merchantDTO.setUserDiscount(creditDiscountRate);
            }
//            switch (payDiscountRate.compareTo(creditDiscountRate)) {
//                case 1:
//                    merchantDTO.setUserDiscount(payDiscountRate);
//                    break;
//                case -1:
//                    merchantDTO.setUserDiscount(creditDiscountRate);
//                    break;
//                default:
//                    merchantDTO.setUserDiscount(payDiscountRate);
//                    break;
//            }
        }
        return resultList;
    }

    @Override
    public void shiftUpOrDown(Long id, Integer upOrDown, HttpServletRequest request) throws BizException {
        log.info("移动操作");
        MerchantDTO original = findMerchantById(id);
        MerchantDTO passivity = merchantDAO.shiftUpOrDown(id, upOrDown);

        if (passivity == null) {
            log.info("移动操作失败");
            throw new BizException(I18nUtils.get("move.failed", getLang(request)));
        }

        Integer originalSort = original.getTopSort();
        original.setTopSort(passivity.getTopSort());
        passivity.setTopSort(originalSort);

        updateMerchant(id, original, request);
        updateMerchant(passivity.getId(), passivity, request);
    }

    @Override
    public int listOfWebSiteCount(Map<String, Object> params) {
        List<MerchantDTO> merchantDTOList = find(params, null, null);
        return merchantDTOList.size();
    }

    @Override
    public List<MerchantDTO> listOfWebSite(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws Exception {
        params = getUnionParams(params, scs, null);
        List<MerchantDTO> merchantDTOList = merchantDAO.selectDTO(params);
        ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
        Long today = System.currentTimeMillis();
        merchantDTOList.forEach(merchantDTO -> {
            Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
            BigDecimal extraDiscount = null;
            if (today.longValue() < extraDiscountPeriod.longValue()) {
                extraDiscount = merchantDTO.getExtraDiscount();
            } else {
                extraDiscount = new BigDecimal("0.00");
            }
            // 对比支付和分期付折扣
//            BigDecimal payDiscountRate = merchantDTO.getPaySellDiscount()
//                    .add(merchantDTO.getMarketingDiscount()
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getMerchantDiscountRatePlatformProportion())))
//                    .add(extraDiscount
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountPayPlatform()))).setScale(4, RoundingMode.HALF_UP);
//            BigDecimal creditDiscountRate = merchantDTO.getSellDiscount()
//                    .add(merchantDTO.getMarketingDiscount()
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getDiscountRate())))
//                    .add(extraDiscount
//                            .multiply(MathUtils.subtract(new BigDecimal(1), parametersConfigDTO.getExtraDiscountCreditPlatform()))).setScale(4, RoundingMode.HALF_UP);
            BigDecimal balance = null;
            try {
                balance = userService.getBalance(merchantDTO.getUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            if (balance != null && (balance.compareTo(new BigDecimal("0")) != 0)) {
                merchantDTO.setUserDiscount(merchantDTO.getWholeSaleUserDiscount());
            } else {
                BigDecimal creditDiscountRate = merchantDTO.getBaseRate().add(extraDiscount).add(merchantDTO.getMarketingDiscount());
                merchantDTO.setUserDiscount(creditDiscountRate);
            }
//            switch (payDiscountRate.compareTo(creditDiscountRate)) {
//                case 1:
//                    merchantDTO.setUserDiscount(payDiscountRate);
//                    break;
//                case -1:
//                    merchantDTO.setUserDiscount(creditDiscountRate);
//                    break;
//                default:
//                    merchantDTO.setUserDiscount(payDiscountRate);
//                    break;
//            }
        });
        // 按照折扣排序
        Collections.sort(merchantDTOList, (m1, m2) -> m2.getUserDiscount().compareTo(m1.getUserDiscount()));
        if (params.get("sortDisCount") != null) {
            Integer sortDisCount = Integer.valueOf(params.get("sortDisCount").toString());
            Collections.sort(merchantDTOList, new Comparator<MerchantDTO>() {
                @Override
                public int compare(MerchantDTO o1, MerchantDTO o2) {
                    if (sortDisCount.intValue() == 0) {
                        return o1.getUserDiscount().compareTo(o2.getUserDiscount());
                    } else {
                        return o2.getUserDiscount().compareTo(o1.getUserDiscount());
                    }
                }
            });
        }
        // 分页
        merchantDTOList = merchantDTOList.stream().filter(merchantDTO -> merchantDTO.getState().intValue() == StaticDataEnum.MERCHANT_STATE_1.getCode()
                || merchantDTO.getState().intValue() == StaticDataEnum.MERCHANT_STATE_4.getCode()
                || merchantDTO.getState().intValue() == StaticDataEnum.MERCHANT_STATE_3.getCode())
                .filter(merchantDTO -> merchantDTO.getIsAvailable().intValue() == StaticDataEnum.MERCHANT_STATE_1.getCode())
                .skip(pc.getPageIndex()).limit(pc.getPageSize()).collect(Collectors.toList());
        return merchantDTOList;
    }

    @Override
    public JSONObject docusignContract(Long merchantId, Integer contractType, HttpServletRequest request) throws Exception {

        JSONObject docusignRequesInfo = null;

        MerchantDTO merchantDTO = findMerchantById(merchantId);

        // 必输字段校验
        checkDocusignField(merchantDTO, request);

        int entityType = merchantDTO.getEntityType().intValue();

//        String docusignContractId;
//
//        if (contractType.intValue() == StaticDataEnum.DOCUSIGN_CONTRACT_TYPE_0.getCode()) {
//            docusignContractId = StaticDataEnum.DOCUSIGN_CONTRACT_TYPE_0.getMessage();
//        } else {
//            docusignContractId = StaticDataEnum.DOCUSIGN_CONTRACT_TYPE_1.getMessage();
//        }

        String docusignContractId = RELEASE_SERVER_TYPE.equals(serverType) ? StaticDataEnum.DOCUSIGN_CONTRACT_TYPE_FORMAL.getMessage()
                : StaticDataEnum.DOCUSIGN_CONTRACT_TYPE_Y.getMessage();

        if (entityType == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {

            docusignRequesInfo = companyEntityMerchantService.docusignRequest(merchantDTO, docusignContractId, request);

        } else if (entityType == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || entityType == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {

            docusignRequesInfo = soleAndOtherEntityMerchantService.docusignRequest(merchantDTO, docusignContractId, request);

        } else if (entityType == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {

            docusignRequesInfo = partnerEntityMerchantService.docusignRequest(merchantDTO, docusignContractId, request);

        } else {

            docusignRequesInfo = trusteeEntityMerchantService.docusignRequest(merchantDTO, docusignContractId, request);

        }

        log.info("docusign request data:{}", docusignRequesInfo);

        JSONObject requsetResult = null;

        try {
            requsetResult = docuSignService.genSignUrl(docusignRequesInfo, request);
        } catch (Exception e) {
            log.info("error message:{}, e:{}", e.getMessage(), e);
            throw new BizException(I18nUtils.get("docusign.failed", getLang(request)));
        }

        log.info("docusign request result:{}", requsetResult);

        // 保存docusign返回id
        String envelopeId = requsetResult.getString("envelopeId");
        merchantDTO.setState(StaticDataEnum.MERCHANT_STATE_8.getCode());
        merchantDTO.setDocusignEnvelopeid(envelopeId);
        merchantDTO.setContractType(contractType);
        merchantDTO.setContractStartTime(System.currentTimeMillis());
        updateMerchant(merchantDTO.getId(), merchantDTO, request);

        return requsetResult;
    }

    /**
     * 校验docusign信息
     * @param merchantDTO
     */
    private void checkDocusignField(MerchantDTO merchantDTO, HttpServletRequest request) throws Exception {
        if (StringUtils.isEmpty(merchantDTO.getCorporateName())) {
            throw new BizException(I18nUtils.get("docusign.businessName.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getPracticalName())) {
            throw new BizException(I18nUtils.get("docusign.tradingName.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getAddress())) {
            throw new BizException(I18nUtils.get("docusign.address.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getCity())) {
            throw new BizException(I18nUtils.get("docusign.suburb.isNull", getLang(request)));
        }
        if (merchantDTO.getMerchantState() == null) {
            throw new BizException(I18nUtils.get("docusign.state.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getPostcode())) {
            throw new BizException(I18nUtils.get("docusign.postcode.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getAbn())) {
            throw new BizException(I18nUtils.get("docusign.abn.isNull", getLang(request)));
        }
        if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode() && StringUtils.isEmpty(merchantDTO.getOtherEntity())) {
            throw new BizException(I18nUtils.get("docusign.otherEntity.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getDocusignSigner())) {
            throw new BizException(I18nUtils.get("docusign.signer.isNull", getLang(request)));
        }
        if (merchantDTO.getAuthorisedTitle() == null){
            throw new BizException(I18nUtils.get("docusign.signer.title",getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getBankName())) {
            throw new BizException(I18nUtils.get("docusign.bankName.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getAccountNo())) {
            throw new BizException(I18nUtils.get("docusign.accountNo.isNull", getLang(request)));
        }
        if (StringUtils.isEmpty(merchantDTO.getBsb())) {
            throw new BizException(I18nUtils.get("docusign.bsb.isNull", getLang(request)));
        }
        if (merchantDTO.getEstimatedAnnualSales() == null) {
            throw new BizException(I18nUtils.get("docusign.estimatedAnnualSales.isNull", getLang(request)));
        }
        if (merchantDTO.getAvgSalesValue() == null) {
            throw new BizException(I18nUtils.get("docusign.avgSalesValue.isNull", getLang(request)));
        }
        if (merchantDTO.getSalesValueByCard() == null) {
            throw new BizException(I18nUtils.get("docusign.salesValueByCard.isNull", getLang(request)));
        }
        Map<String, Object> params = new HashMap<>(16);
        params.put("merchantId", merchantDTO.getId());
        List<ContactPersonDTO> contactPersonDTOList = contactPersonService.find(params, null, null);
        if (contactPersonDTOList != null && contactPersonDTOList.isEmpty()) {
            throw new BizException(I18nUtils.get("docusign.contactPerson.isNull", getLang(request)));
        } else {
            contactPersonDTOList.stream().forEach(contactPersonDTO -> {
                if (StringUtils.isEmpty(contactPersonDTO.getName())) {
                    throw new RuntimeException(I18nUtils.get("docusign.contactPerson.name.isNull", getLang(request)));
                }
                if (StringUtils.isEmpty(contactPersonDTO.getMobile())) {
                    throw new RuntimeException(I18nUtils.get("docusign.contactPerson.phone.isNull", getLang(request)));
                }
                if (StringUtils.isEmpty(contactPersonDTO.getEmail())) {
                    throw new RuntimeException(I18nUtils.get("docusign.contactPerson.email.isNull", getLang(request)));
                }
                if (StringUtils.isEmpty(contactPersonDTO.getTitle())) {
                    throw new RuntimeException(I18nUtils.get("docusign.contactPerson.title.isNull", getLang(request)));
                }
            });
        }
        if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            List<DirectorDTO> directorDTOList = directorService.find(params, null, null);
            if (merchantDTO.getCompanyType().intValue() == StaticDataEnum.STATUS_0.getCode()) {
                if (directorDTOList == null || directorDTOList.isEmpty()) {
                    throw new BizException(I18nUtils.get("docusign.director.isNull", getLang(request)));
                }
            }
            if (shareholderDTOList == null || shareholderDTOList.isEmpty()) {
                throw new BizException(I18nUtils.get("docusign.owner.isNull", getLang(request)));
            }
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {
            List<PartnerDTO> partnerDTOList = partnerService.find(params, null, null);
            if (partnerDTOList == null || partnerDTOList.isEmpty()) {
                throw new BizException(I18nUtils.get("docusign.owner.isNull", getLang(request)));
            }
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {
            List<DirectorDTO> directorDTOList = directorService.find(params, null, null);
            List<TrusteeDTO> trusteeDTOList = trusteeService.find(params, null, null);
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            if (merchantDTO.getTrusteeType().intValue() == StaticDataEnum.STATUS_0.getCode()) {
                if (trusteeDTOList == null || trusteeDTOList.isEmpty()) {
                    throw new BizException(I18nUtils.get("docusign.director.isNull", getLang(request)));
                }
            } else {
                if (directorDTOList == null || directorDTOList.isEmpty()) {
                    throw new BizException(I18nUtils.get("docusign.director.isNull", getLang(request)));
                }
            }
            if (shareholderDTOList == null || shareholderDTOList.isEmpty()) {
                throw new BizException(I18nUtils.get("docusign.owner.isNull", getLang(request)));
            }
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            if (shareholderDTOList == null || shareholderDTOList.isEmpty()) {
                throw new BizException(I18nUtils.get("docusign.owner.isNull", getLang(request)));
            }
        }
    }

    @Override
    public ModelAndView docusignCallBack(String docusignEnvelopeid, HttpServletRequest request) throws Exception {
        log.info("docusign call back, event id:{}", docusignEnvelopeid);
        ModelAndView modelAndView = new ModelAndView("docusign");
        // 签署状态记录、合同下载、跳转H5
        Map<String, Object> params = new HashMap<>(16);
        params.put("docusignEnvelopeid", docusignEnvelopeid);
        MerchantDTO merchantDTO = findOneMerchant(params);
        String event = request.getParameter("event");
        String status = "";
        Long merchantId = null;
        if (StaticDataEnum.DOCUSIGN_COMPLETE.getMessage().equals(event)) {
            JSONObject fileRequest = new JSONObject();
            fileRequest.put("envelopId", docusignEnvelopeid);
            fileRequest.put("contractType", merchantDTO.getContractType());
            JSONObject docuSignFiles = null;
            try {
                docuSignFiles = docuSignService.getDocument(fileRequest, request);
            } catch (Exception e) {
                log.info("docuSignFiles download failed, merchantId:{}, error message:{}, e:{}", merchantDTO.getId(), e.getMessage(), e);
            }
            merchantDTO.setState(StaticDataEnum.MERCHANT_STATE_1.getCode());
            merchantDTO.setDocusignHasSigned(StaticDataEnum.DOCUSIGN_COMPLETE.getCode());
            merchantDTO.setDocusignFiles(docuSignFiles.toJSONString());
            merchantDTO.setContractEndTime(System.currentTimeMillis());
            log.info("update docusign info, merchant:{}", merchantDTO);
            updateMerchant(merchantDTO.getId(), merchantDTO, request);
            status = "COMPLETE";
            merchantId = merchantDTO.getId();

            try{
                //发邮件
                //查询模板
                MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_9.getCode() + "");
                //邮件内容
                String sendMsg = mailTemplateDTO.getEnSendContent();
                Map<String, Object> userSearchParams = new HashMap<>(4);
                userSearchParams.put("merchantId", merchantDTO.getId());
                List<UserDTO> userDTOList = userService.find(userSearchParams, null, null);
                if (!CollectionUtils.isEmpty(userDTOList)) {
                    userSearchParams.clear();
                    for (UserDTO userDTO1 : userDTOList) {
                        long wholeSaleAction = 59L;
                        userSearchParams.put("userId", userDTO1.getId());
                        userSearchParams.put("actionId", wholeSaleAction);
                        UserActionDTO userActionDTO = userActionService.findOneUserAction(userSearchParams);
                        if (userActionDTO.getId() != null) {
                            //发站内信
                            NoticeDTO noticeDTO= new NoticeDTO();
                            noticeDTO.setContent(sendMsg);
                            noticeDTO.setTitle(mailTemplateDTO.getEnMailTheme());
                            noticeDTO.setUserId(userDTO1.getId());
                            noticeService.saveNotice(noticeDTO,request);
                            //记录邮件流水
                            userService.saveMailLog(userDTO1.getEmail(),sendMsg,0,request);
                        }
                    }
                }
            }catch (Exception e){
                log.info("MerchantServiceImpl.passMerchant,发送邮件异常"+e.getMessage(),e);
            }
        }
        modelAndView.addObject("status", status);
        modelAndView.addObject("referrerCode", merchantId);
        return modelAndView;
    }

    @Override
    public Map<String, Object> getMerchantListQueryParams(JSONObject data) {
        Map<String, Object> params = new HashMap<>();
        // 必要参数
        params.put("app", "app");
        params.put("isAvailable", StaticDataEnum.MERCHANT_AVAILABLE_1.getCode());
        params.put("practicalName", data.getString("practicalName"));
        // 条件检索
        String mainBusiness = data.getString("mainBusiness");
        if (StringUtils.isNotBlank(mainBusiness)) {
            params.put("mainBusiness", mainBusiness);
        }
        // APP首页需传此参数
        params.put("isTop", data.get("isTop"));
        params.put("deals", data.get("deals"));
        params.put("lat", data.getString("lat"));
        params.put("lng", data.getString("lng"));
        params.put("sortDisCount", data.getInteger("sortDisCount"));
        params.put("sortDistance", data.getInteger("sortDistance"));
        return params;
    }

    @Override
    public List<JSONObject> getMerchantList(JSONObject data, Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws Exception {
        List<JSONObject> resultList = new ArrayList<>();
        List<MerchantDTO> list;
        if (data.get("deals") != null) {
            list = this.appFindList(params, null, null);
        } else if (data.get("isTop") != null) {
            params.clear();
            list = this.topList(params, null, null);
        } else {
            list = this.appFindList(params, scs, pc);
            if (data.getInteger("sortDisCount") != null) {
                Collections.sort(list, (o1, o2) -> {
                    if (data.getInteger("sortDisCount").intValue() == 0) {
                        return o1.getUserDiscount().compareTo(o2.getUserDiscount());
                    } else {
                        return o2.getUserDiscount().compareTo(o1.getUserDiscount());
                    }
                });
            }
            if (data.getInteger("sortDistance") != null) {
                Collections.sort(list, (o1, o2) -> {
                    if (data.getInteger("sortDistance").intValue() == 0) {
                        return o1.getDistance().compareTo(o2.getDistance());
                    } else {
                        return o2.getDistance().compareTo(o1.getDistance());
                    }
                });
            }
        }
        list.forEach(item -> {
            JSONObject merchant = JSONResultHandle.resultHandle(item, MerchantDTO.class);
            resultList.add(merchant);
        });

        return resultList;
    }

    @Override
    public int merchantContractManageCount(Map<String, Object> params) {
        return merchantDAO.merchantContractManageCount(params);
    }

    @Override
    public List<MerchantDetailDTO> merchantContractManage(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantDetailDTO> resultList = merchantDAO.merchantContractManage(params);
        return resultList;
    }

    @Override
    public int countMerchantChangeApprove(Map<String, Object> params) {
        return merchantDAO.countMerchantChangeApprove(params);
    }

    @Override
    public List<MerchantDetailDTO> listMerchantChangeApprove(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantDetailDTO> resultList = merchantDAO.listMerchantChangeApprove(params);
        return resultList;
    }

    @Override
    public Object merchantSearchList(JSONObject data, HttpServletRequest request) throws BizException{
        String keyword = data.getString("keyword");
        if (StringUtils.isBlank(keyword)){
            throw new BizException(I18nUtils.get("merchantList.no.result.found",getLang(request)));
        }
        //完全匹配tag表中的数据,如果匹配上,则popular 字段+1
        tagService.updateTagPopular(keyword,request);
        String lng = data.getString("lng");
        String lat = data.getString("lat");
        //有经纬度信息
        if (StringUtils.isNoneEmpty(new String[]{lng,lat})){

        }else {

        }
        return null;
    }

    @Async("taskExecutor")
    @Override
    public void updateHaveWholeSell(Long merchantId, Integer haveWholeSell, HttpServletRequest request) {
        log.info("更新商户是否还有整体出售额度表示位,商户id:{},更新为:{}",merchantId,haveWholeSell);
        try{
            updateMerchant(merchantId,MerchantDTO.builder().haveWholeSell(haveWholeSell).build(),request);
        }catch (Exception e){
            log.info("更新商户是否还有整体出售额度表示位,商户id:{},更新为:{},异常:{}",merchantId,haveWholeSell,e.getMessage());
        }
    }

    @Override
    public int getMerchantClearMessageCount(Map<String, Object> params) {
        return merchantDAO.getMerchantClearMessageCount(params);
    }

    @Override
    public List<MerchantDTO> merchantClearMessageList(Map<String, Object> params) {
        return merchantDAO.merchantClearMessageList(params);
    }

    @Override
    public List<MerchantDTO> getClearMerchantList(Map<String, Object> params) {
        return merchantDAO.getClearMerchantList(params);
    }

    @Override
    public List<MerchantDTO> getWholeSaleClearMerchantList(Map<String, Object> params) {
        return merchantDAO.getWholeSaleClearMerchantList(params);
    }

    @Override
    public Map<String,Object> getMerchantDetailsById(String merchantId, HttpServletRequest request) throws BizException {
        if (merchantId==null||StringUtils.isBlank(merchantId)){
            throw new BizException(I18nUtils.get("api.merchantId.isNull", getLang(request)));
        }
        Map<String,Object> result=new HashMap<>();
        // 查询店铺信息
        Map<String,Object> param=new HashMap<>();
        param.put("id",merchantId);
        MerchantDTO merchantDTO = merchantDAO.getMerchantInfo(param);
        if (merchantDTO==null||merchantDTO.getId()==null){
            // 店铺不存在
            throw new BizException(I18nUtils.get("merchant.not.found", getLang(request)));
        }
        // 判断店铺是否删除
        Integer status = merchantDTO.getStatus();
        if (status!=null&&status.equals(StaticDataEnum.STATUS_0.getCode())){
            throw new BizException(I18nUtils.get("merchant.is.not.exist", getLang(request)));
        }
        // 判断是否可用
        Integer isAvailable = merchantDTO.getIsAvailable();
        if (isAvailable!=null&&isAvailable==StaticDataEnum.AVAILABLE_0.getCode()){
            throw new BizException(I18nUtils.get("merchant.is.unavailable", getLang(request)));
        }
        result.put("tradingName",merchantDTO.getPracticalName());
        param.clear();
        //获取店铺email
        param.put("id",merchantDTO.getUserId());
        UserDTO userDTO = userDAO.selectOneDTO(param);
        if (userDTO==null){
            result.put("merchantAccount","");
        }else {
            result.put("merchantAccount",userDTO.getEmail());
        }
        param.clear();
        // 根据merchantId查询NFC
        param.put("merchantId",merchantId);
        param.put("state",StaticDataEnum.NFCCODE_STATE_1.getCode());
        List<NfcCodeInfoDTO> nfcCodeInfoDAOList = nfcCodeInfoDAO.selectDTO(param);
        result.put("nfcCodeList",nfcCodeInfoDAOList);
        // 根据merchantId查询QR
        List<QrcodeInfoDTO> qrCodeList = qrcodeInfoDAO.selectDTO(param);
        result.put("qrCodeList",qrCodeList);
        return result;
    }

    @Override
    public JSONObject getMerchantList(Long endTime,String searchKeyword, HttpServletRequest request, PagingContext pc) {
        log.info("模糊查询商户列表,searchKeyword:{}",searchKeyword);
        Map<String,Object> param=new HashMap<>();
        param.put("searchKeyword",searchKeyword);
        param.put("pc",pc);
        param.put("endTime",endTime);
        //按条件查询merchantList
        List<Map<String, Object>> merchantListByPracticalName = merchantDAO.getMerchantListByPracticalName(param);
        // 查询最后一条时间
        Long maxMerchantListByPracticalName = merchantDAO.getMaxMerchantListByPracticalName(param);
        JSONObject result=new JSONObject();
        result.put("merchantList",merchantListByPracticalName);
        // 未查到返回上次时间
        result.put("startTime",maxMerchantListByPracticalName==null?endTime:maxMerchantListByPracticalName);
        return result;
    }

    @Override
    public Boolean bingNfcCode(String merchantId, String nfcCode, HttpServletRequest request) throws BizException {
        log.info("绑定NFC,merchantId:{},nfcCode:{}",merchantId,nfcCode);
        if (merchantId==null||StringUtils.isBlank(merchantId)){
            throw new BizException(I18nUtils.get("api.merchantId.isNull", getLang(request)));
        }
        if (nfcCode==null||StringUtils.isBlank(nfcCode)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        Map<String,Object> param=new HashMap<>();
        // 校验merchantId是否可用
        param.put("isAvailable",StaticDataEnum.AVAILABLE_1.getCode());
        param.put("id",merchantId);
        Merchant merchant = merchantDAO.selectOne(param);
        if (merchant==null||merchant.getId()==null){
            throw new BizException(I18nUtils.get("merchant.is.not.exist", getLang(request)));
        }
        param.clear();
        // 校验nfc是否可用
        param.put("code",nfcCode);
        NfcCodeInfo nfcCodeInfo = nfcCodeInfoDAO.selectOne(param);
        if (nfcCodeInfo==null||nfcCodeInfo.getMerchantId()!=null){
            throw new BizException(I18nUtils.get("nfc.code.not.exist", getLang(request)));
        }
        if (nfcCodeInfo.getState().equals(StaticDataEnum.NFCCODE_STATE_1.getCode())){
            throw new BizException(I18nUtils.get("code.binded", getLang(request)));
        }
        // 绑定NFC
        NfcCodeInfo nfc = new NfcCodeInfo();
        BeanUtils.copyProperties(nfcCodeInfo,nfc);
        NfcCodeInfo nfcInfo = (NfcCodeInfo)this.packModifyBaseProps(nfc,request);
        nfcInfo.setMerchantId(Long.parseLong(merchantId));
        nfcInfo.setState(StaticDataEnum.NFCCODE_STATE_1.getCode());
        nfcInfo.setCorrelationTime(System.currentTimeMillis());
        nfcInfo.setUserId(merchant.getUserId());
        int cnt = nfcCodeInfoDAO.update(nfcInfo);
        if (cnt != 1) {
            log.error("update error, data:{}", nfcInfo);
            throw new BizException(I18nUtils.get("nfc.bind.error", getLang(request)));
        }
        //添加操作记录
        try{
            CodeUpdateLogDTO codeUpdateLogDTO=new CodeUpdateLogDTO();
            codeUpdateLogDTO.setCode(nfcCode);
            codeUpdateLogDTO.setMerchantId(Long.parseLong(merchantId));
            codeUpdateLogDTO.setState(StaticDataEnum.BIND_TYPE_BIND.getCode());
            codeUpdateLogDTO.setType(StaticDataEnum.CODE_TYPE_NFC.getCode());
            codeUpdateLogService.saveCodeUpdateInfo(codeUpdateLogDTO,request);
        }catch (Exception e){
            log.error("记录NFCb绑定记录错误,data:{}",e);
        }
        return true;
    }

    @Override
    public JSONObject getMenchatQrCodeById(Long merchantId, HttpServletRequest request) throws BizException {
        if (merchantId==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        JSONObject result=new JSONObject();
        JSONObject param =new JSONObject(2);
        param.put("merchantId",merchantId);
        param.put("state",StaticDataEnum.QRCODE_STATE_1.getCode());
        List<Map<String, Object>> qrList = qrcodeInfoService.findQrList(param,request);
        result.put("qrList",qrList);
        return result;
    }


    /**
     * 根据关键字模糊匹配 商户列表
     * @param keywords 输入内容
     * @param request
     * @return
     * @throws BizException
     */
    @Override
    public List<MerchantAppHomePageDTO> getMerchantListByKeywords(String keywords, HttpServletRequest request) throws BizException{
        return merchantDAO.getMerchantListByKeywords(null, keywords, null);
    }


    @Override
    public List<MerchantAppHomePageDTO> getBannerMerchantListByKeywords(@NotBlank String categoryType, String keywords, @NotBlank String stateName, HttpServletRequest request) throws BizException{
        if(categoryType.equals(Constant.MERCHANT_CATEGORY_EXCLUSIVE_OFFER_VALUE)){
            //查询该州下的所有商户
            HashMap<String, Object> stateMap = Maps.newHashMapWithExpectedSize(3);
            stateMap.put("code", "merchantState");
            stateMap.put("enName", stateName);
            StaticDataDTO staticStateData = staticDataService.findOneStaticData(stateMap);
            return merchantDAO.getMerchantListByKeywords(null, keywords, staticStateData.getValue());
        }
        return merchantDAO.getMerchantListByKeywords(categoryType, keywords, null);
    }


    /**
     * 商户每日统计
     */
    @Override
    public void dailyReportStatistics(){
        //开始时间  昨天0点
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        Long yesterdayZeroTimeStamp = calendar.getTime().getTime();

        //结束时间  当天0点
        calendar.clear();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Long todayZeroTimeStamp = calendar.getTime().getTime();

        //查询
        //所有签约商户 select count(*) from u_merchant where  `status` = 1 and  merchant_approve_pass_time is not null;


        //生成excel？
    }

    /**
     * 根据ABN查询商户列表
     *
     * @param params
     * @return
     * @throws BizException
     */
    @Override
    public JSONArray getMerchantListByABN(Map<String, Object> params) {
        List<MerchantDTO> list = merchantDAO.getMerchantListByABN(params);
        JSONArray resultArray = new JSONArray(list.size());
        JSONObject merchantInfo = new JSONObject(2);
        for(MerchantDTO merchant: list){
            merchantInfo.clear();
            merchantInfo.put("merchantId", merchant.getId());
            merchantInfo.put("merchantName", merchant.getPracticalName());
            resultArray.add(merchantInfo);
        }
        //todo 分页?
        return resultArray;
    }


    /**
     * 获取商户支付成功通知URL
     * @author zhangzeyuan
     * @date 2021/3/24 15:50
     * @param merchantId
     * @return java.lang.String
     */
    @Override
    public String getMerchantNotifyUrl(Long merchantId){
        return merchantDAO.getMerchantNotifyUrl(merchantId);
    }

    /**
     * 将合同转为纸质合同
     *
     * @param merchantId
     * @author zhangzeyuan
     * @date 2021/3/26 9:58
     */
    @Override
    public void convertToPaperContract(Long merchantId, HttpServletRequest request) throws BizException {
        if(Objects.isNull(merchantId)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        Merchant updateRecord = new Merchant();
        updateRecord.setId(merchantId);
        updateRecord.setDocusignContractType(Constant.CONTRACT_PAPER);
        //审核状态：-1：审核拒绝 0:待审核  1：审核通过 2：审核中 3:变更审核中 4：变更审核拒绝
        updateRecord.setState(1);

        long currentTimeMillis = System.currentTimeMillis();
        updateRecord.setMerchantApprovePassTime(System.currentTimeMillis());
        //是否签署docusign， 0：未签 1：已签署
        updateRecord.setDocusignHasSigned(1);
        updateRecord.setContractStartTime(currentTimeMillis);
        updateRecord.setContractEndTime(currentTimeMillis);
        updateRecord = (Merchant) this.packModifyBaseProps(updateRecord, request);
        merchantDAO.update(updateRecord);
    }

    /**
     * 更新上传文件路径
     *
     * @param merchantDTO
     * @param request
     * @author zhangzeyuan
     * @date 2021/3/26 11:29
     */
    @Override
    public void updateContratFilePath(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        Long id = merchantDTO.getId();
        String docusignFiles = merchantDTO.getDocusignFiles();
        if(Objects.isNull(id) || StringUtils.isBlank(docusignFiles)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        Merchant updateRecord = new Merchant();
        updateRecord.setId(id);
        //拼接json参数
        JSONObject docusignFileJson = new JSONObject();
//        {"":"paper/541812143849394176.pdf","envelopId":""}
        docusignFileJson.put("Merchant Application Form", docusignFiles);
        docusignFileJson.put("envelopId", "paperToElectronic");
        updateRecord.setDocusignFiles(docusignFileJson.toJSONString());
        updateRecord = (Merchant) this.packModifyBaseProps(updateRecord, request);
        merchantDAO.update(updateRecord);
    }

    @Override
    public boolean checkPayDistance(JSONObject data, HttpServletRequest request) throws BizException {
        Long merchantId = data.getLong("merchantId");
        String lat = data.getString("lat");
        String lng = data.getString("lng");
        // 商户经纬度信息不能为空
        if( StringUtils.isEmpty(lng) || StringUtils.isEmpty(lat) || StringUtils.isEmpty(data.getString("merchantId"))){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        Map<String,Object> params = new HashMap<>();
        params.put("id",merchantId);
        MerchantDTO merchantDTO = this.findOneMerchant(params);
        if(merchantDTO == null || merchantDTO .getId() == null || merchantDTO.getIsAvailable() != StaticDataEnum.AVAILABLE_1.getCode()){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        // 对比经纬度，获得用户位置与商户距离，如果大于500米抛错
        GlobalCoordinates source = new GlobalCoordinates(new Double(lat), new Double(lng));
        GlobalCoordinates target = new GlobalCoordinates(new Double(merchantDTO.getLat()), new Double(merchantDTO.getLng()));
        double distance = GeodesyUtil.getDistanceMeter(source, target, Ellipsoid.Sphere);
        if(new BigDecimal(distance).compareTo(new BigDecimal(Constant.PAY_DISTANCE)) > 0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public JSONObject checkPayDistanceV2(JSONObject data, HttpServletRequest request) throws BizException {
        JSONObject result = new JSONObject(4);
        Long merchantId = data.getLong("merchantId");
        String lat = data.getString("lat");
        String lng = data.getString("lng");
        // 商户经纬度信息不能为空
        if( StringUtils.isEmpty(lng) || StringUtils.isEmpty(lat) || StringUtils.isEmpty(data.getString("merchantId"))){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        Map<String,Object> params = new HashMap<>();
        params.put("id",merchantId);
        MerchantDTO merchantDTO = this.findOneMerchant(params);
        if(merchantDTO == null || merchantDTO .getId() == null || merchantDTO.getIsAvailable() != StaticDataEnum.AVAILABLE_1.getCode()){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        // 对比经纬度，获得用户位置与商户距离，如果大于500米抛错
        GlobalCoordinates source = new GlobalCoordinates(new Double(lat), new Double(lng));
        GlobalCoordinates target = new GlobalCoordinates(new Double(merchantDTO.getLat()), new Double(merchantDTO.getLng()));
        double distance = GeodesyUtil.getDistanceMeter(source, target, Ellipsoid.Sphere);
        if(new BigDecimal(distance).compareTo(new BigDecimal(Constant.PAY_DISTANCE)) > 0){
            result.put("distanceState",false) ;
        }else{
            result.put("distanceState",true) ;
        }

        if(request != null){
            params.clear();
            params.put("state", 1);
            params.put("gatewayType", 0);
            GatewayDTO oneGateway = gatewayService.findOneGateway(params);
            if(null == oneGateway || null == oneGateway.getId()){
                throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
            }

            int tempStripeState = 0;

            if(oneGateway.getType().intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                Long userId = getUserId(request);
                UserDTO user = userService.findUserById(userId);
                if(user != null && user.getId() != null){
                    tempStripeState = user.getStripeState();
                }
            }
            result.put("stripeState", tempStripeState);
        }
        return result;
    }

    @Override
    public Object getMerchantListInfo(JSONObject data, HttpServletRequest request) throws BizException {
        if (null == data || data.isEmpty() || null == data.getJSONArray("idList")){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        List<Long> idList = data.getJSONArray("idList").toJavaList(Long.class);
        if (CollectionUtil.isNotEmpty(idList)){
            //将id 集合拼接成 xx,yy,zz 格式 用 id in (---) 查询
            String idListStr = idList.stream().map(Object::toString).collect(Collectors.joining(","));
            List<JSONObject> resList =  merchantDAO.getMerchantListInfo(idListStr);
            JSONObject result = new JSONObject(resList.size());
            for (JSONObject dto : resList) {
                result.put(dto.getString("merchantId"),dto);
            }
            return result;
        }
        return new JSONObject();
    }

    @Override
    public List<MerchantAppHomePageDTO> listCustomStateMerchantDataByIds(String merchantIds) {
        return merchantDAO.listCustomStateMerchantDataByIds(merchantIds);
    }

    @Override
    public void updateMethod(@NonNull Long id, @NonNull MerchantDTO merchantDTO, HttpServletRequest request) throws BizException {
        log.info("full update merchantDTO:{}", merchantDTO);
        Merchant merchant = BeanUtil.copyProperties(merchantDTO, new Merchant());
        merchant.setId(id);
        int cnt = merchantDAO.update((Merchant) this.packModifyBaseProps(merchant, request));
        if (cnt != 1) {
            log.error("update error, data:{}", merchantDTO);
            throw new BizException("update merchantDTO Error!");
        }
    }

    @Override
    public MerchantDTO getOtherMerchantMessage(MerchantDTO merchantDTO) {
        Map<String ,Object> params = new HashMap<>(8);
        params.put("merchantId", merchantDTO.getId());
        if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_1.getCode()) {
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            List<BeneficiaryDTO> beneficiaryDTOList = beneficiaryService.find(params, null, null);
            List<DirectorDTO> directorDTOList = directorService.find(params, null, null);
            merchantDTO.setDirectorDTOList(directorDTOList);
            merchantDTO.setShareholderDTOList(shareholderDTOList);
            merchantDTO.setBeneficiaryDTOList(beneficiaryDTOList);
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_3.getCode()) {
            List<PartnerDTO> partnerDTOList = partnerService.find(params, null, null);
            merchantDTO.setPartnerDTOList(partnerDTOList);
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_4.getCode()) {
            List<DirectorDTO> directorDTOList = directorService.find(params, null, null);
            merchantDTO.setDirectorDTOList(directorDTOList);
            List<TrusteeDTO> trusteeDTOList = trusteeService.find(params, null, null);
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            merchantDTO.setTrusteeDTOList(trusteeDTOList);
            merchantDTO.setShareholderDTOList(shareholderDTOList);
        } else if (merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_2.getCode() || merchantDTO.getEntityType().intValue() == StaticDataEnum.MERCHANT_ENTITY_6.getCode()) {
            List<ShareholderDTO> shareholderDTOList = shareholderService.find(params, null, null);
            merchantDTO.setShareholderDTOList(shareholderDTOList);
        }

        List<ContactPersonDTO> contactPersonDTOList = contactPersonService.find(params, null, null);
        merchantDTO.setContactPersonDTOList(contactPersonDTOList);

//        int appChargeChoice = MathUtils.multiply(merchantDTO.getAppChargeRate(), new BigDecimal("100")).intValue();
//        merchantDTO.setAppChargeChoice(new Integer(appChargeChoice).toString());
//        merchantDTO.setBaseRate(merchantDTO.getBaseRate().multiply(new BigDecimal("100")));
//        merchantDTO.setExtraDiscount(merchantDTO.getExtraDiscount().multiply(new BigDecimal("100")));
//        merchantDTO.setMarketingDiscount(merchantDTO.getMarketingDiscount().multiply(new BigDecimal("100")));


        if (!StringUtils.isEmpty(merchantDTO.getFileList())) {
            List<String> urls = JSONArray.parseArray(merchantDTO.getFileList(), String.class);
            if (urls != null && urls.size() > 0) {
                List<JSONObject> urlFile = new ArrayList<>(urls.size());
                urls.stream().forEach(s -> {
                    JSONObject file = new JSONObject();
                    String a = SnowflakeUtil.getEncryptionId();
                    String[] url = s.split("/");
                    file.put("uid", a);
                    file.put("name", url[url.length - 1]);
                    file.put("url", s);
                    file.put("status", "done");
                    urlFile.add(file);
                });
                merchantDTO.setFiles(urlFile);
            }
        }
        // 信托文件
        if (!StringUtils.isEmpty(merchantDTO.getTrusteeFileList())) {
            List<String> trusteeUrls = JSONArray.parseArray(merchantDTO.getTrusteeFileList(), String.class);
            if (trusteeUrls != null && trusteeUrls.size() > 0) {
                List<JSONObject> trusteeUrlFile = new ArrayList<>(trusteeUrls.size());
                trusteeUrls.stream().forEach(s -> {
                    JSONObject file = new JSONObject();
                    String a = SnowflakeUtil.getEncryptionId();
                    String[] url = s.split("/");
                    file.put("uid", a);
                    file.put("name", url[url.length - 1]);
                    file.put("url", s);
                    file.put("status", "done");
                    trusteeUrlFile.add(file);
                });
                merchantDTO.setTrusteeFiles(trusteeUrlFile);
            }
        }
        if (!StringUtils.isEmpty(merchantDTO.getDocusignFiles())) {
            JSONObject docusignFiles = JSONObject.parseObject(merchantDTO.getDocusignFiles());
            List<JSONObject> docusignFileList = new ArrayList<>(3);
            docusignFiles.keySet().stream().forEach(key -> {
                if (!key.startsWith("envelopId")) {
                    JSONObject file = new JSONObject();
                    file.put("name", key);
                    file.put("url", docusignFiles.getString(key));
                    docusignFileList.add(file);
                }
            });
            merchantDTO.setDocusignFileList(docusignFileList);
        }
//        params.clear();
//        params.put("merchantId", merchantDTO.getId());
//        SecondMerchantGatewayInfoDTO gatewayInfo = secondMerchantGatewayInfoService.findOneSecondMerchantGatewayInfo(params);
//        merchantDTO.setSecondMerchantGatewayInfoDTO(gatewayInfo);
        return merchantDTO;
    }
}
