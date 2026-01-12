package com.example.smart_bin_server.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.verification.url}")
    private String verificationUrl;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String firstName, String token){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Smart Bin - Verify Email");

            String htmlContent = buildVerificationEmailHtml(firstName, token);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        }catch (MessagingException ex){
            throw new RuntimeException("Failed to send verification email", ex);
        }
    }

    private String buildVerificationEmailHtml(String firstName, String token){
        String verifyLink = verificationUrl + "?token=" + token;

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; 
                             color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Smart Bin System</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for registering with Smart Bin System!</p>
                        <p>Please verify your email address by clicking the button below:</p>
                        <center>
                            <a href="%s" class="button">Verify Email</a>
                        </center>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #4CAF50;">%s</p>
                        <p>This link will expire in 24 hours.</p>
                        <p>If you didn't create this account, please ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 Smart Bin System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName, verifyLink, verifyLink);
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Smart Bin System");

            String htmlContent = buildWelcomeEmailHtml(firstName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    private String buildWelcomeEmailHtml(String firstName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Smart Bin System! 🎉</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Your email has been successfully verified!</p>
                        <p>You can now start using Smart Bin System to manage your smart waste bins.</p>
                        <h3>Getting Started:</h3>
                        <ul>
                            <li>Add your first device</li>
                            <li>Monitor waste levels in real-time</li>
                            <li>Track recycling statistics</li>
                            <li>Receive notifications when bins are full</li>
                        </ul>
                        <p>If you have any questions, feel free to contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 Smart Bin System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName);
    }
}
