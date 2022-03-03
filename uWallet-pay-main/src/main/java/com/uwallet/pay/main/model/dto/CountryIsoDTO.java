package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryIsoDTO {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 国家
     */
    private String country;
    /**
     * iso编码
     */
    private String digitalCoding;
    /**
     * iso编码
     */
    private String twoLettersCoding;
    /**
     * iso编码
     */
    private String threeLettersCoding;
    /**
     * iso编码
     */
    private Integer status;

}
