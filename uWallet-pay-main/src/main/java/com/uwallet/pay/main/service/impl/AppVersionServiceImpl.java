package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.model.dto.BaseDTO;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.VersionEnum;
import com.uwallet.pay.main.dao.AppVersionDAO;
import com.uwallet.pay.main.model.dto.AppVersionDTO;
import com.uwallet.pay.main.model.dto.UserDTO;
import com.uwallet.pay.main.model.entity.AppVersion;
import com.uwallet.pay.main.service.AppVersionService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.service.UserService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * APP版本号管理表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: APP版本号管理表
 * @author: aaron.S
 * @date: Created in 2020-12-02 14:07:13
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: aaron.S
 */
@Service
@Slf4j
public class AppVersionServiceImpl extends BaseServiceImpl implements AppVersionService {

    @Resource
    private AppVersionDAO appVersionDAO;

    @Resource
    private UserService userService;

    @Override
    public void saveAppVersion(@NonNull AppVersionDTO appVersionDTO, HttpServletRequest request) throws BizException {
        AppVersion appVersion = BeanUtil.copyProperties(appVersionDTO, new AppVersion());
        log.info("save AppVersion:{}", appVersion);
        if (appVersionDAO.insert((AppVersion) this.packAddBaseProps(appVersion, request)) != 1) {
            log.error("insert error, data:{}", appVersion);
            throw new BizException("Insert appVersion Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAppVersionList(@NonNull List<AppVersion> appVersionList, HttpServletRequest request) throws BizException {
        if (appVersionList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = appVersionDAO.insertList(appVersionList);
        if (rows != appVersionList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, appVersionList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAppVersion(@NonNull Long id, @NonNull AppVersionDTO appVersionDTO, HttpServletRequest request) throws BizException {
        log.info("full update appVersionDTO:{}", appVersionDTO);
        AppVersion appVersion = BeanUtil.copyProperties(appVersionDTO, new AppVersion());
        appVersion.setId(id);
        int cnt = appVersionDAO.update((AppVersion) this.packModifyBaseProps(appVersion, request));
        if (cnt != 1) {
            log.error("update error, data:{}", appVersionDTO);
            throw new BizException("update appVersion Error!");
        }
    }

    @Override
    public void updateAppVersionSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        appVersionDAO.updatex(params);
    }

    @Override
    public void logicDeleteAppVersion(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = appVersionDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAppVersion(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = appVersionDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AppVersionDTO findAppVersionById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AppVersionDTO appVersionDTO = appVersionDAO.selectOneDTO(params);
        return appVersionDTO;
    }

    @Override
    public AppVersionDTO findOneAppVersion(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AppVersion appVersion = appVersionDAO.selectOne(params);
        AppVersionDTO appVersionDTO = new AppVersionDTO();
        if (null != appVersion) {
            BeanUtils.copyProperties(appVersion, appVersionDTO);
        }
        return appVersionDTO;
    }

    @Override
    public List<AppVersionDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AppVersionDTO> resultList = appVersionDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return appVersionDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return appVersionDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = appVersionDAO.groupCount(conditions);
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
        return appVersionDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = appVersionDAO.groupSum(conditions);
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
    public JSONObject appVersionVerify(JSONObject jsonObject, HttpServletRequest request) throws BizException {
        Long currentVersionId = jsonObject.getLong("currentVersionId");
        if(currentVersionId == 51032604326030998L){
            currentVersionId = 522614113821085695L;
        }
        String newVersionNo = jsonObject.getString("newVersionNo");
        if (StringUtils.isBlank(newVersionNo) || null == currentVersionId){
            throw new BizException(I18nUtils.get("parameters.null",getLang(request)));
        }
        AppVersionDTO versionDTO = findAppVersionById(currentVersionId);
        if (null == versionDTO || null == versionDTO.getId()){
            throw new BizException(I18nUtils.get("app.version.not.found",getLang(request)));
        }
        //包装查询参数,通过 设备类型,商店类型,上架国家,强制更新的版本,版本比现在有版本高的版本信息
        JSONObject param = this.packVersionParam(versionDTO,newVersionNo);
        //获取商店最新版本的信息
        AppVersionDTO newVersionInfo = findOneAppVersion(param);
        JSONObject result = new JSONObject(2);

        if (null == newVersionInfo || null == newVersionInfo.getId()){
            throw new BizException(I18nUtils.get("app.version.not.found",getLang(request)));
        }

        param.put("newVersionId",newVersionInfo.getId());
        /* 逻辑:
             如果当前运行版本(不包含) 到 商店最新版本(包含) 之间(包含最新版本) 有任何需要强制更新的版本
             则返回强制更新标识符
         */
        Integer count = appVersionDAO.countNeedUpdate(param);
        result.put("needUpdate",count > 0 ? VersionEnum.NEED_UPDATE.getCode() : VersionEnum.NOT_NEED_UPDATE.getCode());
        return result;
    }

    @Override
    public JSONObject appVersionVerifyNewV3(JSONObject jsonObject, HttpServletRequest request) throws BizException {
        Long currentVersionId = jsonObject.getLong("currentVersionId");
        String newVersionNo = jsonObject.getString("newVersionNo");
        if (null == currentVersionId){
            throw new BizException(I18nUtils.get("parameters.null",getLang(request)));
        }

        String jwt = request.getHeader("Authorization");
        if(StringUtils.isNotBlank(jwt)){
            //新版本 记录版本号 机型
            String phoneModel = jsonObject.getString("phoneModel");
            // 手机系统
            Long userId = getUserId(request);
            UserDTO updateUser = new UserDTO();
            if(jsonObject.getInteger("phoneSystem")!=null){
                updateUser.setPhoneSystem(jsonObject.getInteger("phoneSystem"));
            }

            if (currentVersionId!=null){
                updateUser.setAppVersionId(currentVersionId.toString());
                // 手机系统版本
                if(StringUtils.isNotBlank(jsonObject.getString("phoneSystemVersion"))){
                    updateUser.setPhoneSystemVersion(jsonObject.getString("phoneSystemVersion"));
                }
                // 手机型号
                if(StringUtils.isNotBlank(jsonObject.getString("mobileModel"))){
                    updateUser.setMobileModel(jsonObject.getString("mobileModel"));
                }
                if(StringUtils.isNotBlank(phoneModel)){
                    updateUser.setPhoneModel(phoneModel);
                }
                try{
                    userService.updateUser(userId, updateUser, request);
                }catch (Exception e){
                    log.error("获取APP是否更新接口 更新用户信息出错，userId:{}, e:{}", userId, e.getMessage());
                }
            }
        }

        if(currentVersionId == 51032604326030998L){
            currentVersionId = 522614113821085695L;
        }
        AppVersionDTO versionDTO = findAppVersionById(currentVersionId);
        if (null == versionDTO || null == versionDTO.getId()){
            throw new BizException(I18nUtils.get("app.version.not.found",getLang(request)));
        }
        //2021年2月10号添加逻辑, 添加 manualForceUpdate字段,当上架后,库中修改该字段=1, 可人工触发强制更新字段
        Integer currentVersionManualForceUpdate = versionDTO.getManualForceUpdate();
        if (null != currentVersionManualForceUpdate && currentVersionManualForceUpdate == VersionEnum.MANUAL_FORCE_UPDATE_NEED.getCode()) {
            //如果当前版本 manualForceUpdate 字段为1,强制更新, 不为1, 则返回无需任何操作
            return this.packReturnRes(VersionEnum.NEED_UPDATE.getCode());
        }
        if (StringUtils.isNotBlank(newVersionNo)){
            //有当前版本号信息
            String version = versionDTO.getVersion();

            //如果版本号一致,返回不需要强制更新
            if (version.equals(newVersionNo)){
                return this.packReturnRes(VersionEnum.DO_NOTHING.getCode());
            }
            //包装查询参数,通过 设备类型,商店类型,上架国家,强制更新的版本,版本比现在有版本高的版本信息
            JSONObject param = this.packVersionParam(versionDTO,newVersionNo);
            //获取商店最新版本的信息
            AppVersionDTO newVersionInfo = findOneAppVersion(param);
            if (null == newVersionInfo || null == newVersionInfo.getId()){
                throw new BizException(I18nUtils.get("app.version.not.found",getLang(request)));
            }
            //通过比较库表数据ID, 雪花算法生成的id, 新版本的id肯定比旧的大
            if (currentVersionId > newVersionInfo.getId()){
                return this.packReturnRes(VersionEnum.DO_NOTHING.getCode());
            }
            //2021年2月10号添加逻辑, 添加 manualForceUpdate字段,当上架后,库中修改该字段=1, 可人工触发强制更新字段
            Integer manualForceUpdate = newVersionInfo.getManualForceUpdate();
            if (null != manualForceUpdate && manualForceUpdate == (VersionEnum.MANUAL_FORCE_UPDATE_NEED.getCode())){
                return this.packReturnRes(VersionEnum.NEED_UPDATE.getCode());
            }
            /* 逻辑:
                 如果当前运行版本(不包含) 到 商店最新版本(包含) 之间(包含最新版本) 有任何需要强制更新的版本
                 则返回强制更新标识符
             */
            param.put("newVersionId",newVersionInfo.getId());
            param.put("needUpdate",VersionEnum.NEED_UPDATE.getCode());
            Integer forceUpdateCount = appVersionDAO.countNeedUpdateV2(param);
            if (forceUpdateCount > 0 ){
                return this.packReturnRes(VersionEnum.NEED_UPDATE.getCode());
            }else {
                param.put("needUpdate",VersionEnum.NOT_NEED_UPDATE.getCode());
                Integer notForceUpdateCount = appVersionDAO.countNeedUpdateV2(param);
                if (notForceUpdateCount > 0){
                    return this.packReturnRes(VersionEnum.NOT_NEED_UPDATE.getCode());
                }
            }
        }
        return this.packReturnRes(VersionEnum.DO_NOTHING.getCode());
    }


    @Override
    public JSONObject appVersionVerifyNewV2(JSONObject jsonObject, HttpServletRequest request) throws BizException {
        Long currentVersionId = jsonObject.getLong("currentVersionId");
        String newVersionNo = jsonObject.getString("newVersionNo");
        if (null == currentVersionId){
            throw new BizException(I18nUtils.get("parameters.null",getLang(request)));
        }
        if(currentVersionId == 51032604326030998L){
            currentVersionId = 522614113821085695L;
        }
        AppVersionDTO versionDTO = findAppVersionById(currentVersionId);
        if (null == versionDTO || null == versionDTO.getId()){
            throw new BizException(I18nUtils.get("app.version.not.found",getLang(request)));
        }
        //2021年2月10号添加逻辑, 添加 manualForceUpdate字段,当上架后,库中修改该字段=1, 可人工触发强制更新字段
        Integer currentVersionManualForceUpdate = versionDTO.getManualForceUpdate();
        if (null != currentVersionManualForceUpdate && currentVersionManualForceUpdate == VersionEnum.MANUAL_FORCE_UPDATE_NEED.getCode()) {
            //如果当前版本 manualForceUpdate 字段为1,强制更新, 不为1, 则返回无需任何操作
            return this.packReturnRes(VersionEnum.NEED_UPDATE.getCode());
        }
        if (StringUtils.isNotBlank(newVersionNo)){
            //有当前版本号信息
            String version = versionDTO.getVersion();

            //如果版本号一致,返回不需要强制更新
            if (version.equals(newVersionNo)){
                return this.packReturnRes(VersionEnum.DO_NOTHING.getCode());
            }
            //包装查询参数,通过 设备类型,商店类型,上架国家,强制更新的版本,版本比现在有版本高的版本信息
            JSONObject param = this.packVersionParam(versionDTO,newVersionNo);
            //获取商店最新版本的信息
            AppVersionDTO newVersionInfo = findOneAppVersion(param);
            if (null == newVersionInfo || null == newVersionInfo.getId()){
                throw new BizException(I18nUtils.get("app.version.not.found",getLang(request)));
            }
            //通过比较库表数据ID, 雪花算法生成的id, 新版本的id肯定比旧的大
            if (currentVersionId > newVersionInfo.getId()){
                return this.packReturnRes(VersionEnum.DO_NOTHING.getCode());
            }
            //2021年2月10号添加逻辑, 添加 manualForceUpdate字段,当上架后,库中修改该字段=1, 可人工触发强制更新字段
            Integer manualForceUpdate = newVersionInfo.getManualForceUpdate();
            if (null != manualForceUpdate && manualForceUpdate == (VersionEnum.MANUAL_FORCE_UPDATE_NEED.getCode())){
                return this.packReturnRes(VersionEnum.NEED_UPDATE.getCode());
            }
            /* 逻辑:
                 如果当前运行版本(不包含) 到 商店最新版本(包含) 之间(包含最新版本) 有任何需要强制更新的版本
                 则返回强制更新标识符
             */
            param.put("newVersionId",newVersionInfo.getId());
            param.put("needUpdate",VersionEnum.NEED_UPDATE.getCode());
            Integer forceUpdateCount = appVersionDAO.countNeedUpdateV2(param);
            if (forceUpdateCount > 0 ){
                return this.packReturnRes(VersionEnum.NEED_UPDATE.getCode());
            }else {
                param.put("needUpdate",VersionEnum.NOT_NEED_UPDATE.getCode());
                Integer notForceUpdateCount = appVersionDAO.countNeedUpdateV2(param);
                if (notForceUpdateCount > 0){
                    return this.packReturnRes(VersionEnum.NOT_NEED_UPDATE.getCode());
                }
            }
        }
        return this.packReturnRes(VersionEnum.DO_NOTHING.getCode());
    }


    private JSONObject packReturnRes( int code) {
        JSONObject result = new JSONObject(2);
        result.put("needUpdate",code);
        return result;
    }

    private JSONObject packVersionParam(AppVersionDTO versionDTO,String newVersionNo) {
        JSONObject param = new JSONObject(7);
        param.put("deviceType",versionDTO.getDeviceType());
        param.put("appType",versionDTO.getAppType());
        param.put("storeCountry",versionDTO.getStoreCountry());
        param.put("storeType",versionDTO.getStoreType());
        param.put("currentVersionId",versionDTO.getId());
        param.put("newVersionNo",newVersionNo);
        return param;
    }

}
