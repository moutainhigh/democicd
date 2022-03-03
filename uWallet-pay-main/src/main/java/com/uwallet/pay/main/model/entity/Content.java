package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 广告表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 广告表
 * @author: Strong
 * @date: Created in 2020-01-14 11:10:06
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "广告表")
public class Content extends BaseEntity implements Serializable {

  /**
   * 广告标题
   */
  @ApiModelProperty(value = "广告标题")
  private String title;
  /**
   * 内容描述
   */
  @ApiModelProperty(value = "内容描述")
  private String description;
  /**
   * 跳转地址
   */
  @ApiModelProperty(value = "跳转地址")
  private String path;

}
