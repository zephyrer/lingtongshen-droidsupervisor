package com.google.GFS;
import android.util.Log;

import com.google.GFS.MailSenderInfo;
import com.google.GFS.SimpleMailSender;

public class MailDriver {	
	private SimpleMailSender sms;
    private final String HOST="smtp.163.com";
    private final String PORT="25";
    
    public boolean sendMailPlainText(
    		String mail_from, 
    		String mail_passwd, 
    		String mail_to, 
    		String subject, 
    		String content) throws Exception
    {
    	MailSenderInfo mailInfo = new MailSenderInfo();   
    	mailInfo.setMailServerHost(HOST);   
    	mailInfo.setMailServerPort(PORT);   
    	mailInfo.setValidate(true);   
    	mailInfo.setUserName(mail_from);
    	mailInfo.setPassword(mail_passwd);
    	mailInfo.setFromAddress(mail_from);   
    	mailInfo.setToAddress(mail_to);
    	mailInfo.setSubject(subject);     
    	mailInfo.setContent(content);
    	sms = new SimpleMailSender();
    	return sms.sendTextMail(mailInfo);
    }
}
