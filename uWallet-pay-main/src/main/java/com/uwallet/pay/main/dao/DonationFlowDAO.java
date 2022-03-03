package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.DonationFlowClearDTO;
import com.uwallet.pay.main.model.dto.DonationFlowDTO;
import com.uwallet.pay.main.model.dto.DonationUserListDTO;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.entity.DonationFlow;
import com.uwallet.pay.main.model.entity.QrPayFlow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 捐赠流水
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 捐赠流水
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:37:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface DonationFlowDAO extends BaseDAO<DonationFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<DonationFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 DonationFlowDTO
     * @param params
     * @return
     */
    DonationFlowDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询用户捐赠订单列表
     * @param params
     * @return
     */
    List<JSONObject> selectDTOList(JSONObject params);

    /**
     * 根据id 修改捐赠订单结算状态
     * @param param
     * @return
     */
    int updateSettlementState(JSONObject param);

    /**
     * 导出导出明细
     * @param param
     * @return
     */
    List<JSONObject> exportUserOrder(DonationFlowClearDTO param);

    /**
     * 查询总数,总金额
     * @param param
     * @return
     */
    List<JSONObject> findTotal(DonationFlowClearDTO param);

    /**
     * 根据flowId 更新 state状态
     * @author zhangzeyuan
     * @date 2021/7/23 15:16
     * @param params
     * @return int
     */
    int updateStateByFlowId(Map<String, Object> params);

    /**
     * 捐赠用户列表
     * @param params
     * @return
     */
    List<DonationUserListDTO> getUserDonationList(Map<String, Object> params);
    /**
     * 捐赠用户个数
     * @param params
     * @return
     */
    int getUserDonationListCount(Map<String, Object> params);

    /**
     * 查询捐赠数据
     * @param params
     * @return
     */
    List<DonationFlowDTO> getDonationDataList(Map<String, Object> params);

    /**
     * 根据用户id 状态查询列表总数
     * @param params
     * @return
     */
    int countOrderByUserId(Map<String, Object> params);

    /**
     * 清算
     * @param donationFlowClearDTO
     * @return
     */
    int clear(JSONObject donationFlowClearDTO);


    /**
     * 获取捐赠订单明细
     * @author zhangzeyuan
     * @date 2021/7/27 9:25
     * @param batchId
     * @return java.util.List<com.uwallet.pay.main.model.dto.DonationUserListDTO>
     */
    List<QrPayFlowDTO> getClearBatchList(Long batchId);


    /**
     * 捐赠订单 结算失败
     * @author zhangzeyuan
     * @date 2021/7/28 14:21
     * @param params
     * @return int
     */
    int updateSettlementRollback(Map<String, Object> params);

}
