package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * <p>
 * 对账表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 对账表
 * @author: aaronS
 * @date: Created in 2021-01-25 16:11:20
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "对账表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReconciliationBatch extends BaseEntity implements Serializable {

  /**
   * 三方对账文件名
   */
  @ApiModelProperty(value = "三方对账文件名")
  private String thirdFileName;
  /**
   * 文件路径
   */
  @ApiModelProperty(value = "文件路径")
  private String path;
  /**
   * 存储文件名
   */
  @ApiModelProperty(value = "存储文件名")
  private String fileName;
  /**
   * 通道类型：
   */
  @ApiModelProperty(value = "通道类型：")
  private Integer type;
  /**
   * 交易类型: 0、支付 1、退款
   */
  @ApiModelProperty(value = "交易类型: 0、支付 1、退款")
  private Integer transactionType;
  /**
   * 对账文件总条数
   */
  @ApiModelProperty(value = "对账文件总条数")
  private Integer totalNumber;
  /**
   * 失败条数
   */
  @ApiModelProperty(value = "失败条数")
  private Integer failNumber;
  /**
   * 对账状态：0:处理中， 1：成功， 2：处理失败
   */
  @ApiModelProperty(value = "对账状态：0:处理中， 1：成功， 2：处理失败")
  private Integer state;

}
