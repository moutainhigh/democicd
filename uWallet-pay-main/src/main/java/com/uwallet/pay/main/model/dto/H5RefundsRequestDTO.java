package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * <p>
 * h5退款请求参数
 * </p>
 *
 * @description: h5退款请求参数
 * @author: zhoutt
 * @date: Created in 2021-08-19 10:44:10
 */
@ApiModel("h5退款请求参数")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class H5RefundsRequestDTO  {

    private String payment;

    private String reason;

    private H5RequestAmountDTO amount;

    private H5RequestMerchantDTO merchant;

    private String idKey;

}
