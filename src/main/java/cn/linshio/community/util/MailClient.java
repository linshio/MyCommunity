package cn.linshio.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

//使用邮箱注册工具
@Component
public class MailClient {

    public static final Logger LOGGER = LoggerFactory.getLogger(MailClient.class);


    //发送方邮件名
    @Value("${spring.mail.username}")
    private String from;

//    JavaMailSender是Spring Email的核心组件，负责发送邮件。
    @Resource
    private JavaMailSender sender;

    /**
     * 发送邮件
     * @param to        收件人邮箱
     * @param subject   邮件主题
     * @param content   邮件内容
     */
    public void sendMail(String to,String subject,String content){
//        MimeMessage用于封装邮件的相关信息
        MimeMessage mimeMessage = sender.createMimeMessage();
//        MimeMessageHelper用于辅助构建MimeMessage对象
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            //开启支持html内容
            helper.setText(content,true);
            //发送邮件
            sender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            LOGGER.error("发送邮件失败==>"+e.getMessage());
        }
    }

}
