package com.uwallet.pay.main.model.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * 用户
 * </p>
 *
 * @description: 用户
 * @author: baixinyue
 * @date: Created in 2019-12-10 17:57:14
 */
@ApiModel("用户")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ExcelTarget("emp")
public class UserExcelDTO implements Serializable {
//    /**
//     * 邮箱
//     */
//    @Excel(name = "邮箱",  width = 30, isImportField = "true_st")
//    @ApiModelProperty(value = "邮箱")
//    private String email;

    /**
     * 手机号
     */
    @Excel(name = "手机号",  width = 30, isImportField = "true_st")
    @ApiModelProperty(value = "手机号")
    private String phone;
    /**
     * 创建时间，存储时间戳。
     * 获取示例：<code>System.currentTimeMillis()</code>
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long createdDate;
    /**
     * 创建时间，存储时间戳。
     * 获取示例：<code>System.currentTimeMillis()</code>
     */
    @Excel(name = "注册时间", isImportField = "true_st", width = 30)
    private String date;

    /**
     * 开始时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private String start;

    /**
     * 结束时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private String end;
//    /**
//     * 余额
//     */
//    @Excel(name = "余额", isImportField = "true_st", width = 30)
//    @ApiModelProperty(value = "余额")
//    private BigDecimal balance;
//    /**
//     * BSB
//     */
//    @Excel(name = "BSB", isImportField = "true_st", width = 30)
//    @ApiModelProperty(value = "BSB")
//    private String bsb;
//    /**
//     * 银行账号
//     */
//    @Excel(name = "银行账号", isImportField = "true_st", width = 30)
//    @ApiModelProperty(value = "银行账号")
//    private String cardNo;
//    /**
//     * 银行名
//     */
//    @Excel(name = "银行名", isImportField = "true_st", width = 30)
//    @ApiModelProperty(value = "银行名")
//    private String bankName;
//    /**
//     * 银行卡号
//     */
//    @Excel(name = "银行卡号", isImportField = "true_st", width = 30)
//    @ApiModelProperty(value = "银行卡号")
//    private String card;
//    /**
//     * 银行卡背面的安全码（CVN/CVV2）
//     */
//    @Excel(name = "银行卡背面的安全码（CVN/CVV2）", isImportField = "true_st", width = 30)
//    @ApiModelProperty(value = "银行卡背面的安全码（CVN/CVV2）")
//    private String customerCcCvc;
//    /**
//     * 有效期
//     */
//    @Excel(name = "有效期", isImportField = "true_st", width = 30)
//    @ApiModelProperty(value = "有效期")
//    private String expirationDate;
//    /**
//     * 持卡人姓名
//     */
//    @Excel(name = "持卡人姓名", isImportField = "true_st", width = 30)
//    @ApiModelProperty(value = "持卡人姓名")
//    private String name;

}
