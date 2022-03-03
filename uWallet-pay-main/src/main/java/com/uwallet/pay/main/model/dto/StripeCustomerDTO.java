package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.dto.StripeBaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * Stripe-API Customer 描述客户信息
 * </p>
 *
 * @description:
 * @author: zhangzeyuan
 * @date: Created in 2022年1月10日15:50:38
 */
@ApiModel("Stripe-API Customer客户信息")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StripeCustomerDTO extends StripeBaseDTO{

    private static final long serialVersionUID = -1410000607208634830L;

    /**
     * 地址信息 city  country  line1 line2 postal_code  state
     */
    private Map<String, Object> address;
    /**
     * 描述信息
     */
    @ApiModelProperty(value = "描述信息")
    private String description;
    /**
     * 邮件
     */
    @ApiModelProperty(value = "邮件")
    private String email;

    /**
     * The customer’s full name or business name.
     */
    @ApiModelProperty(value = "The customer’s full name or business name")
    private String name;
    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;
    /**
     * 默认付款源
     */
    @ApiModelProperty(value = "默认付款源")
    private String default_source;

}
