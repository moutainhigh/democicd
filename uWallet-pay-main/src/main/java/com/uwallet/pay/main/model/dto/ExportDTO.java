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
 * 用户
 * </p>
 *
 * @description: 用户
 * @author: baixinyue
 * @date: Created in 2019-12-10 17:57:14
 */
@ApiModel("用户")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExportDTO extends BaseDTO implements Serializable {

    /**
     * 文件下载路径
     */
    @ApiModelProperty(value = "文件下载路径")
    private String path;
}
