package net.javajudd.attis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
@Slf4j
public class MailService {

    @Autowired
    JavaMailSender mailSender;

    @Value("${message.send.from}")
    String sendFrom;

    @Autowired
    SpringTemplateEngine thymeleafTemplateEngine;

    public void sendMessage(String to, String subject, String text) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(sendFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            mailSender.send(message);
        } catch (MessagingException ex) {
            log.error("Unable to sent email to {} with message: {} becaue of exception {}", to, text, ex);
        }
    }

    public void sendTemplateMessage(String to, String subject, String template, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        String body = thymeleafTemplateEngine.process("email/" +template + ".html", context);
        sendMessage(to, subject, body);
    }

}
