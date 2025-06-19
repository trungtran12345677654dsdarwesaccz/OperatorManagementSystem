package org.example.operatormanagementsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;  // gui mail den sever trc khi gui den nguoi dung

    @Value("${spring.mail.port}")
    private int port;  // cong

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.starttls.required}")
    private boolean starttlsRequired;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.starttls.required", starttlsRequired);
        props.put("mail.debug", "true");

        return mailSender;
    }
}

//Ung dung cua ban muon gui mot email. No can mot "buu dien" (SMTP server) de gui thu di.
//No se ket noi den host (dia chi cua buu dien) va port (cong vao cua buu dien) ma ban da cau hinh.
//Tai day, username va password duoc su dung de xac thuc ban co quyen gui thu thong qua buu dien nay khong.
//Neu xac thuc thanh cong, ung dung se giao noi dung email cho may chu SMTP.
