package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 合同记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 合同记录表
 * @author: fenmi
 * @date: Created in 2021-04-29 10:11:38
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "合同记录表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantContractFileRecord extends BaseEntity implements Serializable {

  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long merchantId;
  /**
   * 文件路径
   */
  @ApiModelProperty(value = "文件路径")
  private String filePath;
  /**
   * 原文件名
   */
  @ApiModelProperty(value = "原文件名")
  private String fileOldName;
  /**
   * 文件名
   */
  @ApiModelProperty(value = "文件名")
  private String fileName;
  /**
   * 文件类型
   */
  @ApiModelProperty(value = "文件类型")
  private String fileType;

}
