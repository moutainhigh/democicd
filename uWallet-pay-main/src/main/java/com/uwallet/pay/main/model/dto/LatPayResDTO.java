package com.uwallet.pay.main.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * LatPay
 */
@ApiModel("LatPay支付返回参数表")
@Data
public class LatPayResDTO implements Serializable {

    //token代扣响应
    private Integer  ResponseType;
    private String  LPS_transaction_id;
    private String  Merchant_ref_number;
    private String  Lpsid;
    private String  Lpspwd;
    private String  Fraudscreening_status;
    private String  Bank_status;
    private String  Amount;
    private String  Currency;
    private String  Bank_transaction_no;
    private String  Bank_authorisation_no;
    private String  Bank_date;
    private String  Bank_time;
    private String  Bank_original_code;

    //代扣交易查证响应
    private Integer responsetype;
    private String authstatuscheck_id;
    private String merchant_ref_number;
    private String amount;
    private String currency;
    private String authstatuscheck_status;
    private String lps_transaction_id;
    private String transaction_status;
    private String bank_transaction_no;
    private String bank_authorisation_no;
    private String transactiondate;
    private String crdstrg_token;

    //卡令牌生成响应
//    private Integer ResponseType;
    private String RequestType;
    private String Requestid;
    private String CrdStrg_Token;
    private String Bill_firstname;
    private String Bill_lastname;
    private String Bill_address1;
    private String Bill_address2;
    private String Bill_city;
    private String Bill_state;
    private String Bill_country;
    private String Bill_zip;
    private String CardBin;
    private String CardLast4;
    private String Customer_cc_type;
    private String Customer_cc_expmo;
    private String Customer_cc_expyr;
    private String Customer_dc_startmo;
    private String Customer_dc_startyr;
    private String Status;



}
