package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.NfcCodeInfoDTO;
import com.uwallet.pay.main.model.entity.NfcCodeInfo;
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
 * NFC信息、绑定表
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: NFC信息、绑定表
 * @author: zhoutt
 * @date: Created in 2020-03-23 14:31:21
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
public interface NfcCodeInfoService extends BaseService {

   /**
    * 保存一条 NfcCodeInfo 数据
    *
    * @param nfcCodeInfoDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveNfcCodeInfo(NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 NfcCodeInfo 数据
     *
     * @param nfcCodeInfoList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveNfcCodeInfoList(List<NfcCodeInfo> nfcCodeInfoList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 NfcCodeInfo 数据
     *
     * @param id 数据唯一id
     * @param nfcCodeInfoDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateNfcCodeInfo(Long id, NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 nfcCodeInfo
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateNfcCodeInfoSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 NfcCodeInfo
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteNfcCodeInfo(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 NfcCodeInfo
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteNfcCodeInfo(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 NfcCodeInfo
     *
     * @param id 数据唯一id
     * @return 查询到的 NfcCodeInfo 数据
     */
    NfcCodeInfoDTO findNfcCodeInfoById(Long id);

    /**
     * 根据条件查询得到第一条 nfcCodeInfo
     *
     * @param params 查询条件
     * @return 符合条件的一个 nfcCodeInfo
     */
    NfcCodeInfoDTO findOneNfcCodeInfo(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<NfcCodeInfoDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 列表查询
     * @param params
     * @param scs
     * @param pc
     * @return
     */
     List<NfcCodeInfoDTO> findList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

     /**
      * 绑定校验
      * @param id
      * @param nfcCodeInfoDTO
      * @param request
      * @return
      * @throws BizException
      */
     NfcCodeInfoDTO checkMerchant(Long id, NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request) throws BizException;

    /**
     * 获取解绑参数
     * @param id
     * @param request
     * @return
     */
    NfcCodeInfoDTO getRemoveBind(Long merchantId,String code,Long id, HttpServletRequest request) throws BizException;

    /**
     * 解绑
     * @param id
     * @param nfcCodeInfoDTO
     * @param request
     * @return
     */
    int removeBindNfcCodeInfo(Long id, NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request)  throws BizException;


     /**
      * nfc导入
      * @param multipartFile
      * @param request
      * @throws Exception
      */
     void importNfc(MultipartFile multipartFile, HttpServletRequest request) throws Exception;

    /**
     * NFC码查询商户信息
     * @param data
     * @return
     */
     JSONObject findUserInfoByNFCCode(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * NFC码查询绑定信息
     * @param nfcCode
     * @param request
     * @return
     */
     Map<String,Object> findMerchantByNfcCode(String nfcCode,HttpServletRequest request) throws BizException;

 /**
  * 解绑NFC码
  * @param code
  * @param request
  * @return
  */
     Boolean unBindNfcCode(String code, HttpServletRequest request) throws BizException;
}
