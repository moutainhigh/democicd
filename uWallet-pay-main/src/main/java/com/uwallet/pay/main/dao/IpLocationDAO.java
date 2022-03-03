package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.IpLocationDTO;
import com.uwallet.pay.main.model.entity.IpLocation;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * ip定位
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: ip定位
 * @author: baixinyue
 * @date: Created in 2021-01-12 13:54:55
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface IpLocationDAO extends BaseDAO<IpLocation> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<IpLocationDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 IpLocationDTO
     * @param params
     * @return
     */
    IpLocationDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询ip所属区域
     * @param ipNum
     * @return
     */
    IpLocationDTO selectIpLocationByIp(Long ipNum);

}
