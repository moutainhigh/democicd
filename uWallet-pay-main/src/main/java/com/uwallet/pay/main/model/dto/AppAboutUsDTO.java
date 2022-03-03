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
 * app 关于我们
 * </p>
 *
 * @description: app 关于我们
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:53
 */
@ApiModel("app 关于我们")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppAboutUsDTO extends BaseDTO implements Serializable {

    /**
     * logo路径
     */
    @ApiModelProperty(value = "logo路径")
    private String path;
    /**
     * 简介
     */
    @ApiModelProperty(value = "简介")
    private String appIntro;
    /**
     * 电话
     */
    @ApiModelProperty(value = "电话")
    private String phone;
    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;

}
