package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.NoticeDTO;
import com.uwallet.pay.main.model.entity.Notice;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.exception.BizException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 消息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 消息表
 * @author: baixinyue
 * @date: Created in 2019-12-11 16:54:08
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
public interface NoticeService extends BaseService {

   /**
    * 保存一条 Notice 数据
    *
    * @param noticeDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveNotice(NoticeDTO noticeDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 Notice 数据
     *
     * @param noticeList 待保存的数据列表
     * @throws BizException 保存失败异常
     */
    void saveNoticeList(List<Notice> noticeList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 Notice 数据
     *
     * @param id 数据唯一id
     * @param noticeDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateNotice(Long id, NoticeDTO noticeDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 notice
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateNoticeSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 Notice
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteNotice(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 Notice
     *
     * @param id 数据唯一id
     * @throws BizException 物理删除异常
     */
    void deleteNotice(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 Notice
     *
     * @param id 数据唯一id
     * @return 查询到的 Notice 数据
     */
    NoticeDTO findNoticeById(Long id);

    /**
     * 根据条件查询得到第一条 notice
     *
     * @param params 查询条件
     * @return 符合条件的一个 notice
     */
    NoticeDTO findOneNotice(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<NoticeDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params  查询条件
     * @param columns 需要查询的字段信息
     * @param scs     排序信息
     * @param pc      分页信息
     * @return 查询结果的数据集合
     * @throws BizException 查询异常
     */
    List<Map> findMap(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException;

    /**
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int count(Map<String, Object> params);

    /**
     * 根据给定字段以及查询条件进行分组查询，并统计id的count
     *
     * @param group 分组的字段
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的统计条数
     */
    Map<String, Integer> groupCount(String group, Map<String, Object> conditions);

    /**
     * 根据给定字段查询统计字段的sum结果
     *
     * @param sumField sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 返回sum计算的结果值
     */
    Double sum(String sumField, Map<String, Object> conditions);

    /**
     * 根据给定字段以及查询条件进行分组查询，并sum统计Field
     *
     * @param group 分组的字段。
     * @param sumField sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的求和
     */
    Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions);

    /**
     * 是否全部已读
     * @param userId
     * @return
     */
    boolean allNoticeHasRead(Long userId);

    /**
     * 修改全部已读
     * @param userId
     */
    void allNoticeRead(Long userId, HttpServletRequest request);

    /**
     * 获取未读状态和条数
     * @param userId
     * @return
     */
    JSONObject getAllNoticeHasRead(Long userId);

 /**
  * 首页一键清除所有通知信息
  * @param userId
  * @param request
  */
 void noticeClearAll(Long userId, HttpServletRequest request);

 /**
  * 保存批量站内信
  * @param noticeList
  * @throws BizException
  */
 void saveNoticeListNew(List<Notice> noticeList) throws BizException;

}
