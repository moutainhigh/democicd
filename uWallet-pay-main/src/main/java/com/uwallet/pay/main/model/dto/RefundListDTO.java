package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author baixinyue
 * @createDate 2020/03/10
 * @decription 退款管理
 */

@ApiModel("退款管理")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundListDTO implements Serializable {

    /**
     * 商户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;

    /**
     * 公司名称
     */
    private String corporateName;

    /**
     * bsb
     */
    private String bsb;

    /**
     * 银行账号
     */
    private String accountNo;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款条数
     */
    private Integer refundCount;

}
