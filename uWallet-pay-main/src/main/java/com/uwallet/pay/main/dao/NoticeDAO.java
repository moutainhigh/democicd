package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.NoticeDTO;
import com.uwallet.pay.main.model.entity.Notice;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 消息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 消息表
 * @author: baixinyue
 * @date: Created in 2019-12-11 16:54:08
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface NoticeDAO extends BaseDAO<Notice> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<NoticeDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 NoticeDTO
     * @param params
     * @return
     */
    NoticeDTO selectOneDTO(Map<String, Object> params);

    /**
     * 首页一键清除所有通知信息
     * @param userId
     */
    void noticeClearAll(@Param("userId") Long userId);
}
