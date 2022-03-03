package com.uwallet.pay.main.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: liming
 * @Date: 2020/10/19 16:57
 * @Description: 结算信息
 */
@Data
public class SettledInfoDTO {

    private Integer number;

    private BigDecimal amount;
}
