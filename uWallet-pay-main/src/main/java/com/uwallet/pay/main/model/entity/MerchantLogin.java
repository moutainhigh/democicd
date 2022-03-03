package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 用户主表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户主表
 * @author: zhoutt
 * @date: Created in 2020-02-21 16:19:27
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "用户主表")
public class MerchantLogin extends BaseEntity implements Serializable {

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
