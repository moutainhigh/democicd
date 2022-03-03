package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * Tag数据
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: Tag数据
 * @author: aaronS
 * @date: Created in 2021-01-07 11:19:48
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "Tag数据")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tag extends BaseEntity implements Serializable {

  /**
   *
   */
  @ApiModelProperty(value = "")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long parentId;
  /**
   * tag中文名
   */
  @ApiModelProperty(value = "tag中文名")
  private String cnName;
  /**
   * tag英文名
   */
  @ApiModelProperty(value = "tag英文名")
  private String enValue;
  /**
   * 搜索计数
   */
  @ApiModelProperty(value = "搜索计数")
  private BigDecimal popular;
  /**
   * 是否展示: 1.展示 0.不展示
   */
  @ApiModelProperty(value = "是否展示: 1.展示 0.不展示")
  private Integer showState;

}
