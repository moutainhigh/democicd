package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
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
 * 小费清算文件记录
 * </p>
 *
 * @description: 小费清算文件记录
 * @author: zhangzeyuan
 * @date: Created in 2021-08-11 17:21:14
 */
@ApiModel("小费清算文件记录")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TipClearFileRecordDTO extends BaseDTO implements Serializable {

    /**
     * 清算总条数
     */
    @ApiModelProperty(value = "清算总条数")
    private Long totalNumber;
    /**
     * 实际清算金额
     */
    @ApiModelProperty(value = "实际清算金额")
    private BigDecimal clearAmount;
    /**
     * 导出文件名
     */
    @ApiModelProperty(value = "导出文件名")
    private String fileName;
    /**
     * 文件url
     */
    @ApiModelProperty(value = "文件url")
    private String url;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long settlementDate;


    /**
     * 清算批次号
     */
    @ApiModelProperty(value = "清算批次号")
    private Long clearBatchId;

}
