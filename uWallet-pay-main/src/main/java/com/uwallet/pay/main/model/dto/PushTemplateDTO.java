package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
/**
 * <p>
 * 模板
 * </p>
 *
 * @description: 模板
 * @author: zhoutt
 * @date: Created in 2020-01-04 13:52:28
 */
@ApiModel("模板")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushTemplateDTO extends BaseDTO implements Serializable {

    /**
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
    private String name;
    /**
     * 发送节点
     */
    @ApiModelProperty(value = "发送节点")
    private Integer sendingNode;
    /**
     * 模板内容
     */
    @ApiModelProperty(value = "模板内容")
    private String content;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private Integer state;

}
