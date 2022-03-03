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
 * 码操作记录表
 * </p>
 *
 * @description: 码操作记录表
 * @author: xucl
 * @date: Created in 2021-03-09 09:55:32
 */
@ApiModel("码操作记录表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeUpdateLogDTO extends BaseDTO implements Serializable {

    /**
     * 商户ID
     */
    @ApiModelProperty(value = "商户ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 码
     */
    @ApiModelProperty(value = "码")
    private String code;
    /**
     * 码类型：0 nfc 码 1 QR码
     */
    @ApiModelProperty(value = "码类型：0 nfc 码 1 QR码")
    private Integer type;
    /**
     * 操作类型 0 绑定 1 解绑
     */
    @ApiModelProperty(value = "操作类型 0 绑定 1 解绑")
    private Integer state;

}
