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
 * 节假日表
 * </p>
 *
 * @description: 节假日表
 * @author: baixinyue
 * @date: Created in 2020-09-08 11:24:52
 */
@ApiModel("节假日表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidaysConfigDTO extends BaseDTO implements Serializable {

    /**
     * 年
     */
    @ApiModelProperty(value = "年")
    private Integer year;
    /**
     * 节假日 MM-dd
     */
    @ApiModelProperty(value = "节假日 MM-dd")
    private String holidays;

}
