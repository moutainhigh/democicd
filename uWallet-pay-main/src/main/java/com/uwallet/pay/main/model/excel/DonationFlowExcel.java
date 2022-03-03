package com.uwallet.pay.main.model.excel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author xuchenglong
 * Illion记录excl类
 * @date 2021/7/23
 */

@ApiModel("捐赠明细excl类")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationFlowExcel implements Serializable {
    private String orderNo;
    private String merchant;
    private String fullName;
    private String mobile;
    private String paymentMode;
    private String cardType;
    private String cardNo;
    private String orderAmount;
    private String totalAmount;
    private String discount;
    private String payoMoney;
    private String txnFee;
    private String tip;
    private String donation;
    private String orderTime;
    private String settlementTime;
    private String settlementStatus;
}
