package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.SetupIntent;
import com.uwallet.pay.core.common.StripeAPIResponse;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.StripeCustomerDTO;
import com.uwallet.pay.main.model.dto.StripeSetupIntentDTO;
import com.uwallet.pay.main.model.dto.UserDTO;
import com.uwallet.pay.main.service.ServerService;
import com.uwallet.pay.main.service.StripeAPIService;
import com.uwallet.pay.main.service.StripeBusinessService;
import com.uwallet.pay.main.service.UserService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author zhangzeyuan
 * @date 2022年01月10日 15:13
 */
@Slf4j
@Service
public class StripeBussinessServiceImpl implements StripeBusinessService {

    @Resource
    private UserService userService;
    @Resource
    private ServerService serverService;


    @Resource
    private StripeAPIService stripeAPIService;

    /**
     * 获取stripe客户端秘钥
     *
     * @param userId
     * @author zhangzeyuan
     * @date 2022/1/19 10:18
     */
    @Override
    public JSONObject getClientSecret(Long userId) throws Exception{
        Customer customer = null;
        //根据用户ID获取customer
        StripeAPIResponse retrieveUserRes = stripeAPIService.retrieveCustomer(userId);
        if(!retrieveUserRes.isSuccess()){
            //查询用户信息
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
            map.put("id", userId);
            UserDTO oneUser = userService.findOneUser(map);
            //封装用户信息
            StripeCustomerDTO stripeCustomer = packageStripeCustomerByUserDTO(oneUser);
            StripeAPIResponse createUserRes = stripeAPIService.createCustomer(stripeCustomer);
            if(!createUserRes.isSuccess()){
                throw new BizException();
            }
            customer = (Customer) createUserRes.getData();
        }else{
            customer = (Customer) retrieveUserRes.getData();
        }


        //创建 设置未来付款意向
        StripeSetupIntentDTO setupIntentDTO = new StripeSetupIntentDTO();
        setupIntentDTO.setCustomer(customer.getId());

        List<String> paymentMethodTypes = new ArrayList<>();
        paymentMethodTypes.add("card");
        setupIntentDTO.setPayment_method_types(paymentMethodTypes);

        StripeAPIResponse setupIntentRes = stripeAPIService.createSetupIntent(setupIntentDTO);

        if(!setupIntentRes.isSuccess()){
            throw new BizException();
        }
        SetupIntent setupIntent = (SetupIntent) setupIntentRes.getData();

        JSONObject result = new JSONObject();
        result.put("clientSecret", setupIntent.getClientSecret());
        result.put("email", customer.getEmail());
        return result;
    }

    /**
     * @param cardToken
     * @param userId
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2022/1/20 17:30
     */
    @Override
    public Card bindCard(@NotBlank String cardToken, @NotNull Long userId, HttpServletRequest request) throws Exception {
        //获取客户
        Customer customer = getOrCreateCustomer(userId);
        if(null == customer){
            throw new BizException(I18nUtils.get("card.bind.failed", getLang(request)));
        }
        //添加卡
        StripeAPIResponse cardByCardToken = stripeAPIService.createCardByCardToken(userId, cardToken);
        if(!cardByCardToken.isSuccess()){
            throw new BizException(I18nUtils.get("card.bind.failed", getLang(request)));
        }
        return (Card) cardByCardToken.getData();
    }

    /**
     * 根据用户实体封装创建stripe需要的customer
     * @author zhangzeyuan
     * @date 2022/1/19 10:31
     * @param userDTO
     * @return com.uwallet.pay.main.model.dto.StripeCustomerDTO
     */
    private StripeCustomerDTO packageStripeCustomerByUserDTO(UserDTO userDTO){
        StripeCustomerDTO customerDTO = new StripeCustomerDTO();
        customerDTO.setId(userDTO.getId().toString());
        customerDTO.setEmail(userDTO.getEmail());
        customerDTO.setName(userDTO.getUserFirstName() + " " + userDTO.getUserLastName());
        customerDTO.setPhone(userDTO.getPhone());
        return customerDTO;
    }


    /**
     * 得到一个customer  没有则创建
     *  customer可能为空！
     * @author zhangzeyuan
     * @date 2022/1/20 17:35
     * @param userId
     * @return com.stripe.model.Customer
     */
    private Customer getOrCreateCustomer(Long userId){
        //获取客户并绑卡
        Customer customer = null;
        //根据用户ID获取customer
        StripeAPIResponse retrieveUserRes = stripeAPIService.retrieveCustomer(userId);
        if(!retrieveUserRes.isSuccess()){
            //查询用户信息
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
            map.put("id", userId);
            UserDTO oneUser = userService.findOneUser(map);
            //封装用户信息
            StripeCustomerDTO stripeCustomer = packageStripeCustomerByUserDTO(oneUser);
            StripeAPIResponse createUserRes = stripeAPIService.createCustomer(stripeCustomer);
            customer = (Customer) createUserRes.getData();
        }else{
            customer = (Customer) retrieveUserRes.getData();
        }
        return customer;
    }


    /**
     * 获取当前语言，默认保持英文
     * @author faker
     * @param request
     * @return
     */
    public Locale getLang(HttpServletRequest request) {
        Locale lang = Locale.US;
        // 获取当前语言
        String headerLang = request.getHeader("lang");
        Locale locale = LocaleContextHolder.getLocale();
        if (StringUtils.isNotEmpty(headerLang) && "zh-CN".equals(headerLang)) {
            lang = Locale.SIMPLIFIED_CHINESE;
        }
        return lang;
    }

}
