package com.google.GFS;

import java.util.Date;   
import java.util.Properties;  
import javax.mail.Address;   
import javax.mail.BodyPart;   
import javax.mail.Message;   
import javax.mail.MessagingException;   
import javax.mail.Multipart;   
import javax.mail.Session;   
import javax.mail.Transport;   
import javax.mail.internet.InternetAddress;   
import javax.mail.internet.MimeBodyPart;   
import javax.mail.internet.MimeMessage;   
import javax.mail.internet.MimeMultipart;   

import android.util.Log;
public class SimpleMailSender  {   
    public boolean sendTextMail(MailSenderInfo mailInfo) throws Exception {   
      MyAuthenticator authenticator = null;  
      Transport transport =null;
      Properties pro = mailInfo.getProperties();  
      if (mailInfo.isValidate()) {   
        authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());   
      }  
      pro.setProperty("mail.smtp.connectiontimeout", "5000");
      pro.setProperty("mail.smtp.timeout", "5000");
      Session sendMailSession = Session.getDefaultInstance(pro,authenticator);   
      try {   
      Message mailMessage = new MimeMessage(sendMailSession);   
      Address from = new InternetAddress(mailInfo.getFromAddress());   
      mailMessage.setFrom(from);   
      Address to = new InternetAddress(mailInfo.getToAddress());   
      mailMessage.setRecipient(Message.RecipientType.TO,to);   
      mailMessage.setSubject(mailInfo.getSubject());   
      mailMessage.setSentDate(new Date()); 
      mailMessage.setHeader("Reply-To", "mobilemonitor@163.com");
      String mailContent = mailInfo.getContent();   
      mailMessage.setText(mailContent);
      transport = sendMailSession.getTransport("smtp");
      transport.connect(mailInfo.getMailServerHost(), mailInfo.getUserName(), mailInfo.getPassword());
      transport.sendMessage(mailMessage, mailMessage.getAllRecipients()); 
      return true;   
      } catch (MessagingException ex) {   
          ex.printStackTrace();   
      }
      
      finally {
    	   transport.close();
    	   transport = null;
      }

      return false;   
    }   
       
}   
