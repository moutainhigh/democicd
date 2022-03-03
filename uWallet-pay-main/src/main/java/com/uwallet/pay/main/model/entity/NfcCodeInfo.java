package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * NFC信息、绑定表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: NFC信息、绑定表
 * @author: zhoutt
 * @date: Created in 2020-03-23 14:31:21
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "NFC信息、绑定表")
public class NfcCodeInfo extends BaseEntity implements Serializable {

  /**
   * 关联用户
   */
  @ApiModelProperty(value = "关联用户")
  private Long userId;
  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  private Long merchantId;
  /**
   * 二维码
   */
  @ApiModelProperty(value = "二维码")
  private String qrCode;
  /**
   * nfc码
   */
  @ApiModelProperty(value = "nfc码")
  private String code;
  /**
   * 0:未关联 1:关联
   */
  @ApiModelProperty(value = "0:未关联 1:关联")
  private Integer state;
  /**
   * 关联时间
   */
  @ApiModelProperty(value = "关联时间")
  private Long correlationTime;

}
