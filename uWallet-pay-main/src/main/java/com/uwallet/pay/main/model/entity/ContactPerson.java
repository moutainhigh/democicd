package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 联系人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 联系人信息表
 * @author: baixinyue
 * @date: Created in 2020-08-06 11:45:37
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "联系人信息表")
public class ContactPerson extends BaseEntity implements Serializable {

  /**
   * 商户号
   */
  @ApiModelProperty(value = "商户号")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long merchantId;
  /**
   * 名字
   */
  @ApiModelProperty(value = "名字")
  private String name;
  /**
   * 标题
   */
  @ApiModelProperty(value = "标题")
  private String title;
  /**
   * 电话
   */
  @ApiModelProperty(value = "电话")
  private String mobile;
  /**
   * 微信
   */
  @ApiModelProperty(value = "微信")
  private String wechat;
  /**
   * 邮箱
   */
  @ApiModelProperty(value = "邮箱")
  private String email;

}
