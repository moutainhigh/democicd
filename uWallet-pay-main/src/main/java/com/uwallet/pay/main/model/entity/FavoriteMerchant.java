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
 * 用户的收藏商户数据
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户的收藏商户数据
 * @author: aaron S
 * @date: Created in 2021-04-07 18:04:58
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "用户的收藏商户数据")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavoriteMerchant extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 商户id,merchant表主键
   */
  @ApiModelProperty(value = "商户id,merchant表主键")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long merchantId;

}
