package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.DonationInstituteConfigDTO;
import com.uwallet.pay.main.model.entity.DonationInstituteConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 捐赠机构配置
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 捐赠机构配置
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:38:26
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface DonationInstituteConfigDAO extends BaseDAO<DonationInstituteConfig> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<DonationInstituteConfigDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 DonationInstituteConfigDTO
     * @param params
     * @return
     */
    DonationInstituteConfigDTO selectOneDTO(Map<String, Object> params);

}
