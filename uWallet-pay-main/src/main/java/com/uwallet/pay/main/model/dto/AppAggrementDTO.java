package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
/**
 * <p>
 * app 协议
 * </p>
 *
 * @description: app 协议
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:23
 */
@ApiModel("app 协议")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppAggrementDTO extends BaseDTO implements Serializable {

    /**
     * 协议名
     */
    @ApiModelProperty(value = "协议名")
    private String name;
    /**
     * 10：隐私 20：分期付 30：合同
     */
    @ApiModelProperty(value = "10：隐私 20：分期付 30：合同")
    private Integer type;
    /**
     * 协议内容
     */
    @ApiModelProperty(value = "协议内容")
    private String content;

}
