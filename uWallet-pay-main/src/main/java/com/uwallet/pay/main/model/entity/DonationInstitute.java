package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 捐赠机构
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 捐赠机构
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:38:12
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "捐赠机构")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationInstitute extends BaseEntity implements Serializable {

  /**
   * 捐赠机构名称
   */
  @ApiModelProperty(value = "捐赠机构名称")
  private String instituteName;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;

}
