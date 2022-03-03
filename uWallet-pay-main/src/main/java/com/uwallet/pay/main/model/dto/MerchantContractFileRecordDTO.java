package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
 * 合同记录表
 * </p>
 *
 * @description: 合同记录表
 * @author: fenmi
 * @date: Created in 2021-04-29 10:11:38
 */
@ApiModel("合同记录表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantContractFileRecordDTO extends BaseDTO implements Serializable {

    /**
     * 商户id
     */
    @ApiModelProperty(value = "商户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 文件路径
     */
    @ApiModelProperty(value = "文件路径")
    private String filePath;
    /**
     * 原文件名
     */
    @ApiModelProperty(value = "原文件名")
    private String fileOldName;
    /**
     * 文件名
     */
    @ApiModelProperty(value = "文件名")
    private String fileName;
    /**
     * 文件类型
     */
    @ApiModelProperty(value = "文件类型")
    private String fileType;

    /**
     * 操作人
     */
    @ApiModelProperty(value = "操作人")
    private String userName;

}
