package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 捐赠机构配置
 * </p>
 *
 * @description: 捐赠机构配置
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:38:26
 */
@ApiModel("捐赠机构配置")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationInstituteConfigDTO extends BaseDTO implements Serializable {

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
