package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 权限表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.entity
 * @description: 权限表
 * @author: Strong
 * @date: Created in 2019-09-16 17:55:12
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "权限表")
public class Action extends BaseEntity implements Serializable {

  /**
   * 权限名称
   */
  @ApiModelProperty(value = "权限名称")
  private String name;
  /**
   * 权限英文名称
   */
  @ApiModelProperty(value = "权限英文名称")
  private String enName;
  /**
   * 权限标识
   */
  @ApiModelProperty(value = "权限标识")
  private String flag;
  /**
   * 是否为app权限，0：否，1：是
   */
  @ApiModelProperty(value = "是否为app权限，0：否，1：是")
  private Integer appAction;
  /**
   * 后台路由
   */
  @ApiModelProperty(value = "后台路由")
  private String url;
  /**
   * 图标
   */
  @ApiModelProperty(value = "图标")
  private String icon;
  /**
   * 父级ID
   */
  @ApiModelProperty(value = "父级ID")
  private Long parentId;
  /**
   * 权限类型1.菜单,2.动作
   */
  @ApiModelProperty(value = "权限类型1.菜单,2.动作")
  private Integer type;

}
