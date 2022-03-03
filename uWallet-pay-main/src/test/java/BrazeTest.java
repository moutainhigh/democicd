import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.main.MainApplication;
import com.uwallet.pay.main.constant.StaticDataEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MainApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
@ActiveProfiles("test")
public class BrazeTest {
    @Test
    public void sendEmailMessage() throws Exception {
        String url="https://rest.iad-05.braze.com"+"/messages/send";
        JSONObject head = new JSONObject();
        head.put("Content-Type","application/json");
        head.put("Authorization","Bearer 4d40ca2b-49a2-4942-823a-119e7471ff05");
        JSONObject param = new JSONObject();
        // 批量发送需要设置为true
        param.put("broadcast",false);
        // id数组或者字符串
        List<String> arr=new ArrayList();
        arr.add("395371281553903617");
        param.put("external_user_ids",arr);

        // 已选择加入 ( opted_in) 的用户、已订阅或已选择加入 ( subscribed) 的用户或所有用户（包括未订阅的用户）发送消息（all）
        param.put("recipient_subscription_state","subscribed");
        HashMap<String,Object> message = new HashMap<>();
        // appID
        message.put("app_id","0f1c1caf-5b25-4b53-a36c-62bbd5b4640d");
        // 来源
        message.put("from","email@address.com");
        // 不回复
//        message.put("reply_to","NO_reply_to");
        // 内容
        message.put("body","<p>我是测试内容</p>");
//        message.put("preheader","11111111111111111111111111111111111111111111111111111111");
        JSONObject email=new JSONObject();
        email.put("email",message);
        param.put("messages",email);

        System.out.println(param.toJSONString());
        String s = HttpClientUtils.postByHeader(url, head, param.toJSONString());
        log.info("返回数据：{}",s);
    }

    @Test
    public void createUserAlias() throws Exception {
        String url="https://rest.iad-05.braze.com"+"/users/alias/new";
        JSONObject head = new JSONObject();
        head.put("Content-Type","application/json");
        head.put("Authorization","Bearer 4d40ca2b-49a2-4942-823a-119e7471ff05");
        JSONObject param = new JSONObject();
        JSONObject user=new JSONObject();
        // external_id 更新aliasName，不传创建用户（需要调用identify，传external_id）
//        user.put("external_id","395371281553903619");
        user.put("alias_name","test_name2");
        user.put("alias_label","test_label2");
        List<JSONObject> arr=new ArrayList();
        arr.add(user);
        param.put("user_aliases",arr);
        String s = HttpClientUtils.postByHeader(url, head, param.toJSONString());
        log.info("返回数据：{}",s);
    }
    @Test
    public void identifyUser() throws Exception {
        String url="https://rest.iad-05.braze.com"+"/users/identify";
        JSONObject head = new JSONObject();
        head.put("Content-Type","application/json");
        head.put("Authorization","Bearer 4d40ca2b-49a2-4942-823a-119e7471ff05");
        JSONObject param = new JSONObject();
        JSONObject user=new JSONObject();
        user.put("external_id","395371281553903620");
        JSONObject userIn=new JSONObject();
        userIn.put("alias_name","test_name2");
        userIn.put("alias_label","test_label2");
        user.put("user_alias",userIn);

        List<JSONObject> arr=new ArrayList();
        arr.add(user);
        param.put("aliases_to_identify",arr);
        String s = HttpClientUtils.postByHeader(url, head, param.toJSONString());
        log.info("返回数据：{}",s);
    }
    @Test
    public void sendIOSPush() throws Exception {
        String url="https://rest.iad-05.braze.com"+"/messages/send";
        JSONObject head = new JSONObject();
        head.put("Content-Type","application/json");
        head.put("Authorization","Bearer 4d40ca2b-49a2-4942-823a-119e7471ff05");
        JSONObject param = new JSONObject();
        // 批量发送需要设置为true
        param.put("broadcast",false);
        // id数组或者字符串
        List<String> arr=new ArrayList();
        arr.add("656019126739357696");
        param.put("external_user_ids",arr);
        // 已选择加入 ( opted_in) 的用户、已订阅或已选择加入 ( subscribed) 的用户或所有用户（包括未订阅的用户）发送消息（all）
        param.put("recipient_subscription_state","subscribed");
        HashMap<String,Object> message = new HashMap<>();
        message.put("alert","testAlert");
        message.put("content-available","我是内容");
        message.put("collapse_id","这是消息ID");
        JSONObject extra=new JSONObject();
        extra.put("route",2);
        extra.put("voice", StaticDataEnum.VOICE_0.getCode());
        message.put("extra",extra);
        JSONObject email=new JSONObject();
        email.put("apple_push",message);
        param.put("messages",email);
        String s = HttpClientUtils.postByHeader(url, head, param.toJSONString());
        log.info("返回数据：{}",s);
    }

    @Test
    // 创建用户，同步用户数据
    public void trackUser() throws Exception {
        String url="https://rest.iad-05.braze.com"+"/users/track";
        JSONObject head = new JSONObject();
        head.put("Content-Type","application/json");
        head.put("Authorization","Bearer 4d40ca2b-49a2-4942-823a-119e7471ff05");
        JSONObject param = new JSONObject();
        JSONObject user=new JSONObject();
        user.put("external_id","656380373561331712");
        user.put("first_name","testFirstName");
        user.put("last_name","testLastName");
        user.put("email","313097897@qq.com");
        user.put("phone","+8615762212286");
        List<JSONObject> arr=new ArrayList();
        arr.add(user);
        param.put("attributes",arr);
        String s = HttpClientUtils.postByHeader(url, head, param.toJSONString());
        log.info("返回数据：{}",s);
    }
    @Test
    // 用户订阅短信
    public void subscriptionUser() throws Exception {
        String url="https://rest.iad-05.braze.com"+"/subscription/status/set";
        JSONObject head = new JSONObject();
        head.put("Content-Type","application/json");
        head.put("Authorization","Bearer 4d40ca2b-49a2-4942-823a-119e7471ff05");
        JSONObject param = new JSONObject();
        param.put("subscription_group_id","f7f168fb-4762-4d8f-ac6d-7b1768160977");
        param.put("subscription_state","subscribed");
        param.put("external_id","395371281553903621");
        param.put("phone","8615762212286");
        String s = HttpClientUtils.postByHeader(url, head, param.toJSONString());
        log.info("返回数据：{}",s);
    }

    @Test
    public void sendSMS() throws Exception {
        String url="https://rest.iad-05.braze.com"+"/messages/send";
        JSONObject head = new JSONObject();
        head.put("Content-Type","application/json");
        head.put("Authorization","Bearer 4d40ca2b-49a2-4942-823a-119e7471ff05");
        JSONObject param = new JSONObject();
        // 批量发送需要设置为true
        param.put("broadcast",false);
        // id数组或者字符串
        List<String> arr=new ArrayList();
        arr.add("8d01dea0-ee1f-4563-acf3-77ed63592e9d");
        param.put("external_user_ids",arr);

        // 已选择加入 ( opted_in) 的用户、已订阅或已选择加入 ( subscribed) 的用户或所有用户（包括未订阅的用户）发送消息（all）
        param.put("recipient_subscription_state","subscribed");
        // 苹果推送
//        HashMap<String,Object> message = new HashMap<>();
//        message.put("alert","testAlert");
//        message.put("extra","这是额外的对象");
//        message.put("content-available","我是内容");
//        message.put("collapse_id","这是消息ID");
        // 安卓推送
        HashMap<String,Object> message = new HashMap<>();
        message.put("alert","testAlert");
        message.put("title","标题");
        // 优先级
        message.put("priority",1);
        JSONObject extra=new JSONObject();
        extra.put("to",1);
        message.put("extra",extra);
        message.put("content-available","我是内容");
        message.put("collapse_id","这是自定义消息ID");
        // logo URL
//        message.put("push_icon_image_url","这是消息ID");

//        JSONObject applePush=new JSONObject();
//        applePush.put("apple_push",message);
//        param.put("messages",applePush);
        JSONObject androidPush=new JSONObject();
        androidPush.put("android_push",message);
        param.put("messages",androidPush);
        String s = HttpClientUtils.postByHeader(url, head, param.toJSONString());
        log.info("返回数据：{}",s);
    }
}
