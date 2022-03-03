package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 二级商户渠道信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 二级商户渠道信息表
 * @author: baixinyue
 * @date: Created in 2019-12-26 17:02:13
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "二级商户渠道信息表")
public class SecondMerchantGatewayInfo extends BaseEntity implements Serializable {

  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  private Long merchantId;
  /**
   * 渠道id
   */
  @ApiModelProperty(value = "渠道id")
  private Long gatewayId;
  /**
   * 渠道方颁发的商户id
   */
  @ApiModelProperty(value = "渠道方颁发的商户id")
  private String gatewayMerchantId;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String gatewayMerchantPassword;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;

}
