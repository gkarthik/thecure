package org.scripps.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;


public class Mail {

	transient Session			mailSession;
	//TODO: add preferences
	public String		mailhost;
	public String		mailuser;
	public String		mailpassword;
	public static String		mailPort = "465";
	public static String		mailSecurity = "true";
	public static String		mailFromAddress = "bgood@scripps.edu";

	// Open the mail session if it isn't already open.
	final static SimpleDateFormat	logFormat		= new SimpleDateFormat("HH:mm:ss:SSS");

	public Mail(String propsfile){
		InputStream in  = Mail.class.getResourceAsStream(propsfile);
		Properties props = new Properties();
		try {
			props.load(in);
			in.close();
			mailuser = props.getProperty("SMTPUsername");
			mailpassword = props.getProperty("SMTPPassword");
			mailhost = props.getProperty("ServerName");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String [] args) throws IOException{
		Mail m = new Mail("/props/EmailCredentials.properties");
		String messageText ="hello one more time again ben"; String subject = "testing 123"; String addrFrom = "bgood@scripps.edu"; String nameFrom = "Ben Good";
		String nameto = "Benjamin"; String addrto = "ben.mcgee.good@gmail.com";
		m.sendMail(messageText, subject, addrFrom, nameFrom, nameto, addrto);
		
	}
	
	private class PasswordAuthenticator extends javax.mail.Authenticator {
		 public PasswordAuthentication getPasswordAuthentication() {
		 return new PasswordAuthentication(mailuser, mailpassword);
		}
	}
	
	private class MailAuthenticator extends Authenticator {
		  public PasswordAuthentication getPasswordAuthentication() {
		    return new PasswordAuthentication(mailuser, mailpassword);
		  }
	}

	Session getMailSession() {
		// Create mail session if it doesn't exist
		if (mailSession == null) {
			Authenticator auth = new MailAuthenticator();
			Properties props = new Properties();
			props.put("mail.host", mailhost);
			props.put("mail.smtp.host", mailhost);
			props.put("mail.smtp.auth", "true");
			if (mailPort.length()>1)
				props.put("mail.smtp.port", mailPort);
			if (mailSecurity.length()>1)
				props.put("mail.smtp.starttls.enable", mailSecurity);
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", mailhost);
			props.put("mail.smtp.auth", "true");
			 // use your gmail account username here
			props.put("mail.mime.charset", "ISO-8859-1");
			props.put("mail.smtp.socketFactory.port", mailPort);
			props.put("mail.smtp.socketFactory.fallback", "false");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");			
			mailSession = Session.getDefaultInstance(props, auth);
		}
		return mailSession;
	}

	public boolean sendMail(String messageText, String subject, String addrFrom, String nameFrom, String nameto, String addrto) {
		Session s = getMailSession();
		// Create new message
		MimeMessage msg = new MimeMessage(s);
		int location = 0;
		try {
			// add related multi-part for root
			MimeMultipart multipartRoot = new MimeMultipart("mixed");
			MimeBodyPart contentRoot = new MimeBodyPart();
			MimeMultipart multipartAlt = new MimeMultipart("alternative");
			// alternative message
			BodyPart messageBodyPart;
			messageBodyPart = new MimeBodyPart();
			location++;
			if (messageText != null && messageText.length() > 0) {
				messageBodyPart.setContent(messageText, "text/plain");
				multipartAlt.addBodyPart(messageBodyPart);
			}
			location++;
//			if (messageHTML != null && messageHTML.length() > 0) {
//				messageBodyPart = new MimeBodyPart();
//				messageBodyPart.setContent(messageHTML, "text/html");
//				multipartAlt.addBodyPart(messageBodyPart);
//			}
			location++;
			contentRoot.setContent(multipartAlt);
			location++;
			multipartRoot.addBodyPart(contentRoot);
			location++;
			// Part two is attachment
			// filename is full path to file on the server starting at the serverRoot directory not including it.
//			if ((serverFilePath != null) && (serverFilePath.length() > 2)) {
//				if (attachedFilename != null && attachedFilename.length() > 1) {
//					messageBodyPart = new MimeBodyPart();
//					DataSource source = new FileDataSource(FileResponder.serverRoot + serverFilePath);
//					messageBodyPart.setDataHandler(new DataHandler(source));
//					messageBodyPart.setFileName(attachedFilename);
//					multipartRoot.addBodyPart(messageBodyPart);
//				}
//				if (attachedFilename1 != null && attachedFilename1.length() > 1) {
//					BodyPart messageBodyPart1 = new MimeBodyPart();
//					DataSource source1 = new FileDataSource(FileResponder.serverRoot + serverFilePath1);
//					messageBodyPart1.setDataHandler(new DataHandler(source1));
//					messageBodyPart1.setFileName(attachedFilename1);
//					multipartRoot.addBodyPart(messageBodyPart1);
//				}
//				if (attachedFilename2 != null && attachedFilename2.length() > 1) {
//					BodyPart messageBodyPart2 = new MimeBodyPart();
//					DataSource source2 = new FileDataSource(FileResponder.serverRoot + serverFilePath2);
//					messageBodyPart2.setDataHandler(new DataHandler(source2));
//					messageBodyPart2.setFileName(attachedFilename2);
//					multipartRoot.addBodyPart(messageBodyPart2);
//				}
//			}
			if (subject != null) {
				msg.setSubject(subject);
			}
			if (nameFrom == null)
				nameFrom = "";
			if (nameto == null)
				nameto = "";
			//addrFrom = addrFrom.trim();
			//addrto = addrto.trim();
			// Address fromAddr;
			// if (nameFrom.length()>0)
			// fromAddr = new InternetAddress(addrFrom, nameFrom);
			// else
			// fromAddr = new InternetAddress(addrFrom);
			// msg.setFrom(fromAddr);
			// msg.setFrom(new InternetAddress("noreply@epeteonline.com"));
			msg.setFrom(new InternetAddress(mailFromAddress));
			Address replyAddr;
			if (nameFrom.length() > 0)
				replyAddr = new InternetAddress(addrFrom, nameFrom);
			else
				replyAddr = new InternetAddress(addrFrom);
			Address[] replyAddresses;
			replyAddresses = new Address[1];
			replyAddresses[0] = replyAddr;
			// if (nameFrom.length()>0)
			// fromAddr = new InternetAddress(addrFrom, nameFrom);
			// else
			// fromAddr = new InternetAddress(addrFrom);
			msg.setReplyTo(replyAddresses);
			Address toAddr;
			if (nameto.length() > 0)
				toAddr = new InternetAddress(addrto, nameto);
			else
				toAddr = new InternetAddress(addrto);
			msg.addRecipient(Message.RecipientType.TO, toAddr);
//			if (addrCC != null) {
//				addrCC = addrCC.trim();
//				if (addrCC.length() > 0) {
//					Address ccAddr = new InternetAddress(addrCC);
//					if (ccAddr != null)
//						msg.addRecipient(Message.RecipientType.CC, ccAddr);
//				}
//			}
			msg.setContent(multipartRoot);
			msg.setSentDate(new Date());
			// Send the message
			location++;
			Transport.send(msg);
			return true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException m) {
			m.printStackTrace();
//			String message = logFormat.format(new Date()) + "  " + location + "  Messaging exception: "
//					+ m.getMessage() + "\n";
//			message += "To Address:" + getAddrto() + "\n";
//			message += ("Name To:" + getNameto()) + "\n";
//			message += ("From Address:" + getAddrFrom()) + "\n";
//			message += ("Name From:" + getNameFrom()) + "\n";
//			message += ("CC Address:" + getAddrCC()) + "\n";
//			if (messageText != null && messageText.length() > 0) {
//				message += "TEXT :\n"+messageText+ "\n\n";
//			}
//			if (messageHTML != null && messageHTML.length() > 0) {
//				message += "HTML :\n"+messageHTML+"\n\n";
//			}
//
//			ErrorHandler.handleStatus(message, 0);
		} catch (Exception g) {
			g.printStackTrace();
//			String message = (logFormat.format(new Date()) + "  " + location + " Unknown  Exception" + g.getMessage());
//			message += "To Address:" + getAddrto() + "\n";
//			message += "Name To:" + getNameto() + "\n";
//			message += "From Address:" + getAddrFrom() + "\n";
//			message += "Name From:" + getNameFrom() + "\n";
//			message += "CC Address:" + getAddrCC();
//			ErrorHandler.handleStatus(message, 0);
		}
		return false;
	}


}