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
 * 用户权限阶段记录
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户权限阶段记录
 * @author: baixinyue
 * @date: Created in 2020-06-30 16:52:45
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "用户权限阶段记录")
public class UserStepLog extends BaseEntity implements Serializable {

  /**
   * 权限阶段id
   */
  @ApiModelProperty(value = "权限阶段id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long stepId;
  /**
   * 阶段状态：11：kyc通过 12：kyc失败 13: kyc审核 21：illion通过 22： illion拒绝 31：分期付风控通过 32： 分期付风控拒绝 33： 分期付风控审核
   */
  @ApiModelProperty(value = "阶段状态：11：kyc通过 12：kyc失败 13: kyc审核 21：illion通过 22： illion拒绝 31：分期付风控通过 32： 分期付风控拒绝 33： 分期付风控审核")
  private Integer stepStatus;
  /**
   * 风控审核批号
   */
  @ApiModelProperty(value = "风控审核批号")
  private String riskBatchNo;
  /**
   * 拒绝原因
   */
  @ApiModelProperty(value = "拒绝原因")
  private String refuseReason;
  /**
   * kyc进件信息
   */
  @ApiModelProperty(value = "kyc进件信息")
  private String kycInfo;

}
