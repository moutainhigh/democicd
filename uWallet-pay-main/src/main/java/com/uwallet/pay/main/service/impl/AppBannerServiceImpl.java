package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.AppBannerDAO;
import com.uwallet.pay.main.model.dto.AppBannerDTO;
import com.uwallet.pay.main.model.entity.AppBanner;
import com.uwallet.pay.main.service.AppBannerService;
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
 * app banner
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: app banner
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:29:08
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class AppBannerServiceImpl extends BaseServiceImpl implements AppBannerService {

    @Autowired
    private AppBannerDAO appBannerDAO;

    @Override
    public void saveAppBanner(@NonNull AppBannerDTO appBannerDTO, HttpServletRequest request) throws BizException {
        // 先查询上架的banner数量是否够5个
        Map<String, Object> params = new HashMap<>(1);
        params.put("state", 1);
        if (count(params) == StaticDataEnum.APP_BANNER_UPPER_LIMIT.getCode()) {
            throw new BizException(I18nUtils.get("banner.enough", getLang(request)));
        }
        // 查询最大排序数
        Integer maxSort = appBannerDAO.selectMaxSort();
        if (maxSort != null) {
            appBannerDTO.setSort(maxSort += 1);
        } else {
            appBannerDTO.setSort(1);
        }

        AppBanner appBanner = BeanUtil.copyProperties(appBannerDTO, new AppBanner());
        log.info("save AppBanner:{}", appBanner);
        if (appBannerDAO.insert((AppBanner) this.packAddBaseProps(appBanner, request)) != 1) {
            log.error("insert error, data:{}", appBanner);
            throw new BizException("Insert appBanner Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAppBannerList(@NonNull List<AppBanner> appBannerList, HttpServletRequest request) throws BizException {
        if (appBannerList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = appBannerDAO.insertList(appBannerList);
        if (rows != appBannerList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, appBannerList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAppBanner(@NonNull Long id, @NonNull AppBannerDTO appBannerDTO, HttpServletRequest request) throws BizException {
        // 判断当前时上架还是下架操作
        if(appBannerDTO.getRack() != null && appBannerDTO.getRack().intValue() == StaticDataEnum.STATUS_1.getCode()) {
            // 先查询上架的banner数量是否够5个
            Map<String, Object> params = new HashMap<>(1);
            params.put("state", 1);
            if (count(params) == StaticDataEnum.APP_BANNER_UPPER_LIMIT.getCode()) {
                throw new BizException(I18nUtils.get("banner.enough", getLang(request)));
            }
        }

        log.info("full update appBannerDTO:{}", appBannerDTO);
        AppBanner appBanner = BeanUtil.copyProperties(appBannerDTO, new AppBanner());
        appBanner.setId(id);
        int cnt = appBannerDAO.update((AppBanner) this.packModifyBaseProps(appBanner, request));
        if (cnt != 1) {
            log.error("update error, data:{}", appBannerDTO);
            throw new BizException("update appBanner Error!");
        }
    }

    @Override
    public void updateAppBannerSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        appBannerDAO.updatex(params);
    }

    @Override
    public void logicDeleteAppBanner(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = appBannerDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAppBanner(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = appBannerDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AppBannerDTO findAppBannerById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AppBannerDTO appBannerDTO = appBannerDAO.selectOneDTO(params);
        return appBannerDTO;
    }

    @Override
    public AppBannerDTO findOneAppBanner(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AppBanner appBanner = appBannerDAO.selectOne(params);
        AppBannerDTO appBannerDTO = new AppBannerDTO();
        if (null != appBanner) {
            BeanUtils.copyProperties(appBanner, appBannerDTO);
        }
        return appBannerDTO;
    }

    @Override
    public List<AppBannerDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AppBannerDTO> resultList = appBannerDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return appBannerDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return appBannerDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = appBannerDAO.groupCount(conditions);
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
        return appBannerDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = appBannerDAO.groupSum(conditions);
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
    public void shiftUpOrDown(Long id, Integer upOrDown, HttpServletRequest request) throws BizException {
        log.info("移动操作");
        AppBannerDTO original = findAppBannerById(id);
        AppBannerDTO passivity = appBannerDAO.shiftUpOrDown(id, upOrDown);

        if (passivity == null) {
            log.info("移动操作失败");
            throw new BizException(I18nUtils.get("move.failed=", getLang(request)));
        }

        Integer originalSort = original.getSort();
        original.setSort(passivity.getSort());
        passivity.setSort(originalSort);

        updateAppBanner(id, original, request);
        updateAppBanner(passivity.getId(), passivity, request);

    }

    @Override
    public List<AppBannerDTO> getBannerImg() {
        return appBannerDAO.getBannerImg();
    }

}
