package com.uwallet.pay.main.docuSignComponent;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.client.auth.OAuth;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.FormatUtil;
import com.uwallet.pay.main.config.DocuSignConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static com.uwallet.pay.main.config.DocuSignConfig.*;

/**
 * This is an example base class to be extended to show functionality example.
 * its has a apiClient member as a constructor argument for later usage in API calls.
 *
 * @author aaron.S
 * @since 2020/8/18
 */
@Component
@Slf4j
public class DocuSignClient {
    /**
     * 配置文件 生产环境的 服务器类型标识符
     */
    private static final String SERVER_TYPE_PROD="prod";

    /**
     * 区分生产账号与测试
     */
    @Value("${server.type}")
    private void testOrProd(String serverType) {
        if (serverType.equals(SERVER_TYPE_PROD)){
            DocuSignClient.DS_AUTH_SERVER = DocuSignConfig.DS_AUTH_SERVER_prod;
            DocuSignClient.DS_CLIENT_ID = DocuSignConfig.DS_CLIENT_ID_prod;
            DocuSignClient.DS_IMPERSONATED_USER_GUID = DocuSignConfig.DS_IMPERSONATED_USER_GUID_prod;
            DocuSignClient.privateKeyString = DocuSignConfig.privateKeyString_prod;
            DocuSignClient.BASE_PATH = DocuSignConfig.basePath_prod;
            DocuSignClient.ACCOUNT_ID = DocuSignConfig.accountId_prod;
        }else {
            DocuSignClient.DS_AUTH_SERVER = DocuSignConfig.DS_AUTH_SERVER_test;
            DocuSignClient.DS_CLIENT_ID = DocuSignConfig.DS_CLIENT_ID_test;
            DocuSignClient.DS_IMPERSONATED_USER_GUID = DocuSignConfig.DS_IMPERSONATED_USER_GUID_test;
            DocuSignClient.privateKeyString = DocuSignConfig.privateKeyString_test;
            DocuSignClient.BASE_PATH = basePath_test;
            DocuSignClient.ACCOUNT_ID = accountId_test;
        }
    }

    private static final long TOKEN_EXPIRATION_IN_SECONDS = 3600*1000;
    private static OAuth.Account OAUTH_ACCOUNT;
    private static long expiresAt;
    private static String TOKEN = null;
    private static String DS_AUTH_SERVER;
    private static String DS_CLIENT_ID;
    private static String DS_IMPERSONATED_USER_GUID;
    private static String DS_TARGET_ACCOUNT_ID = null;
    private static String privateKeyString;
    private static String BASE_PATH;
    private static String ACCOUNT_ID;

    private final ApiClient apiClient=new ApiClient();




    /**
     * 获取ApiClient对象(设置好参数的)
     * @return
     */
    public  ApiClient getApiClient() throws Exception{
        try{
            //检查Token状态,如果为空或者过期,则重置
            if(DocuSignClient.TOKEN == null || (System.currentTimeMillis() ) > DocuSignClient.expiresAt) {
                updateToken();
            }
        } catch (Exception e){
             e.printStackTrace();
             log.error("DocuSign update token error ");
             throw new BizException("\nDocuSign Fetching access token Error");
        }
        return this.apiClient;
    }

    private void updateToken() throws IOException, ApiException {
        System.out.println("\nDocuSign Fetching an access token via JWT grant....................");

        java.util.List<String> scopes = new ArrayList<String>();
        // Only signature scope is needed. Impersonation scope is implied.
        scopes.add(OAuth.Scope_SIGNATURE);
        byte[] privateKeyBytes = privateKeyString.getBytes();
        /*
         生产使用:account.docusign.com
         */
        apiClient.setOAuthBasePath(DS_AUTH_SERVER);
        /*
           获取access token
         */
        OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken (
                                                            DS_CLIENT_ID,
                                                            DS_IMPERSONATED_USER_GUID,
                                                            scopes,
                                                            privateKeyBytes,
                                                            TOKEN_EXPIRATION_IN_SECONDS);
        apiClient.setAccessToken(oAuthToken.getAccessToken(), oAuthToken.getExpiresIn());
        if(OAUTH_ACCOUNT == null) {
            OAUTH_ACCOUNT=new OAuth.Account();
            OAUTH_ACCOUNT.setAccountId(ACCOUNT_ID);
            OAUTH_ACCOUNT.setBaseUri(BASE_PATH);
            OAUTH_ACCOUNT.setIsDefault("true");
            OAUTH_ACCOUNT.setAccountName(accountName);
        }
        apiClient.setBasePath(OAUTH_ACCOUNT.getBaseUri() + DocuSignConfig.API_PATH);
        TOKEN = apiClient.getAccessToken();
        /*
         官方返回的过期时间为秒
         */
        expiresAt = System.currentTimeMillis() + (oAuthToken.getExpiresIn()*1000);
        System.out.println("Done. Continuing...\nToken expires at :"+ FormatUtil.getTodayTimeField(new Date(expiresAt),"dd-MM-yyyy  hh:mm:ss:ms") +"....................");
    }
}
