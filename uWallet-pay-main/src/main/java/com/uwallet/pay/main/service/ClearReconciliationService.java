package com.uwallet.pay.main.service;

import com.uwallet.pay.main.model.dto.ClearReconciliationDTO;
import com.uwallet.pay.main.model.entity.ClearReconciliation;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.exception.BizException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 清算对账表
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 清算对账表
 * @author: baixinyue
 * @date: Created in 2020-03-06 09:00:14
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
public interface ClearReconciliationService extends BaseService {

   /**
    * 保存一条 ClearReconciliation 数据
    *
    * @param clearReconciliationDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveClearReconciliation(ClearReconciliationDTO clearReconciliationDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 ClearReconciliation 数据
     *
     * @param clearReconciliationList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveClearReconciliationList(List<ClearReconciliation> clearReconciliationList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 ClearReconciliation 数据
     *
     * @param id 数据唯一id
     * @param clearReconciliationDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateClearReconciliation(Long id, ClearReconciliationDTO clearReconciliationDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 clearReconciliation
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateClearReconciliationSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 ClearReconciliation
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteClearReconciliation(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 ClearReconciliation
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteClearReconciliation(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 ClearReconciliation
     *
     * @param id 数据唯一id
     * @return 查询到的 ClearReconciliation 数据
     */
    ClearReconciliationDTO findClearReconciliationById(Long id);

    /**
     * 根据条件查询得到第一条 clearReconciliation
     *
     * @param params 查询条件
     * @return 符合条件的一个 clearReconciliation
     */
    ClearReconciliationDTO findOneClearReconciliation(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<ClearReconciliationDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 导入清算文件
     * @param file
     * @param request
     * @throws Exception
     */
    void importClearFile(Integer type, MultipartFile file, HttpServletRequest request) throws Exception;

}
