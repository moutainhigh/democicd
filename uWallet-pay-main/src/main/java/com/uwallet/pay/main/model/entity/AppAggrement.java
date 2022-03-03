package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * app 协议
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: app 协议
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:23
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "app 协议")
public class AppAggrement extends BaseEntity implements Serializable {

  /**
   * 协议名
   */
  @ApiModelProperty(value = "协议名")
  private String name;
  /**
   * 10：隐私 20：分期付 30：合同
   */
  @ApiModelProperty(value = "10：隐私 20：分期付 30：合同")
  private Integer type;
  /**
   * 协议内容
   */
  @ApiModelProperty(value = "协议内容")
  private String content;

}
