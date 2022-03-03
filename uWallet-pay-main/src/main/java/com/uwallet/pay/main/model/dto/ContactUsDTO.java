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
 * 联系我们
 * </p>
 *
 * @description: 联系我们
 * @author: baixinyue
 * @date: Created in 2020-06-17 08:52:22
 */
@ApiModel("联系我们")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactUsDTO extends BaseDTO implements Serializable {

    /**
     * 姓名
     */
    @ApiModelProperty(value = "姓名")
    private String name;
    /**
     * 电话
     */
    @ApiModelProperty(value = "电话")
    private String mobile;
    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;
    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String message;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private Integer state;

}
