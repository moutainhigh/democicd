package com.uwallet.pay.main.dao;


import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.PosQrPayFlowDTO;
import com.uwallet.pay.main.model.dto.PosTransactionRecordDTO;
import com.uwallet.pay.main.model.entity.PosQrPayFlow;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @package: com.fenmi.generator.mapper
 * @description:
 * @author: zhangzeyuan
 * @date: Created in 2021-03-22 15:46:35
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface PosQrPayFlowDAO extends BaseDAO<PosQrPayFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    List<PosQrPayFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 PosQrPayFlowDTO
     *
     * @param params
     * @return
     */
    PosQrPayFlowDTO selectOneDTO(Map<String, Object> params);


    /**
     *  获取POS订单历史信息
     * @author zhangzeyuan
     * @date 2021/3/23 13:55
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.PosQrPayFlowDTO>
     */
    List<PosTransactionRecordDTO> listPosTransaction(Map<String, Object> params);

    /**
     *  获取POS历史订单信息
     * @author zhangzeyuan
     * @date 2021/3/23 13:58
     * @param params
     * @return java.lang.Integer
     */
    Integer countPosTransaction(Map<String, Object> params);



    /**
     * 根据三方系统订单号 更新系统订单号
     * @author zhangzeyuan
     * @date 2021/3/30 9:05
     * @param sysNo
     * @param thirdNo
     * @param userId
     * @param modifyTime
     */
    void updateSysTransNoByThirdTransNo(String sysNo, String thirdNo, Long userId, Long modifyTime);


    /**
     * 根据系统订单号更新流水状态
     * @author zhangzeyuan
     * @date 2021/3/30 10:32
     * @param sysTransNo
     * @param orderStatus
     * @param userId
     * @param modifyTime
     */
    int updateOrderStatusBySysTransNo(String sysTransNo, Integer orderStatus, Long userId, Long modifyTime);

}
