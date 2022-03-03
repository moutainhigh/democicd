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
 * @description Omipay交易明细支付导入
 */

@ApiModel("Omipay交易明细支付导入")
@Data
@ExcelTarget("Transaction")
public class Transaction implements Serializable {

    /**
     * OmiPay交易单号
     */
    @Excel(name = "transaction number")
    private String transactionNumber;

    /**
     * 支付时间
     */
    @Excel(name = "transaction time", exportFormat = "yyyy/MM/dd hh:mm:ss")
    private Date transactionTime;

    /**
     * 支付渠道
     */
    @Excel(name = "payment channel")
    private String paymentChannel;

    /**
     * 输入交易金额
     */
    @Excel(name = "transaction amount")
    private BigDecimal transactionAmount;

    /**
     * 总金额
     */
    @Excel(name = "gross amount")
    private BigDecimal grossAmount;

    /**
     * 外部订单编号
     */
    @Excel(name = "out_order_number")
    private String outOrderNumber;
    /**
     * 查证到的交易状态 Rejected = 2 /Accepted =1
     *  stripe 状态 1 成功 2 失败 3 requires_action 4其他
     */
    @Excel(name = "status")
    private Integer status;


    /**
     * 描述
     */
    @Excel(name = "desc")
    private String desc;

}
