package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 清算批次
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 清算批次
 * @author: zhoutt
 * @date: Created in 2020-09-29 09:59:01
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "清算批次")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClearBatch extends BaseEntity implements Serializable {

  /**
   * 清算总条数
   */
  @ApiModelProperty(value = "清算总条数")
  private Long totalNumber;
  /**
   * 清算总金额
   */
  @ApiModelProperty(value = "清算总金额")
  private BigDecimal totalAmount;
  /**
   * 订单总金额
   */
  @ApiModelProperty(value = "订单总金额")
  private BigDecimal borrowAmount;
  /**
   * 实际清算金额
   */
  @ApiModelProperty(value = "实际清算金额")
  private BigDecimal clearAmount;
  /**
   * 导出文件名
   */
  @ApiModelProperty(value = "导出文件名")
  private String fileName;
  /**
   * 清算类型 0：三方渠道 1：api商户
   */
  @ApiModelProperty(value = "清算类型 0：三方渠道 1：api商户")
  private Integer clearType;
  /**
   * 文件url
   */
  @ApiModelProperty(value = "文件url")
  private String url;
  /**
   * 清算状态 0：处理中 1：处理成功 2：失败
   */
  @ApiModelProperty(value = "清算状态 0：处理中 1：处理成功 2：失败")
  private Integer state;
  /**
   * 文件下载状态 0：未下载 1：已下载
   */
  @ApiModelProperty(value = "文件下载状态 0：未下载 1：已下载")
  private Integer fileState;
  /**
   *
   */
  @ApiModelProperty(value = "")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long clearStartDate;
  /**
   *
   */
  @ApiModelProperty(value = "")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long clearEndDate;
  /**
   * 通道ID
   */
  @ApiModelProperty(value = "通道ID")
  private Integer gatewayId;
  /**
   * 对账时间
   */
  @ApiModelProperty(value = "对账时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long checkTime;
  /**
   * 对账状态
   */
  @ApiModelProperty(value = "对账状态")
  private Integer checkState;

}
