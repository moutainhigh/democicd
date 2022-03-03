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
 *
 * </p>
 *
 * @description:
 * @author: xucl
 * @date: Created in 2021-04-16 15:56:48
 */
@ApiModel("")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushAndSendMessageLogDTO extends BaseDTO implements Serializable {

    /**
     * 请求json数据
     */
    @ApiModelProperty(value = "请求json数据")
    private String data;
    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 发送类型1push2短信
     */
    @ApiModelProperty(value = "发送类型1push2短信")
    private Integer type;

}
