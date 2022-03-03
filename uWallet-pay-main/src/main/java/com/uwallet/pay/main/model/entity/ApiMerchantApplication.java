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
 * h5 api 商户申请表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: h5 api 商户申请表
 * @author: zhoutt
 * @date: Created in 2021-09-23 10:25:50
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "h5 api 商户申请表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMerchantApplication extends BaseEntity implements Serializable {

  /**
   * 商户等级 0：一级 1：二级
   */
  @ApiModelProperty(value = "商户等级 0：一级 1：二级")
  private Integer merchantClass;
  /**
   * 一级商户id
   */
  @ApiModelProperty(value = "一级商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long superMerchantId;
  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long merchantId;
  /**
   * 商户名称
   */
  @ApiModelProperty(value = "商户名称")
  private String practicalName;
  /**
   * abn
   */
  @ApiModelProperty(value = "abn")
  private String abn;
  /**
   * 审核类型：1：商户入网
   */
  @ApiModelProperty(value = "审核类型：1：商户入网")
  private Integer type;
  /**
   * 审核状态：-1：审核拒绝 0：未提交审核  1：审核通过 2：审核中
   */
  @ApiModelProperty(value = "审核状态：-1：审核拒绝 0：未提交审核  1：审核通过 2：审核中")
  private Integer state;
  /**
   * 申请数据
   */
  @ApiModelProperty(value = "申请数据")
  private String data;
  /**
   * 审核拒绝备注
   */
  @ApiModelProperty(value = "审核拒绝备注")
  private String remark;

}
