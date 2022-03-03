package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.AdsDAO;
import com.uwallet.pay.main.model.dto.AdsDTO;
import com.uwallet.pay.main.model.entity.Ads;
import com.uwallet.pay.main.service.AdsService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 广告表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 广告表
 * @author: Strong
 * @date: Created in 2020-01-11 09:45:03
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: Strong
 */
@Service
@Slf4j
public class AdsServiceImpl extends BaseServiceImpl implements AdsService {

    @Autowired
    private AdsDAO adsDAO;

    @Override
    public void saveAds(@NonNull AdsDTO adsDTO, HttpServletRequest request) throws BizException {
        if (adsDTO.getState() == null) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (adsDTO.getState() == 1) {
            Map<String, Object> params = new HashMap<>(1);
            params.put("state", adsDTO.getState());
            AdsDTO ad = adsDAO.selectOneDTO(params);
            if (ad != null) {
                throw new BizException(I18nUtils.get("advertisement.exist", getLang(request)));
            }
        }
        Ads ads = BeanUtil.copyProperties(adsDTO, new Ads());
        log.info("save Ads:{}", ads);
        if (adsDAO.insert((Ads) this.packAddBaseProps(ads, request)) != 1) {
            log.error("insert error, data:{}", ads);
            throw new BizException("Insert ads Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdsList(@NonNull List<Ads> adsList, HttpServletRequest request) throws BizException {
        if (adsList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = adsDAO.insertList(adsList);
        if (rows != adsList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, adsList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAds(@NonNull Long id, @NonNull AdsDTO adsDTO, HttpServletRequest request) throws BizException {
        if (adsDTO.getState() == null) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (adsDTO.getState() == 1) {
            Map<String, Object> params = new HashMap<>(1);
            params.put("state", 1);
            params.put("id", id);
            AdsDTO ad = adsDAO.selectOneByIdDTO(params);
            if (ad != null) {
                throw new BizException(I18nUtils.get("advertisement.exist", getLang(request)));
            }
        }
        log.info("full update adsDTO:{}", adsDTO);
        Ads ads = BeanUtil.copyProperties(adsDTO, new Ads());
        ads.setId(id);
        int cnt = adsDAO.update((Ads) this.packModifyBaseProps(ads, request));
        if (cnt != 1) {
            log.error("update error, data:{}", adsDTO);
            throw new BizException("update ads Error!");
        }
    }

    @Override
    public void updateAdsSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        adsDAO.updatex(params);
    }

    @Override
    public void logicDeleteAds(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = adsDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAds(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = adsDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AdsDTO findAdsById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AdsDTO adsDTO = adsDAO.selectOneDTO(params);
        return adsDTO;
    }

    @Override
    public List<AdsDTO> appFindOneAds(@NonNull Map<String, Object> params) {
        log.info("find params:{}", params);
        List<AdsDTO> adsDTO = adsDAO.appFindOneAds(params);
        return adsDTO;
    }

    @Override
    public AdsDTO findOneAds(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Ads ads = adsDAO.selectOne(params);
        AdsDTO adsDTO = new AdsDTO();
        if (null != ads) {
            BeanUtils.copyProperties(ads, adsDTO);
        }
        return adsDTO;
    }

    @Override
    public List<AdsDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AdsDTO> resultList = adsDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return adsDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return adsDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = adsDAO.groupCount(conditions);
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
        return adsDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = adsDAO.groupSum(conditions);
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

}
