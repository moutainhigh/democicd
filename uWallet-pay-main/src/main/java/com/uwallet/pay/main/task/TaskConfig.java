package com.uwallet.pay.main.task;

import com.uwallet.pay.main.docuSignComponent.DocuSignClient;
import com.uwallet.pay.main.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description: 定时任务
 * @author Rainc
 * @date 2019/12/02
 */
@Component
@Slf4j
public class TaskConfig {

    /*@Autowired
    private RechargeFlowService rechargeFlowService;*/

    @Autowired
    private QrPayService qrPayService;

    @Autowired
    private ClearDetailService clearDetailService;

    @Autowired
    private OrderRefundService orderRefundService;

    @Autowired
    private ClearBatchService clearBatchService;

    @Autowired
    private LatPayService latPayService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocuSignClient docuSignClient;

    @Autowired
    private WholeSalesFlowService wholeSalesFlowService;

    @Autowired
    private SplitService splitService;
    @Autowired
    private TagService tagService;
    @Autowired
    private QrPayFlowService qrPayFlowService;
    @Autowired
    private RefundService refundService;
    @Autowired
    private ApiQrPayFlowService apiQrPayFlowService;
    @Autowired
    private MarketingFlowService marketingFlowService;

    @Autowired
    private MarketingManagementService marketingManagementService;

    @Resource
    private DailyStatisticsService  dailyStatisticsService;

    /**
     * 充值latpay交易结果查询
     */

    /**
     * 充值交易账户阶段可疑流水处理
     * @throws Exception
     */
//    @Scheduled(cron = "0 0 0 * * ?")
//    public void rechargeAccountDoubtHandle () throws Exception {
//        rechargeFlowService.rechargeAccountDoubtHandle();
//    }

    /**
     * 充值交易账户阶段失败流水处理
     * @throws Exception
     */
//    @Scheduled(cron = "0 0 0 * * ?")
//    public void rechargeAccountFailedHandle () throws Exception {
//        rechargeFlowService.rechargeAccountFailedHandle();
//    }

    /**
     * 扫码支付商户入账阶段可疑流水处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/20 * * * ?")
    public void qrPayAccountDoubtHandle () throws Exception {
        qrPayService.qrPayAccountDoubtHandle();
    }

    /**
     * 扫码支付账户阶段失败流水处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void qrPayAccountFailHandle () throws Exception {
        qrPayService.qrPayAccountFailHandle();
    }

    /**
     * 扫码支付三方支付可疑流水处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void qrPayThirdDoubtHandle () throws Exception {
        qrPayService.qrPayThirdDoubtHandle();
    }


    /**
     * 扫码支付红包、整体出售出账可疑流水处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void qrPayBatchAmountOutDoubtHandle () throws Exception {
        qrPayService.qrPayBatchAmountOutDoubtHandle();
    }



    /**
     * 扫码支付红包、整体出售出账回滚可疑流水处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void qrPayBatAmtOutRollbackDoubtHandle() throws Exception {
        qrPayService.qrPayBatAmtOutRollbackDoubtHandle();
    }

    /**
     * 扫码支付红包、整体出售出账回滚失败流水处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void qrPayBatAmtOutRollbackFailHandle() throws Exception {
        qrPayService.qrPayBatAmtOutRollbackFailHandle();
    }



    /**
     * 退款状态查询并修改状态
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void refundStatusCheck () throws Exception {
        orderRefundService.refundStatusCheck();
    }

    /**
     * 出账回滚失败在处理
     * @throws Exception
     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void refundFailedAccountRollbackFailedHandle () throws Exception {
//        orderRefundService.refundFailedAccountRollbackFailedHandle();
//    }

    /**
     * 出账回滚可疑处理
     * @throws Exception
     */
//    @Scheduled(cron = "0 0/20 * * * ?")
//    public void refundFailedAccountRollbackDoubtHandle () throws Exception {
//        orderRefundService.refundFailedAccountRollbackDoubtHandle();
//    }

    /**
     * 退款出账可疑操作
     * @throws Exception
     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void refundFailedAccountOut () throws Exception {
//        orderRefundService.refundFailedAccountOut();
//    }

    /**
     * 清算可疑流水处理
     * @throws Exception
     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void clearDoubtHandle () throws Exception {
//        clearBatchService.clearDoubtHandle();
//    }


    /**
     * 清算回滚可疑流水处理
     * @throws Exception
     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void clearRollbackDoubtHandle () throws Exception {
//        clearBatchService.clearRollbackDoubtHandle();
//    }

    /**
     * 清算回滚失败流水处理
     * @throws Exception
     */
//    @Scheduled(cron = "0 0/10 * * * ?")
//    public void clearRollbackFailHandle () throws Exception {
//        clearBatchService.clearRollbackFailHandle();
//    }

    /**
     * latpay账户交易状态查询
     * @throws Exception
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void directDebitStatusCheck () throws Exception {
        latPayService.directDebitStatusCheck();
    }

    /**
     * 账户解绑卡失败处理操作
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void cardAccountSystemUnbundlingHandle() throws Exception {
        userService.cardAccountSystemUnbundlingHandle();
        //
    }
    /**
     * 定时更新DocuSign Token
     */
    @Scheduled(cron = "0 0/20 * * * ?")
    public void updateDocuSignToken() throws Exception {
        docuSignClient.getApiClient();
    }

    /**
     * 红包入账可疑处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void walletBookedDoubleHandle() throws Exception {
        userService.walletBookedDoubleHandle();
    }

    /**
     * 红包入账失败处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/20 * * * ?")
    public void walletBookedFailedHandle() throws Exception {
        userService.walletBookedFailedHandle();
    }

    /**
     * 整体出售失败处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void wholeSaleAmountInFailedHandle() throws Exception {
        wholeSalesFlowService.wholeSaleAmountInFailedHandle();
    }

    /**
     * 整体出售可疑处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void wholeSaleAmountInDoubfulHandle() throws Exception {
        wholeSalesFlowService.wholeSaleAmountInDoubfulHandle();
    }

    /**
     * split 交易中跑批查询
     */
    @Scheduled(cron = "0 0 0/6 * * ?")
    public void splitTransactionDoubleHandle() throws Exception {
        splitService.splitTransactionDoubleHandle();
    }
    /**
     * 系统跑批更新redis top10 tag数据
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void updateTop10Tags() throws Exception {
        tagService.updateTop10Tags();
    }
    /**
     * 每月用户数据统计
     */
    @Scheduled(cron = "0 10 1 1 * ?")
    public void getMonthlyUserSavedTask() throws Exception {
        qrPayFlowService.getMonthlyUserSavedTask();
    }


    /**
     * 有过交易但一一个月已上未有交易 发送邮件  生成开启 准生产 注释掉
     * @author zhangzeyuan
     * @date 2021/5/13 13:41
     */
    @Scheduled(cron = "0 0 08 * * ?")
//    @Scheduled(cron = "0 01 17 * * ?")
    public void sendMailOneMonthNoTransaction() throws Exception {
        qrPayFlowService.sendMailOneMonthNoTransaction();
    }


    /**
     * 新用户2个周后还未消费  发送邮件 站内信 生成开启 准生产 注释掉！
     * edit by zhangzeyuan 2021年9月24日10:28:35   去掉该节点发送功能
     * @author zhangzeyuan
     * @date 2021/5/13 13:41
     */
    /*@Scheduled(cron = "0 0 08 * * ?")
//    @Scheduled(cron = "0 07 19 * * ?")
    public void sendMsgUserTwoWeeksNoTransaction() throws Exception {
        qrPayFlowService.sendMsgUserTwoWeeksNoTransaction();
    }*/

    /**
     * 分期付调用卡支付查证跑批
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void doPayByCardHandle () throws Exception {
        qrPayService.doPayByCardHandle();
    }


    /**
     * 分期付 首次25%卡支付 可疑处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/3 * * * ? ")
    public void creditFirstCardPayDoubtHandle() throws Exception {
        qrPayService.creditFirstCardPayDoubtHandle();
    }


    /**
     * 分期付 冻结额度回滚 可疑处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void  creditRollbackAmountDoubtHandle() throws Exception {
        qrPayService.creditRollbackAmountDoubtHandle();
    }



    /**
     * 分期付 生成订单 可疑处理
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void creditCreateOrderDoubtHandle () throws Exception {
        qrPayService.creditCreateOrderDoubtHandle();
    }
    /**
     * 退款可疑订单查证（到分期付）
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void creditRefundDoubtHandle () throws Exception {
        refundService.creditRefundDoubtHandle();
    }

    /**
     * h5幂等过期时间
     * @throws Exception
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void h5ApiQrPayFlowDoubtHandle () throws Exception {
        apiQrPayFlowService.redisApiqrPayFlowDoubtHandle();
    }


    /**
     * 新注册用户 8am 跑批邮件发送
     * @author zhangzeyuan
     * @date 2021/9/24 15:40
     */
    @Scheduled(cron = "0 0 08 * * ? ")
    public void newUser8amEmailScheduled(){
        userService.newUser8amEmailScheduled();
    }


    /**
     * 新注册用户 4pm 跑批邮件发送
     * @author zhangzeyuan
     * @date 2021/9/24 15:40
     */
    @Scheduled(cron = "0 0 16 * * ? ")
    public void newUser4pmEmailScheduled(){
        userService.newUser4pmEmailScheduled();
    }


    /**
     * 新注册用户 1pm 跑批邮件发送
     * @author zhangzeyuan
     * @date 2021/9/24 15:40
     */
    @Scheduled(cron = "0 0 13 * * ? ")
    public void newUser1pmEmailScheduled(){
        userService.newUser1pmEmailScheduled();
    }


    /**
     * 卡券回退可疑查证
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void marketingRollBackDoubtHandle (){
        marketingFlowService.marketingRollBackDoubtHandle();
    }


    /**
     * 卡券回退失败处理
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void marketingRollBackFailHandle (){
        marketingFlowService.marketingRollBackFailHandle();
    }

    /**
     * 卡券获得开始终止跑批
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void marketingManagerHandle (){
        marketingManagementService.marketingManagerHandle();
    }

    /**
     * 每日运营统计
     * @author zhangzeyuan
     * @date 2021/12/14 10:17
     */
    @Scheduled(cron = "0 0 05 * * ? ")
    public void dailyOperationStatistics(){
        try{
            dailyStatisticsService.dailyStatistics();
        }catch (Exception e){
            log.error("每日运营统计定时任务出错",  e.getMessage());
        }
    }

}
