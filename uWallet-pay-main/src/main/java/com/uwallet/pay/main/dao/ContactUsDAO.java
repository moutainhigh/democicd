package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ContactUsDTO;
import com.uwallet.pay.main.model.entity.ContactUs;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 联系我们
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 联系我们
 * @author: baixinyue
 * @date: Created in 2020-06-17 08:52:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface ContactUsDAO extends BaseDAO<ContactUs> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ContactUsDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ContactUsDTO
     * @param params
     * @return
     */
    ContactUsDTO selectOneDTO(Map<String, Object> params);

}
