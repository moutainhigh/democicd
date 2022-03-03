package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: liming
 * @Date: 2020/10/22 10:28
 * @Description: 结算记录
 */
@Data
public class SettlementInfoDTO {

    /**
     * 交易流水id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long flowId;

    /**
     * 商户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;

    /**
     * 商户名
     */
    private String merchantName;

    /**
     * 生意类型
     */
    private Integer entityType;

    /**
     * 主营业务
     */
    private Integer mainBusiness;

    /**
     * 商业注册号
     */
    private String abn;

    /**
     * 清算条数
     */
    private Integer clearNumber;

    /**
     * 清算金额
     */
    private BigDecimal clearAmount;

    /**
     * 清算状态
     */
    private Integer state;

    /**
     * 清算时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long createdDate;
}
