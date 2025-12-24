package io.github.jiwontechinnovation.common.util;

import org.springframework.stereotype.Component;

@Component
public class MailUtil {
    public void sendMail(String to, String subject, String body) {
        System.out.println("Sending mail to: " + to);
    }
}
