package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户详情- 欠款列表详情DTO
 * </p>
 *
 * @description: 用户详情 欠款列表详情DTO
 * @author: zhangzeyuan
 * @date: Created in 2021年9月22日14:12:33
 */
@ApiModel("用户详情- 欠款列表详情DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailRepaymentDetailDTO implements Serializable {

    @JsonSerialize(using = LongJsonSerializer.class)
    private Long flowId;

    @JsonSerialize(using = LongJsonSerializer.class)
    private Long borrowId;

    @JsonSerialize(using = LongJsonSerializer.class)
    private Long repayId;

    /**
     * 订单编号
     */
    @ApiModelProperty(value = "订单编号")
    private String transNo;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    /**
     * 类型 1 订单 2 逾期费
     */
    @ApiModelProperty(value = "类型")
    private Integer orderType;

    /**
     * 本期应还期数编号
     */
    @ApiModelProperty(value = "本期应还期数编号")
    private String periodSort;


    /**
     * 本期应还金额
     */
    @ApiModelProperty(value = "本期应还金额")
    private BigDecimal shouldPayAmt;

    /**
     * 逾期天数
     */
    @ApiModelProperty(value = "逾期天数")
    private String overdueDays;


    /**
     * 是否逾期 1：是 0否
     */
    @ApiModelProperty(value = "是否逾期")
    private Integer overdueStatus;

    /**
     * 订单时间
     * */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long createdDate;

    private String expectRepayTimeStr;

}
