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
 * 审核日志
 * </p>
 *
 * @description: 审核日志
 * @author: zhoutt
 * @date: Created in 2021-09-23 15:39:54
 */
@ApiModel("审核日志")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiApproveLogDTO extends BaseDTO implements Serializable {

    /**
     * 商户id
     */
    @ApiModelProperty(value = "商户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 商户级别
     */
    @ApiModelProperty(value = "商户级别")
    private Integer merchantClass;
    /**
     * 城市
     */
    @ApiModelProperty(value = "城市")
    private Integer merchantCity;
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

    private ApiMerchantDTO merchantDTO;

}
