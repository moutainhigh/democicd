package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 用户风控审核日志
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户风控审核日志
 * @author: baixinyue
 * @date: Created in 2020-03-25 10:11:54
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "用户风控审核日志")
public class RiskApproveLog extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  private Long userId;
  /**
   * 审核类型：0：理财风控人审
   */
  @ApiModelProperty(value = "审核类型：0：理财风控人审")
  private Integer approveType;
  /**
   * 审核状态：0:待审核  1：审核通过 2：审核拒绝
   */
  @ApiModelProperty(value = "审核状态：0:待审核  1：审核通过 2：审核拒绝")
  private Integer state;
  /**
   * 审核人id
   */
  @ApiModelProperty(value = "审核人id")
  private Long approvedBy;
  /**
   * 用户进件信息
   */
  @ApiModelProperty(value = "用户进件信息")
  private String data;
  /**
   * 审核拒绝备注
   */
  @ApiModelProperty(value = "审核拒绝备注")
  private String remark;

  @ApiModelProperty(value = "开通业务")
  private String openBusiness;

}
