package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
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
 * 群发消息表
 * </p>
 *
 * @description: 群发消息表
 * @author: baixinyue
 * @date: Created in 2020-02-21 08:50:22
 */
@ApiModel("群发消息表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoticeMassDTO extends BaseDTO implements Serializable {

    /**
     * 定位商户
     */
    @ApiModelProperty(value = "定位商户")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long locating;
    /**
     * 范围
     */
    @ApiModelProperty(value = "范围")
    private BigDecimal range;
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;
    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;
    /**
     * 性别 1：男 2：女 0:全部
     */
    @ApiModelProperty(value = "性别 1：男 2：女 0:全部")
    private Integer sex;
    /**
     * 最小年龄
     */
    @ApiModelProperty(value = "最小年龄")
    private String ageMin;
    /**
     * 最大年龄
     */
    @ApiModelProperty(value = "最大年龄")
    private String ageMax;
    /**
     * 消息类型(0:默认 1：商户 2：理财 3：H5页面)
     */
    @ApiModelProperty(value = "消息类型(0:默认 1：商户 2：理财 3：H5页面)")
    private Integer msgType;
    /**
     * 商户id，msg_type = 1时有数据
     */
    @ApiModelProperty(value = "商户id，msg_type = 1时有数据")
    private String merchantId;
    /**
     * 产品id，msg_type = 2时有数据
     */
    @ApiModelProperty(value = "产品id，msg_type = 2时有数据")
    private String productId;
    /**
     * H5链接URL，msg_type = 3时有数据
     */
    @ApiModelProperty(value = "H5链接URL，msg_type = 3时有数据")
    private String h5Url;
    /**
     * 推送方式 (0：站内信 1：短信 2：邮箱 3：push）
     */
    @ApiModelProperty(value = "推送方式 (0：站内信 1：短信 2：邮箱 3：push）")
    private String sendMode;
    /**
     * 发送时间
     */
    @ApiModelProperty(value = "发送时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long sendTime;
    /**
     * 发送状态 0：未发送 1：已发送
     */
    @ApiModelProperty(value = "发送状态 0：未发送 1：已发送")
    private Integer sendState;

}
