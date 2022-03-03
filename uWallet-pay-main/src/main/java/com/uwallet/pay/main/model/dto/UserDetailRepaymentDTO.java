package com.uwallet.pay.main.model.dto;

import com.amazonaws.services.dynamodbv2.xspec.B;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 用户详情- 欠款列表DTO
 * </p>
 *
 * @description: 用户详情 欠款列表DTO
 * @author: zhangzeyuan
 * @date: Created in 2021年9月22日14:12:33
 */
@ApiModel("用户详情- 欠款列表DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailRepaymentDTO implements Serializable {


    /**
     * 还款时间
     */
    @ApiModelProperty(value = "还款时间")
    private Long expectRepayTime;


    /**
     * 还款时间 字符串
     */
    @ApiModelProperty(value = "还款时间")
    private String expectRepayTimeStr;


    /**
     * 详情列表
     */
    @ApiModelProperty(value = "详情列表")
    private List<UserDetailRepaymentDetailDTO> detailList;

    /**
     * 本期应还总金额
     * */
    @ApiModelProperty(value = "本期应还总金额")
    private BigDecimal shouldPayAmount;
}
