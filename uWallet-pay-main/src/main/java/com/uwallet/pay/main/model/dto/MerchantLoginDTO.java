package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
/**
 * <p>
 * 用户主表
 * </p>
 *
 * @description: 用户主表
 * @author: zhoutt
 * @date: Created in 2020-02-21 16:19:27
 */
@ApiModel("用户主表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantLoginDTO extends BaseDTO implements Serializable {

    /**
     * 账户类型（10：客户 ；20：商户）
     */
    @ApiModelProperty(value = "账户类型（10：客户 ；20：商户）")
    private Integer userType;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String password;
    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码")
    private String phone;
    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;

}
