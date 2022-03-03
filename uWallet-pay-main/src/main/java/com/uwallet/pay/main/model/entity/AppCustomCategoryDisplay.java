package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * APP首页自定义分类展示信息
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: APP首页自定义分类展示信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 15:09:09
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "APP首页自定义分类展示信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppCustomCategoryDisplay extends BaseEntity implements Serializable {

  /**
   * 展示在APP的位置（app从上到下 1 2 3 4 5，不重复，支持上下移动）
   */
  @ApiModelProperty(value = "展示在APP的位置（app从上到下 1 2 3 4 5，不重复，支持上下移动）")
  private Integer displayOrder;
  /**
   * 对应的种类（值对应u_static_data value?）
   */
  @ApiModelProperty(value = "对应的种类（值对应u_static_data value?）")
  private Integer categoryType;
  /**
   * 种类名称
   */
  @ApiModelProperty(value = "种类名称")
  private String categoryName;
  /**
   * APP显示的描述信息
   */
  @ApiModelProperty(value = "APP显示的描述信息")
  private String description;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;

}
