package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.QrcodeInfoDTO;
import com.uwallet.pay.main.model.dto.QrcodeListDTO;
import com.uwallet.pay.main.model.entity.QrcodeInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 二维码信息、绑定
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 二维码信息、绑定
 * @author: baixinyue
 * @date: Created in 2019-12-10 14:39:07
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
public interface QrcodeInfoService extends BaseService {

   /**
    * 保存一条 QrcodeInfo 数据
    *
    * @param qrcodeInfoDTO 待保存的数据
    * @param request
    * @throws BizException 保存失败异常
    */
    void saveQrcodeInfo(QrcodeInfoDTO qrcodeInfoDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 QrcodeInfo 数据
     *
     * @param qrcodeInfoList 待保存的数据列表
     * @throws BizException 保存失败异常
     */
    void saveQrcodeInfoList(List<QrcodeInfo> qrcodeInfoList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 QrcodeInfo 数据
     *
     * @param id 数据唯一id
     * @param qrcodeInfoDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateQrcodeInfo(Long id, QrcodeInfoDTO qrcodeInfoDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 qrcodeInfo
     *
     * @param dataMap 需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateQrcodeInfoSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 QrcodeInfo
     *
     * @param id 数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteQrcodeInfo(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 QrcodeInfo
     *
     * @param id 数据唯一id
     * @throws BizException 物理删除异常
     */
    void deleteQrcodeInfo(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 QrcodeInfo
     *
     * @param id 数据唯一id
     * @return 查询到的 QrcodeInfo 数据
     */
    QrcodeInfoDTO findQrcodeInfoById(Long id);

    /**
     * 根据条件查询得到第一条 qrcodeInfo
     *
     * @param params 查询条件
     * @return 符合条件的一个 qrcodeInfo
     */
    QrcodeInfoDTO findOneQrcodeInfo(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<QrcodeInfoDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * 二维码生成 批量生成
     * @param createNumber
     * @param request
     * @throws Exception
     */
    public void qrCodeCreate(int createNumber, HttpServletRequest request) throws Exception;

    /**
     * 二维码下载
      * @param ids
     * @param request
     * @return
     * @throws Exception
     */
    public List<String> qrCodeDownLoad(Long[] ids, HttpServletRequest request);

    /**
     * 二维码列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<QrcodeListDTO> findQRCodeList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 二维码列表分页
     * @param params
     * @return
     */
    int findQRCodeListCount(Map<String, Object> params);

    /**
     * 返回二维码服务器地址
     * @param userId
     * @return
     */
    String findQRCodePath(Long merchantId, Long timestamp, String oldPath, HttpServletRequest request) throws Exception;

    /**
     * 扫码获取用户信息
     * @param requestInfo
     * @return
     */
    JSONObject findUserInfoByQRCode(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 根据userId查询用户或商户信息
     * @param requestInfo
     * @return
     * @throws Exception
     */
    JSONObject findUserInfoId(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 二维码生成方法
     * @param hopRouting 跳转路由
     * @return
     * @throws Exception
     */
    JSONObject qrCodeCreate(String hopRouting, Long timestamp) throws Exception;

    /**
     * 商户后台绑定二维码
     * @param requestInfo
     * @throws Exception
     */
    void merchantBindingQRCode(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 获取QR绑定信息
     * @param qrCode
     * @param request
     * @return
     */
    JSONObject findMerchantByqrCode(String qrCode,HttpServletRequest request) throws Exception;

    /**
     * 绑定QR码
     * @param qrCode
     * @param merchantId
     * @param request
     * @return
     */
    JSONObject bindQrCode(String qrCode,Long merchantId,HttpServletRequest request) throws BizException;

    /**
     * 解除绑定QR码
     * @param qrCode
     * @param request
     * @return
     */
    JSONObject unbindQrCode(String qrCode,HttpServletRequest request) throws BizException;

   /**
    * 根据店铺ID查询店铺绑定QR码
    * @param params
    * @param httpServletRequest
    * @return
    */
    List<Map<String,Object>> findQrList(Map<String, Object> params,HttpServletRequest httpServletRequest);


    /**
     * 获取商家二维码列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<QrcodeListDTO> listMerchantQrList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);


}
