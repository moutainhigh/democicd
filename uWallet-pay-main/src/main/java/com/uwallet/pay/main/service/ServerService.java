package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.*;
import lombok.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author baixinyue
 * @description 调用外部系统业务层
 * @createDate 2019/12/16
 */
public interface ServerService {

    /**
     * 支付用户信息完善
     * @param userInfo
     * @param request
     * @throws Exception
     */
    void infoSupplement(JSONObject userInfo, HttpServletRequest request) throws Exception;

    /**
     * 查询账户信息
     * @param userId
     * @return
     * @throws Exception
     */
    JSONObject getAccountInfo(Long userId) throws Exception;
    /**
     * 查询账户信息
     * @param userId
     * @return
     * @throws Exception
     */
    JSONObject getAccountInfoJson(Long userId,Integer cardType) throws Exception;

    /**
     * 保存账户信息
     * @param accountInfo
     * @return
     * @throws Exception
     */
    void saveAccount(JSONObject accountInfo) throws Exception;

    /**
     * 绑卡操作
     * @param cardInfo
     * @throws Exception
     */
    Long tieOnCard(JSONObject cardInfo) throws Exception;

    /**
     * 获取银行卡信息
     * @param cardId
     * @return
     * @throws Exception
     */
    JSONObject getCardInfo(Long cardId) throws Exception;


    /**
     * 获取银行卡信息
     * @param cardId
     * @return
     * @throws Exception
     */
    JSONObject getCardNoAndTypeByCardId(Long cardId) throws Exception;


    /**
     * 查询用户信息
     * @param userId
     * @return
     * @throws Exception
     */
    JSONObject userInfoByQRCode(Long userId) throws Exception;

    /**
     * 入账
     * @param amountIn
     * @return
     * @throws Exception
     */
    JSONObject amountIn(JSONObject amountIn) throws Exception;

    /**
     * 互转交易
     * @param transInfo
     * @throws Exception
     * @return
     */
    JSONObject accountTransfer(JSONObject transInfo);

    /**
     * 渠道上送流水
     * @param channelSerialNumber
     * @throws Exception
     * @return
     */
    JSONObject transactionInfo(String channelSerialNumber) throws Exception;

    /**
     * 保存商户信息（分期付）
     * @param jsonObject
     * @return
     * @throws BizException
     */
    void saveMerchant(JSONObject jsonObject) throws BizException;

    /**
     * 根据商户ID查询商户信息（分期付）
     * @param merchantId
     * @return
     */
    JSONObject getMerchantByMerchantId(Long merchantId);

    /**
     * 更新商户信息（分期付）
     * @param id
     * @param jsonObject
     * @return
     * @throws BizException
     */
    void updateMerchant(Long id, JSONObject jsonObject, HttpServletRequest request) throws BizException;

    /**
     * 账户信息查询（调用账户信息查询）
     * @param id
     * @param request
     * @return
     * @throws Exception
     */
    Object selectAccountUser(Long id, HttpServletRequest request) throws Exception;

    /**
     * 用户管理(营销活动)
     * @param accountInfo
     * @return
     * @throws Exception
     */
    Object selectUserAndCard(JSONObject accountInfo) throws Exception;

    /**
     * 用户管理(营销活动下挂卡)
     * @param id
     * @param request
     * @return
     * @throws Exception
     */
    JSONArray selectAccountCard(Long id, HttpServletRequest request) throws Exception;


    /**
     *出账
     * @param amountOut
     * @return
     * @throws Exception
     */
    JSONObject amountOut(JSONObject amountOut) throws Exception;

    /**
     * 同步分期付系统修改
     *
     * @param amountOut
     * @return
     * @throws Exception
     */
    void updateParametersConfig(JSONObject amountOut) throws Exception;

    /**
     * 单设备推送
     * @param firebaseDTO
     * @return
     * @throws Exception
     */
    void pushFirebase(FirebaseDTO firebaseDTO,HttpServletRequest request) throws Exception;

    /**
     * 多设备按主题推送
     * @param firebaseDTO
     * @return
     * @throws Exception
     */
    void pushFirebaseList(FirebaseDTO firebaseDTO,HttpServletRequest request) throws Exception;

    /**
     * 查询理财产品列表
     * @return
     * @throws Exception
     */
    JSONObject findInvestProducts() throws Exception;

    /**
     * 查询理财产品详情
     * @param productId
     * @return
     * @throws Exception
     */
    JSONObject findInvestProductInfo(Long productId) throws Exception;

    /**
     * 查找消息发送用户
     * @param params
     * @return
     * @throws Exception
     */
    JSONArray sendNoticeUser(JSONObject params) throws Exception;

    /**
     * 修改其它系统手机号
     * @param updateInfo
     * @throws Exception
     */
    void updatePhone(JSONObject updateInfo) throws Exception;

    /**
     *分期付交易状态主动通知
     */
    void transTypeToCredit(JSONObject params)  throws Exception;

    /**
     * 银行logo列表
     * @param params
     * @return
     * @throws Exception
     */
    JSONObject bankLogoList(JSONObject params) throws Exception;

    /**
     * 无分页银行列表
     * @return
     * @throws Exception
     */
    JSONObject getBankLogoList() throws Exception;

    /**
     * 银行列表详情
     * @param id
     * @return
     * @throws Exception
     */
    JSONObject bankLogoInfo(Long id) throws Exception;

    /**
     * 银行logo保存
     * @param params
     * @throws Exception
     */
    void bankLogoSave(JSONObject params) throws Exception;

    /**
     * 银行logo更新
     * @param id
     * @param params
     * @throws Exception
     */
    void updateBankLogo(Long id, JSONObject params) throws Exception;

    /**
     * 银行logo删除
     * @param id
     * @throws Exception
     */
    void bankLogoDelete(Long id) throws Exception;

    /**
     * 卡解绑操作
     * @param unBundlingInfo
     * @throws Exception
     */
    void cardUnbundling(JSONObject unBundlingInfo) throws Exception;
    /**
     * 卡解绑操作
     * @param
     * @throws Exception
     */
    void cardInfoUpdate(JSONObject updateInfo) throws Exception;

    /**
     * 临时接口 更新卡类型信息
     * @param cardInfoUpdateInfo
     * @throws Exception
     */
    void cardInfoUpdateTemp(JSONObject cardInfoUpdateInfo) throws Exception;

    /**
     * 查询分期付银行账户信息
     * @param cardNo
     * @return
     * @throws Exception
     */
    JSONObject findInvestCardInfo(Long userId, String cardNo) throws Exception;

    /**
     * 查询分期付用户信息
     * @param requestInfo
     * @return
     * @throws Exception
     */
    JSONObject findCreditUserInfo(JSONObject requestInfo) throws Exception;


    /**
     * 查询分期付用户详情
     * @author zhangzeyuan
     * @date 2021/9/9 16:19
     * @param requestInfo
     * @return com.alibaba.fastjson.JSONObject
     */
    JSONObject findCreditUserDetail(JSONObject requestInfo) throws Exception;

    /**
     * 查询用户
     * @return
     * @throws Exception
     */
    JSONObject findOneUserInfo(JSONObject requestInfo) throws Exception;

    /**
     * 查询风控记录
     * @param batchNo
     * @return
     * @throws Exception
     */
    JSONArray findRiskLog(String batchNo) throws Exception;

    /**
     * 查询
     * @param userId
     * @return
     * @throws Exception
     */
    JSONObject findInstallmentRiskLog(Long userId) throws Exception;

    /**
     * 分期付重新认证
     * @param userId
     * @throws Exception
     */
    void installmentRecertification(Long userId) throws Exception;

    /**
     * 修改邮箱
     * @param updateInfo
     */
    void updateEmail(JSONObject updateInfo) throws Exception;

    /**
     * 分期付审核通知
     * @param data
     * @param request
     * @throws Exception
     */
    void installmentAuditNotice(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * api分期付订单创建
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject apiCreditOrder(JSONObject requestInfo, HttpServletRequest request) throws Exception;


    /**
     * api分期付订单创建
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject apiCreditOrderOld(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * api分期付订单查询
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject apiCreditOrderSearch(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * api查询用户分期付系统信息
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject userInfoCredit(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * api验证用户是否激活分期付
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject apiUserVerify(HttpServletRequest request) throws Exception;

    /**
     * api激活分期付
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject activationInstallment(HttpServletRequest request) throws Exception;

    /**
     * api分期付绑账户
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject creditTieOnCard(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * api分期付订单状态回滚
     * @param requestInfo
     * @param request
     * @throws Exception
     */
    void orderStateRollback(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 单独开子户
     * @param accountInfo
     */
    void createSubAccount(JSONObject accountInfo)  throws Exception;

    /**
     * 分期付还款
     * @param requestData
     * @param request
     * @return
     */
    JSONObject creditSystemRepayment(JSONObject requestData, HttpServletRequest request) throws Exception;


    /**
     * 用卡号和bsb查询用户卡信息
     * @param cardInfo
     * @return
     * @throws Exception
     */
    JSONObject getCardByMessage(JSONObject cardInfo) throws Exception;

    /**
     * 绑卡信息修改
     * @param card
     * @throws Exception
     */
    void cardMessageModify(JSONObject card) throws Exception;

    /**
     * app绑定还款账户
     * @param requestInfo
     * @return
     * @throws Exception
     */
    void appCreditTieOnCard(JSONObject requestInfo) throws Exception;

    /**
     * 批量查询余额
     * @param requestData
     * @return
     * @throws Exception
     */
    JSONArray getSubAccountBalanceList(JSONObject requestData) throws Exception;

    /**
     * 账户批量动账
     * @param jsonObject
     * @return
     * @throws Exception
     */
    JSONObject batchChangeBalance(JSONObject jsonObject)  throws Exception;

    /**
     * 获取分期付订单 reapy list
     * @param userId
     * @param borrowId
     * @return
     * @throws Exception
     */
    JSONArray getRepayList(String userId, String borrowId) throws Exception;
    /**
     * 批量获取分期付订单 borrow list
     * @param borrowIdList 分期付订单id
     * @param isExcel 是否导出Excel
     * @return
     * @throws Exception
     */
    JSONArray getBorrowList(@NonNull  List<Long> borrowIdList, int isExcel) throws Exception;

    /**
     * 批量查询用户姓名
     * @param requestData
     * @return
     * @throws Exception
     */
    JSONArray getUsernameList(JSONObject requestData) throws Exception;

    /**
     * 测试账户系统拦截器
     * @param data
     * @return
     * @throws BizException
     */
    JSONObject testVerifyAccountServer(JSONObject data) throws Exception;

    /**
     * 初始化卡类型接口
     * todo 上线后删除
     * @return
     */
    List<JSONObject> getAll()throws Exception ;

    /**
     * 将卡设置为默认卡
     * @param jsonObject
     * @throws Exception
     */
    void presetCard(JSONObject jsonObject) throws Exception;

    /**
     * illion成功后 更新分期付User 状态
     * @param userId
     * @param request
     * @throws BizException
     */
    void updateUserCreditState(Long userId, HttpServletRequest request) throws Exception;

    /**
     * 通知分期付illion授权结果
     * @param referralCode
     */
    void illionService(String referralCode,HttpServletRequest request) throws Exception;

    /**
     * 更新分期付的user state
     * @param data userId,state:要更新的状态的int值
     * @param request
     * @throws Exception
     */
    void  updateFailedIllionUserState(JSONObject data,HttpServletRequest request) throws Exception;

    /**
     * 更新分期付用户信息 firstName lastName birth(前端传入dd-MM-yyyy格式 需要转换时间撮)
     * @param data
     * @param request
     *
     */
    void infoSupplementCredit(JSONObject data,HttpServletRequest request) throws Exception;

    /**
     * 通知分期付卡已绑定卡
     * @param data
     * @param request
     * @throws Exception
     */

    void updateCreditCard(JSONObject data,HttpServletRequest request)throws Exception;

    /**
     * 查询分期付用户逾期费订单
     * @param data
     * @return
     */
    List<JSONObject> getOverdueFeeList(JSONObject data) throws Exception;

    /**
     * 查询支付通道费率信息
     * @param data
     * @param request
     */
    JSONObject getGateWayFeeData(JSONObject data, HttpServletRequest request) throws BizException;

    /**
     * 根据用户信息查询用户
     * @param userData
     * @return
     */
    List<UserDTO> findUserInfoByParam(JSONObject userData) throws Exception;
    /**
     *  查询用户分期付state
     * @param userId
     * @return
     */
    JSONObject findCreditUserState(Long userId) throws Exception;

    /**
     * 分期付退款
     * @param requestData
     * @param request
     * @return
     */
    JSONObject creditRefund(JSONObject requestData, HttpServletRequest request) throws Exception;


    /**
     * 三方退款
     *
     * @param flowId
     * @param refundAmount
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/8/23 10:49
     */
    JSONObject creditThirdRefund(Long flowId, BigDecimal refundAmount, String reason, String orderNo, HttpServletRequest request) throws Exception;


    /**
     * laypay发起退款
     *
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/8/24 14:44
     */
    JSONObject latpayRefund(WithholdFlowDTO withholdFlowDTO, RefundFlowDTO refundFlowDTO, Integer refundType, HttpServletRequest request) throws Exception;


    /**
     * 查询分期付退款结果
     *
     * @param id
     * @return
     * @throws Exception
     */
    JSONObject creditRefundDoubt(Long id) throws Exception;



    /**
     * 查询分期付配置表
     * @param id
     * @return
     */
    JSONObject getOneConfigById(@NonNull Long id);



    /**
     * 获取评分等级列表
     * @author zhangzeyuan
     * @date 2021/9/8 18:56
     * @param data
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    JSONObject getRiskScoreGradeList(JSONObject data, HttpServletRequest request) throws BizException;

    /**
     * 修改分期付状态
     * @param creditUserInfo
     * @param request
     */
    JSONObject updateUserCreditStateV1(JSONObject creditUserInfo, HttpServletRequest request) throws Exception;



    /**
     * 重置用户主动还款累计次数
     * @param userId
     * @param request
     */
    void updateUserRepayTimes(Long userId, HttpServletRequest request) throws Exception;

    /**
     *  查询最近一次还款数据
     * @param param
     * @param request
     * @return
     */
    JSONObject findUserUseRepay(JSONObject param, HttpServletRequest request) throws Exception;

    /**
     * 营销券信息查询
     * @param id
     * @return
     * @throws Exception
     */
    public JSONObject getMarketingMessage(Long id,Long userId) throws Exception;

    /**
     * 使用卡券
     * @param param
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject useMarketing(JSONObject param,HttpServletRequest request) throws Exception;

    /**
     * 卡券回退
     * @param id
     * @return
     * @throws Exception
     */
    JSONObject marketingRollBack(Long id) throws Exception;


    /**
     * 这只卡支付成功标识
     * @param jsonObject
     */
    JSONObject setCardSuccessPay(JSONObject jsonObject)  throws Exception;



    /**
     * 添加卡券
     * @author zhangzeyuan
     * @date 2021/10/29 9:44
     * @param param
     * @param request
     * @return java.lang.Object
     */
    JSONObject addPromotionCode(JSONObject param, HttpServletRequest request)throws Exception ;



    /**
     * 用户额度调整
     * @param paramButton
     * @param request
     */
    void updateUserCreditAmount(JSONObject paramButton, HttpServletRequest request) throws Exception;

    /**
     * 延迟还款
     * @param param
     * @param request
     */
    void delayPayment(JSONObject param, HttpServletRequest request) throws Exception;

    /**
     * 根据card token 查询 卡信息
     * @param cardToken
     * @return
     * @throws Exception
     */
    JSONObject getCardInfoByStripeToken(String cardToken) throws Exception;

    /**
     * 根据卡后四位查询数量
     * @author zhangzeyuan
     * @date 2022/1/24 17:21
     * @param userId
     * @param last4
     * @return com.alibaba.fastjson.JSONObject
     */
    Integer countByCardNoLast4(String userId, String last4)throws Exception;

    /**
     * 获取stripe卡列表
     * @author zhangzeyuan
     * @date 2022/1/25 9:24
     * @param userId
     * @return com.alibaba.fastjson.JSONObject
     */
    List<CardDTO> getStripeCardList(String userId) throws Exception;

    /**
     * 获取latpay卡列表
     * @author zhangzeyuan
     * @date 2022/1/25 9:24
     * @param userId
     * @return com.alibaba.fastjson.JSONObject
     */
    List<CardDTO> getLatpayCardList(String userId) throws Exception;

    /**
     * @param withholdFlowDTO
     * @param refundFlowDTO
     * @param refundType
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject stripeRefund(WithholdFlowDTO withholdFlowDTO, RefundFlowDTO refundFlowDTO, Integer refundType, HttpServletRequest request) throws Exception;

}
