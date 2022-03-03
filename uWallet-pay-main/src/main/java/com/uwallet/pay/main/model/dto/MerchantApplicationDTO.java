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
 * 商户申请表
 * </p>
 *
 * @description: 商户申请表
 * @author: zhoutt
 * @date: Created in 2021-04-15 15:03:19
 */
@ApiModel("商户申请表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantApplicationDTO extends BaseDTO implements Serializable {

    /**
     *
     */
    @ApiModelProperty(value = "")
    private String email;
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
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
     * 审核类型：0：开户1：商户入网 2：整体出售
     */
    @ApiModelProperty(value = "审核类型：0：开户1：商户入网 2：整体出售")
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

    /**
     * 创建时间用与存储转换后时间
     * */

    /**
     * 整体出售意向id
     */
    @ApiModelProperty(value = "整体出售意向id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long wholeSaleId;
    private String operator;
}
