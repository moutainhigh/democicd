package com.uwallet.pay.main.model.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author baixinyue
 * @description 清算导入类
 * @createDate 2020/02/20
 */
@Data
@ExcelTarget("Clear")
public class Clear implements Serializable {

    /**
     * 清算时间
     */
    @Excel(name = "FinanceDate", exportFormat = "yyyy-MM-dd")
    private Date financeDate;

    /**
     * 清算金额
     */
    @Excel(name = "ClearingAmount")
    private BigDecimal clearingAmount;

    /**
     * OmiPay清算编号
     */
    @Excel(name = "ClearingNumber")
    private String clearingNumber;

    /**
     * 支付总金额
     */
    @Excel(name = "PaymentGrossAmount")
    private BigDecimal paymentGrossAmount;

    /**
     * 退款总金额
     */
    @Excel(name = "RefundAmount")
    private BigDecimal refundAmount;

    /**
     * 支付笔数
     */
    @Excel(name = "PayCount")
    private Integer payCount;

    /**
     * 退款笔数
     */
    @Excel(name = "RefundCount")
    private Integer refundCount;

}
