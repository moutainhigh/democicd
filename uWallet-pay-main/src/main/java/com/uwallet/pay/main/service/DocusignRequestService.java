package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.MerchantDTO;

import javax.servlet.http.HttpServletRequest;


/**
 * @author baixinyue
 * @createdDate 2020/08/20
 * @desciption docusign请求
 */

@FunctionalInterface
public interface DocusignRequestService {

    JSONObject docusignRequest(MerchantDTO merchantDTO, String docusignContractId, HttpServletRequest request) throws Exception;

}
