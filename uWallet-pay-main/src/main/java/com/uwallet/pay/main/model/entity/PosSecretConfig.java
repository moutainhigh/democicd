package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * pos商户秘钥配置表
 * </p>
 *
 * @package: com.fenmi.generator.entity
 * @description: pos商户秘钥配置表
 * @author: zhangzeyuan
 * @date: Created in 2021-03-24 14:32:28
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "pos商户秘钥配置表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PosSecretConfig extends BaseEntity implements Serializable {

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
