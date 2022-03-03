package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ClearBillCSVDTO;
import com.uwallet.pay.main.model.dto.ClearDetailDTO;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.entity.ClearDetail;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 清算表生成
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 清算表生成
 * @author: zhoutt
 * @date: Created in 2019-12-20 10:53:36
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface ClearDetailDAO extends BaseDAO<ClearDetail> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ClearDetailDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ClearDetailDTO
     * @param params
     * @return
     */
    ClearDetailDTO selectOneDTO(Map<String, Object> params);


    List<ClearDetailDTO> getClearBatch(Map<String, Object> params);

    /**
     * 查询清算异常交易列表
     * @return
     */
    List<ClearDetailDTO> getClearDoubt(int state);


    List<ClearBillCSVDTO> getClearBillList(Long clearBatchId);

    List<ClearDetailDTO> getClearABAList(Long clearBatchId);

    List<ClearDetailDTO> getClearBatchNew(Map<String, Object> map);

    ClearDetailDTO clearTotal(Map<String, Object> map);

    List<ClearDetailDTO> geApiPlatformClearData(Map<String, Object> updateMap);

    void clearData(Map<String, Object> map);

    List<ClearBillCSVDTO> getApiPltClearBillList(Long id);

    List<ClearDetailDTO> selectApiPltClearBatchBorrow(Map<String, Object> params);

    int getClearedDetailListCount(Map<String, Object> params);

    List<ClearDetailDTO> getClearedDetailList(Map<String, Object> params);

    /**
     * 整体出售结算成功
     * @param id
     */
    int dealWholeSaleClearSuccess(Long id);

    /**
     * 整体出售结算失败
     * @param id
     */
    int dealWholeSaleClearFail(Long id);

    List<ClearDetail> getDonationClearBatch(Map<String, Object> params);


    int updateStateByBatchId(Map<String, Object> params);

    /**
     * H5商户清算数据
     * @param params
     * @return
     */
    List<ClearDetailDTO> getH5MerchantClearDetail(Map<String, Object> params);

    /**
     * 查询H5商户清算数据
     * @param params
     * @return
     */
    List<ClearDetailDTO> selectH5MerchantClearData(Map<String, Object> params);

    /**
     * 生成H5商户清算文件
     * @param id
     * @return
     */
    List<ClearBillCSVDTO> getH5ClearBillList(Long id);

    /**
     * 查询h5商户清算条数
     * @param id
     * @return
     */
    List<QrPayFlowDTO> h5ClearCount(Long id);
}
