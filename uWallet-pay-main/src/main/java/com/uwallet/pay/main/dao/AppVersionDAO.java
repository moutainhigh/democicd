package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.AppVersionDTO;
import com.uwallet.pay.main.model.entity.AppVersion;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * APP版本号管理表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: APP版本号管理表
 * @author: aaron.S
 * @date: Created in 2020-12-02 14:07:13
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: aaron.S
 */
@Mapper
public interface AppVersionDAO extends BaseDAO<AppVersion> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AppVersionDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AppVersionDTO
     * @param params
     * @return
     */
    AppVersionDTO selectOneDTO(Map<String, Object> params);

    /**
     * 当前版本与商店最新版本之间 有没有需要强更的版本 count
     * @param param
     * @return
     */
    Integer countNeedUpdate(JSONObject param);
    /**
     * 当前版本与商店最新版本之间 有没有需要强更的版本 count v2
     * @param param
     * @return
     */
    Integer countNeedUpdateV2(JSONObject param);
}
