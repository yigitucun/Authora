package com.authora.authorization.server.infrastructure.mail;

public interface EmailTemplate {
    String subject();
    String body();
}
