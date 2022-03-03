package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * pos商户秘钥配置表
 * </p>
 *
 * @description: pos商户秘钥配置表
 * @author: zhangzeyuan
 * @date: Created in 2021-03-24 14:32:28
 */
@ApiModel("pos商户秘钥配置表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PosSecretConfigDTO extends BaseDTO implements Serializable {

    /**
     * 商户ID
     */
    @ApiModelProperty(value = "商户ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 颁发私钥信息
     */
    @ApiModelProperty(value = "颁发私钥信息")
    private String privateSecret;
    /**
     * 过期时间时间戳
     */
    @ApiModelProperty(value = "过期时间时间戳")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long expiredDate;

}
