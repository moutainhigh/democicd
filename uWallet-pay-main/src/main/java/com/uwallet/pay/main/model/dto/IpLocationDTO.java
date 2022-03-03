package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
/**
 * <p>
 * ip定位
 * </p>
 *
 * @description: ip定位
 * @author: baixinyue
 * @date: Created in 2021-01-12 13:54:55
 */
@ApiModel("ip定位")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpLocationDTO extends BaseDTO implements Serializable {

    /**
     * ip
     */
    @ApiModelProperty(value = "ip")
    private Long ipFrom;
    /**
     * ip
     */
    @ApiModelProperty(value = "ip")
    private Long ipTo;
    /**
     * 国家
     */
    @ApiModelProperty(value = "国家")
    private String country;
    /**
     * 州
     */
    @ApiModelProperty(value = "州")
    private String state;
    /**
     * 城市
     */
    @ApiModelProperty(value = "城市")
    private String city;
    /**
     * 纬度
     */
    @ApiModelProperty(value = "经度")
    private String lng;
    /**
     * 经度
     */
    @ApiModelProperty(value = "纬度")
    private String lat;

}
