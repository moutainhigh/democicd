package com.uwallet.pay.core.util;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class JSONResultHandle {

    public static <T> JSONObject resultHandle(T data, Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        JSONObject object = JSONObject.parseObject(JSONObject.toJSONString(data));
        if (object != null) {
            for (int i = 0; i < fields.length; i++) {
                if (object.get(fields[i].getName()) == null) {
                    object.put(String.valueOf(fields[i].getName()), "");
                }
            }
        } else {
            object = new JSONObject();
        }

        return object;
    }

    public static JSONObject resultHandle(JSONObject object) {
        if (object != null) {
            Set<Map.Entry<String, Object>> entrySet = object.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                if (entry.getValue() == null) {
                    entry.setValue("");
                }
            }
        } else {
            object = new JSONObject();
        }
        return object;
    }

    public static JSONObject resultHandle(String data) {
        JSONObject returnData = new JSONObject();
        returnData.put("data", "");
        returnData.put("transactionFailed", "");
        String[] params = data.split("&");
        if (params.length != 1) {
            JSONObject formatData = new JSONObject();
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    formatData.put(pair[0], pair[1]);
                } else {
                    formatData.put(pair[0], "");
                }
            }
            returnData.put("data", formatData);
        } else {
            returnData.put("transactionFailed", params);
        }
        return returnData;
    }

    /**
     * 获取初始化容器大小
     *
     * @param size 元素个数
     * @return 结果
     */
    public static int getContainerSize(int size) {
        int i = BigDecimal.valueOf(size).divide(BigDecimal.valueOf(0.75), 0, BigDecimal.ROUND_UP).intValue();
        return i % 2 != 0 ? ++i : i;
    }

}
