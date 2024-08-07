package org.sola.admin.services.ejb.scheduler.businesslogic;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import jakarta.activation.DataHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.sola.common.ConfigConstants;
import org.sola.common.DateUtility;
import org.sola.common.StringUtility;
import org.sola.services.common.EntityAction;
import org.sola.services.common.logging.LogUtility;
import org.sola.admin.services.ejb.system.businesslogic.SystemAdminEJBLocal;
import org.sola.admin.services.ejb.system.repository.entities.EmailTask;

/**
 * Scheduler task to process and send emails
 */
@Singleton
@Startup
public class MailerAdmin implements MailerAdminLocal {

    @EJB
    private SystemAdminEJBLocal systemEJB;
    @Resource
    TimerService timerService;

    private String jndiSessionName = "mail/sola";
    private String adminAddress = "";
    private String adminName = "";
    private String failedSendBody = "Message send to the user #{userName} has been failed to deliver after number of attempts with the following error: <br/>#{error}";
    private String failedSendSubject = "SOLA Mailer - Failed to send message";
    private int serviceInterval = 10;
    private boolean enableService = false;
    private boolean htmlFormat = true;
    private int sendInterval1 = 1;
    private int sendInterval2 = 120;
    private int sendInterval3 = 1440;
    private int sendAttempts1 = 2;
    private int sendAttempts2 = 2;
    private int sendAttempts3 = 1;
    private int totalAttempts = 5;

    /**
     * Initialization method to setup timer and email service settings.
     */
    //@PostConstruct
    @Override
    public void init() {
        // Tests to determine if the mail session details are configured correctly or not. 
        jndiSessionName = systemEJB.getSetting(ConfigConstants.EMAIL_MAILER_JNDI_NAME, "mail/sola");
        getMailSession();
        configureMailer();
        serviceInterval = Integer.parseInt(systemEJB.getSetting(ConfigConstants.EMAIL_SERVICE_INTERVAL, "10"));
        long periodMs = (long) serviceInterval * 1000;
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        timerService.createIntervalTimer(periodMs, periodMs, timerConfig);
    }

    /**
     * Configures the mailer settings based on the values in the system.settings table. 
     */
    private void configureMailer() {
        enableService = systemEJB.getSetting(ConfigConstants.EMAIL_ENABLE_SERVICE, "0").equals("1"); 
        if (enableService) {
            htmlFormat = systemEJB.getSetting(ConfigConstants.EMAIL_BODY_FORMAT, "html").equals("html"); 
            adminAddress = systemEJB.getSetting(ConfigConstants.EMAIL_ADMIN_ADDRESS, adminAddress);
            adminName = systemEJB.getSetting(ConfigConstants.EMAIL_ADMIN_NAME, adminName);
            failedSendBody = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_FAILED_SEND_BODY, failedSendBody);
            failedSendSubject = systemEJB.getSetting(ConfigConstants.EMAIL_MSG_FAILED_SEND_SUBJECT, failedSendBody);
            sendInterval1 = Integer.parseInt(systemEJB.getSetting(ConfigConstants.EMAIL_SEND_INTERVAL1, "1"));
            sendInterval2 = Integer.parseInt(systemEJB.getSetting(ConfigConstants.EMAIL_SEND_INTERVAL2, "120"));
            sendInterval3 = Integer.parseInt(systemEJB.getSetting(ConfigConstants.EMAIL_SEND_INTERVAL3, "1440"));
            sendAttempts1 = Integer.parseInt(systemEJB.getSetting(ConfigConstants.EMAIL_SEND_ATTEMPTS1, "2"));
            sendAttempts2 = Integer.parseInt(systemEJB.getSetting(ConfigConstants.EMAIL_SEND_ATTEMPTS2, "2"));
            sendAttempts3 = Integer.parseInt(systemEJB.getSetting(ConfigConstants.EMAIL_SEND_ATTEMPTS3, "1"));
            totalAttempts = sendAttempts1 + sendAttempts2 + sendAttempts3;
        }
    }

    /**
     * Retrieves the latest configuration for the mail session using a context lookup. 
     *
     * @return mailSession or null if no mail session is configured.
     */
    private Session getMailSession() {
        Session mailSession = null;
        try {
            InitialContext ic = new InitialContext();
            mailSession = (Session) ic.lookup(jndiSessionName);
        } catch (NamingException ex) {
            LogUtility.log("Failed to retrieve mail session. Check JavaMail "
                    + "session " + StringUtility.empty(jndiSessionName) + " is configured correctly on Glassfish. ", ex);
        }
        return mailSession;
    }

    @Timeout
    public void processEmails(Timer timer) {
        try {
            if (!enableService) {
                return;
            }
            Session mailSession = getMailSession();
            if (mailSession == null) {
                LogUtility.log("mailSession not configured. Check " + StringUtility.empty(jndiSessionName) + " on Glassfish.");
                return;
            }

            List<EmailTask> tasks = systemEJB.getEmailsToSend();

            for (EmailTask email : tasks) {
                try {
                    Message msg = new MimeMessage(mailSession);
                    msg.setSubject(StringUtility.empty(email.getSubject()));
                    msg.setRecipient(RecipientType.TO, new InternetAddress(
                            StringUtility.empty(email.getRecipient()),
                            StringUtility.empty(email.getRecipientName())));
                    msg.setFrom(new InternetAddress(
                            StringUtility.empty(mailSession.getProperty("mail.from")),
                            StringUtility.empty(mailSession.getProperty("mail.user"))));

                    // Add CC
                    if (!StringUtility.isEmpty(email.getCc())) {
                        InternetAddress[] ccList = InternetAddress.parse(email.getCc());
                        for (InternetAddress cc : ccList) {
                            msg.addRecipient(RecipientType.CC, cc);
                        }
                    }

                    // Add BCC
                    if (!StringUtility.isEmpty(email.getBcc())) {
                        InternetAddress[] bccList = InternetAddress.parse(email.getBcc());
                        for (InternetAddress bcc : bccList) {
                            msg.addRecipient(RecipientType.BCC, bcc);
                        }
                    }

                    if (email.getAttachment() != null && email.getAttachment().length > 0
                            && !StringUtility.isEmpty(email.getAttachmentName())
                            && !StringUtility.isEmpty(email.getAttachmentMimeType())) {
                        // Set body text
                        BodyPart msgBodyPart = new MimeBodyPart();
                        if (htmlFormat) {
                            msgBodyPart.setContent(StringUtility.empty(email.getBody()), "text/html");
                        } else {
                            msgBodyPart.setText(StringUtility.empty(email.getBody()));
                        }

                        Multipart multipart = new MimeMultipart();
                        multipart.addBodyPart(msgBodyPart);

                        // Attachment part
                        msgBodyPart = new MimeBodyPart();

                        ByteArrayDataSource source = new ByteArrayDataSource(email.getAttachment(), email.getAttachmentMimeType());
                        msgBodyPart.setDataHandler(new DataHandler(source));
                        msgBodyPart.setFileName(email.getAttachmentName());
                        multipart.addBodyPart(msgBodyPart);
                        msg.setContent(multipart);
                    } else {
                        // Send without attachment
                        if (htmlFormat) {
                            msg.setContent(StringUtility.empty(email.getBody()), "text/html");
                        } else {
                            msg.setText(StringUtility.empty(email.getBody()));
                        }
                    }

                    Transport.send(msg);
                    deleteEmail(email);

                } catch (Exception e) {
                    MessagingException ee;
                    LogUtility.log("Email message has been failed to send with the following error: " + e.getLocalizedMessage(), Level.SEVERE);

                    // Increase attempt or delete message if it is over maximum attempts
                    try {
                        int currentAttempt = email.getAttempt();
                        if (currentAttempt >= totalAttempts) {
                            // Delete message
                            deleteEmail(email);
                        } else {
                            // Increase time and attempts
                            int minutesToAdd;
                            if (currentAttempt < sendAttempts1) {
                                minutesToAdd = sendInterval1;
                            } else if (currentAttempt < sendAttempts1 + sendAttempts2) {
                                minutesToAdd = sendInterval2;
                            } else {
                                minutesToAdd = sendInterval3;
                            }

                            email.setAttempt(currentAttempt + 1);
                            email.setTimeToSend(DateUtility.addTime(email.getTimeToSend(), minutesToAdd, Calendar.MINUTE));
                            email.setEntityAction(EntityAction.UPDATE);
                            email.setError(e.getLocalizedMessage());
                            systemEJB.saveEmailTask(email);
                        }
                    } catch (Exception ex) {
                        LogUtility.log(ex.getLocalizedMessage(), Level.SEVERE);
                    }

                    // Try to notify administrator
                    if (!StringUtility.isEmpty(adminAddress)) {
                        try {
                            Message msg = new MimeMessage(mailSession);
                            msg.setSubject(StringUtility.empty(failedSendSubject));
                            msg.setRecipient(RecipientType.TO, new InternetAddress(
                                    adminAddress, StringUtility.empty(adminName)));
                            msg.setFrom(new InternetAddress(
                                    StringUtility.empty(mailSession.getProperty("mail.from")),
                                    StringUtility.empty(mailSession.getProperty("mail.user"))));

                            String messageBody = StringUtility.empty(failedSendBody);
                            messageBody = messageBody.replace("#{userName}", StringUtility.empty(email.getRecipient()));
                            messageBody = messageBody.replace("#{error}", StringUtility.empty(e.getLocalizedMessage()));

                            if (htmlFormat) {
                                msg.setContent(messageBody, "text/html");
                            } else {
                                msg.setText(messageBody);
                            }

                            Transport.send(msg);
                        } catch (Exception ex) {
                            LogUtility.log(ex.getLocalizedMessage(), Level.SEVERE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtility.log(e.getLocalizedMessage(), Level.SEVERE);
        }
    }

    private void deleteEmail(EmailTask email) {
        email.setEntityAction(EntityAction.DELETE);
        systemEJB.saveEmailTask(email);
    }
}
