package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.QrcodeInfoDTO;
import com.uwallet.pay.main.model.dto.QrcodeListDTO;
import com.uwallet.pay.main.model.entity.QrcodeInfo;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 二维码信息、绑定
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 二维码信息、绑定
 * @author: baixinyue
 * @date: Created in 2019-12-10 14:39:07
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface QrcodeInfoDAO extends BaseDAO<QrcodeInfo> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<QrcodeInfoDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 QrcodeInfoDTO
     * @param params
     * @return
     */
    QrcodeInfoDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据id集合将所有二维码服务器路径查出
     * @param ids
     * @return
     */
    List<String> findCodePath(Long[] ids);

    /**
     * 二维码列表
     * @param params
     * @return
     */
    List<QrcodeListDTO> findQRCodeList(Map<String, Object> params);

    /**
     * 二维码列表分页
     * @param params
     * @return
     */
    int findQRCodeListCount(Map<String, Object> params);

    /**
     * 扫码获取用户信息
     * @param code
     * @return
     */
    QrcodeListDTO findUserInfoByQRCode(String code);

    /**
     * 解除QR绑定
     * @param qrcodeInfo
     * @return
     */
    int removeBindQrCodeInfo(QrcodeInfo qrcodeInfo);

    /**
     * 根据店铺ID查询店铺绑定QR码
     * @param params
     * @return
     */
    List<Map<String,Object>> findQrList(Map<String, Object> params);


    /**
     * 获取商户二维码列表
     * @param params
     * @return
     */
    List<QrcodeListDTO> listMerchantQrList(Map<String, Object> params);
}
