package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * app banner
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: app banner
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:29:08
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "app banner")
public class AppBanner extends BaseEntity implements Serializable {

  /**
   * banner名称
   */
  @ApiModelProperty(value = "banner名称")
  private String name;
  /**
   * 跳转类型 10：广告 20：理财
   */
  @ApiModelProperty(value = "跳转类型 10：广告 20：理财")
  private Integer skipType;
  /**
   * 跳转路由
   */
  @ApiModelProperty(value = "跳转路由")
  private String skipRoute;
  /**
   * 图片路径
   */
  @ApiModelProperty(value = "图片路径")
  private String path;
  /**
   * 富文本内容
   */
  @ApiModelProperty(value = "富文本内容")
  private String info;
  /**
   * 排序
   */
  @ApiModelProperty(value = "排序")
  private Integer sort;
  /**
   * 0:下架 1:上架
   */
  @ApiModelProperty(value = "0:下架 1:上架")
  private Integer state;

}
