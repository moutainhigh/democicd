package com.uwallet.pay.main.docuSignComponent;


import com.alibaba.fastjson.JSONObject;
import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.model.*;
import com.docusign.esign.model.Checkbox;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.config.DocuSignConfig;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.DocuSignEnum;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static com.uwallet.pay.main.config.DocuSignConfig.authenticationMethod;

/**
 * @author aaron.S
 * @since 2020/8/18
 */

@Slf4j
public class DocuSignClientCallable implements Callable {
    /**
     * 请求参数(Service层已经校验)
     */
    private JSONObject jsonObject;
    private HttpServletRequest request;
    private ApiClient apiClient;
    private String accountId;
    private static final String SIGN_SUB_FIX="_sign_anchor/";
    private static final String NAME_SUB_FIX="_name_anchor/";
    private static final String SIGNED_BY_SUB_FIX="_signed_by/";
    private static final String ABN_SUB_FIX="_abn/";
    private static final String LEFT_SUB_FIX="-l";
    private static final String RIGHT_SUB_FIX="-r";

    /**
     * 签署位置占位符
     *
     *1. Individual/Sole Trader
     *     - /individual_sole_trader_sign_anchor/
     *     - /individual_sole_trader_name_anchor/
     *     - /individual_sole_trader_signed_by/
     *
     * 2. Director
     *     - /director_sign_anchor/-l
     *     - /director_name_anchor/-l
     *     - /director_signed_by/
     *
     *     - /director_sign_anchor/-r
     *     - /director_name_anchor/-r
     *     - /director_signed_by/
     *
     * 3. Authorised Officer
     *     - /authorised_officer_sign_anchor/
     *     - /authorised_officer_name_anchor/
     *     - /authorised_officer_signed_by/
     *
     * 4. Attorney
     *     - /attorney_sign_anchor/
     *     - /attorney_name_anchor/
     *     - /attorney_signed_by/
     *     - /attorney_presence_of/
     *     /attorney_date/
     *     /attorney_abn/
     *
     */

    public DocuSignClientCallable(JSONObject jsonObject,
                                  ApiClient apiClient,
                                  String accountId,
                                  HttpServletRequest request){
        this.jsonObject = jsonObject;
        this.request = request;
        this.apiClient = apiClient;
        this.accountId = accountId;
    }

    @Override
    public Object call() throws Exception {
        /*
           设置签署人信息,签署人姓名,签署人邮箱
         */
        String signerName = jsonObject.getString("signerName");
        String signerEmail = jsonObject.getString("signerEmail");
        //本地定义的UserID
        String clientUserId = jsonObject.getString("clientUserId");
        /*
          生成<信封定义对象>, 在模板基础上,通过该对象,
          可对模板内的签署人,占位符等信息,进行客制化
         */
        EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
        //放入事先已经编辑好的 合同模板的 ->模板ID
        envelopeDefinition.setTemplateId(jsonObject.getString("templateId"));
        /*
          生成签署角色对象, 占位符的值,签署人信息等都要封装进该对象
          (针对每一个需要签署的用户都要讲其信息封装进该对象)
         */
        TemplateRole templateRole=new TemplateRole();
        templateRole.setRoleName(DocuSignConfig.DEFAULT_ROLE_NAME);
        templateRole.setName(signerName);
        templateRole.setEmail(signerEmail);
        templateRole.setClientUserId(clientUserId);
        RecipientEmailNotification recipientEmailNotification= new RecipientEmailNotification();
        //recipientEmailNotification.setEmailSubject();
        //recipientEmailNotification.emailBody()
        templateRole.setEmailNotification(recipientEmailNotification);
        /*
         *定义签署完成后, 将合同转发给Uwallet销售人员的信息
         */
        TemplateRole uwalletSales=new TemplateRole();
        uwalletSales.setRoleName(DocuSignConfig.DEFAULT_ROLE_CC_NAME);
        uwalletSales.setName(DocuSignConfig.SALES_NAME);
        uwalletSales.setEmail(DocuSignConfig.SALES_EMAIL);
        uwalletSales.setEmailNotification(new RecipientEmailNotification());
        /**
         * 创建一个签署位置锚点
         */
        String authorisedTitle = jsonObject.getString("authorisedTitle");
        String anchorString = this.getAnchorString(authorisedTitle,request);
        //创建锚点对象
        SignHere contractSignHere = this.buildAnchorPoint(authorisedTitle,anchorString,request);
        String businessName = "";
        String abn="";
        /*
         此处取出所有占位符的 键值对(名字为"textTabs"的JSONObject)
         生成Text对象的list, 放入 Tabs对象中,再将tabs对象放入 TemplateRole对象,最后封装进<信封定义对象中>
         */
        Tabs tabs=new Tabs();
        JSONObject textTabs = jsonObject.getJSONObject("textTabs");
        List<Text> textsTabs = new ArrayList<>();
        if (textTabs != null && textTabs.size() > Constant.ZERO){
           textsTabs.addAll(this.packTextTabs(textTabs,request)) ;
           businessName = textTabs.getString("companyInformation_businessRegisteredName");
           abn = textTabs.getString("companyInformation_companyABNACN");
        }
        //放置签名位置旁边的签署人姓名, 封装Signed by 占位符信息
        textsTabs.addAll(this.packSignedByInfo(authorisedTitle,anchorString,businessName,abn,signerName,request));
        tabs.setTextTabs(textsTabs);
        /*
         设置 checkbox自动勾选
         */
        JSONObject checkboxJson = jsonObject.getJSONObject("checkbox");
        if (checkboxJson != null && checkboxJson.size()>Constant.ZERO){
            List<Checkbox> checkboxList = this.packCheckBox(checkboxJson, request);
            tabs.setCheckboxTabs(checkboxList);
        }
        //放置生成的签名位置tab
        tabs.setSignHereTabs(Arrays.asList(contractSignHere));
        templateRole.setTabs(tabs);

        //将<输入框>,<勾选框> 封装进 信封定义对象中
        envelopeDefinition.setTemplateRoles(Arrays.asList(templateRole,uwalletSales));
        //将信封的状态设置为"sent",只有状态为sent的信封对象才可以被签署
        envelopeDefinition.setStatus(DocuSignEnum.ENVELOP_EVENT_SENT.getCode());

        String redirectUrl=null;
        String envelopeId=null;
        try {
            /*
            步骤二.请求docuSign去创建并发送信封
            * DocuSign核心对象,通过exampleBase获取已经封装好信息的对象
            */
            //获取创建信封的对象
            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            EnvelopeSummary results = envelopesApi.createEnvelope(accountId, envelopeDefinition);
            envelopeId = results.getEnvelopeId();

            /*
               步骤三: 信封已经被创建
                Step 3. The envelope has been created.
               获取一个签署人签署仪式试图链接地址(就是签署人要跳转并签署文件的页面(docuSign的页面))
             */
            RecipientViewRequest viewRequest = new RecipientViewRequest();
            // 设置当签署完成后,需要签署人跳转的页面(callback route somewhere in your app)
            viewRequest.setReturnUrl(jsonObject.getString("callBackUrl")+"/"+envelopeId);
            viewRequest.setAuthenticationMethod(authenticationMethod);
            viewRequest.setEmail(signerEmail);
            viewRequest.setUserName(signerName);
            viewRequest.setClientUserId(clientUserId);
            //设置签署链接 如果没有交互 超时时间为1200秒
            //viewRequest.setPingFrequency("1200");
            //viewRequest.setPingUrl("http://www.baidu.com");

            // 请求<创建签署人视图>API (call the CreateRecipientView API)
            ViewUrl viewUrl = envelopesApi.createRecipientView(accountId, envelopeId, viewRequest);
            //步骤四:从返回数据中取出URL 返回给前端用于跳转
            redirectUrl = viewUrl.getUrl();
        }catch (Exception e){
            log.error("DocuSign gen Singing Url error:{},param:{}",e.getMessage(),jsonObject);
            throw new BizException("DocuSign error, please try again later");
        }

        JSONObject data=new JSONObject(Constant.TWO);
        data.put("url",redirectUrl);
        data.put("envelopeId",envelopeId);
        System.out.println(redirectUrl);
        return data;
    }

    /**
     * 封装 Signed by 占位符信息
     *  生成签署人,签名位置旁边的 姓名显示 Tab
     * @param anchorString
     * @param businessName
     * @param request
     * @return
     */
    private List<Text> packSignedByInfo(String authorisedTitle,String anchorString, String businessName,String abn,String signerName, HttpServletRequest request) {
        List<Text> res = new ArrayList<>(Constant.TWO);
        String signedByAnchor = anchorString + SIGNED_BY_SUB_FIX;
        String fullNameAnchor = anchorString + NAME_SUB_FIX;
        String abnAnchor = anchorString + ABN_SUB_FIX;
        Boolean fullNameMarker = true;
        Boolean abnMarker = true;
        String value = businessName;
        if (authorisedTitle.equals(DocuSignEnum.AUTHORISED_TITLE_SOLE_TRADE.getCode())){
            value = signerName;
            fullNameMarker = false;
            abnMarker = false;
        }else if (authorisedTitle.equals(DocuSignEnum.AUTHORISED_TITLE_DIRECTOR.getCode())){
            /*
             Director相关有,左/右 2个签署,展示位置, 目前使用左侧位置签署展示
             */
            fullNameAnchor += LEFT_SUB_FIX;
        }else {
            //处理Attorney相关展示逻辑
            // 占位符anchor: /attorney_presence_of/
        }
        // full name 占位符 Tab
        if (fullNameMarker) {
            Text fullNameTab = new Text();
            fullNameTab.setValue(signerName);
            fullNameTab.setAnchorString(fullNameAnchor);
            res.add(fullNameTab);
        }
        if (abnMarker){
            Text abnTab = new Text();
            abnTab.setValue(abn);
            abnTab.setAnchorString(abnAnchor);
            res.add(abnTab);
        }
        //signed By 占位符 Tab
        Text signedByTab = new Text();
        if (authorisedTitle.equals(DocuSignEnum.AUTHORISED_TITLE_SOLE_TRADE.getCode())) {
            signedByTab.setAnchorString(signedByAnchor);
        }else {
            signedByTab.setTabLabel(signedByAnchor);
        }
        signedByTab.setValue(value);
        res.add(signedByTab);
        return res;
    }


    /**
     * 获取当前签署人对应的签署地标识符
     * @param authorisedTitle
     * @param request
     * @return
     */
    private String getAnchorString(String authorisedTitle, HttpServletRequest request) {
        String anchorString;
        if (authorisedTitle.equals(DocuSignEnum.AUTHORISED_TITLE_SOLE_TRADE.getCode())){
            anchorString = DocuSignEnum.AUTHORISED_TITLE_SOLE_TRADE.getMessage();
        }else if (authorisedTitle.equals(DocuSignEnum.AUTHORISED_TITLE_DIRECTOR.getCode())){
            anchorString = DocuSignEnum.AUTHORISED_TITLE_DIRECTOR.getMessage();
        }else {
            anchorString = DocuSignEnum.AUTHORISED_TITLE_ATTORNEY.getMessage();
        }
        return anchorString;
    }

    /**
     * 通过参数中的 "authorisedTitle" 确认放置签名位置
     * @param anchorString
     * @param request
     * @return
     */
    private SignHere buildAnchorPoint(String authorisedTitle,String anchorString, HttpServletRequest request) {
        String anchor = anchorString + SIGN_SUB_FIX;
        if (authorisedTitle.equals(DocuSignEnum.AUTHORISED_TITLE_DIRECTOR.getCode())){
            anchor += LEFT_SUB_FIX;
        }
        SignHere contractSignHere = new SignHere();
        contractSignHere.setAnchorString(anchor);//设置要匹配的自动锚定字符串
        contractSignHere.setAnchorUnits("pixels");
        contractSignHere.setAnchorXOffset("20");
        contractSignHere.anchorYOffset("10");
        contractSignHere.setTooltip("Please sign here.");
        return contractSignHere;
    }

    /**
     * 将勾选框赋值
     * @param checkboxJson
     * @param request
     * @return
     */
    private List<Checkbox> packCheckBox(JSONObject checkboxJson, HttpServletRequest request) {
        List<Checkbox> checkboxList = new ArrayList<>(checkboxJson.size());
        for (String key : checkboxJson.keySet()) {
            Checkbox checkbox=new Checkbox();
            checkbox.setTabLabel(key);
            checkbox.setSelected("true");
            checkboxList.add(checkbox);
        }
        return checkboxList;
    }

    /**
     * 对模板中的占位符进行赋值
     * @param textTabsJson
     * @param request
     * @return
     */
    private List<Text> packTextTabs(JSONObject textTabsJson, HttpServletRequest request) {
        List<Text> res=new ArrayList<>(textTabsJson.size());
        for (String key : textTabsJson.keySet()) {
            Text textTab = new Text();
            textTab.setTabLabel(key);
            textTab.setValue(textTabsJson.getString(key));
            res.add(textTab);
        }
        return res;
    }

    /**
     * us-one项目移至过来的代码
     *
     */
    private static void oldCode(){
        /*// 签署位置通过锚定方式自动匹配,示例文档中使用的是-->/sn1/
        String anchorString=jsonObject.getString("anchorString");
        //签署人收到的邮件标题->EmailSubject
        String emailSubject=jsonObject.getString("emailSubject");
        //Base64格式的合同文档
        String contractBase64 = jsonObject.getString("contract");
        //合同文件名
        String contractName = jsonObject.getString("contactName");
        //是否让签署者接受邮件通知
        String signerNotify = jsonObject.getString("signerNotify");
        *//*
         签署人需要什么样的授权,如果没有填写none
         获取更多授权信息`authenticationMethod' 官方文档:
         https://developers.docusign.com/esign-rest-api/reference/Envelopes/EnvelopeViews/createRecipient
         *//*
        String authenticationMethod = DocuSignConfig.authenticationMethod;
        *//*
          测试文档: World_Wide_Corp_lorem.pdf
          系统本地路径+文件名称
         *//*
        byte[] buffer =  file2byte("/Users/aarons/Downloads/World_Wide_Corp_lorem.pdf");
        String docBase64 = new String(Base64.encode(buffer));

        //创建<合同>文件
        Document contract = new Document();
        contract.setDocumentBase64(docBase64);
        contract.setName(contractName); // 可以与实际文件名不同
        String contractType=contractName.substring(contractName.lastIndexOf(".")+1);
        contract.setFileExtension(contractType); // 可以支持不同格式的文件
        contract.setDocumentId(Constant.ONE); // 如果有多个doc,以此作为文件顺序标识符

        *//**
         * 文件列表
         *//*
        List<Document> documents = Arrays.asList(contract);
        // 创建签署人对象
        Signer signer = new Signer();
        signer.setEmail(signerEmail);
        signer.setName(signerName);
        signer.clientUserId(clientUserId);
        signer.recipientId("1");
        //不让签署者收到邮件通知
        signer.setSuppressEmails("true");
        if (signerNotify !=null && "y".equalsIgnoreCase(signerNotify)){
            signer.setSuppressEmails("false");
        }
        *//*
           创建一个signHere tabs (区域) 在文件上,
         *//*
        SignHere contractSignHere = new SignHere();
        contractSignHere.setAnchorString(anchorString);//设置要匹配的自动锚定字符串
        contractSignHere.setAnchorUnits("pixels");
        contractSignHere.setAnchorXOffset("20");
        contractSignHere.anchorYOffset("10");
        contractSignHere.setTooltip("Please sign here.");
        contractSignHere.setDocumentId("1");//对应着之前为document对象设置的documentID
        contractSignHere.setRecipientId("1");//设置签署人的id信息

        *//*
        //通过坐标确定要签署的位置
        signHere.setTabLabel("SignHereTab");//
        signHere.setXPosition("195");//签署位置x轴坐标
        signHere.setYPosition("147");//签署位置y轴坐标
        *//*

        *//*
        将tab(创建的signHere对象)添加到签署人对象中--Add the tabs to the signer object
        可以创建多个签署区域,接收Array作为参数 The Tabs object wants arrays of the different field/tab types
         *//*
        Tabs signerTabs = new Tabs();
        signerTabs.setSignHereTabs(Arrays.asList(contractSignHere));
        signer.setTabs(signerTabs);

        //接下来，创建顶层信封定义并填充它。
        // Next, create the top level envelope definition and populate it.
        EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
        //设置签署收到的邮件提示的标题
        envelopeDefinition.setEmailSubject(emailSubject);
        envelopeDefinition.setDocuments(documents);
        // 将签署人信息添加到envelope对象中 Add the recipient to the envelope object
        Recipients recipients = new Recipients();
        recipients.setSigners(Arrays.asList(signer));
        envelopeDefinition.setRecipients(recipients);
        *//*
           将信封的状态设置为"sent",只有状态为sent的信封对象才可以被签署
         *//*
        envelopeDefinition.setStatus(DocuSignEnum.ENVELOP_STATUS_SENT.getCode());
        //设置WebHook==>eventNotification URL:https://developers.docusign.com/esign-rest-api/reference/Envelopes/Envelopes/create#eventNotification
        EventNotification eventNotification=new EventNotification();
        //创建通知webHook的时间节点->设置为完成
        EnvelopeEvent envelopeEvent=new EnvelopeEvent();
        envelopeEvent.setEnvelopeEventStatusCode(DocuSignEnum.ENVELOP_EVENT_COMPLETED.getCode());
        eventNotification.setEnvelopeEvents(Arrays.asList(envelopeEvent));
        *//**
         * 通过HTTPS POST请求将Webhook通知消息发送到的端点。
         * 网址必须以https开头。
         * 客户的Web服务器必须使用SSL / TLS证书，
         *//*
        eventNotification.setUrl(DocuSignConfig.resultWebhookURL);
        envelopeDefinition.setEventNotification(eventNotification);
        String redirectUrl=null;
        String envelopeId=null;
        try {
            *//**
             * 步骤二.请求docuSign去创建并发送信封
             * DocuSign核心对象,通过exampleBase获取已经封装好信息的对象
             *//*
            //获取创建信封的对象
            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            EnvelopeSummary results = envelopesApi.createEnvelope(accountId, envelopeDefinition);
            envelopeId = results.getEnvelopeId();

            // 步骤三: 信封已经被创建
            // Step 3. The envelope has been created.
            // 获取一个签署人签署仪式试图链接地址(就是签署人要跳转并签署文件的页面(docuSign的页面))
            // Request a Recipient View URL (the Signing Ceremony URL)
            RecipientViewRequest viewRequest = new RecipientViewRequest();
            // 设置当签署完成后,需要签署人跳转的页面(callback route somewhere in your app)
            viewRequest.setReturnUrl(localBaseUrl);
            viewRequest.setAuthenticationMethod(authenticationMethod);
            viewRequest.setEmail(signerEmail);
            viewRequest.setUserName(signerName);
            viewRequest.setClientUserId(clientUserId);

            // 请求<创建签署人视图>API (call the CreateRecipientView API)
            ViewUrl viewUrl = envelopesApi.createRecipientView(accountId, envelopeId, viewRequest);
            //步骤四:从返回数据中取出URL 返回给前端用于跳转
            redirectUrl = viewUrl.getUrl();
        }catch (Exception e){
            log.error("DocuSign gen Singing Url error:{},param:{}",e.getMessage(),jsonObject);
            throw new BizException(e.getMessage());
        }*/
    }
}
