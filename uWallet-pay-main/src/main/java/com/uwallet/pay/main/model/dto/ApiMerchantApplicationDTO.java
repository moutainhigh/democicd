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
 * h5 api 商户申请表
 * </p>
 *
 * @description: h5 api 商户申请表
 * @author: zhoutt
 * @date: Created in 2021-09-23 10:25:50
 */
@ApiModel("h5 api 商户申请表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMerchantApplicationDTO extends BaseDTO implements Serializable {

    /**
     * 商户等级 0：一级 1：二级
     */
    @ApiModelProperty(value = "商户等级 0：一级 1：二级")
    private Integer merchantClass;
    /**
     * 一级商户id
     */
    @ApiModelProperty(value = "一级商户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long superMerchantId;
    /**
     * 商户id
     */
    @ApiModelProperty(value = "商户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String practicalName;
    /**
     * abn
     */
    @ApiModelProperty(value = "abn")
    private String abn;
    /**
     * 审核类型：1：商户入网
     */
    @ApiModelProperty(value = "审核类型：1：商户入网")
    private Integer type;
    /**
     * 审核状态：-1：审核拒绝 0：未提交审核  1：审核通过 2：审核中
     */
    @ApiModelProperty(value = "审核状态：-1：审核拒绝 0：未提交审核  1：审核通过 2：审核中")
    private Integer state;
    /**
     * 申请数据
     */
    @ApiModelProperty(value = "申请数据")
    private String data;
    /**
     * 审核拒绝备注
     */
    @ApiModelProperty(value = "审核拒绝备注")
    private String remark;

    private String operator;

}
