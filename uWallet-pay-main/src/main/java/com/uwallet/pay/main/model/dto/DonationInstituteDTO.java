package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * 捐赠机构
 * </p>
 *
 * @description: 捐赠机构
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:38:12
 */
@ApiModel("捐赠机构")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationInstituteDTO extends BaseDTO implements Serializable {

    /**
     * 捐赠机构名称
     */
    @ApiModelProperty(value = "捐赠机构名称")
    private String instituteName;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

}
