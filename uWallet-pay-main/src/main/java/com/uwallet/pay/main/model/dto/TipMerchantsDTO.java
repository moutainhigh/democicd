package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@ApiModel("包含小费商户列表商户")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TipMerchantsDTO extends BaseDTO implements Serializable {
    /**
     * 商户名称
     * */
    private String practicalName;
    /**
     * 商户城市
     * */
    private Integer city;
    /**
     * ABN
     * */
    private String ABN;
    /**
     * 商户id
     * */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    private Integer unclearedNumber;
    private BigDecimal unclearedAmount;
    private Integer clearedNumber;
    private BigDecimal clearedAmount;
    private Integer delayClearNumber;
    private BigDecimal delayClearAmount;
}
