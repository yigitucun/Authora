package com.authora.authorization.server.infrastructure.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    public void sendEmail(String to, EmailTemplate template) {
        MimeMessage message = mailSender.createMimeMessage();
        try{
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setTo(to);
            helper.setSubject(template.subject());
            helper.setText(template.body(),true);
            mailSender.send(message);
        }catch (MessagingException e){
            System.out.println(e);
        }
    }
}
