package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ChannelFeeConfigDTO;
import com.uwallet.pay.main.model.entity.ChannelFeeConfig;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 通道手续费配置表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 通道手续费配置表
 * @author: zhoutt
 * @date: Created in 2021-03-08 10:12:26
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface ChannelFeeConfigDAO extends BaseDAO<ChannelFeeConfig> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ChannelFeeConfigDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ChannelFeeConfigDTO
     * @param params
     * @return
     */
    ChannelFeeConfigDTO selectOneDTO(Map<String, Object> params);

}
