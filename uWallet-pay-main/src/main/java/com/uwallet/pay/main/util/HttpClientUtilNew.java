package com.uwallet.pay.main.util;


import com.uwallet.pay.core.common.PosApiCodeEnum;
import com.uwallet.pay.main.exception.PosApiException;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @Author Jun
 * @Date 2019/3/28 14:21
 * @Description 使用httpclient发送请求
 */
public class HttpClientUtilNew {
    // 编码格式。发送编码格式统一用UTF-8
    private static final String ENCODING = "UTF-8";
    // 设置连接超时时间，单位毫秒。
    private static final int CONNECT_TIMEOUT = 6000;
    // 请求获取数据的超时时间(即响应时间)，单位毫秒。
    private static final int SOCKET_TIMEOUT = 6000;
    /**
     * setConnectTimeout：设置连接超时时间，单位毫秒。
     * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
     * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
     * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
     */
    private static final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setSocketTimeout(SOCKET_TIMEOUT)
            .build();


    /**
     * 向指定URL发送GET方法的请求
     * 不带参数的get请求
     *
     * @param url 发送请求的URL 例如：http://localhost:8080/demo/login
     * @return URL 所代表远程资源的响应结果
     */
    public static String doGet(String url) {
        return sendGet(url, null, null);
    }

    /**
     * 向指定URL发送GET方法的请求
     * 带参数 不带字符集编码
     *
     * @param url   发送请求的URL 例如：http://localhost:8080/demo/login
     * @param param 请求参数 例：{ "userName":"admin", "password":"123456" }
     * @return URL 所代表远程资源的响应结果
     */
    public static String doGet(String url, Map<String, String> param) {
        return sendGet(url, param, null);
    }


    /**
     * 向指定URL发送GET方法的请求
     * 带参数和字符集的get请求
     *
     * @param url     发送请求的URL 例如：http://localhost:8080/demo/login
     * @param param   请求参数      例：{ "userName":"admin", "password":"123456" }
     * @param charset 字符集编码     例："UTF-8"
     * @return URL 所代表远程资源的响应结果
     */
    public static String doGet(String url, Map<String, String> param, String charset) {
        return sendGet(url, param, charset);
    }


    /**
     * 向指定URL发送POST方法的请求
     *
     * @param url     资源地址
     * @param params  参数列表
     * @param charset 字符编码集
     * @return
     */
    public static String doPost(String url, Map<String, String> params, String charset) {
        return sendPost(url, null, params, charset);
    }

    /**
     * 向指定URL发送POST方法的请求
     *
     * @param url    资源地址
     * @param params 参数列表
     * @return
     */
    public static String doPost(String url, Map<String, String> params) {
        return sendPost(url, null, params, null);
    }

    /**
     * 向指定URL发送带请求头参数的POST方法的请求
     *
     * @param url        url地址          例如：http://localhost:8080/demo/login
     * @param headerMap  请求头参数        { "user_head":"hah*********ha" }
     * @param contentMap 需要发送的参数    { "userName":"admin", "password":"123456" }
     * @return 发送结果的返回字符串
     */
    public static String doPost(String url,
                                Map<String, String> headerMap,
                                Map<String, String> contentMap) {
        return sendPost(url, headerMap, contentMap, null);
    }

    private static String sendPost(String url,
                                   Map<String, String> headerMap,
                                   Map<String, String> contentMap,
                                   String charset) {
        String result = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            //创建httpclient对象
            httpClient = createHttpClient();
            //创建post方式请求对象
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            if (headerMap != null) {
                //循环增加header
                for (Entry<String, String> elem : headerMap.entrySet()) {
                    httpPost.addHeader(elem.getKey(), elem.getValue());
                }
            }
            //装填参数
            List<NameValuePair> nvps = new ArrayList<>();
            if (contentMap != null) {
                for (Entry<String, String> entry : contentMap.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            if (charset == null) {
                charset = ENCODING;
            }
            if (nvps.size() > 0) {
                //装填参数
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps, charset);
                //设置参数到请求对象中
                httpPost.setEntity(entity);
            }
            response = httpClient.execute(httpPost);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                //获取response的body部分
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    //按指定编码转换结果实体为String类型
                    result = EntityUtils.toString(entity, charset);
                }
            }
            return result;
        } catch (IOException e) {
            System.out.println("发送POST请求出现异常！" + e);
            e.printStackTrace();
        } catch (KeyManagementException e) {
            System.out.println("绕过秘钥验证失败！" + e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("找不到恢复密钥的算法！" + e);
            e.printStackTrace();
        } finally {
            try {
                release(response, httpClient);
            } catch (IOException e) {
                System.out.println("释放连接错误！" + e);
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 封装HTTP GET方法
     * 有参数的Get请求
     *
     * @param url      资源地址
     * @param paramMap 发送的参数
     * @param charset  字符集编码
     * @return
     */
    private static String sendGet(String url, Map<String, String> paramMap, String charset) {
        String result = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = createHttpClient();
            HttpGet httpGet = new HttpGet();
            httpGet.setConfig(requestConfig);
            List<NameValuePair> params = new ArrayList<>();
            if (paramMap != null) {
                Set<Entry<String, String>> set = paramMap.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            if (charset == null) {
                charset = ENCODING;
            }
            String param = URLEncodedUtils.format(params, charset);
            URI uri = URI.create(url + "?" + param);
            httpGet.setURI(uri);
            httpGet.setHeader("X-API-KEY", "ZQCAXA874JA8JHGAUF9MXZDH3QZYP7K84FV27HDQ");
            System.out.println(uri);
            response = httpClient.execute(httpGet);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity, charset);
                }
            }
            return result;
        } catch (IOException e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        } catch (KeyManagementException e) {
            System.out.println("绕过秘钥验证失败！" + e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("找不到恢复密钥的算法！" + e);
            e.printStackTrace();
        } finally {
            try {
                release(response, httpClient);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Description: 释放资源
     *
     * @param httpResponse
     * @param httpClient
     * @throws IOException
     */
    private static void release(CloseableHttpResponse httpResponse,
                                CloseableHttpClient httpClient) throws IOException {
        // 释放资源
        if (httpResponse != null) {
            httpResponse.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3");
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    /**
     * 创建自定义的httpclient对象
     *
     * @return
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     */
    private static CloseableHttpClient createHttpClient() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslcontext = createIgnoreVerifySSL();
        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        HttpClients.custom().setConnectionManager(connManager);
        //创建自定义的httpclient对象
        CloseableHttpClient client = HttpClients.custom().setConnectionManager(connManager).build();
        return client;
    }


    /**
     * 发送JSON 字符串格式的 POST请求
     *
     * @param url
     * @param json
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/3/23 15:48
     */
    public static String sendJsonStrPost(String url, String json) {
        String result = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            //创建httpclient对象
            httpClient = createHttpClient();
            //创建post方式请求对象
            HttpPost httpPost = new HttpPost(url);
            //配置
            httpPost.setConfig(requestConfig);
            //参数
            StringEntity requestEntity = new StringEntity(json, ENCODING);
            requestEntity.setContentEncoding(ENCODING);
            httpPost.setEntity(requestEntity);
            //头部信息
            httpPost.setHeader("Content-type", "application/json");

            response = httpClient.execute(httpPost);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                //获取response的body部分
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    //按指定编码转换结果实体为String类型
                    result = EntityUtils.toString(entity, ENCODING);
                }
            }
            return result;
        } catch (IOException e) {
            System.out.println("发送回调通知POST请求出现异常！ 请求URL：" + url + "||请求数据：" + json);
            e.printStackTrace();
            throw new PosApiException(PosApiCodeEnum.NOTIFY_ERROR);
        } catch (KeyManagementException e) {
            System.out.println("绕过秘钥验证失败！" + e);
            e.printStackTrace();
            throw new PosApiException(PosApiCodeEnum.NOTIFY_ERROR);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("找不到恢复密钥的算法！" + e);
            e.printStackTrace();
            throw new PosApiException(PosApiCodeEnum.NOTIFY_ERROR);
        } finally {
            try {
                release(response, httpClient);
            } catch (IOException e) {
                System.out.println("释放连接错误！" + e);
                e.printStackTrace();
                throw new PosApiException(PosApiCodeEnum.NOTIFY_ERROR);
            }
        }
    }


    public static void main(String[] args) {
        Map<String, String> a = new HashMap<>(2);
        a.put("institution", "Australian1");
        String s = doGet("https://test.bankstatements.com.au/api/v1/preload", a);
        System.out.println(s);
    }

}
