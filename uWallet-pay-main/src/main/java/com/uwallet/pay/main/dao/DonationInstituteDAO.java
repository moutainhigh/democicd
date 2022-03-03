package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.DonationInstituteDTO;
import com.uwallet.pay.main.model.dto.DonationInstituteDataDTO;
import com.uwallet.pay.main.model.entity.DonationInstitute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 捐赠机构
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 捐赠机构
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:38:12
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface DonationInstituteDAO extends BaseDAO<DonationInstitute> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<DonationInstituteDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 DonationInstituteDTO
     * @param params
     * @return
     */
    DonationInstituteDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据id查询一条捐赠记录
     * @author zhangzeyuan
     * @date 2021/7/22 10:18
     * @param donationInstituteId
     * @return com.uwallet.pay.main.model.dto.DonationInstituteDTO
     */
    DonationInstituteDataDTO  getDonationDataById(@Param("id")String donationInstituteId);

}
