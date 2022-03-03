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
/**
 * <p>
 * 二维码信息、绑定表
 * </p>
 *
 * @description: 二维码信息、绑定表
 * @author: baixinyue
 * @date: Created in 2019-12-11 16:10:10
 */
@ApiModel("二维码信息、绑定表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrcodeInfoDTO extends BaseDTO implements Serializable {

    /**
     * 关联用户
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "关联用户")
    private Long userId;
    /**
     * 商户id
     */
    @ApiModelProperty(value = "商户id")
    private Long merchantId;
    /**
     * 二维码信息
     */
    @ApiModelProperty(value = "二维码信息")
    private String code;
    /**
     * 二维码服务器路径
     */
    @ApiModelProperty(value = "二维码服务器路径")
    private String path;
    /**
     * 二维码跳转路由
     */
    @ApiModelProperty(value = "二维码跳转路由")
    private String hopRouting;
    /**
     * 10： 用户 20： 商户
     */
    @ApiModelProperty(value = "10： 用户 20： 商户")
    private Integer qrcodeUserType;
    /**
     * 关联时间
     */
    @ApiModelProperty(value = "关联时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long correlationTime;
    /**
     * 0:未关联 1:关联
     */
    @ApiModelProperty(value = "0:未关联 1:关联")
    private Integer state;

}
