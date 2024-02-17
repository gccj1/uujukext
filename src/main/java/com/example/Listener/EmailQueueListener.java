package com.example.Listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "email")
public class EmailQueueListener {
    @Resource
    JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;
    @RabbitHandler
    public void processEmailVerify(Map<String,Object> map) {
        String email=(String) map.get("email");
        int code=(int) map.get("code");
        String type=(String) map.get("type");
        SimpleMailMessage message=switch (type){
            case "register"->
                    mailMessage("GCCJTop验证邮件",
                            "您的注册验证码是："+code+"一分钟内有效",
                            email);
            case "forget"->
                    mailMessage("GCCJTop验证邮件",
                            "正在重置密码,您的验证码是："+code+"一分钟内有效",
                            email);
            default -> null;
        };
        if(message==null) return;
        System.out.println(code);
       // mailSender.send(message);
    }
    private SimpleMailMessage mailMessage(String subject,String content,String to){
        SimpleMailMessage message=new SimpleMailMessage();
        message.setSubject(subject);
        message.setText(content);
        message.setTo(to);
        message.setFrom(from);
        return message;
    }
}

