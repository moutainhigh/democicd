package com.uwallet.pay.main.model.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author xuchenglong
 * @date 2021/3/24
 */
@ApiModel("整体出售订单")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderExcelDTO implements Serializable {

    @ApiModelProperty(value = "交易单号")
    private String orderNo;

    @ApiModelProperty(value = "整体销售金额")
    private String transAmount;

    @ApiModelProperty(value = "创建时间")
    private String createdDate;

    @ApiModelProperty(value = "整体出售折扣率")
    private String wholeSalesDiscount;

    @ApiModelProperty(value = "销售类型")
    private String saleType;

    @ApiModelProperty(value = "交易类型")
    private String transType;

    @ApiModelProperty(value = "支付金额")
    private String payAmount;

}
