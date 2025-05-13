package com.cinebook.util;

import com.cinebook.model.Reservation;

import java.io.File;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * Service for sending emails.
 */
public class EmailService {
    
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "noreply@cinebook.com";
    private static final String EMAIL_USERNAME = "username"; // Replace with actual email credentials
    private static final String EMAIL_PASSWORD = "password"; // Replace with actual email credentials
    
    /**
     * Sends an email with the specified parameters.
     *
     * @param to The recipient's email address
     * @param subject The email subject
     * @param body The email body
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendEmail(String to, String subject, String body) {
        // Setup mail server properties
        Properties props = setupMailServerProperties();
        
        // Get the Session object
        Session session = createMailSession(props);
        
        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);
            
            // Set From
            message.setFrom(new InternetAddress(EMAIL_FROM));
            
            // Set To
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            
            // Set Subject
            message.setSubject(subject);
            
            // Set Content
            message.setContent(body, "text/html");
            
            // Send message
            Transport.send(message);
            System.out.println("Email sent successfully to " + to);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sends an email with an attachment.
     *
     * @param to The recipient's email address
     * @param subject The email subject
     * @param body The email body
     * @param attachmentPath The path to the attachment file
     * @param attachmentName The name of the attachment
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendEmailWithAttachment(String to, String subject, String body, 
                                          String attachmentPath, String attachmentName) {
        // Setup mail server properties
        Properties props = setupMailServerProperties();
        
        // Get the Session object
        Session session = createMailSession(props);
        
        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);
            
            // Set From
            message.setFrom(new InternetAddress(EMAIL_FROM));
            
            // Set To
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            
            // Set Subject
            message.setSubject(subject);
            
            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            
            // Body part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html");
            multipart.addBodyPart(messageBodyPart);
            
            // Attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachmentPath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachmentName);
            multipart.addBodyPart(messageBodyPart);
            
            // Send the complete message parts
            message.setContent(multipart);
            
            // Send message
            Transport.send(message);
            System.out.println("Email with attachment sent successfully to " + to);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending email with attachment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sends an email with an attachment from a byte array.
     *
     * @param to The recipient's email address
     * @param subject The email subject
     * @param body The email body
     * @param attachmentBytes The attachment as a byte array
     * @param attachmentName The name of the attachment
     * @param mimeType The MIME type of the attachment
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendEmailWithByteAttachment(String to, String subject, String body,
                                             byte[] attachmentBytes, String attachmentName, String mimeType) {
        // Setup mail server properties
        Properties props = setupMailServerProperties();
        
        // Get the Session object
        Session session = createMailSession(props);
        
        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);
            
            // Set From
            message.setFrom(new InternetAddress(EMAIL_FROM));
            
            // Set To
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            
            // Set Subject
            message.setSubject(subject);
            
            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            
            // Body part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html");
            multipart.addBodyPart(messageBodyPart);
            
            // Attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(attachmentBytes, mimeType);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachmentName);
            multipart.addBodyPart(messageBodyPart);
            
            // Send the complete message parts
            message.setContent(multipart);
            
            // Send message
            Transport.send(message);
            System.out.println("Email with byte attachment sent successfully to " + to);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending email with byte attachment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sets up mail server properties.
     *
     * @return The Properties object with mail server settings
     */
    private Properties setupMailServerProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        return props;
    }
    
    /**
     * Creates a mail Session with authentication.
     *
     * @param props The Properties object with mail server settings
     * @return The Session object
     */
    private Session createMailSession(Properties props) {
        return Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });
    }
    
    /**
     * Sends tickets via email.
     *
     * @param email The recipient's email address
     * @param reservation The reservation information
     * @param pdfFile The PDF file with tickets
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendTicketsEmail(String email, Reservation reservation, File pdfFile) {
        String subject = "Your CineBook CDO Tickets - Booking #" + reservation.getId();
        
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("<html><body>");
        bodyBuilder.append("<h2>Thank you for booking with CineBook CDO!</h2>");
        bodyBuilder.append("<p>Your booking is confirmed. Please find your tickets attached to this email.</p>");
        
        bodyBuilder.append("<h3>Booking Details:</h3>");
        bodyBuilder.append("<p>Booking ID: ").append(reservation.getId()).append("</p>");
        bodyBuilder.append("<p>Movie: ").append(reservation.getMovieTitle()).append("</p>");
        bodyBuilder.append("<p>Cinema: ").append(reservation.getCinemaName()).append("</p>");
        bodyBuilder.append("<p>Date & Time: ").append(reservation.getFormattedScreeningTime()).append("</p>");
        
        bodyBuilder.append("<p>Please present your tickets at the entrance.</p>");
        bodyBuilder.append("<p>Enjoy your movie!</p>");
        bodyBuilder.append("<p>The CineBook CDO Team</p>");
        bodyBuilder.append("</body></html>");
        
        return sendEmailWithAttachment(email, subject, bodyBuilder.toString(), pdfFile.getPath(), "Your_Tickets.pdf");
    }
}