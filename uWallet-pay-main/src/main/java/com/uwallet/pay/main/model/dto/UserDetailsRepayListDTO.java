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

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户详情还款计划DTO
 * </p>
 *
 * @description: 用户详情还款计划DTO
 * @author: zhangzeyuan
 * @date: Created in 2021-09-16 17:55:12
 */
@ApiModel("用户详情还款计划DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailsRepayListDTO implements Serializable {

    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;

    /**
     * 期数编号
     */
    @ApiModelProperty(value = "期数编号")
    private Integer periodSort;
    /**
     * 应还款日
     */
    @ApiModelProperty(value = "应还款日")
    private String expectRepayTime;
    /**
     * 应还金额
     */
    @ApiModelProperty(value = "应还金额")
    private BigDecimal shouldPayAmout;
    /**
     * 实际还款金额
     */
    @ApiModelProperty(value = "实际还款金额")
    private BigDecimal truelyPayPrincipal;
    /**
     * 卡类型
     */
    @ApiModelProperty(value = "卡类型")
    private Integer cardType;
    /**
     * 还款卡号
     */
    @ApiModelProperty(value = "还款卡号")
    private String cardNo;


    /**
     * 分期还款状态 0  '未结清' 1  '已结清'    2 '处理中'
     */
    private Integer state;


    /**
     * 是否逾期   0 否  1 是  2 未到还款日
     */
    @ApiModelProperty(value = "是否逾期")
    private Integer overdueStatus;

    /**
     * 还款时间
     */
    @ApiModelProperty(value = "还款时间")
    private String truelyRepayTime;

    /**
     *  逾期天数
     */
    @ApiModelProperty(value = "逾期天数")
    private Integer overdueDays;

    /**
     * 还款状态 1：还款代扣，2：主动还款，3：财务平账 4 Unpaid 未支付
     */
    private Integer repayType;

}
