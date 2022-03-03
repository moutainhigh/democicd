package com.uwallet.pay.main.service;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.EnterKycPageLogDTO;
import com.uwallet.pay.main.model.entity.EnterKycPageLog;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 
 * @author: zhangzeyuan
 * @date: Created in 2021-08-13 15:47:46
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
public interface EnterKycPageLogService extends BaseService {

   /**
    * 保存一条 EnterKycPageLog 数据
    *
    * @param enterKycPageLogDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveEnterKycPageLog(EnterKycPageLogDTO enterKycPageLogDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 EnterKycPageLog 数据
     *
     * @param enterKycPageLogList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveEnterKycPageLogList(List<EnterKycPageLog> enterKycPageLogList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 EnterKycPageLog 数据
     *
     * @param id 数据唯一id
     * @param enterKycPageLogDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateEnterKycPageLog(Long id, EnterKycPageLogDTO enterKycPageLogDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 enterKycPageLog
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateEnterKycPageLogSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 EnterKycPageLog
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteEnterKycPageLog(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 EnterKycPageLog
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteEnterKycPageLog(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 EnterKycPageLog
     *
     * @param id 数据唯一id
     * @return 查询到的 EnterKycPageLog 数据
     */
    EnterKycPageLogDTO findEnterKycPageLogById(Long id);

    /**
     * 根据条件查询得到第一条 enterKycPageLog
     *
     * @param params 查询条件
     * @return 符合条件的一个 enterKycPageLog
     */
    EnterKycPageLogDTO findOneEnterKycPageLog(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<EnterKycPageLogDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     *  新增或者更新
     * @author zhangzeyuan
     * @date 2021/8/13 15:56
     * @param userId
     * @param request
     * @return java.lang.Integer
     */
    Integer upsertLog(Long userId, HttpServletRequest request) throws BizException;

}
