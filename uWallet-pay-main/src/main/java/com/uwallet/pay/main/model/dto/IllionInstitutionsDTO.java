package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * @date: Created in 2021-03-19 09:37:47
 */
@ApiModel("")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IllionInstitutionsDTO extends BaseDTO implements Serializable {

    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String name;
    /**
     * 银行简写
     */
    @ApiModelProperty(value = "银行简写")
    private String slug;
    /**
     * logo地址
     */
    @ApiModelProperty(value = "logo地址")
    private String img;

}
