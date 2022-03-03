package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * <p>
 * 审核日志
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 审核日志
 * @author: zhoutt
 * @date: Created in 2021-09-23 15:39:54
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "审核日志")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiApproveLog extends BaseEntity implements Serializable {

  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long merchantId;
  /**
   * 商户级别
   */
  @ApiModelProperty(value = "商户级别")
  private Integer merchantClass;
  /**
   * 城市
   */
  @ApiModelProperty(value = "城市")
  private Integer merchantCity;
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
