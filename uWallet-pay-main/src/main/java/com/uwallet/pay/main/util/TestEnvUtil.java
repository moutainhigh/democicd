package com.uwallet.pay.main.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 检查本地配置文件的类型, 返回是否为开启挡板的结果 : true/false
 * @Author shao
 */
@Component
public class TestEnvUtil {
    /**
     * 【测试服务器】
     * 配置文件信息当前服务器类型：
     */
    private static String type;
    /**
     * 测试环境配置文件的 server.type 集合, 可能为 test 或者 dev
     */
    private static List<String> TEST_ENV_TYPE_LIST ;
    /**
     * 测试环境配置文件的 server.type 集合, 可能为 test 或者 dev
     */
    private static List<String> LOCAL_ENV_TYPE_LIST ;

    public static Boolean isTest;

    static {
//        TEST_ENV_TYPE_LIST = Arrays.asList("test","dev","hkTest", "prod-test");
        TEST_ENV_TYPE_LIST = Arrays.asList("test","dev","prod-test");
        LOCAL_ENV_TYPE_LIST = Arrays.asList("local");
        isTest = TEST_ENV_TYPE_LIST.contains(type);
    }

    /**
     * 判断是否为测试环境,是则开启挡板 TestEnvChecker.isTestEnv()
     * @return
     */
    public static Boolean isTestEnv() {
        //return false;
        return TEST_ENV_TYPE_LIST.contains(type);
    }

    /**
     * 本地测试环境
     * @return
     */
    public static Boolean isLocalEnv(){
        return LOCAL_ENV_TYPE_LIST.contains(type);
    }

    /**
     * 读取当前配置文件中的当前服务器类型信息
     * @param type
     */
    @Value("${server.type}")
    private void setServerType(String type){
        TestEnvUtil.type=type;
    }
}
