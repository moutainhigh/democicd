package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 角色表

 * </p>
 *
 * @package:  com.loancloud.rloan.main.entity
 * @description: 角色表

 * @author: Strong
 * @date: Created in 2019-09-16 17:34:46
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "角色表")
public class Role extends BaseEntity implements Serializable {

  /**
   * 角色名称
   */
  @ApiModelProperty(value = "角色名称")
  private String name;
  /**
   * 别名
   */
  @ApiModelProperty(value = "别名")
  private String remarkName;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;
  /**
   * 0：禁用 1：可用
   */
  @ApiModelProperty(value = "0：禁用 1：可用")
  private Integer stats;

}
