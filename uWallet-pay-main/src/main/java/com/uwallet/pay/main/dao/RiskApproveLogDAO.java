package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.InvestApproveDTO;
import com.uwallet.pay.main.model.dto.RiskApproveLogDTO;
import com.uwallet.pay.main.model.entity.RiskApproveLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户风控审核日志
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户风控审核日志
 * @author: baixinyue
 * @date: Created in 2020-03-25 10:11:54
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface RiskApproveLogDAO extends BaseDAO<RiskApproveLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RiskApproveLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 RiskApproveLogDTO
     * @param params
     * @return
     */
    RiskApproveLogDTO selectOneDTO(Map<String, Object> params);

    /**
     * 审核列表
     * @param params
     * @return
     */
    List<InvestApproveDTO> approveList(Map<String, Object> params);

    /**
     * 审核列表分页条数
     * @param params
     * @return
     */
    int approveListCount(Map<String, Object> params);

    /**
     * 审核记录
     * @param params
     * @return
     */
    List<InvestApproveDTO> approveLogList(Map<String, Object> params);

    /**
     * 审核记录列表页
     * @param params
     * @return
     */
    int approveLogCount(Map<String, Object> params);

}
