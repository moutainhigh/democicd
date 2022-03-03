package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
/**
 * <p>
 * 商户修改记录表
 * </p>
 *
 * @description: 商户修改记录表
 * @author: xucl
 * @date: Created in 2021-03-15 08:39:48
 */
@ApiModel("商户修改记录表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantUpdateLogDTO extends BaseDTO implements Serializable {

    /**
     * 商户ID
     */
    @ApiModelProperty(value = "商户ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 修改前Json
     */
    @ApiModelProperty(value = "修改前Json")
    private String onUpdate;
    /**
     * 修改后Json
     */
    @ApiModelProperty(value = "修改后Json")
    private String afterUpdate;
    /**
     * 修改类型：0 基础信息修改 1费率修改
     */
    @ApiModelProperty(value = "修改类型：0 基础信息修改 1费率修改")
    private Integer updateType;

}
