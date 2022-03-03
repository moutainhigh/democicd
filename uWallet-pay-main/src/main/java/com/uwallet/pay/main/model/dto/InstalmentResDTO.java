package com.uwallet.pay.main.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InstalmentResDTO {

        /**
         * 让利用户
         */
        private BigDecimal percentageToUser;
        /**
         * 让利平台
         */
        private BigDecimal percentageToPlatform;

}
