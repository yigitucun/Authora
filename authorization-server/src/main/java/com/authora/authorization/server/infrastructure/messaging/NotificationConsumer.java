package com.authora.authorization.server.infrastructure.messaging;

import com.authora.authorization.server.infrastructure.mail.EmailService;
import com.authora.authorization.server.infrastructure.mail.VerificationEmailTemplate;
import com.authora.authorization.server.infrastructure.messaging.event.EmailVerificationEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "notification.email.verification",
            groupId = "authora-authorization-server",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleEmailVerification(EmailVerificationEvent event) {
        log.info("Sending verification email to: {}", event.to());
        VerificationEmailTemplate template = VerificationEmailTemplate.builder()
                .verificationUrl(event.verificationUrl())
                .build();
        emailService.sendEmail(event.to(), template);
    }
}
