package com.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class IPReader {

	public void updateProps() throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(System.getProperty("user.dir")+"/clientConfig.ini"));
		try {
			String ip = getIP();
			properties.put("ip", ip);
			properties.store(new FileOutputStream(System.getProperty("user.dir")+"/clientConfig.ini"), "test");
			System.out.println("p updated successfully !!!");
			Thread.sleep(1000);
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static String getIP() throws MessagingException {

		Folder inbox;
		String email = "vickysengupta006@gmail.com";
		String pass = "chinat0wn";
		String ip = null;

		/* Set the mail properties */
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
			/* Create the session and get the store for read the mail. */
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", email, pass);

			/* Mention the folder name which you want to read. */
			inbox = store.getFolder("Inbox");
			//System.out.println("No of Unread Messages : " + inbox.getUnreadMessageCount());

			/* Open the inbox using store. */
			inbox.open(Folder.READ_ONLY);

			/* Get the messages which is unread in the Inbox */
			/*Message messages[] = inbox.search(new FlagTerm(
					new Flags(Flag.SEEN), false));*/
			
			Message messages[] = inbox.getMessages(new int[]{inbox.getMessageCount()});
			String subject = messages[0].getSubject();
			ip = subject.substring(subject.indexOf("=") + 1, subject.length());
			System.out.println(ip + " , " +messages[0].getReceivedDate());
		return ip;
	}

	public static void main(String args[]) throws MessagingException, IOException {
		new IPReader().updateProps();
	}

}
