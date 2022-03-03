package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * <p>
 * 码操作记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 码操作记录表
 * @author: xucl
 * @date: Created in 2021-03-09 09:55:32
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "码操作记录表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeUpdateLog extends BaseEntity implements Serializable {

  /**
   * 商户ID
   */
  @ApiModelProperty(value = "商户ID")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long merchantId;
  /**
   * 码
   */
  @ApiModelProperty(value = "码")
  private String code;
  /**
   * 码类型：0 nfc 码 1 QR码
   */
  @ApiModelProperty(value = "码类型：0 nfc 码 1 QR码")
  private Integer type;
  /**
   * 操作类型 0 绑定 1 解绑
   */
  @ApiModelProperty(value = "操作类型 0 绑定 1 解绑")
  private Integer state;

}
