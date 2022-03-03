package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * <p>
 * h5请求金额
 * </p>
 *
 * @description: h5请求金额
 * @author: caisj
 * @date: Created in 2021-08-19 10:44:10
 */
@ApiModel("h5请求金额")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class H5CreateTotalRequestAmountDTO {

    private String amount;

    private String currency;
}
