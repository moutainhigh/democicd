package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * top deal
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: top deal
 * @author: zhoutt
 * @date: Created in 2020-03-12 14:34:50
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "top deal")
public class TopDeal extends BaseEntity implements Serializable {

  /**
   * 名称
   */
  @ApiModelProperty(value = "名称")
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
