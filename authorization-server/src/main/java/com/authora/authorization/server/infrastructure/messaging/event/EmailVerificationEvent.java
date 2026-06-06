package com.authora.authorization.server.infrastructure.messaging.event;

/**
 * Kafka event published when a user needs their email verified.
 * The consumer picks this up and sends the actual email.
 */
public record EmailVerificationEvent(
        String to,
        String verificationUrl
) {}
