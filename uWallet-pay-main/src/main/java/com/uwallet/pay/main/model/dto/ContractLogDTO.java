package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
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
 * 合同记录表
 * </p>
 *
 * @description: 合同记录表
 * @author: xucl
 * @date: Created in 2021-04-27 10:13:42
 */
@ApiModel("合同记录表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractLogDTO extends BaseDTO implements Serializable {

    /**
     * 文件路径
     */
    @ApiModelProperty(value = "文件路径")
    private String fileUrl;
    /**
     * 原文件名
     */
    @ApiModelProperty(value = "原文件名")
    private String fileOldName;
    /**
     * 商户id
     */
    @ApiModelProperty(value = "商户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 文件状态 1使用 2删除
     */
    @ApiModelProperty(value = "文件状态 1使用 2删除")
    private Integer state;

}
