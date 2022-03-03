package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 捐赠流水
 * </p>
 *
 * @description: 捐赠用户列表
 * @author: zhoutt
 * @date: Created in 2021-07-23 08:37:50
 */
@ApiModel("捐赠流水")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationUserListDTO  extends BaseDTO implements Serializable {


    private String userName;

    private String userId;

    private String phone;

    private Integer unSettledCount;

    private BigDecimal unSettledAmount;

    private Integer settledCount;

    private BigDecimal settledAmount;

    private Integer delayCount;

    private BigDecimal delayAmount;

}
