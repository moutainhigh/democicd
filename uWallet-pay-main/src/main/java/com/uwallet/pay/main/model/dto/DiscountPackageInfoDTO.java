package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import lombok.Data;

/**
 * @author: liming
 * @Date: 2020/10/19 16:34
 * @Description: 整体出售待结算商户信息
 */
@Data
public class DiscountPackageInfoDTO {

    /**
     * 商户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;
    /**
     * 店长id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;

    /**
     * 商户名
     */
    private String merchantName;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 生意类型
     */
    private Integer entityType;

    /**
     * 主营业务
     */
    private Integer mainBusiness;

    /**
     * ABN
     */
    private String abn;

    /**
     * 银行账号BSB
     */
    private String bsb;

    /**
     * 银行账号
     */
    private String accountNo;

    /**
     * 开户人姓名
     */
    private String accountName;

    /**
     * 银行账号开户行
     */
    private String bankName;

    /**
     * 未结算数据
     */
    private SettledInfoDTO toBeSettled;

    /**
     * 结算数据
     */
    private SettledInfoDTO settled;


    /**
     * 延时结算数据
     */
    private SettledInfoDTO delayed;

    /**
     * 不延时结算数据
     */
    private SettledInfoDTO undelayed;

    /**
     * 通过审核的总单数
     */
    private SettledInfoDTO total;
}
