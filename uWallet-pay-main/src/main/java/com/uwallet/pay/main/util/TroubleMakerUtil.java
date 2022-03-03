package com.uwallet.pay.main.util;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.StaticDataDTO;
import com.uwallet.pay.main.service.StaticDataService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 异常制造类, 在测试环境中:
 * 1. 如果是测试环境-> 调用时查询数据字典表,查询异常节点
 *    如果节点匹配  则抛出异常
 *
 *    备注: 异常节点定义在 数据字典表, id= 666666667;
 *    通过匹配value字段 确定是否抛出异常
 *
 *    insert sql:
 *    INSERT INTO `uwallet_pay`.`u_static_data`
 *    (`id`, `code`, `name`, `en_name`, `value`, `parent`, `builtin`, `modified_by`, `modified_date`, `created_by`, `created_date`, `status`, `ip`)
 *    VALUES
 *    (666666667, 'troubleMaker', '测试环境异常制造', '测试环境异常制造', '0', NULL, 0, 1, 1, 0, 1, 1, '测试环境异常制造');
 * @Author aaronS
 */
@Slf4j
@Component
public class TroubleMakerUtil {

    private static StaticDataService StaticDataService;

    public TroubleMakerUtil(StaticDataService StaticDataService){
        TroubleMakerUtil.StaticDataService = StaticDataService;
    }

    private static JSONObject param ;
    /**
     * 数据字典 code 名
     */
    private static final String STATIC_DATA_CODE = "troubleMaker";

    static {
        //初始化查询条件
        param = new JSONObject(2);
        param.put("code",STATIC_DATA_CODE);
    }

    /**
     * 在指定节点制造异常, 返回提示信息
     * @param nodeCode 节点号 string类型
     * @param message  抛出异常时的提示信息, 可传 null/"" 没有则抛出固定信息 + 节点号
     * @throws BizException
     */
    public static void makeTrouble(@NonNull String nodeCode, String message) throws BizException {
        if (TestEnvUtil.isTestEnv()){
            StaticDataDTO troubleMakerData = StaticDataService.findOneStaticData(param);
            if (troubleMakerData != null && troubleMakerData.getValue() != null){
                if (nodeCode.equals(troubleMakerData.getValue())){
                    String finalMsg = StringUtils.isNotBlank(message) ? message + ",异常节点: " + nodeCode : "后台人工异常,异常节点: " + nodeCode;
                    log.error("测试环境->指定节点制造异常(TroubleMakerUtil.makeTrouble()), 异常节点:{},异常信息:{}",nodeCode,finalMsg);
                    throw new BizException(finalMsg);
                }
            }
        }
    }
}
