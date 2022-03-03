package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.experimental.Tolerate;

import java.io.Serializable;
/**
 * <p>
 * 用户的收藏商户数据
 * </p>
 *
 * @description: 用户的收藏商户数据
 * @author: aaron S
 * @date: Created in 2021-04-07 18:04:58
 */
@ApiModel("用户的收藏商户数据")
@Builder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavoriteMerchantDTO extends BaseDTO implements Serializable {

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

    @Tolerate
    public FavoriteMerchantDTO(){}

}
