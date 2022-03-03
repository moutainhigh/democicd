package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 审核日志表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 审核日志表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:34:12
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "审核日志表")
public class ApproveLog extends BaseEntity implements Serializable {

  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  private Long merchantId;
  /**
   * 审核类型：0：商户入网审核 1：商户信息修改审核
   */
  @ApiModelProperty(value = "审核类型：0：商户入网审核 1：商户信息修改审核")
  private Integer approveType;
  /**
   * 审核状态：-1：审核拒绝 0:待审核  1：审核通过 2：审核中
   */
  @ApiModelProperty(value = "审核状态：-1：审核拒绝 0:待审核  1：审核通过 2：审核中")
  private Integer state;
  /**
   * 审核人id
   */
  @ApiModelProperty(value = "审核人id")
  private Long approvedBy;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String data;
  /**
   * 审核拒绝备注
   */
  @ApiModelProperty(value = "审核拒绝备注")
  private String remark;

}
