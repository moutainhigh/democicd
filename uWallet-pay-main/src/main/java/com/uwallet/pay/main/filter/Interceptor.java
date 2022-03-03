package com.uwallet.pay.main.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.uwallet.pay.core.common.ConstantCore;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.main.model.entity.UserActionButton;
import com.uwallet.pay.main.service.AccessPlatformService;
import com.uwallet.pay.main.service.UserActionButtonService;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.core.util.JwtUtils;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.SignErrorCode;
import com.uwallet.pay.main.exception.SignException;
import com.uwallet.pay.main.exception.TokenException;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.AdminService;
import com.uwallet.pay.main.util.CheckAppSignUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author: Strong
 * @date: 2019-07-2
 * @modified: Strong
 */
public class Interceptor extends BaseServiceImpl implements HandlerInterceptor {
	/**
	 * H5pay中存储接入方登陆tokenKey
	 */
	private static final String H5_PAY_TOKEN_SECRET = "jti";

	@Autowired
	private RedisUtils redisUtils;
	@Autowired
	private AdminService adminService;
	@Autowired
	private AccessPlatformService accessPlatformService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private UserActionButtonService buttonService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		//去掉OPTIONS 请求,否则token获取为null
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		HttpServletResponse httpResponse = WebUtils.toHttp(response);
		if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
			httpResponse.setHeader("Access-control-Allow-Origin", httpRequest.getHeader("Origin"));
			httpResponse.setHeader("Access-Control-Allow-Headers",
					httpRequest.getHeader("Access-Control-Request-Headers"));
			httpResponse.setStatus(HttpStatus.OK.value());
			return false;
		}
		// 校验的token
		// 检查该用户是否具有该路由的访问权限
		// 如果不是映射到方法直接通过
		if(!(handler instanceof HandlerMethod)){
			return true;
		}
		HandlerMethod handlerMethod=(HandlerMethod)handler;
		Method methods=handlerMethod.getMethod();
		// 接口验证
		if (methods.isAnnotationPresent(SignVerify.class)) {
			SignVerify pass = methods.getAnnotation(SignVerify.class);
			if (pass.required()) {
				BufferedReader streamReader = new BufferedReader( new InputStreamReader(request.getInputStream(), "UTF-8"));
				StringBuilder responseStrBuilder = new StringBuilder();
				String inputStr;
				while ((inputStr = streamReader.readLine()) != null) {
					responseStrBuilder.append(inputStr);
				}
				JSONObject article = JSONObject.parseObject(responseStrBuilder.toString());
				boolean flag = CheckAppSignUtil.decrypt(article.toJSONString(),request);
				if (!flag) {
					throw new SignException(SignErrorCode.SIGN_ERROR.getCode(), SignErrorCode.SIGN_ERROR.getMessage());
				}

			}
		}
		// 接口验证
		if (methods.isAnnotationPresent(H5SignVerify.class)) {
			H5SignVerify pass = methods.getAnnotation(H5SignVerify.class);
			if (pass.required()) {
				String sign = request.getHeader("sign");
				String timestamp = request.getHeader("timestamp");
				JSONObject param=new JSONObject();
				param.put("sign",sign);
				param.put("timestamp",timestamp);
				boolean flag = CheckAppSignUtil.decryptH5(param.toJSONString(),request);
				if (!flag) {
					throw new SignException(SignErrorCode.SIGN_ERROR.getCode(), SignErrorCode.SIGN_ERROR.getMessage());
				}
			}
		}
		// 验证H5PayToken
		if (methods.isAnnotationPresent(H5PayToken.class)) {
			//验证token
			H5PayToken h5PayToken = methods.getAnnotation(H5PayToken.class);
			if (h5PayToken.required()) {
				String authHeader = request.getHeader("Authorization");
				if (authHeader == null || !authHeader.startsWith("Bearer ")) {
					//throw new SignException(SignErrorCode.SIGN_ERROR.getCode(),"Missing or invalid Authorization header");
					throw new SignException(SignErrorCode.SIGN_ERROR.getCode(),"100200403");
				}
				String tokens = authHeader.replaceFirst("Bearer ", "").trim();
				DecodedJWT verifyCode = JwtUtils.verifyCode(tokens);
				if(null == verifyCode){
					throw new SignException(SignErrorCode.SIGN_ERROR.getCode(),"100200404");
				}
				String subject = verifyCode.getClaim(H5_PAY_TOKEN_SECRET).asString();
				if(null == subject){
					throw new SignException(SignErrorCode.SIGN_ERROR.getCode(),"100200404");
				}
				String saveToken = (String) redisUtils.get(subject);
				if (saveToken != null && tokens.equals(saveToken)) {
					return true;
				} else {
					throw new SignException(SignErrorCode.SIGN_ERROR.getCode(),"100200404");
					//throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), "The token is incorrectly obtained. Procedure");
				}
			}
		}
		//(1)检查是否有passtoken注释，无需登录,有则跳过认证
		if (methods.isAnnotationPresent(PassToken.class)) {
			PassToken passToken = methods.getAnnotation(PassToken.class);
			if (passToken.required()) {
				return true;
			}
		}
		// 验证操作管理员权限
		if (methods.isAnnotationPresent(AdminActionButton.class)){
			int value = methods.getAnnotation(AdminActionButton.class).value();
			if (value!=-1){
				String headTokenValue = request.getHeader("Authorization");
				Long loginName = JwtUtils.getId(headTokenValue.replace("Bearer ", ""));
				JSONObject param=new JSONObject();
				param.put("userId",loginName);
				param.put("type",value);
				UserActionButtonDTO oneUserActionButton = buttonService.findOneUserActionButton(param);
//				if (oneUserActionButton.getId()==null){
//					throw new BizException(I18nUtils.get("action.rule.sign", getLang(request)));
//				}
			}
		}

		// 验证AppToken
		if (methods.isAnnotationPresent(AppToken.class)) {
			//redis 接口幂等
			/*if (methods.isAnnotationPresent(InterfaceIdempotent.class)) {
				String apiToken = request.getHeader("apiToken");
				if (StringUtils.isBlank(apiToken) || ! redisTemplate.delete(apiToken)){
					throw new SignException(SignErrorCode.SIGN_ERROR.getCode(),"Invalid request, please refresh and try again");
				}
			}*/
			/*
			  3/Dec/2020 添加逻辑,所有未添加强制更新的app 禁止访问后台, 强制更新到新版本后才可使用
			 */
			/*String appType = request.getHeader("appType");
			if (StaticDataEnum.APP_TYE.getMessage().equals(appType)) {
				if (StringUtils.isBlank(request.getHeader("isSplitAdapted"))) {
					throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("app.need.update", getLang(request)));
				}
			}*/
			AppToken appToken = methods.getAnnotation(AppToken.class);
			if (appToken.required()) {
				String token = request.getHeader("Authorization");
				String tokenKey = JwtUtils.getAppTokenKey(token);
				String saveToken = (String) redisUtils.get(tokenKey);
				if (saveToken != null && token.equals(saveToken)) {
					return true;
				} else if (saveToken==null){
					throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
				}else if (!token.equals(saveToken)){
					throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("login.rule.another.time", getLang(request)));
				}else {
					throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
				}
			}
		}
		// 验证AccessToken
		if (methods.isAnnotationPresent(AccessToken.class)) {
			AccessToken accessToken = methods.getAnnotation(AccessToken.class);
			if (accessToken.required()) {
				String accessTokenValue = request.getHeader("accessToken");
				// 验证api用户登陆token
				if (accessPlatformService.apiVerify(accessTokenValue, request) != null) {
					// 验证ApiToken
					if (methods.isAnnotationPresent(ApiToken.class)) {
						ApiToken apiToken = methods.getAnnotation(ApiToken.class);
						if (apiToken.required()) {
							String apiTokenValue = request.getHeader("apiToken");
							String apiTokenKey = JwtUtils.getApiTokenKey(apiTokenValue);
							String saveApiToken = (String) redisUtils.get(apiTokenKey);
							if (saveApiToken != null && apiTokenValue.equals(saveApiToken)) {
								return true;
							} else {
								throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
							}
						}
					} else {
						return true;
					}
				} else {
					throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), I18nUtils.get("filter.rule.timeOut", getLang(request)));
				}
			}
		}
		//判断访问用户身份, 管理员 标识: admin,有访问权限功能,没有权限则不能访问 用户 标识:merchant,目前没有权限拦截
		String sign;
		String loginName;
		// 从请求头中获取token的值,不是key(前台传)
		String requestURI = request.getRequestURI();
		String headTokenValue = request.getHeader("Authorization");
		if (headTokenValue != null && !Constant.NULL.equals(headTokenValue)) {
			sign = JwtUtils.getPerson(headTokenValue.replace("Bearer ", ""));
			loginName = JwtUtils.getUsername(headTokenValue.replace("Bearer ", ""));
		} else {
			setCorsMappings(request, response);
			throw new TokenException(I18nUtils.get("login.rule.no",getLang(request)), ErrorCodeEnum.INTERCEPTOR__ERROR.getCode());
		}

		if (Constant.ADMIN.equals(sign) && StringUtils.isNotEmpty(sign)) {
			boolean hasKey;
			hasKey = redisUtils.hasKey(sign+loginName);
			// 判断是否Redis中存在
			if (!hasKey) {
				//(1)此时可能是用户访问还是会传递token,但是redis中已经失效
				setCorsMappings(request, response);
				throw new TokenException(I18nUtils.get("filter.rule.timeOut",getLang(request)), ErrorCodeEnum.INTERCEPTOR__ERROR.getCode());
			} else {
				String redisvalue = (String) redisUtils.get(sign+loginName);
				// postman 返回的数据带Bearer 字符串	redisvalue = "Bearer " + redisvalue;
				if (redisvalue.replace(Constant.BEARER, "").equals(headTokenValue.replace(Constant.BEARER, ""))) {
					//(2)检查有没有需要用户权限的注解
					if (methods.isAnnotationPresent(ActionFlag.class)) {
						ActionFlag userAction = methods.getAnnotation(ActionFlag.class);
						if (userAction.required()) {
							ActionFlag annotation = methods.getAnnotation(ActionFlag.class);
							String detail = annotation.detail();
							String[] details;
							if(StringUtils.isNotEmpty(detail)) {
								details= detail.split(",");
							}else {
								throw new TokenException(I18nUtils.get("action.rule.sign",getLang(request)), ErrorCodeEnum.INTERCEPTOR__ERROR.getCode());
							}
						// 查询用户的权限
                        AdminDTO actions = null;
						if (!StringUtils.isEmpty(loginName)) {
							//先查看redis中是否有该用户权限
							if (redisUtils.hasKey(sign+loginName+Constant.ACTION)) {
								String str = redisUtils.get(sign+loginName+Constant.ACTION).toString();
								actions = JSON.parseObject(str,AdminDTO.class);
							} else {
								// 根据传过来的用户名查询数据库中的权限
								actions = adminService.findActionByAdmin(loginName);
								//将第一次查出来的权限存入redis中
								redisUtils.set(sign+loginName+Constant.ACTION, JSON.toJSONString(actions), ConstantCore.TOKEN_TIME);
							}
						}
                            List<AdminRoleDTO> riskAdminRoleDTO = actions.getAdminRoleDTO();
                            for(AdminRoleDTO riskAdminRole:riskAdminRoleDTO){
                                List<RoleDTO> riskRoleDTO = riskAdminRole.getRoleDTO();
                                for(RoleDTO riskRole:riskRoleDTO){
                                    List<RoleActionDTO> riskRoleActionDTO = riskRole.getRoleActionDTO();
                                    for(RoleActionDTO riskRoleAction:riskRoleActionDTO){
                                        List<ActionOnlyDTO> riskActionDTO = riskRoleAction.getActionDTO();
                                        if (null != riskActionDTO && !riskActionDTO.isEmpty()) {
                                            for (ActionOnlyDTO act : riskActionDTO) {
                                                boolean res = Arrays.asList(details).contains(act.getIdentification());
                                                if(res){
                                                    // 该用户具有该路由的访问权限,下面的拦截器不拦截
                                                    // 同时重置redis缓存中的token时间
                                                    redisUtils.set(sign+loginName, headTokenValue, ConstantCore.TOKEN_TIME);
													//同时重置redis缓存中的权限时间
													redisUtils.set(sign+loginName+Constant.ACTION, JSON.toJSONString(actions), ConstantCore.TOKEN_TIME);
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
						}else {
							//userAction.required()获取失败
							//没有权限
							setCorsMappings(request, response);
							throw new TokenException(I18nUtils.get("action.rule.no",getLang(request)), ErrorCodeEnum.INTERCEPTOR__ERROR.getCode());
						}
						}else {
							//没有添加拦截注解
							return true;
						}
				} else {
					// 此情况是用户之前登录成功,但是这次请求传入的token值错误,不是登录时的token值
					setCorsMappings(request, response);
					throw new TokenException(I18nUtils.get("login.rule.another",getLang(request)), ErrorCodeEnum.INTERCEPTOR__ERROR.getCode());
				}
			}
		} else if (Constant.MERCHANT.equals(sign) && StringUtils.isNotEmpty(sign)) {
			boolean hasKey;
			hasKey = redisUtils.hasKey(sign+loginName);
			// 判断是否Redis中存在
			if (!hasKey) {
				//(1)此时可能是用户访问还是会传递token,但是redis中已经失效
				//(2)被加入黑名单
				setCorsMappings(request, response);
				throw new TokenException(I18nUtils.get("filter.rule.timeOut",getLang(request)), ErrorCodeEnum.INTERCEPTOR__ERROR.getCode());
			} else {
				String redisvalue = (String) redisUtils.get(sign+loginName);
				// postman 返回的数据带Bearer 字符串 redisvalue = "Bearer " + redisvalue;
				if (redisvalue.replace(Constant.BEARER, "").equals(headTokenValue.replace(Constant.BEARER, ""))) {
					// 是此用户自己的token值,已经登录,继续此次请求
					// 同时重置redis缓存中的token时间
					redisUtils.set(sign+loginName, headTokenValue, ConstantCore.TOKEN_TIME);
					return true;
				} else {
					// 此情况是用户之前登录成功,但是这次请求传入的token值错误,不是登录时的token值
					setCorsMappings(request, response);
					throw new TokenException(I18nUtils.get("login.rule.another",getLang(request)), ErrorCodeEnum.INTERCEPTOR__ERROR.getCode());
				}
			}

		} else {
			setCorsMappings(request, response);
			throw new TokenException(I18nUtils.get("account.type.error",getLang(request)), ErrorCodeEnum.INTERCEPTOR__ERROR.getCode());
		}

		//没有权限
		setCorsMappings(request, response);
		throw new TokenException(I18nUtils.get("action.rule.no",getLang(request)), ErrorCodeEnum.INTERCEPTOR__ERROR.getCode());
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			 {
	}
	private void setCorsMappings(HttpServletRequest request, HttpServletResponse response){
		String origin = request.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Origin", origin);
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with,Authorization");
		response.setHeader("Access-Control-Allow-Credentials", "true");
	}

}
