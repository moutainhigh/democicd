package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.Merchant;
import lombok.NonNull;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 商户信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service
 * @description: 商户信息表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:22:53
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
public interface MerchantService extends BaseService {

    /**
     * 保存一条 Merchant 数据
     *
     * @param merchantDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    Long saveMerchant(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 Merchant 数据
     *
     * @param merchantList 待保存的数据列表
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveMerchantList(List<Merchant> merchantList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 Merchant 数据
     *
     * @param id          数据唯一id
     * @param merchantDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateMerchant(Long id, MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 Merchant 数据
     *
     * @param id          数据唯一id
     * @param merchantDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateMerchanth5(Long id, ApiMerchantDTO merchantDTO, HttpServletRequest request) throws BizException;


    /**
     * 根据Id部分更新实体 merchant
     *
     * @param dataMap      需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateMerchantSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 Merchant
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteMerchant(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 Merchant
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 物理删除异常
     */
    void deleteMerchant(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 Merchant
     *
     * @param id 数据唯一id
     * @return 查询到的 Merchant 数据
     */
    MerchantDTO findMerchantById(Long id);

    /**
     * 根据条件查询得到第一条 merchant
     *
     * @param params 查询条件
     * @return 符合条件的一个 merchant
     */
    MerchantDTO findOneMerchant(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<MerchantDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

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
     * @param group      分组的字段
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的统计条数
     */
    Map<String, Integer> groupCount(String group, Map<String, Object> conditions);

    /**
     * 根据给定字段查询统计字段的sum结果
     *
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 返回sum计算的结果值
     */
    Double sum(String sumField, Map<String, Object> conditions);

    /**
     * 根据给定字段以及查询条件进行分组查询，并sum统计Field
     *
     * @param group      分组的字段。
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的求和
     */
    Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions);

    /**
     * 统计符合条件的商户审核条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countMerchantApprove(Map<String, Object> params);

    /**
     * 根据查询条件得到商户审核列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<MerchantDetailDTO> listMerchantApprove(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 统计符合条件的商户列表条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countMerchant(Map<String, Object> params);
    /**
     * H5统计符合条件的商户列表条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countMerchantH5(Map<String, Object> params);

    /**
     * 根据查询条件得到商户列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<MerchantDetailDTO> listMerchant(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * H5根据查询条件得到商户列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<ApiMerchantDTO> listMerchantH5(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据id查询一条商户审核数据
     *
     * @param params
     * @return 查询到的 商户审核 数据
     */
    MerchantDetailDTO selectMerchantApproveById(Map<String, Object> params);
    /**
     * H5根据id查询一条商户审核数据
     *
     * @param params
     * @return 查询到的 商户审核 数据
     */
    MerchantDetailH5DTO selectMerchantApproveByIdH5(Map<String, Object> params);

    /**
     * 补充商户基础信息
     *
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    void replenishMerchant(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 股东、董事添加
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    void replenishDirectorAndOwner(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 补充商户银行信息
     *
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    void replenishBank(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 补充商户图片信息
     *
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    void replenishLogo(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 提交审核
     *
     * @param userId
     * @param request
     * @throws BizException
     */
    void submitAudit(Long userId, HttpServletRequest request) throws BizException;

    /**
     * 商户审核拒绝
     *
     * @param id
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    void refuseMerchant(Long id, MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 商户审核通过
     *
     * @param merchantDetailDTO
     * @param request
     * @throws BizException
     */
    void passMerchant(MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) throws Exception;

    /**
     * 审核页面费率修改
     * @param merchantDetailDTO
     * @param request
     * @throws BizException
     */
    void reviewUpdateRate(MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) throws BizException;

    /**
     * 通过id查询商户支付渠道信息
     *
     * @param id 商户唯一id
     * @return 查询到的 MerchantDetailDTO 数据
     */
    MerchantDetailDTO route(Long id);

    /**
     * 修改费率
     *
     * @param id
     * @param merchantDetailDTO
     * @param request
     * @throws BizException
     */
    void updateRate(Long id, MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) throws BizException;

    /**
     * 商户变更审批提交审核
     *
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    void submitChange(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条商户变更审批数据
     *
     * @param params
     * @return 查询到的 商户变更审批 数据
     */
    MerchantDetailDTO selectMerchantChange(Map<String, Object> params);

    /**
     * 商户变更审批审核
     *
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    void auditChange(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据userId获取商户信息
     *
     * @param userId
     * @return
     */
    MerchantDTO getMerchantByUserId(Long userId);

    /**
     * 是否推荐
     *
     * @param id
     * @param merchantDTO
     * @param request
     * @throws BizException
     */
    void isTop(Long id, MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 Merchant
     *
     * @param id 数据唯一id
     * @return 查询到的 Merchant 数据
     */
    MerchantDTO selectMerchantById(Long id) throws Exception;

    /**
     * 查询商户列表
     *
     * @param merchantIdList
     * @return
     */
    List<ContactsFileDTO> selectMerchantListByIdList(List<Long> merchantIdList);

    /**
     * 导出商户详细信息成Excel文件
     * @param id
     * @param request
     * @return
     */
    HSSFWorkbook exportMerchantExcel(Long id, HttpServletRequest request);

    /**
     * 修改一条 Merchant 数据
     *
     * @param id          数据唯一id
     * @param merchantDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateDetail(Long id, MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    List<MerchantDTO> findMerchantLogInList(Map<String, Object> params);

    /**
     * 获取商户列表
     * @param data
     * @param request
     * @return
     */
    JSONObject getMerchantLoginList(JSONObject data, HttpServletRequest request);

    /**
     * 全部商户列表
     * @return
     */
    List<MerchantDTO> merchantList();

    void setDiscountRate(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;

    /**
     * app商户发现列表分页计数
     * @param params
     * @return
     */
    int countAppFindList(Map<String, Object> params);

    /**
     * app商户发现列表
     * @param params
     * @return
     */
    List<MerchantDTO> appFindList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws Exception;

    /**
     * 商户推荐修改
     * @param id
     * @param merchantDTO
     * @param request
     */
    void merchantTopChange(Long id, MerchantDTO merchantDTO, HttpServletRequest request) throws Exception;

    /**
     * 推荐排序列表页分页计数
     * @return
     */
    int topCount();

    /**
     * 推荐列表分页
     * @param params
     * @return
     */
    List<MerchantDTO> topList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws Exception;

    /**
     * 上移或下移
     * @param id
     * @param upOrDown
     * @throws BizException
     */
    void shiftUpOrDown(Long id, Integer upOrDown, HttpServletRequest request) throws BizException;

    /**
     * 官网列表分页总数
     * @param params
     * @return
     */
    int listOfWebSiteCount(Map<String, Object> params);

    /**
     * 官网列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<MerchantDTO> listOfWebSite(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws Exception;

    /**
     * docusign 返回合同连接
     * @param merchantId
     * @return
     * @throws Exception
     */
    JSONObject docusignContract(Long merchantId, Integer contractType, HttpServletRequest request) throws Exception;

    /**
     * docusign callback
     * @param request
     * @return
     */
    ModelAndView docusignCallBack(String docusignEnvelopeid, HttpServletRequest request) throws Exception;

    /**
     * 获得商户查询条件
     * @param data
     * @return
     */
    Map<String, Object> getMerchantListQueryParams(JSONObject data);

    /**
     * 获得商户查询结果
     *
     * @param data
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<JSONObject> getMerchantList(JSONObject data, Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) throws Exception;

    /**
     * 商户合同管理分页
     * @param params
     * @return
     */
    int merchantContractManageCount(Map<String, Object> params);

    /**
     * 商户合同管理
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<MerchantDetailDTO> merchantContractManage(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);


    /**
     * 查询商户变更审核条数
     * @param params
     * @return
     */
    int countMerchantChangeApprove(Map<String, Object> params);

    /**
     * 查询商户变更审核列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<MerchantDetailDTO> listMerchantChangeApprove(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 商户列表页搜索按钮,返回商户列表
     * @param data
     * @param request
     * @return
     */
    Object merchantSearchList(JSONObject data, HttpServletRequest request)  throws BizException;

    /**
     * 更新商户, have_whole_sell 字段
     *  当 <整体出售审批通过时> 更新为 : 1
     *  当 <出现混合销售/普通出售时> 更新为 : 0
     * @param merchantId 商户id
     * @param haveWholeSell 表示位 1:有, 0:无
     * @param request
     * @return
     */
    void updateHaveWholeSell(@NonNull Long merchantId,@NonNull Integer haveWholeSell, HttpServletRequest request);

    /**
     * 查询可清算的商户个数
     * @return
     */
    int getMerchantClearMessageCount(Map<String, Object> params);

    /**
     * 查询可清算的商户
     * @param params
     * @return
     */
    List<MerchantDTO> merchantClearMessageList(Map<String, Object> params);

    /**
     * 获取正常出售待清算商户
     * @param params
     * @return
     */
    List<MerchantDTO> getClearMerchantList(Map<String, Object> params);

    /**
     * 查询整体出售待清算商户
     * @param params
     * @return
     */
    List<MerchantDTO> getWholeSaleClearMerchantList(Map<String, Object> params);

    /**
     * 根据merchantId获取店铺QR与NFC绑定信息
     * @param merchantId
     * @param request
     * @return
     */
    Map<String,Object> getMerchantDetailsById(String merchantId,HttpServletRequest request) throws BizException;


    /**
     * 根据店铺名称模糊查询
     * @param endTime
     * @param searchKeyword
     * @param request
     * @param pc
     * @return
     */
    JSONObject getMerchantList(Long endTime,String searchKeyword,HttpServletRequest request, PagingContext pc);

    /**绑定Nfc
     * @param merchantId
     * @param nfcCode
     * @param request
     * @return
     */
    Boolean bingNfcCode(String merchantId,String nfcCode,HttpServletRequest request) throws BizException;

    /**
     * 获取商户绑定QR码信息
     * @param merchantId
     * @param request
     * @return
     */
    JSONObject getMenchatQrCodeById(Long merchantId,HttpServletRequest request) throws BizException;


    /**
     * 根据关键字模糊匹配 商户列表
     * @param keywords 输入内容
     * @param request
     * @return
     * @throws BizException
     */
    List<MerchantAppHomePageDTO> getMerchantListByKeywords(String keywords, HttpServletRequest request) throws BizException;


    /**
     * app 首页自定义分类  根据关键字模糊匹配 商户列表
     * @param keywords 输入内容
     * @param request
     * @return
     * @throws BizException
     */
    List<MerchantAppHomePageDTO> getBannerMerchantListByKeywords(String categoryType, String keywords, String stateName,HttpServletRequest request) throws BizException;

    /**
     * 每日商户报表统计
     */
    void dailyReportStatistics();


    /**
     *  根据ABN查询商户列表
     * @param params
     * @return
     * @throws BizException
     */
    JSONArray getMerchantListByABN(Map<String, Object> params);

    /**
     * 获取商户支付成功通知URL
     * @author zhangzeyuan
     * @date 2021/3/24 15:50
     * @param merchantId
     * @return java.lang.String
     */
    String getMerchantNotifyUrl(Long merchantId);



    /**
     * 将合同转为纸质合同
     * @author zhangzeyuan
     * @date 2021/3/26 9:58
     * @param merchantId
     */
    void convertToPaperContract(Long merchantId, HttpServletRequest request) throws BizException;


    /**
     *  更新上传文件路径
     * @author zhangzeyuan
     * @date 2021/3/26 11:29
     */
    void updateContratFilePath(MerchantDTO merchantDTO, HttpServletRequest request) throws BizException;


    /**
     * 判断是否在商家附近支付
     * @param data
     * @param request
     */
    boolean checkPayDistance(JSONObject data, HttpServletRequest request) throws BizException;


    /**
     * 判断是否在商家附近支付v2
     * @param data
     * @param request
     */
    JSONObject checkPayDistanceV2(JSONObject data, HttpServletRequest request) throws BizException;

    /**
     * 通过商户id集合获取商户商户信息
     * @param data
     * @param request
     * @return
     * @throws BizException
     */
    Object getMerchantListInfo(JSONObject data, HttpServletRequest request) throws BizException;


    List<MerchantAppHomePageDTO> listCustomStateMerchantDataByIds(String merchantIds) ;

    /**
     * 原生更新方法
     * @param id
     * @param merchantDTO
     * @param request
     */
    void updateMethod(@NonNull Long id, @NonNull MerchantDTO merchantDTO, HttpServletRequest request) throws  BizException;

    /**
     * 查询商户其他信息
     * @param merchantDTO
     * @return
     */
    MerchantDTO getOtherMerchantMessage(MerchantDTO merchantDTO);


    /**
     * 修改H5商户信息
     * @param merchantDTO
     * @param request
     */
    void replenishH5DirectorAndOwner(ApiMerchantDTO merchantDTO, HttpServletRequest request) throws BizException;
}
