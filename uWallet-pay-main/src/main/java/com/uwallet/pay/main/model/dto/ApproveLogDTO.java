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
/**
 * <p>
 * 审核日志表
 * </p>
 *
 * @description: 审核日志表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:34:12
 */
@ApiModel("审核日志表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApproveLogDTO extends BaseDTO implements Serializable {

    /**
     * 商户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "商户id")
    private Long merchantId;
    /**
     * 审核类型：0：商户入网审核 1：商户信息修改审核
     */
    @ApiModelProperty(value = "审核类型：0：商户入网审核 1：商户信息修改审核")
    private Integer approveType;
    /**
     * 审核状态：-1：审核拒绝 0:待审核  1：审核通过 2：审核中
     */
    @ApiModelProperty(value = "审核状态：-1：审核拒绝 0:待审核  1：审核通过 2：审核中")
    private Integer state;
    /**
     * 审核人id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "审核人id")
    private Long approvedBy;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String data;
    /**
     * 审核拒绝备注
     */
    @ApiModelProperty(value = "审核拒绝备注")
    private String remark;

    // ===================================================================================================
    /**
     * 邮箱（商户账号）
     */
    @ApiModelProperty(value = "邮箱（商户账号）")
    private String email;
    /**
     * 二维码编号
     */
    @ApiModelProperty(value = "二维码编号")
    private String code;
    /**
     * 商户
     */
    @ApiModelProperty(value = "商户")
    private MerchantDTO merchantDTO;

}
