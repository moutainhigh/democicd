package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@ApiModel("二维码列表信息")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrcodeListDTO extends BaseDTO implements Serializable {

    /**
     * 编号
     */
    private String code;

    /**
     * 商户id
     */
    private Long merchantId;

    /**
     * 商户账号
     */
    private String merchantNo;

    /**
     * 若为商户，一般返回商户生意名
     */
    private String merchantName;

    /**
     * 若为用户，一般返回用户名
     */
    private String userName;

    /**
     * 用户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 关联时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long correlationTime;

    /**
     * 0:未关联 1:关联
     */
    private Integer state;


    /**
     * (费率)
     */
    @ApiModelProperty(value = "(费率)")
    private Double rate;

//    /**
//     * 支付折扣率
//     */
//    @ApiModelProperty(value = "支付折扣率")
//    private Double payDiscountRate;

    /**
     * 路由表
     */
    private List<RouteDTO> routeDTOS;

    /**
     * 余额
     */
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    /**
     * 分期付业务状态 0：不可用 1：可用 2：禁用
     */
    @ApiModelProperty(value = "分期付业务状态 0：不可用 1：可用 2：禁用")
    private Integer installmentState;

    /**
     * 费率类型  0：用户承担;1：商户承担
     */
    @ApiModelProperty(value = "费率类型  0：用户承担;1：商户承担")
    private Integer rateType;

    /**
     * 商户整体出售余额
     */
    private BigDecimal wholeSaleBalance;

    /**
     * 商户整体出售用户折扣
     */
    private BigDecimal wholeSaleUserDiscount;

    /**
     * 固定折扣
     */
    private BigDecimal baseRate;
    /**
     * 额外折扣
     */
    private BigDecimal extraDiscount;
    /**
     * 商户营销折扣
     */
    private BigDecimal marketingDiscount;

    /**
     * 用户stripe弹窗标识
     */
    private Integer stripeState;

}
