package com.uwallet.pay.main.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("LatPay支付交易结果查询返回参数表")
@Data
public class LatPayStatusCheckResDTO {

    private Integer  responsetype;
    private String  authstatuscheck_id;
    private String  merchant_ref_number;
    private String  amount;
    private String  currency;
    private String  authstatuscheck_status;
    private String  lps_transaction_id;
    private String  transaction_status;
    private String  bank_transaction_no;
    private String  bank_authorisation_no;
    private String  transactiondate;
    private String  crdstrg_token;
}
