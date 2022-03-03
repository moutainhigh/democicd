package com.uwallet.pay.main.service;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.ReconciliationDTO;
import com.uwallet.pay.main.model.dto.ReconciliationDetailDTO;
import com.uwallet.pay.main.model.entity.Reconciliation;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 对账
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.service
 * @description: 对账
 * @author: baixinyue
 * @date: Created in 2020-02-17 09:59:08
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
public interface ReconciliationService extends BaseService {

   /**
    * 保存一条 Reconciliation 数据
    *
    * @param reconciliationDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveReconciliation(ReconciliationDTO reconciliationDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 Reconciliation 数据
     *
     * @param reconciliationList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveReconciliationList(List<Reconciliation> reconciliationList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 Reconciliation 数据
     *
     * @param id 数据唯一id
     * @param reconciliationDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateReconciliation(Long id, ReconciliationDTO reconciliationDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 reconciliation
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateReconciliationSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 Reconciliation
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteReconciliation(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 Reconciliation
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteReconciliation(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 Reconciliation
     *
     * @param id 数据唯一id
     * @return 查询到的 Reconciliation 数据
     */
    ReconciliationDTO findReconciliationById(Long id);

    /**
     * 根据条件查询得到第一条 reconciliation
     *
     * @param params 查询条件
     * @return 符合条件的一个 reconciliation
     */
    ReconciliationDTO findOneReconciliation(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<ReconciliationDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 导入对账文件
     * @param type
     * @param file
     * @param request
     * @throws Exception
     */
    void importReconciliation(Integer type, MultipartFile file, HttpServletRequest request) throws Exception;

    /**
     * 跑批对账
     */
    int accountChecking(List<Reconciliation> reconciliationList) throws Exception;

    int accountCheckingStripe(List<Reconciliation> reconciliationList) throws Exception;

    /**
     * 统计对账详情列表数量
     * @param params
     * @return
     */
    int countReconciliationDetail(Map<String, Object> params);

    /**
     * 查询对账详情列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<ReconciliationDetailDTO> findReconciliationDetail(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

}
