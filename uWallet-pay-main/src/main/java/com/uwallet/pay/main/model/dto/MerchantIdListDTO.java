package com.uwallet.pay.main.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class MerchantIdListDTO {

    /**
     * 商户id
     */
    private List<Long> idList;
}
