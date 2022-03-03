package com.uwallet.pay.main.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author baixinyue
 * @date 2020/02/14
 * @description Omipay交易明细退款导入
 */

@ApiModel("Omipay交易明细退款导入")
@Data
@ExcelTarget("TransactionRefund")
public class TransactionRefund implements Serializable {

    /**
     * OmiPay交易单号
     */
    @Excel(name = "transaction number")
    private String transactionNumber;

    /**
     * 退款时间
     */
    @Excel(name = "refund time", exportFormat = "yyyy/MM/dd hh:mm:ss")
    private Date refundTime;

    /**
     * 输入交易金额
     */
    @Excel(name = "net refund amount")
    private BigDecimal netRefundAmount;

    /**
     * 总金额
     */
    @Excel(name = "refund amount")
    private BigDecimal refundAmount;

    /**
     * 外部退款编号
     */
    @Excel(name = "out refund number")
    private String outRefundNumber;

}
