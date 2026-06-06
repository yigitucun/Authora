package com.authora.authorization.server.infrastructure.mail;

import lombok.Builder;

@Builder
public record VerificationEmailTemplate(String verificationUrl) implements EmailTemplate {

    @Override
    public String subject() {
        return "Authora - Email Doğrulama";
    }

    @Override
    public String body() {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px;">
                <div style="background: #000; padding: 24px; border-radius: 12px; margin-bottom: 24px;">
                    <h1 style="color: #fff; margin: 0;">Authora</h1>
                </div>
                <h2>Email Doğrulama</h2>
                <p>Hesabınızı doğrulamak için aşağıdaki bağlantıya tıklayın:</p>
                <p style="margin: 20px 0;">
                    <a href="%s" style="background: #000; color: #fff; padding: 12px 20px; border-radius: 8px; text-decoration: none; display: inline-block;">
                        Email Adresimi Doğrula
                    </a>
                </p>
                <p style="color: #666; font-size: 14px;">Bağlantı 30 dakika geçerlidir.</p>
                <p style="color: #666; font-size: 14px;">Eğer bu işlemi siz yapmadıysanız bu maili dikkate almayın.</p>
            </body>
            </html>
        """.formatted(verificationUrl);
    }
}