package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 批量发送消息表
 * </p>
 *
 * @description: 批量发送消息表
 * @author: xucl
 * @date: Created in 2021-05-11 14:18:13
 */
@ApiModel("批量发送消息表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageBatchSendLogDTO extends BaseDTO implements Serializable {

    /**
     * 发送类型 1push 2appMessage
     */
    @ApiModelProperty(value = "发送类型 1push 2appMessage")
    private Integer sendType;
    /**
     * 状态 1未发送 2发送成功 3 不可用
     */
    @ApiModelProperty(value = "状态 1未发送 2发送成功 3 不可用")
    private Integer state;
    /**
     * 发送时间
     */
    @ApiModelProperty(value = "发送时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long sendTime;
    /**
     * 发送内容
     */
    @ApiModelProperty(value = "发送内容")
    private String content;
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;
    /**
     * 发送条数
     */
    @ApiModelProperty(value = "发送条数")
    private Integer sendNumber;
    /**
     * 发送成功条数
     */
    @ApiModelProperty(value = "发送成功条数")
    private Integer sendSuccessNumber;
    /**
     * 创建时查询总条数
     */
    @ApiModelProperty(value = "创建时查询总条数")
    private Integer findNumber;
    /**
     * 州
     */
    @ApiModelProperty(value = "州")
    private Integer territory;
    /**
     * 城市
     */
    @ApiModelProperty(value = "城市")
    private Integer city;
    /**
     * 用户状态
     */
    @ApiModelProperty(value = "用户状态")
    private Integer userStatus;
    /**
     * 产生费用
     */
    @ApiModelProperty(value = "产生费用")
    private BigDecimal money;
    /**
     * push跳转 1 app首页
     */
    @ApiModelProperty(value = "push跳转 1 app首页")
    private Integer pushRedirect;

    private String sendTime2;
    private String sendTime1;
    private String operator;
    private String ids;
}
