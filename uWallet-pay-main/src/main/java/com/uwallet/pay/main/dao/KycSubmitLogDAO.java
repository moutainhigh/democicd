package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.KycSubmitLogDTO;
import com.uwallet.pay.main.model.dto.UserDetailRepaymentDTO;
import com.uwallet.pay.main.model.entity.KycSubmitLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description:
 * @author: xucl
 * @date: Created in 2021-04-08 13:24:29
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface KycSubmitLogDAO extends BaseDAO<KycSubmitLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<KycSubmitLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 KycSubmitLogDTO
     * @param params
     * @return
     */
    KycSubmitLogDTO selectOneDTO(Map<String, Object> params);

    /**
     * 新修改kyc记录
     * @param params
     * @return
     */
    int updateNew(Map<String, Object> params);

    /**
     * 根据条件查询不符合数据
     * @param params
     * @return
     */
    List<KycSubmitLogDTO> selectNoInDTO(Map<String, Object> params);

    /**
     * 新增一条记录
     * @param kycSubmitLog
     * @return
     */
    int insertNew(KycSubmitLog kycSubmitLog);

    /**
     * 修改kyc记录
     * @param kycSubmitLog
     * @return
     */
    int updateNewTow(KycSubmitLog kycSubmitLog);

    /**
     * 查询最近一次提交记录
     * @param findParam
     * @return
     */
    KycSubmitLogDTO findLatelyLog(JSONObject findParam);
}
