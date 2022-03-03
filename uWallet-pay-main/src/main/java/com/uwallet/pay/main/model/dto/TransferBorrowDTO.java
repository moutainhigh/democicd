package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 转账订单列表页
 * @author baixinyue
 * @createDate 2019/12/18
 *
 */
@ApiModel("转账订单列表页")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferBorrowDTO extends BaseDTO implements Serializable {

    /**
     * 付款账号
     */
    private String pay_no;

    /**
     * 收款账号
     */
    private String rec_no;

    /**
     * 转账金额
     */
    private BigDecimal transAmount;

    /**
     * 实付金额
     */
    private BigDecimal truelyPayAmount;

    /**
     * 费率
     */
    private BigDecimal rate;

    /**
     * 渠道名
     */
    private String channelName;

    /**
     * 订单状态
     */
    private Integer state;

}
