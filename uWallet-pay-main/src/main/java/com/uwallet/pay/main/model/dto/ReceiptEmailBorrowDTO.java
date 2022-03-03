package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 发送交易发票信息邮件 需要的 borrow实体
 *
 * @author zhangzeyuan
 * @date 2021/5/13 10:14
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceiptEmailBorrowDTO extends BaseDTO implements Serializable {

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String userName;
    /**
     * 商户Id
     */
    @ApiModelProperty(value = "商户Id")
    private Long merchantId;
    /**
     * 借款金额
     */
    @ApiModelProperty(value = "借款金额")
    private BigDecimal borrowAmount;

    /**
     * 期数
     */
    @ApiModelProperty(value = "期数")
    private Integer productPeriod;
}
