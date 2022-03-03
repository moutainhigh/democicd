package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * 邮件替换内容DTO
 * </p>
 *
 * @description:
 * @author:
 * @date:
 */
@ApiModel("邮件替换内容DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplaceEmailContentDTO implements Serializable {

    /**
     * 替换变量名
     */
    @ApiModelProperty(value = "替换变量名")
    private String name;
    /**
     * 替换 内容
     */
    @ApiModelProperty(value = "替换内容")
    private String value;

}
