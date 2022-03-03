package com.uwallet.pay.main.listener;

import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.service.MessageBatchSendLogService;
import com.uwallet.pay.main.service.NoticeMassService;
import com.uwallet.pay.main.service.QrPayFlowService;
import com.uwallet.pay.main.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author baixinyue
 * @description redis key过期监听类
 * @createDate 2019/02/20
 */

@Component
@Slf4j
public class RedisKeyExpireListener extends KeyExpirationEventMessageListener {

    @Autowired
    NoticeMassService noticeMassService;

    @Autowired
    QrPayFlowService qrPayFlowService;

    @Autowired
    MessageBatchSendLogService messageBatchSendLogService;


    @Resource
    private UserService userService;

    public RedisKeyExpireListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString().replace("\"", "");
        log.info("send notice mass, data:{}", key);
        if (key.startsWith(StaticDataEnum.NOTICE_PREFIX.getMessage())) {
            try {
                Long id = Long.valueOf(key.split(":")[1]);
                noticeMassService.sendMessage(id);
            } catch (Exception e) {
                log.info("send message failed, notice:{}, error message:{}, error:{}", key, e.getMessage(), e);
            }
        } else if (key.contains("api_order")) {
            String orderNo = key.split("~")[1].split("_")[0];
            qrPayFlowService.apiOrderClosed(orderNo);
        }else if (key.startsWith(StaticDataEnum.MESSAGE_PREFIX.getMessage())){
            // 获取定时任务id
            Long id = Long.valueOf(key.split(":")[1]);
            log.info("监听到定时任务,开始定时任务,任务id:{}",id);
            try{
                messageBatchSendLogService.batchSendMessage(id);
            }catch (Exception e){
                log.info("批量发送消息异常,e:{},id:{}",e,id);
            }

        }else if(key.startsWith(Constant.USER_FIRST_PAY_AFTER_2HOURS_SEND_EMAIL_REDIS_KEY)){
            //发送新用户首次交易邮件
            String userId = key.replace(Constant.USER_FIRST_PAY_AFTER_2HOURS_SEND_EMAIL_REDIS_KEY, "");
            userService.sendNewUserFirstPayAfter2HoursEmail(Long.valueOf(userId));
        }
    }

}
