package com.uwallet.pay.core.util;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * http通讯类
 *
 * @author
 * @date 14:41 2019/4/18
 */
@Slf4j
@Component
public class HttpClientUtils {

    private static RedisUtils redisUtils;
    public HttpClientUtils(RedisUtils redisUtils){
        HttpClientUtils.redisUtils = redisUtils;}

    /**
     * 密钥
     * @Author Laity
     */
    private final static String appKey = "3.1415926UWallet&LoanCloud";

    private final static int FAILED_CODE = 400;

    public static String sendPost(String url, String json) throws Exception {
        json = dataPackaging(json);

        String returnValue =  getString(url, json);
        return returnValue;
    }

    public static String sendPostKyc(String url, String json) throws Exception {
        json = dataPackagingKyc(json);
        return getString(url, json);
    }

    private static String getString(String url, String json) throws IOException {
        log.info("请求报文：" + url + json);
        // 这是默认返回值，接口调用失败
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建httpPost对象
            HttpPost httpPost = new HttpPost(url);
            //第三步：给httpPost设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPost.setHeader("Content-type", "application/json");
            //todo 调用账户系统 请求头中放入验签秘钥,生成后存入redis
            if (false){packHeader(httpPost);}
            httpPost.setEntity(requestEntity);
            //第四步：发送HttpPost请求，获取返回值
            returnValue = httpClient.execute(httpPost, responseHandler);
            log.info("返回报文：" + returnValue);
        } catch (Exception e) {
            log.error("post请求异常,url: {} \n param: {} \n error: {}", url, json, e);
            throw new IOException(e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    /**
     * 调用账户系统 验签秘钥,生成后存入redis
     * @param httpPost
     * @return
     * @throws Exception
     */
    private static HttpPost packHeader(HttpPost httpPost) throws Exception{
        //生成秘钥,使用雪花算法
        String redisKey = SnowflakeUtil.generateId().toString();
        //加密redis秘钥 ,到账户系统后解密
        String encrypt = EncryptUtil.encrypt(redisKey);
        //存入redis,过期时间5分钟
        redisUtils.set(redisKey,redisKey,60*5);
        //放入请求头
        httpPost.setHeader("accessKey",encrypt);
        return httpPost;
    }

    public static String sendGet(String url) {
        log.info("请求报文：" + url);
        // 这是默认返回值，接口调用失败
        String returnValue = ErrorCodeEnum.HTTP_SEND_ERROR.getCode();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建HttpGet对象
            HttpGet httpGet = new HttpGet(url);

            //v第三步：发送HttpGet请求，获取返回值
            returnValue = httpClient.execute(httpGet, responseHandler);
            String printValue = returnValue.replaceAll("\r|\n", "").replaceAll(" ","");
            log.info("返回报文：" + printValue);
        } catch (Exception e) {
            log.error("get请求异常,url: {} \n param: {} \n error: {}", url, e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    public static String sendPut(String url, String json) throws Exception {
        json = dataPackaging(json);
        log.info("请求报文："+ url + json);
        // 这是默认返回值，接口调用失败
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建HttpPut对象
            HttpPut httpPut = new HttpPut(url);

            //第三步：给HttpPut设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPut.setHeader("Content-type", "application/json");
            httpPut.setEntity(requestEntity);

            //第四步：发送HttpPut请求，获取返回值
            returnValue = httpClient.execute(httpPut, responseHandler);
            log.info("返回报文：" + returnValue);
        } catch (Exception e) {
            log.error("put请求异常,url: {} \n param: {} \n error: {}", url, json, e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }


    public static String post(String url, String json) throws Exception {
        log.info("请求报文：" +url + json);
        // 这是默认返回值，接口调用失败
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建httpPost对象
            HttpPost httpPost = new HttpPost(url);
            //构建超时等配置信息
            RequestConfig config = RequestConfig.custom()
                    //连接超时时间
                    .setConnectTimeout(30000)
                    //从连接池中取的连接的最长时间
                    .setConnectionRequestTimeout(10000)
                    //数据传输的超时时间
                    .setSocketTimeout(100 * 1000)
                    .build();
            httpPost.setConfig(config);

            //第三步：给httpPost设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);

            //第四步：发送HttpPost请求，获取返回值
            returnValue = httpClient.execute(httpPost, responseHandler);
            log.info("返回报文：" + returnValue);
        } catch (Exception e) {
            log.error("post请求异常,url: {} \n param: {} \n error: {}", url, json, e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    /**
     *
     * @param httpUrl  请求的url
     * @param param  form表单的参数（key,value形式）
     * @return
     */
    public static String sendPostForm(String httpUrl, Map param) throws Exception {
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：15000毫秒
            connection.setConnectTimeout(1000*15);
            // 设置读取主机服务器返回数据超时时间：60000毫秒
            connection.setReadTimeout(1000*50);

            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
            //connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
            // 通过连接对象获取一个输出流
            os = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的(form表单形式的参数实质也是key,value值的拼接，类似于get请求参数的拼接)
            os.write(createLinkString(param).getBytes());
            // 通过连接对象获取一个输入流，向远程读取
            if (connection.getResponseCode() == 200) {

                is = connection.getInputStream();
                // 对输入流对象进行包装:charset根据工作项目组的要求来设置
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                StringBuffer sbf = new StringBuffer();
                String temp = null;
                // 循环遍历一行一行读取数据
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 断开与远程地址url的连接
            connection.disconnect();
        }
        return result;
    }
    /**
     *
     * @param httpUrl  请求的url
     * @param param  form表单的参数（key,value形式）
     * @return
     */
    public static String sendPostFormQueryLatpayCardInfo(String httpUrl, Map param) throws Exception {
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：15000毫秒
            connection.setConnectTimeout(1000*10);
            // 设置读取主机服务器返回数据超时时间：60000毫秒
            connection.setReadTimeout(1000*10);

            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
            //connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
            // 通过连接对象获取一个输出流
            os = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的(form表单形式的参数实质也是key,value值的拼接，类似于get请求参数的拼接)
            os.write(createLinkString(param).getBytes());
            // 通过连接对象获取一个输入流，向远程读取
            if (connection.getResponseCode() == 200) {

                is = connection.getInputStream();
                // 对输入流对象进行包装:charset根据工作项目组的要求来设置
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                StringBuffer sbf = new StringBuffer();
                String temp = null;
                // 循环遍历一行一行读取数据
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 断开与远程地址url的连接
            connection.disconnect();
        }
        return result;
    }


    /**
     * 数据封装
     * @param json
     * @return
     */
    public static String dataPackagingKyc(String json) {
        JSONObject sendData = new JSONObject();
        Long timestamp = System.currentTimeMillis();
        sendData.put("sign", MD5FY.MD5Encode(appKey + timestamp));
        sendData.put("timestamp", timestamp);
        if (json != null) {
            sendData.put("data", json);
        } else {
            sendData.put("data", null);
        }
        return sendData.toJSONString();
    }

    /**
     * 数据封装
     * @param json
     * @return
     */
    public static String dataPackaging(String json) {
        JSONObject sendData = new JSONObject();
        Long timestamp = System.currentTimeMillis();
        sendData.put("sign", MD5FY.MD5Encode(appKey + timestamp));
        sendData.put("timestamp", timestamp);
        if (json != null) {
            sendData.put("data", JSONObject.parseObject(json));
        } else {
            sendData.put("data", null);
        }
        return sendData.toJSONString();
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        StringBuilder prestr = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                prestr.append(key).append("=").append(value);
            } else {
                prestr.append(key).append("=").append(value).append("&");
            }
        }

        return prestr.toString();
    }

    /**
     * integraPay GET请求
     * @param url
     * @param token
     * @return
     */
    public static String getForIntegraPay(String url, String token) {
        log.info("请求报文：" + url);
        // 这是默认返回值，接口调用失败
        String returnValue = ErrorCodeEnum.HTTP_SEND_ERROR.getCode();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建HttpGet对象
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Authorization", token);

            //v第三步：发送HttpGet请求，获取返回值
            returnValue = httpClient.execute(httpGet, responseHandler);
            log.info("返回报文：" + returnValue);
        } catch (Exception e) {
            log.error("get请求异常,url: {} \n param: {} \n error: {}", url, e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    /**
     * integraPay Post请求
     * @param url
     * @param json
     * @return
     * @throws Exception
     */
    public static String postForIntegraPay(String url, String token, String json) throws Exception {
        log.info("请求报文：" +url + json);
        // 这是默认返回值，接口调用失败
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建httpPost对象
            HttpPost httpPost = new HttpPost(url);
            //构建超时等配置信息
            RequestConfig config = RequestConfig.custom()
                    //连接超时时间
                    .setConnectTimeout(5000)
                    //从连接池中取的连接的最长时间
                    .setConnectionRequestTimeout(1000)
                    //数据传输的超时时间
                    .setSocketTimeout(10 * 1000)
                    .build();
            httpPost.setConfig(config);

            //第三步：给httpPost设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPost.setHeader("Content-type", "application/json");
            // 如果token有则添加
            if (!StringUtils.isEmpty(token)) {
                httpPost.setHeader("Authorization", token);
            }
            httpPost.setEntity(requestEntity);

            //第四步：发送HttpPost请求，获取返回值
            returnValue = httpClient.execute(httpPost, responseHandler);
            log.info("返回报文：" + returnValue);
        } catch (Exception e) {
            log.error("post请求异常,url: {} \n param: {} \n error: {}", url, json, e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    public static String putForIntegraPay(String url, String token, String json) throws Exception {
        log.info("请求报文："+ url + json);
        // 这是默认返回值，接口调用失败
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建HttpPut对象
            HttpPut httpPut = new HttpPut(url);

            //第三步：给HttpPut设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPut.setHeader("Content-type", "application/json");
            httpPut.setHeader("Authorization", token);
            httpPut.setEntity(requestEntity);

            //第四步：发送HttpPut请求，获取返回值
            returnValue = httpClient.execute(httpPut, responseHandler);
            log.info("返回报文：" + returnValue);
        } catch (Exception e) {
            log.error("put请求异常,url: {} \n param: {} \n error: {}", url, json, e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    public static String getForSplitPay(String url, String token) throws Exception {
        log.info("请求报文："+ url);
        // 这是默认返回值，接口调用失败
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建HttpPut对象
            HttpGet httpGet = new HttpGet(url);

            //第三步：给HttpPut设置JSON格式的参数
            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Authorization", "Bearer " + token);

            //第四步：发送HttpPut请求，获取返回值
            response = httpClient.execute(httpGet);
            returnValue = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.info("返回报文：" + returnValue);
        } catch (Exception e) {
            log.error("get请求异常,url: {} \n param: {} \n error: {}", url, e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    /**
     * splitPay Post请求
     * @param url
     * @param json
     * @return
     * @throws Exception
     */
    public static String postForSplitPay(String url, String token, String json) throws Exception {
        log.info("请求报文：" +url + json);
        // 这是默认返回值，接口调用失败
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
//        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        CloseableHttpResponse response = null;
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建httpPost对象
            HttpPost httpPost = new HttpPost(url);
            //构建超时等配置信息
            RequestConfig config = RequestConfig.custom()
                    //连接超时时间
                    .setConnectTimeout(5000)
                    //从连接池中取的连接的最长时间
                    .setConnectionRequestTimeout(1000)
                    //数据传输的超时时间
                    .setSocketTimeout(10 * 1000)
                    .build();
            httpPost.setConfig(config);

            //第三步：给httpPost设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPost.setHeader("Content-type", "application/json");
            // 如果token有则添加
            if (!StringUtils.isEmpty(token)) {
                httpPost.setHeader("Authorization", "Bearer " + token);
            }
            httpPost.setEntity(requestEntity);

            //第四步：发送HttpPost请求，获取返回值
            response = httpClient.execute(httpPost);
            returnValue = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (StringUtils.isEmpty(returnValue)) {
                throw new Exception("Request Failed");
            }
            log.info("返回数据,{}", returnValue);
        } catch (Exception e) {
            log.error("post请求异常,url: {} \n param: {} \n error: {}", url, json, e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    public static String putForSplitPay(String url, String token, String json) throws Exception {
        log.info("请求报文："+ url + json);
        // 这是默认返回值，接口调用失败
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建HttpPut对象
            HttpPut httpPut = new HttpPut(url);

            //第三步：给HttpPut设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPut.setHeader("Content-type", "application/json");
            httpPut.setHeader("Authorization", "Bearer " + token);
            httpPut.setEntity(requestEntity);

            //第四步：发送HttpPut请求，获取返回值
            response = httpClient.execute(httpPut);
            returnValue = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.info("返回报文：" + returnValue);
        } catch (Exception e) {
            log.error("put请求异常,url: {} \n param: {} \n error: {}", url, json, e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    public static String deleteForSplitPay(String url, String token) throws Exception {
        log.info("请求报文："+ url);
        // 这是默认返回值，接口调用失败
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建HttpPut对象
            HttpDelete httpDelete = new HttpDelete(url);

            //第三步：给HttpPut设置JSON格式的参数
            httpDelete.setHeader("Authorization", "Bearer " + token);

            //第四步：发送HttpPut请求，获取返回值
            response = httpClient.execute(httpDelete);
            returnValue = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (response.getStatusLine().getStatusCode() == FAILED_CODE) {
                log.info("返回报文：" + returnValue);
                throw new Exception("Request failed");
            }
        } catch (Exception e) {
            log.error("get请求异常,url: {} \n param: {} \n error: {}", url, e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }
    public static String sendGetByHeader(String url,String... header){
        log.info("请求报文：" + url);
        // 这是默认返回值，接口调用失败
        String returnValue = ErrorCodeEnum.HTTP_SEND_ERROR.getCode();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建HttpGet对象
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("X-API-KEY",header[0]);
            //v第三步：发送HttpGet请求，获取返回值
            returnValue = httpClient.execute(httpGet, responseHandler);
            String printValue = returnValue.replaceAll("\r|\n", "").replaceAll(" ","");
            log.info("返回报文：" + printValue);
        } catch (Exception e) {
            log.error("get请求异常,url: {} \n param: {} \n error: {}", url, e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

    public static String postByHeader(String url,JSONObject headerParam,String json) throws Exception {
        String returnValue;
        CloseableHttpClient httpClient = HttpClients.createDefault();
//        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        CloseableHttpResponse response = null;
        try {
            // 第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();

            // 第二步：创建httpPost对象
            HttpPost httpPost = new HttpPost(url);
            //构建超时等配置信息
            RequestConfig config = RequestConfig.custom()
                    //连接超时时间
                    .setConnectTimeout(55*1000)
                    //从连接池中取的连接的最长时间
                    .setConnectionRequestTimeout(60*1000)
                    //数据传输的超时时间
                    .setSocketTimeout(60*1000)
                    .build();
            httpPost.setConfig(config);

            Set<Map.Entry<String, Object>> entries = headerParam.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                httpPost.setHeader(entry.getKey(), entry.getValue().toString());
            }
            //第三步：给httpPost设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);

            //第四步：发送HttpPost请求，获取返回值
            response = httpClient.execute(httpPost);
            returnValue = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (StringUtils.isEmpty(returnValue)) {
                throw new Exception("Request Failed");
            }
            log.info("返回数据,{}", returnValue);
        } catch (Exception e) {
            log.error("post请求异常,url: {} \n error: {}", url, e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient链接关闭异常: {}", e);
            }
        }
        // 第五步：处理返回值
        return returnValue;
    }

}
