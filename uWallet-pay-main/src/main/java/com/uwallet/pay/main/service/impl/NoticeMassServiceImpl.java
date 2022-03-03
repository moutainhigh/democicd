package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.GeodesyUtil;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.NoticeMassDAO;
import com.uwallet.pay.main.dao.UserDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.NoticeMass;
import com.uwallet.pay.main.model.entity.User;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.FireBaseUtil;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.MailUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 群发消息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 群发消息表
 * @author: baixinyue
 * @date: Created in 2020-02-21 08:50:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class NoticeMassServiceImpl extends BaseServiceImpl implements NoticeMassService {

    @Autowired
    private NoticeMassDAO noticeMassDAO;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private AliyunSmsService aliyunSmsService;

    @Autowired
    private NoticeService noticeService;

    @Resource
    private UserService userService;

    @Value("${uWallet.sysEmail}")
    private String sysEmail;

    @Value("${uWallet.sysEmailPwd}")
    private String sysEmailPwd;

    @Autowired
    RedisUtils redisUtils;

    @Override
    public void saveNoticeMass(@NonNull NoticeMassDTO noticeMassDTO, HttpServletRequest request) throws BizException {
        NoticeMass noticeMass = BeanUtil.copyProperties(noticeMassDTO, new NoticeMass());
        noticeMass = (NoticeMass) this.packAddBaseProps(noticeMass, request);
        log.info("save NoticeMass:{}", noticeMass);
        long diff = noticeMass.getSendTime() - noticeMass.getCreatedDate();
        if (diff <= 0) {
            throw new BizException(I18nUtils.get("time.later", getLang(request)));
        }
        if (noticeMassDAO.insert(noticeMass) != 1) {
            log.error("insert error, data:{}", noticeMass);
            throw new BizException("Insert noticeMass Error!");
        }
        //放入redis
        redisUtils.set(StaticDataEnum.NOTICE_PREFIX.getMessage() + noticeMass.getId(), noticeMass.getId(), diff/1000);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNoticeMassList(@NonNull List<NoticeMass> noticeMassList, HttpServletRequest request) throws BizException {
        if (noticeMassList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = noticeMassDAO.insertList(noticeMassList);
        if (rows != noticeMassList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, noticeMassList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateNoticeMass(@NonNull Long id, @NonNull NoticeMassDTO noticeMassDTO, HttpServletRequest request) throws BizException {
        log.info("full update noticeMassDTO:{}", noticeMassDTO);
        NoticeMass noticeMass = BeanUtil.copyProperties(noticeMassDTO, new NoticeMass());
        noticeMass.setId(id);
        if (request != null) {
            noticeMass = (NoticeMass) this.packModifyBaseProps(noticeMass, request);
        } else {
            noticeMass.setModifiedDate(System.currentTimeMillis());
        }
        if (redisUtils.hasKey(StaticDataEnum.NOTICE_PREFIX.getMessage() + noticeMass.getId())) {
            long diff = noticeMass.getSendTime() - noticeMass.getModifiedDate();
            if (diff <= 0) {
                throw new BizException(I18nUtils.get("time.later", getLang(request)));
            }
            int cnt = noticeMassDAO.update(noticeMass);
            if (cnt != 1) {
                log.error("update error, data:{}", noticeMassDTO);
                throw new BizException("update noticeMass Error!");
            }
            //放入redis
            redisUtils.set(StaticDataEnum.NOTICE_PREFIX.getMessage() + noticeMass.getId(), noticeMass.getId(), diff/1000);
        }
    }

    @Override
    public void updateNoticeMassSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        noticeMassDAO.updatex(params);
    }

    @Override
    public void logicDeleteNoticeMass(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = noticeMassDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteNoticeMass(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = noticeMassDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public NoticeMassDTO findNoticeMassById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        NoticeMassDTO noticeMassDTO = noticeMassDAO.selectOneDTO(params);
        return noticeMassDTO;
    }

    @Override
    public NoticeMassDTO findOneNoticeMass(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        NoticeMass noticeMass = noticeMassDAO.selectOne(params);
        NoticeMassDTO noticeMassDTO = new NoticeMassDTO();
        if (null != noticeMass) {
            BeanUtils.copyProperties(noticeMass, noticeMassDTO);
        }
        return noticeMassDTO;
    }

    @Override
    public List<NoticeMassDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("sendState", StaticDataEnum.STATUS_0.getCode());
        params = getUnionParams(params, scs, pc);
        List<NoticeMassDTO> resultList = noticeMassDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return noticeMassDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        params.put("sendState", StaticDataEnum.STATUS_0.getCode());
        return noticeMassDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = noticeMassDAO.groupCount(conditions);
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
        return noticeMassDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = noticeMassDAO.groupSum(conditions);
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
    public void sendMessage(Long id) throws Exception {
        NoticeMassDTO noticeMassDTO = findNoticeMassById(id);
        MerchantDTO locating = merchantService.findMerchantById(noticeMassDTO.getLocating());
        //发送方式
        String[] sendModes = noticeMassDTO.getSendMode().split(",");
        String sendMsg = noticeMassDTO.getContent();
        //获取账户用户id
        JSONObject accountSearchUserParams = new JSONObject();
        accountSearchUserParams.put("ageMin", noticeMassDTO.getAgeMin());
        accountSearchUserParams.put("ageMax", noticeMassDTO.getAgeMax());
        accountSearchUserParams.put("sex", noticeMassDTO.getSex());
        JSONArray accountUserIds = serverService.sendNoticeUser(accountSearchUserParams);
        List<Long> accountIds = (List<Long>) JSONArray.parse(accountUserIds.toJSONString());
        //获取支付用户id
        Map<String, Object> params = new HashMap<>(1);
        params.put("userType", StaticDataEnum.USER_TYPE_10.getCode());
        List<UserDTO> allUser = userService.find(params, null, null);
        List<Long> payIds = new ArrayList<>(1);
        //循环用户，并计算距离
        allUser.stream().forEach(userDTO -> {
            GlobalCoordinates source = new GlobalCoordinates(new Double(locating.getLat()), new Double(locating.getLng()));
            GlobalCoordinates target = new GlobalCoordinates(new Double(userDTO.getLat()), new Double(userDTO.getLng()));
            double distance = GeodesyUtil.getDistanceMeter(source, target, Ellipsoid.Sphere);
            log.info("user distance:{}, range:{}", distance, noticeMassDTO.getRange().doubleValue());
            if ((distance/1000) < noticeMassDTO.getRange().doubleValue()) {
                payIds.add(userDTO.getId());
            }
        });
        //取出两个用户id交集，交集为最终筛选出的用户结果
        payIds.retainAll(accountIds);
        if (payIds != null && !payIds.isEmpty()) {
            //发送信息
            payIds.stream().forEach(userId -> {
                UserDTO userDTO = userService.findUserById(userId);
                for (String sendMode : sendModes) {
                    sendMessage(userDTO, userDTO.getEmail(), userDTO.getPhone(), new Integer(sendMode), noticeMassDTO.getTitle(), sendMsg);
                }
            });
        } else {
            log.info("no user to send notice mass, data:{}", payIds);
        }
        //修改信息状态
        noticeMassDTO.setSendState(StaticDataEnum.STATUS_1.getCode());
        updateNoticeMass(noticeMassDTO.getId(), noticeMassDTO, null);
    }

    @Async("taskExecutor")
    public void sendMessage(UserDTO userDTO, String email, String phone, Integer sendMode, String title, String sendMsg) {
        //根据发送方式站内信、短信、邮箱、push
        if (sendMode.intValue() == StaticDataEnum.NOTICE_SEND_MODE_0.getCode()) {
            NoticeDTO noticeDTO = new NoticeDTO();
            noticeDTO.setUserId(userDTO.getId());
            noticeDTO.setTitle(title);
            noticeDTO.setContent(sendMsg);
            try {
                noticeService.saveNotice(noticeDTO, null);
            } catch (BizException e) {
                log.error("send message by notice failed, info:{}, error msg:{}", sendMsg, e.getMessage());
            }
        } else if (sendMode.intValue() == StaticDataEnum.NOTICE_SEND_MODE_1.getCode()) {
            try {
                String phoneCode  = new StringBuilder(phone).substring(0, 2);
                if (phoneCode.equals(StaticDataEnum.AU.getMessage())) {
                    aliyunSmsService.sendInternationalSms(phone, sendMsg);
                }
            } catch (BizException e) {
                log.error("send message by phone fail, phone:{}, send message:{}, error message:{}", phone, sendMsg, e.getMessage());
            }
        } else if (sendMode.intValue() == StaticDataEnum.NOTICE_SEND_MODE_2.getCode()) {
            try {
                if (!StringUtils.isEmpty(email)) {
                    Session session = MailUtil.getSession(sysEmail);
                    MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, email, title, sendMsg , null, session);
                    MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);
                }
            } catch (Exception e) {
                log.error("send message by email failed, info:{}, error msg:{}", sendMsg, e.getMessage());
            }
        } else {
            if (!StringUtils.isEmpty(userDTO.getPushToken())) {
                FirebaseDTO firebaseDTO = new FirebaseDTO();
                firebaseDTO.setToken(userDTO.getPushToken());
                firebaseDTO.setAppName("UWallet");
                firebaseDTO.setBody(sendMsg);
                firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
                try {
                    serverService.pushFirebase(firebaseDTO,null);
                } catch (Exception e) {
                    log.error("send message by push failed, info:{}, error msg:{}", sendMsg, e.getMessage());
                }
            }
        }
    }
}
