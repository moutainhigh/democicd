package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.stripe.model.Card;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** stripe 业务相关service
 * @author zhangzeyuan
 * @date 2022年01月10日 15:12
 */

public interface StripeBusinessService {


    /**
     * 获取stripe客户端秘钥
     * @author zhangzeyuan
     * @date 2022/1/19 10:18
     * @param userId
     */
     JSONObject getClientSecret(@NotNull Long userId) throws Exception;
     
     
     /**
      * 绑卡
      * @author zhangzeyuan
      * @date 2022/1/20 17:30
      * @param userId 
      * @return com.alibaba.fastjson.JSONObject
      */
     Card bindCard(@NotBlank String cardToken, @NotNull Long userId, HttpServletRequest request) throws Exception;



}
