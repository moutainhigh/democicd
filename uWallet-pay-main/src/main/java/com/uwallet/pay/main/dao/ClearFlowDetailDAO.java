package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ClearFlowDetailDTO;
import com.uwallet.pay.main.model.dto.PayBorrowDTO;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户主表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户主表
 * @author: zhoutt
 * @date: Created in 2020-02-13 12:00:23
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface ClearFlowDetailDAO extends BaseDAO<ClearFlowDetail> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ClearFlowDetailDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ClearFlowDetailDTO
     * @param params
     * @return
     */
    ClearFlowDetailDTO selectOneDTO(Map<String, Object> params);

    List<ClearFlowDetail> getDataByBatchId(Long id);

    BigDecimal findAmountInAmount(Map<String, Object> map);

    int clearData(Map<String, Object> map);

    List<ClearFlowDetailDTO> findAmountOutAmount(Map<String, Object> map);

    int updateAmountOut(Map<String, Object> map);

    List<PayBorrowDTO> selectBatchBorrow(Map<String, Object> params);

    ClearFlowDetailDTO clearTotal(Map<String, Object> map);

    int selectBatchBorrowCount(Map<String, Object> params);

    int updateClearBatchToFail(Map<String, Object> map);

    List<ClearFlowDetail> getApiPltClearDataByBatchId(Long id);

    /**
     * api服务清算明细查询
     * @param params
     * @return
     */
    List<PayBorrowDTO> selectApiPltClearBatchBorrow(Map<String, Object> params);

    /**
     * 查询清算流水
     * @param id
     * @return
     */
    List<ClearFlowDetail> getDataByBatchIdNew(Long id);

    /**
     * 处理整体出售结算结果
     * @param id
     * @return
     */
    int dealWholeSaleClear(Long id);

    /**
     * 查询捐赠清算明细
     * @param id
     * @return
     */
    List<ClearFlowDetail> getDonationDataByBatchId(Long id);


    /**
     * 根据批次号更新状态
     * @author zhangzeyuan
     * @date 2021/8/12 11:10
     * @param params
     * @return int
     */
    int updateStateByBatchId(Map<String, Object> params);
}
