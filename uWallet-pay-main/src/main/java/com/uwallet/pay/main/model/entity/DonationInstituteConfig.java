package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 捐赠机构配置
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 捐赠机构配置
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:38:26
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "捐赠机构配置")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationInstituteConfig extends BaseEntity implements Serializable {

  /**
   * 
   */
  @ApiModelProperty(value = "")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long instituteId;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private Integer key;
  /**
   * 捐赠金额
   */
  @ApiModelProperty(value = "捐赠金额")
  private BigDecimal value;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String remark;

}
