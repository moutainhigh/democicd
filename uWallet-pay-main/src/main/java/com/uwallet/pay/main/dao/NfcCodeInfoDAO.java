package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.NfcCodeInfoDTO;
import com.uwallet.pay.main.model.entity.NfcCodeInfo;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * NFC信息、绑定表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: NFC信息、绑定表
 * @author: zhoutt
 * @date: Created in 2020-03-23 14:31:21
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface NfcCodeInfoDAO extends BaseDAO<NfcCodeInfo> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<NfcCodeInfoDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 NfcCodeInfoDTO
     * @param params
     * @return
     */
    NfcCodeInfoDTO selectOneDTO(Map<String, Object> params);

    List<NfcCodeInfoDTO> findList(Map<String, Object> params);


    /**
     * 解除NFC绑定
     * @param packModifyBaseProps
     * @return
     */
    int removeBindNfcCodeInfo(NfcCodeInfo packModifyBaseProps);
}
