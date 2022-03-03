package com.uwallet.pay.main.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * @author baixinyue
 * @date 2019/09/03
 *
 */
@Component
public class MailUtil {

    private static String emailType;

    @Value("${uWallet.emailType}")
    public void setEmailType (String emailType) {
        MailUtil.emailType = emailType;
    }

    public static String MAIL_CONTENT_1 = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "\t<head>\n" +
            "\t\t<meta charset=\"utf-8\" />\n" +
            "\t\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "\t\t<title></title>\n" +
            "\t\t<style type=\"text/css\">\n" +
            "\t\t\t.colorClass {\n" +
            "\t\t\t\tcolor: rgb(31,78,121);\n" +
            "\t\t\t}\n" +
            "\t\t\t.fontClass {\n" +
            "\t\t\t\tfont-weight: bold;\n" +
            "\t\t\t}\n" +
            "\t\t\t.aColorClass {\n" +
            "\t\t\t\tcolor: rgb(17,85,204);\n" +
            "\t\t\t}\n" +
            "\t\t\t.bankgroundClass {\n" +
            "\t\t\t\twidth: 200px;\n" +
            "\t\t\t\theight: 30px;\n" +
            "\t\t\t\tbackground: url(http://imagetest-image.loancloud.cn/paper/506672400974958592.png) no-repeat 20px 0px;\n" +
            "\t\t\t\tbackground-size: 120px 30px;\n" +
            "\t\t\t}\n" +
            "\t\t</style>\n" +
            "\t</head>\n" +
            "\t<body>\n" +
            "\t\t<p>&nbsp;&nbsp;&nbsp;&nbsp;";

    public static String MAIL_CONTENT_2 = "</p>\n" +
            "\t\t<p>&nbsp;&nbsp;&nbsp;&nbsp;If you have any questions or require any assistance, Please contact us. </p>\n" +
            "\t\t\n" +
            "\t\t<p>&nbsp;&nbsp;&nbsp;&nbsp;Regards, </p>\n" +
            "\t\t<p class=\"colorClass fontClass\">&nbsp;&nbsp;&nbsp;&nbsp;Payo Customer Service Team</p>\n" +
            "\t\t<p class=\"colorClass fontClass\">&nbsp;&nbsp;&nbsp;&nbsp;PAYO FUNDS PTY LTD</p>\n" +
            "\t\t<p>&nbsp;&nbsp;&nbsp;&nbsp;<span class=\"colorClass fontClass\">Tel</span>&nbsp;<span class=\"fontClass\">1800 777 290</span></p>\n" +
            "\t\t<p>&nbsp;&nbsp;&nbsp;&nbsp;<span class=\"colorClass fontClass\">Email</span>&nbsp;<a class=\"fontClass aColorClass\" href=\"info@payo.com.au\">info@payo.com.au</a></p>\n" +
            "\t\t<p>&nbsp;&nbsp;&nbsp;&nbsp;<span class=\"colorClass fontClass\">Web</span>&nbsp;<a class=\"fontClass aColorClass\" href=\"https://www.payo.com.au/\">payo.com.au</a></p>\n" +
            "\t\t<div class=\"bankgroundClass\"></div>\n" +
            "\t</body>\n" +
            "</html>";

    /**
     * 获取邮箱环境信息Session
     * @param senderAddr 发送邮件地址
     * @return
     */
    public static Session getSession(String senderAddr){
        //1.连接邮件服务器参数
        Properties props = new Properties();
        //2.设置用户认证
        props.setProperty("mail.smtp.auth","true");
        //3.设置传输协议
        props.setProperty("mail.transport.protocol","smtp");
        //4.设置发件人smtps服务器地址
        StringBuffer sb = new StringBuffer(senderAddr);
        String smtpsHost = "smtp." + emailType + ".com";
        props.setProperty("mail.smtp.host", smtpsHost);
        props.setProperty("mail.debug", "true");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.ssl.enable", "true");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        //5.创建应用程序所需配置信息的session对象
        Session session = Session.getInstance(props);
        //6.控制台打印信息
        session.setDebug(false);

        return session;
    }

    /**
     * 获取邮件实体
     * @param nickName 邮箱昵称
     * @param senderAddr 发送邮件地址
     * @param recipientAddr 接受邮件地址
     * @param mailTitle 邮件标题
     * @param mailText 邮件正文
     * @param attachments 邮件附件
     * @return
     * @throws Exception
     */
    public static MimeMessage getMimeMessage(String nickName, String senderAddr, String recipientAddr, String mailTitle, String mailText, List<File> attachments,Session session) throws Exception{
//        mailText = MAIL_CONTENT_1 + mailText + MAIL_CONTENT_2;
        //换新邮件模板不用前后拼接内容

        //创建邮件实例
        MimeMessage msg = new MimeMessage(session);
        //设置发送人地址
        msg.setFrom(new InternetAddress(nickName + "<" + senderAddr + ">"));

        //设置收件人地址
        msg.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(recipientAddr));

        //设置邮件主题
        msg.setSubject(mailTitle,"UTF-8");
        //创建邮件主体
        Multipart multipart = new MimeMultipart();
        //设置文本内容
        BodyPart contentPart = new MimeBodyPart();
        contentPart.setContent(mailText,"text/html;charset=UTF-8");
        multipart.addBodyPart(contentPart);
        //设置附件内容
        if(attachments!=null){
            for(File file : attachments){
                BodyPart filePart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                filePart.setDataHandler(new DataHandler(source));
                filePart.setFileName(MimeUtility.encodeText(file.getName()));
                multipart.addBodyPart(filePart);
            }
        }

        msg.setContent(multipart);
        //保存邮件
        msg.saveChanges();

        return msg;
    }

    /**
     * 邮件发送方法
     * @param session 邮件环境配置信息
     * @param message 邮件主体
     * @param user 用户名,一般是发送方的邮箱，如果有账号则填写账号
     * @param password smtps授权码
     * @throws Exception
     */
    public static void sendMail(Session session,Message message,String user,String password) throws Exception{
        //创建传输对象transport
        Transport transport = session.getTransport();
        //发件人用户名和smtps授权密码
        transport.connect(user, password);
        transport.sendMessage(message, message.getAllRecipients());

        //关闭
        transport.close();
    }

    /**
     * 邮件发送方法
     * @param nickName 发送昵称
     * @param senderAddr 发送地址
     * @param recipientAddr 接受地址
     * @param user 用户名
     * @param password smtps授权码
     * @param mailTitle 邮件标题
     * @param mailText 邮件正文
     * @param attachments 邮件附件
     * @throws Exception
     */
    public static  void sendMail(String nickName, String senderAddr,String recipientAddr,String user,String password,String mailTitle,String mailText,List<File> attachments) throws Exception{
        Session session = getSession(senderAddr);
        Message message = getMimeMessage(nickName, senderAddr,recipientAddr,mailTitle,mailText,attachments,session);
        //创建传输对象transport
        Transport transport = session.getTransport();
        //发件人用户名和smtps授权密码
        transport.connect(user,password);
        transport.sendMessage(message,message.getAllRecipients());

        //关闭
        transport.close();
    }

    /**
     * 邮件发送方法
     * @param nickName 发送昵称
     * @param senderAddr 发送地址
     * @param recipientAddr 接受地址
     * @param user 用户名
     * @param password smtps授权码
     * @param mailTitle 邮件标题
     * @param mailText 邮件正文
     * @param attachments 邮件附件
     * @throws Exception
     */
    public static  void sendMail(String nickName, String senderAddr,String recipientAddr,String user,String password,String mailTitle,String mailText,List<File> attachments, List<String> copyTo) throws Exception{
        Session session = getSession(senderAddr);
        Message message = getMimeMessage(nickName, senderAddr,recipientAddr,mailTitle,mailText,attachments,session);
        if (!copyTo.isEmpty()) {
            for (String email : copyTo) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(email));
            }
        }
        //创建传输对象transport
        Transport transport = session.getTransport();
        //发件人用户名和smtps授权密码
        transport.connect(user,password);
        transport.sendMessage(message,message.getAllRecipients());

        //关闭
        transport.close();
    }

}
