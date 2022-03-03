package com.uwallet.pay.core.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * stripe 基类
 * @author zhangzeyuan
 * @date 2022/1/17 16:22
 */
@Data
public class StripeBaseDTO implements Serializable {

    /**
     * 唯一主键
     */
    private String id;


    /**
     * 元数据
     */
    @ApiModelProperty(value = "元数据")
    private Map<String, Object> metadata;


    /**
     * 创建时间，存储时间戳。秒级
     *
     */
    private Long created;

}
