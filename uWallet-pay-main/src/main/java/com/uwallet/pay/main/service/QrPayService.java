package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

public interface QrPayService {

    /**
     * 扫码支付入参校验
     * @param qrPayDTO
     * @param request
     * @throws BizException
     */
    void qrPayReqCheck(QrPayDTO qrPayDTO, HttpServletRequest request) throws BizException;

    /**
     *  支付 入参校验
     * @author zhangzeyuan
     * @date 2021/6/18 17:31
     * @param qrPayDTO
     * @param request
     */
    void qrPayReqCheckV2(QrPayDTO qrPayDTO, HttpServletRequest request) throws BizException;

    /**
     * 扫码支付，卡支付
     * @param request
     * @param payUser
     * @param recUser
     * @param qrPayFlowDTO
     * @param cardDTO
     * @param cardObj
     * @throws BizException
     */
    void qrPayByCard( HttpServletRequest request, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO, GatewayDTO gatewayDTO, CardDTO cardDTO, JSONObject cardObj) throws BizException;

    /**
     * 账户支付
     * @param qrPayDTO
     * @param payUser
     * @param recUser
     * @param qrPayFlowDTO
     * @param gatewayDTO
     * @param request
     * @throws Exception
     */
    void qrPayByAccount(QrPayDTO qrPayDTO, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO, GatewayDTO gatewayDTO, HttpServletRequest request) throws Exception;

    /**
     * 扫码支付，余额支付
     * @param qrPayDTO
     * @param request
     * @param payUser
     * @param recUser
     * @param qrPayFlowDTO
     * @throws BizException
     */
    void qrPayByBalance(QrPayDTO qrPayDTO, HttpServletRequest request, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO) throws BizException;

    /**
     * 扫码支付交易
     * @param qrPayDTO
     * @param request
     * @throws BizException
     * @return
     */
    Object doQrPay(QrPayDTO qrPayDTO, HttpServletRequest request) throws  Exception;

    /**
     * 新版扫码支付
     * @author zhangzeyuan
     * @date 2021/6/29 14:51
     * @param qrPayDTO
     * @param request
     * @return java.lang.Object
     */
    Object doQrPayV2(QrPayDTO qrPayDTO, HttpServletRequest request) throws  Exception;


    /**
     *  支付 v3
     * @author zhangzeyuan
     * @date 2021/10/27 10:05
     * @param qrPayDTO
     * @param request
     * @return java.lang.Object
     */
    Object doQrPayV3(QrPayDTO qrPayDTO, HttpServletRequest request) throws  Exception;

    /**
     *  支付 v4
     * @author zhangzeyuan
     * @date 2022/01/10 10:15
     * @param qrPayDTO
     * @param request
     * @return java.lang.Object
     */
    Object doQrPayV4(QrPayDTO qrPayDTO, HttpServletRequest request) throws  Exception;


    /**
     * 预订单
     * @param qrPayDTO
     * @param request
     * @return
     * @throws Exception
     */
    Object interestCredistOrder(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception;

    /**
     * 查验账户交易状态
     * @param userId
     * @return
     */
    boolean checkAccountState(Long userId);

    /**
     * 修改流水记录
     * @param qrPayFlowDTO
     * @param accountFlowDTO
     * @param withholdFlowDTO
     * @param request
     * @throws BizException
     */
    void updateFlow(QrPayFlowDTO qrPayFlowDTO, AccountFlowDTO accountFlowDTO, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 扫码支付，账户入账组件
     * @param accountFlowDTO
     * @param qrPayFlowDTO
     * @param request
     * @throws BizException
     */
    void doAmountTrans(AccountFlowDTO accountFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException;


    /**
     * 扫码支付账户交易可疑查证
     * @throws Exception
     */
    void qrPayAccountDoubtHandle() throws Exception;

    /**
     * 扫码支付账户失败处理
     * @throws Exception
     */
    void qrPayAccountFailHandle() throws Exception;

    /**
     * 扫码支付三方交易可疑查证
     * @throws Exception
     */
    void qrPayThirdDoubtHandle() throws Exception;


    /**
     * 分期付首次卡支付三方可疑处理
     * @author zhangzeyuan
     * @date 2021/7/7 14:47
     */
    void creditFirstCardPayDoubtHandle() throws Exception;

    /**
     * 一笔分期付首次卡支付三方可疑处理
     * @param qrPayFlowDTO
     */
    int dealOneCreditFirstCardPayDoubt(QrPayFlowDTO qrPayFlowDTO);

    /**
     * 分期付冻结额度回滚可疑处理
     * @author zhangzeyuan
     * @date 2021/7/7 14:47
     */
    void creditRollbackAmountDoubtHandle() throws Exception;

    /**
     * 分期付生成可疑订单处理
     * @author zhangzeyuan
     * @date 2021/7/7 14:47
     */
    void creditCreateOrderDoubtHandle() throws Exception;

    /**
     * 获取账户入账交易请求参数
     * @param accountFlowDTO
     * @return
     */
    Map<String, Object> getAmountInMap(AccountFlowDTO accountFlowDTO);

    /**
     * 根据支付流水创建账务流水
     * @param qrPayFlowDTO
     * @return
     */
    AccountFlowDTO getAccountFlowDTO(QrPayFlowDTO qrPayFlowDTO);

    /**
     * APP查询支付交易结果
     * @param flowId
     * @return
     */
    JSONObject aliOrWechatOrderStatusCheck(Long flowId, HttpServletRequest request) throws Exception;


    /**
     * 防止并发的更新流水组件
     * @param qrPayFlowDTO
     * @param accountFlowDTO
     * @param withholdFlowDTO
     * @param request
     * @throws BizException
     */
    void updateFlowForConcurrency(QrPayFlowDTO qrPayFlowDTO, AccountFlowDTO accountFlowDTO, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 金额计算
     * @param qrPayDTO
     * @param qrPayFlowDTO
     * @param request
     * @return
     * @throws BizException
     */
    QrPayFlowDTO calculation(QrPayDTO qrPayDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 首次消费红包处理方法
     * @param qrPayFlowDTO
     * @param payUser
     * @param request
     */
    void dealFirstDeal(QrPayFlowDTO qrPayFlowDTO, UserDTO payUser, HttpServletRequest request);

    /**
     * 查询扫码支付的交易金额
     * @param data
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject getQrPayTransAmount(JSONObject data, HttpServletRequest request) throws Exception;


    /**
     * 获取支付交易金额详情
     * @author zhangzeyuan
     * @date 2021/6/23 16:06
     * @param data
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */

    JSONObject getPayTransAmountDetail(JSONObject data, HttpServletRequest request) throws Exception;


    /**
     * 获取支付交易金额详情v3
     * @author zhangzeyuan
     * @date 2021/10/27 10:44
     * @param data
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    JSONObject getPayTransAmountDetailV3(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     *  扫码支付红包、整体出售出账可疑流水处理
     */
    void qrPayBatchAmountOutDoubtHandle();

    /**
     * 扫码支付红包、整体出售出账回滚可疑流水处理
     */
    void qrPayBatAmtOutRollbackDoubtHandle();

    /**
     * 扫码支付红包、整体出售出账回滚失败处理
     */
    void qrPayBatAmtOutRollbackFailHandle();

    void qrPayReqCheckOld(QrPayDTO qrPayDTO, HttpServletRequest request)  throws  Exception;

    Object doQrPayOld(QrPayDTO qrPayDTO, HttpServletRequest request)  throws  Exception;

    /**
     * 分期付调用支付进行卡支付
     * @param param
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject doPayByCard(JSONObject param,HttpServletRequest request)throws  Exception;

    /**
     * 分期付调用支付进行卡支付查证
     */
    void doPayByCardHandle() throws Exception;

    /**
     * Latpay交易结果处理
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param request
     * @throws Exception
     */
    void handleCardLatPayPostResult(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO,JSONObject cardObj,
                                    HttpServletRequest request) throws Exception;

    void handleCreditCardPayDataByThirdStatus(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, JSONObject cardObj,
                                                     BigDecimal creditNeedCardPayAmount, BigDecimal creditNeedCardPayNoFeeAmount, BigDecimal remainingCreditAmount,
                                                     BigDecimal cardPayRate, BigDecimal cardPayFee, HttpServletRequest request) throws Exception;


    void creditChannelLimitAndBatchAmountRollBack(QrPayFlowDTO qrPayFlowDTO, BigDecimal creditNeedCardPayNoFeeAmount, Integer state,
                                                  HttpServletRequest request) throws Exception;

    /**
     * 交易出账回退
     * @param qrPayFlowDTO
     * @param request
     */
    void doBatchAmountOutRollBack(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request);

    /**
     * 卡券回退
     * @param qrPayFlowDTO
     * @param request
     * @throws BizException
     */
    void oneMarketingRollback(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException;

    /**
     * 做一笔卡支付交易三方查证
     * @param qrPayFlowDTO
     */
    Integer dealOneThirdDoubtHandle(QrPayFlowDTO qrPayFlowDTO);


    /**
     * 分期付调用支付进行卡支付
     * @param request
     * @return
     * @throws Exception
     */
    Object doPayByCardV2(JSONObject data, HttpServletRequest request) throws Exception;
}

