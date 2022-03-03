package com.uwallet.pay.core.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.uwallet.pay.core.common.ConstantCore;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Jwt工具类
 *
 * @author
 * @date 2019/03/29
 */
@Slf4j
public class JwtUtils {
    /**
     * token过期时长设置(毫秒)
     */
    private static final long EXPIRE_TIME = -1;
    /**
     * Token中存储登录名的变量名
     */
    public static final String TOKEN_CLAIM_KEY_USERNAME = "loginName";
    /**
     * Token中存储登录账号id
     */
    public static final String TOKEN_CLAIM_KEY_ID = "id";
    /**
     * Token中存储登录密码的变量名
     */
    public static final String TOKEN_CLAIM_KEY_PASSWORD = "loginPwd";
    /**
     * redis缓存key，记录token值
     */
    public static String TOKEN_CACHE_KEY_PREFIX = "shiro:cache:token:";
    /**
     * Token中存储登录账号身份标识(a:管理员admin,b:用户merchant)
     */
    public static final String TOKEN_CLAIM_KEY_Person = "person";
    /**
     * Token中存储时间戳
     */
    public static final String TOKEN_CLAIM_KEY_Current = "current";
    /**
     * app中存储tokenKey
     */
    public static final String APP_TOKEN_KEY = "appTokenKey";
    /**
     * api中存储用户登陆tokenKey
     */
    public static final String API_TOKEN_KEY = "apiTokenKey";
    /**
     * api中存储接入方登陆tokenKey
     */
    public static final String ACCESS_PLATFORM_TOKEN_KEY = "accessTokenKey";
    /**
     * api中存储接入方登陆tokenKey
     */
    public static final String ACCESS_PLATFORM_ID = "accessPlatformId";
    /**
     *  token发行者应该声明为”payo”
     */
    public static final String  H5_TOKEN_PAYO = "payo";
    /**
     * 私钥
     */
    private static final String TOKEN_SECRET = "privateKey";
    /**
     * H5pay中存储接入方登陆tokenKey
     */
    private static final String H5_PAY_TOKEN_SECRET = "jti";


    /**
     * APP登陆生成token
     * @param loginName
     * @param password
     * @param userType
     * @param redisUtils
     * @return
     */
    public static String signApp(Long userId, String loginName, String password, Integer userType, RedisUtils redisUtils) {
        Algorithm algorithm = Algorithm.HMAC256(loginName);
        String tokenKey = userId + "_" + loginName + "_" + userType;
        String token = JWT.create()
                .withClaim(APP_TOKEN_KEY, tokenKey)
                .withClaim(TOKEN_CLAIM_KEY_ID, userId)
                .withClaim(TOKEN_CLAIM_KEY_USERNAME, loginName)
                // .withClaim(TOKEN_CLAIM_KEY_PASSWORD, password)
                .withClaim(TOKEN_CLAIM_KEY_Current, System.currentTimeMillis())
                .sign(algorithm);
        // redis记录token
        redisUtils.set(tokenKey, token);
        return token;
    }

    /**
     * API登陆生成token
     * @param loginName
     * @param password
     * @param userType
     * @param redisUtils
     * @return
     */
    public static String signApi(Long userId, String loginName, String password, Integer userType, RedisUtils redisUtils) {
        Algorithm algorithm = Algorithm.HMAC256(loginName);
        String tokenKey = userId + "_" + loginName + "_" + userType + "_API";
        String token = JWT.create()
                .withClaim(API_TOKEN_KEY, tokenKey)
                .withClaim(TOKEN_CLAIM_KEY_ID, userId)
                .withClaim(TOKEN_CLAIM_KEY_USERNAME, loginName)
                // .withClaim(TOKEN_CLAIM_KEY_PASSWORD, password)
                .withClaim(TOKEN_CLAIM_KEY_Current, System.currentTimeMillis())
                .sign(algorithm);
        // redis记录token
        redisUtils.set(tokenKey, token);
        return token;
    }

    /**
     * 接入方登陆token
     * @param accessPlatformId
     * @param redisUtils
     * @return
     */
    public static String accessApi(Long accessPlatformId, RedisUtils redisUtils) {
        Algorithm algorithm = Algorithm.HMAC256(accessPlatformId.toString());
        String tokenKey = accessPlatformId + "_API";
        String token = JWT.create()
                .withClaim(ACCESS_PLATFORM_TOKEN_KEY, tokenKey)
                .withClaim(ACCESS_PLATFORM_ID, accessPlatformId)
                .sign(algorithm);
        // redis记录token
        redisUtils.set(tokenKey, token, 60 * 60 * 8);
        return token;
    }

    /**
     * 生成签名
     *
     * @param loginName 用户名
     * @param password  用户的密码
     * @return 加密的token
     */
    public static String sign(String loginName, String password, RedisUtils redisUtils) {
        Algorithm algorithm = Algorithm.HMAC256(password);
        String tokenKey = getRedisTokenKey(loginName);
        String token;
        //有过期时间则设置过期时间
        if (EXPIRE_TIME >= 0) {
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            // 附带username信息
            token = JWT.create()
                    .withClaim(TOKEN_CLAIM_KEY_USERNAME, loginName)
                    .withClaim(TOKEN_CLAIM_KEY_PASSWORD, password)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } else {
            // 附带username信息
            token = JWT.create()
                    .withClaim(TOKEN_CLAIM_KEY_USERNAME, loginName)
                    .withClaim(TOKEN_CLAIM_KEY_PASSWORD, password)
                    .sign(algorithm);
        }
        // redis记录token
        redisUtils.set(tokenKey, token, 600);
        return token;
    }

    /**
     * 生成签名 为了给admin用户登陆成功,返回带客户端ip
     *
     * @param loginName 用户名
     * @param password  用户的密码
     * @return 加密的token
     */
    public static String signs(String loginName,Long id,String sign,String password, RedisUtils redisUtils) {
        Algorithm algorithm = Algorithm.HMAC256(password);
        //String tokenKey = getRedisTokenKey(loginName);
        String token;
        String now = String.valueOf(System.currentTimeMillis());
        //有过期时间则设置过期时间
        if (EXPIRE_TIME >= 0) {
            Date date = new Date(now + EXPIRE_TIME);
            // 附带username信息
            token = JWT.create()
                    .withClaim(TOKEN_CLAIM_KEY_USERNAME, loginName)
                    .withClaim(TOKEN_CLAIM_KEY_PASSWORD, password)
                    .withClaim(TOKEN_CLAIM_KEY_ID, id)
                    .withClaim(TOKEN_CLAIM_KEY_Person, sign)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } else {
            // 附带username信息
            //里面只能放String字符串例如 now
            token = JWT.create()
                    .withClaim(TOKEN_CLAIM_KEY_USERNAME, loginName)
                    .withClaim(TOKEN_CLAIM_KEY_PASSWORD, password)
                    .withClaim(TOKEN_CLAIM_KEY_ID, id)
                    .withClaim(TOKEN_CLAIM_KEY_Person, sign)
                    .withClaim(TOKEN_CLAIM_KEY_Current, now)
                    .sign(algorithm);
        }
        // redis记录token
        //redis中token的key值添加标识 用于区分管理员和用户,否则同一台服务器ip会覆盖key值
        redisUtils.set(sign+loginName, token, ConstantCore.TOKEN_TIME);
        return token;
    }

    /**
     * redis校验token是否正确
     *
     * @param token 密钥
     * @return 是否正确
     */
    public static R verify(String token, RedisUtils redisUtils) {
        try {
            String loginName = getUsername(token);
            String tokenKey = getRedisTokenKey(loginName);
            // 获取redis中token
            String redisToken = redisUtils.get(tokenKey).toString();
            if (token.equals(redisToken)) {
                log.info("token验证成功,key:{}", tokenKey);
                return R.success();
            } else {
                log.info("token验证失败");
                return R.fail();
            }
        } catch (Exception exception) {
            log.info("token验证失败，异常：{}", exception);
            return R.fail(ErrorCodeEnum.LOGIN_TOKEN_CHECK_ERROR.getCode(), ErrorCodeEnum.LOGIN_TOKEN_CHECK_ERROR.getMessage());
        }
    }

    /**
     * 获得token中的信息无需password解密也能获得
     *
     * @return token中包含的用户名密码
     */
    public static String getUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(TOKEN_CLAIM_KEY_USERNAME).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }
    /**
     * 获得token中的信息无需password解密也能获得
     *
     * @return token中包含的用户名密码
     */
    public static Long getId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
//            System.out.println(jwt.getClaim(TOKEN_CLAIM_KEY_ID).asLong());
            return jwt.getClaim(TOKEN_CLAIM_KEY_ID).asLong();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 获得token中的信息无需password解密也能获得
     *
     * @return token中包含的用户名密码
     */
    public static String getPassword(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(TOKEN_CLAIM_KEY_PASSWORD).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }
    /**
     * 获得token中的信息无需password解密也能获得
     *
     * @return token中包含的用户名密码
     */
    public static String getPerson(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(TOKEN_CLAIM_KEY_Person).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }
    /**
     * 获得token中的信息无需password解密也能获得
     *
     * @return token中包含的用户名密码
     */
    public static String getCurrent(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(TOKEN_CLAIM_KEY_Current).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 获取app token key
     * @param token
     * @return
     */
    public static String getAppTokenKey(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(APP_TOKEN_KEY).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 获取api token key
     * @param token
     * @return
     */
    public static String getApiTokenKey(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(API_TOKEN_KEY).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 获取api token key
     * @param token
     * @return
     */
    public static String getAccessTokenKey(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(ACCESS_PLATFORM_TOKEN_KEY).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

//    /**
//     * 校验token是否正确
//     *
//     * @param token    密钥
//     * @param username 登录名
//     * @param password 密码
//     * @return
//     */
//    public static boolean verifys(String token, String username, String password) {
//        try {
//            Algorithm algorithm = Algorithm.HMAC256(password);
//
//            JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
//
//            DecodedJWT jwt = verifier.verify(token);
//            System.out.println("成功出来");
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public static String getRedisTokenKey(String loginName) {
        return  loginName;
    }
    /**
     * APP登陆生成token
     * @param loginName
     * @param redisUtils
     * @return
     */
    public static String signH5(Long userId, String loginName, RedisUtils redisUtils) {
        Algorithm algorithm = Algorithm.HMAC256(loginName);
        String tokenKey = userId + "_" + loginName+"_H5";
        String token = JWT.create()
                .withClaim(APP_TOKEN_KEY, tokenKey)
                .withClaim(TOKEN_CLAIM_KEY_ID, userId)
                .withClaim(TOKEN_CLAIM_KEY_USERNAME, loginName)
                .withClaim(TOKEN_CLAIM_KEY_Current, System.currentTimeMillis())
                .sign(algorithm);
        // redis记录token
        redisUtils.set(tokenKey, token,ConstantCore.TOKEN_TIME);
        return token;
    }

    /**
     * 生成商户请求Token
     * @param subject 账号(商户号)
     * @param pwp 接受者为 使用pwp的服务方
     * @return
     */
    public static String h5PayQuestToken(String subject,String pwp, RedisUtils redisUtils) {

        String[] strArray={"pay-with-payo"};
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        String tokenKey = subject+"_H5_"+System.currentTimeMillis();
        String token = JWT.create()
                .withHeader(map)
                .withSubject(subject)
                .withClaim(H5_PAY_TOKEN_SECRET, tokenKey)
                .withClaim("iss", "urn:payo:issuer:test")
                .withClaim("aud", "urn:payo:audience:services")
                .withArrayClaim("urn:payo:services", strArray)
                //.withIssuer(H5_TOKEN_PAYO)
                //.withAudience(pwp)
                .withExpiresAt(new Date(System.currentTimeMillis() + 24*60*60*1000))
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .sign(algorithm);

        // redis记录token
        redisUtils.set(tokenKey, token, 24*60*60);
        return token;
    }
    /**
     * 获取app token key
     * @param token
     * @return
     */
    public static String verify(String token){
        try {
            String tokens = token.replaceFirst("Bearer ", "").trim();
            DecodedJWT jwt = JWT.decode(tokens);
            Date exp = jwt.getExpiresAt();
            Date now = new Date(System.currentTimeMillis());
            Boolean expDate = exp.before(now);
            if(expDate){
                return expDate.toString();
            }
            String subject = jwt.getClaim(H5_PAY_TOKEN_SECRET).asString();
            return subject;
        }catch (Exception e){
            return null;
        }
    }
    /**
     * 获取DecodedJWT
     * @param token
     * @return
     */
    public static DecodedJWT verifyCode(String token){
        try {
            String tokens = token.replaceFirst("Bearer ", "").trim();
            DecodedJWT jwt = JWT.decode(tokens);
            return jwt;
        }catch (Exception e){
            return null;
        }
    }



}
