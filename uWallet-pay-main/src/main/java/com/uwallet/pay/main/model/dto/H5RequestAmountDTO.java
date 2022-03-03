package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Digits;

/**
 * <p>
 * h5请求金额
 * </p>
 *
 * @description: h5请求金额
 * @author: zhoutt
 * @date: Created in 2021-08-19 10:44:10
 */
@ApiModel("h5请求金额")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class H5RequestAmountDTO {

    private Long amount;

    private String currency;
}
