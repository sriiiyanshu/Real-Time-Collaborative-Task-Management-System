package com.taskmanager.service;

import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import com.taskmanager.config.AppConfig;

/**
 * Service class for sending various types of emails
 */
public class EmailService {
    
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean useTls;
    private final String senderEmail;
    private final String senderName;
    
    /**
     * Constructor that loads email configuration
     */
    public EmailService() {
        // Load configuration from application properties
        host = AppConfig.getProperty("mail.smtp.host", "smtp.gmail.com");
        port = Integer.parseInt(AppConfig.getProperty("mail.smtp.port", "587"));
        username = AppConfig.getProperty("mail.username", "");
        password = AppConfig.getProperty("mail.password", "");
        useTls = Boolean.parseBoolean(AppConfig.getProperty("mail.smtp.tls", "true"));
        senderEmail = AppConfig.getProperty("mail.sender.email", "noreply@taskmanager.com");
        senderName = AppConfig.getProperty("mail.sender.name", "Task Manager");
    }
    
    /**
     * Initialize and return a mail session
     */
    private Session getMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        
        if (useTls) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        
        // Create authenticator for SMTP authentication
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        
        return Session.getInstance(props, auth);
    }
    
    /**
     * Send welcome email to new users
     */
    public void sendWelcomeEmail(String recipientEmail, String recipientName) {
        String subject = "Welcome to Task Manager!";
        
        String content = 
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Welcome to Task Manager, " + recipientName + "!</h2>" +
                "<p>Thank you for joining our platform. We're excited to have you onboard!</p>" +
                "<p>With Task Manager, you can:</p>" +
                "<ul>" +
                "  <li>Create and manage projects</li>" +
                "  <li>Track tasks and deadlines</li>" +
                "  <li>Collaborate with team members</li>" +
                "  <li>Monitor progress with analytics</li>" +
                "</ul>" +
                "<p>If you have any questions, please don't hesitate to contact our support team.</p>" +
                "<p>Best regards,<br>The Task Manager Team</p>" +
                "</body>" +
                "</html>";
        
        sendEmail(recipientEmail, recipientName, subject, content);
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink) {
        String subject = "Password Reset Request";
        
        String content = 
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Password Reset Request</h2>" +
                "<p>Hello " + recipientName + ",</p>" +
                "<p>We received a request to reset your password. If you didn't make this request, you can ignore this email.</p>" +
                "<p>To reset your password, click the link below:</p>" +
                "<p><a href='" + resetLink + "'>Reset Your Password</a></p>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>If the button above doesn't work, copy and paste the following URL into your browser:</p>" +
                "<p>" + resetLink + "</p>" +
                "<p>Best regards,<br>The Task Manager Team</p>" +
                "</body>" +
                "</html>";
        
        sendEmail(recipientEmail, recipientName, subject, content);
    }
    
    /**
     * Send task assignment notification email
     */
    public void sendTaskAssignmentEmail(String recipientEmail, String recipientName, 
                                        String taskTitle, String projectName, String taskLink) {
        String subject = "New Task Assigned: " + taskTitle;
        
        String content = 
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>New Task Assignment</h2>" +
                "<p>Hello " + recipientName + ",</p>" +
                "<p>You have been assigned a new task:</p>" +
                "<p><strong>Task:</strong> " + taskTitle + "<br>" +
                "<strong>Project:</strong> " + projectName + "</p>" +
                "<p>To view the task details and get started, please click the link below:</p>" +
                "<p><a href='" + taskLink + "'>View Task Details</a></p>" +
                "<p>Best regards,<br>The Task Manager Team</p>" +
                "</body>" +
                "</html>";
        
        sendEmail(recipientEmail, recipientName, subject, content);
    }
    
    /**
     * Send task deadline reminder email
     */
    public void sendTaskDeadlineReminderEmail(String recipientEmail, String recipientName, 
                                             String taskTitle, String dueDate, String taskLink) {
        String subject = "Reminder: Task Due Soon - " + taskTitle;
        
        String content = 
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Task Deadline Reminder</h2>" +
                "<p>Hello " + recipientName + ",</p>" +
                "<p>This is a friendly reminder that the following task is due soon:</p>" +
                "<p><strong>Task:</strong> " + taskTitle + "<br>" +
                "<strong>Due Date:</strong> " + dueDate + "</p>" +
                "<p>To view the task details, please click the link below:</p>" +
                "<p><a href='" + taskLink + "'>View Task Details</a></p>" +
                "<p>Best regards,<br>The Task Manager Team</p>" +
                "</body>" +
                "</html>";
        
        sendEmail(recipientEmail, recipientName, subject, content);
    }
    
    /**
     * Send general notification email
     */
    public void sendNotificationEmail(String recipientEmail, String recipientName, 
                                     String subject, String message, String link) {
        String content = 
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>" + subject + "</h2>" +
                "<p>Hello " + recipientName + ",</p>" +
                "<p>" + message + "</p>";
                
        if (link != null && !link.isEmpty()) {
            content += "<p><a href='" + link + "'>Click here for more details</a></p>";
        }
        
        content += 
                "<p>Best regards,<br>The Task Manager Team</p>" +
                "</body>" +
                "</html>";
        
        sendEmail(recipientEmail, recipientName, subject, content);
    }
    
    /**
     * Send an email
     */
    private void sendEmail(String recipientEmail, String recipientName, String subject, String htmlContent) {
        try {
            Session session = getMailSession();
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail, senderName));
            message.setRecipients(Message.RecipientType.TO, 
                    InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            // Send message
            Transport.send(message);
            
            System.out.println("Email sent successfully to: " + recipientEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}