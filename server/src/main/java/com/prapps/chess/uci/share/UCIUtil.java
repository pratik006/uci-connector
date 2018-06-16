package com.prapps.chess.uci.share;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.prapps.chess.server.uci.tcp.AdminServer;

public class UCIUtil {

	public static final int DEFAULT_BUFFER_SIZE = 1024 * 1000;
	private static Logger LOG = Logger.getLogger(UCIUtil.class.getName());
	public static String CHARSET = "UTF-8";
	
	/*private static byte[] IV = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};*/

	public static String readStream(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int readLen = buffer.length;
		while (readLen == buffer.length && (readLen = is.read(buffer, 0, buffer.length)) != 0) {
			sb.append(new String(buffer, 0, readLen));
		}
		return sb.toString();
	}

	public static void writeString(String line, OutputStream os) throws IOException {
		os.write(line.getBytes());
		os.flush();
	}

	public static String calculateMD5Hash(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] mdbytes = md.digest();

		// convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

	public static boolean authenticate(String command, String userName, char[] password) {
		try {
			String arr[] = command.split("\\|");
			if (arr[1].trim().equals("deep") && UCIUtil.calculateMD5Hash(arr[2].trim()).equals("d41d8cd98f00b204e9800998ecf8427e")) {
				userName = arr[1];
				return true;
			} else {
				return false;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void streamPiper(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int readLen = 0;
		while ((readLen = is.read(buffer, 0, buffer.length)) != -1) {
			os.write(buffer, 0, readLen);
			os.flush();
		}
	}

	public static String readPacket(DatagramPacket packet, byte[] password) throws IOException {
		return new String(readBytePacket(packet, password), CHARSET);
	}
	
	public static byte[] readBytePacket(DatagramPacket packet, byte[] password) throws IOException {
		try {
			return decrypt(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()),password);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage().getBytes(CHARSET);
		}
	}
	
	public static boolean sendPacket(byte[] message, byte[] password, DatagramSocket datagramSocket, InetAddress address, int port) {
		LOG.finest(address + ":" + port);
		byte[] msg;
		try {
			msg = encrypt(message, password);
			DatagramPacket newPacket = new DatagramPacket(msg, msg.length, address, port);
			datagramSocket.send(newPacket);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String[] getMessageParams(String message) {
		return message.split(ProtocolConstants.DELIMITER);
	}

	public static byte[] encrypt(byte[] plainText, byte[] password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		/*Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
		SecretKeySpec key = new SecretKeySpec(password, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
		return cipher.doFinal(plainText);*/
		return plainText;
	}

	public static byte[] decrypt(byte[] cipherText, byte[] encryptionKey) throws Exception {
		/*Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
		SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
		return cipher.doFinal(cipherText);*/
		return cipherText;
	}
	
	
	public static String IPADDRESS_PATTERN =  "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	
	public static void main(String[] arg) {
		/*try {
			mailExternalIP(getExternalIP(), "vickysengupta006@gmail.com", "chinat0wn", "");
			System.out.println(getExternalIP());
	    } 
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }*/
		System.out.println(getLocalIP());
	}
	
	public static void mailExternalIP(String ip, final String fromMailAddress, final String password, final String toMailAddress) {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromMailAddress, password);
			}
		  });
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromMailAddress));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(toMailAddress));
			message.setSubject("External_IP="+ip);
			message.setText("External_IP="+ip+":time="+new Date().getTime()); 
			Transport.send(message); 
			System.out.println("Mail sent to "+toMailAddress);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/*public static void mailExternalIP(String mailAddress) throws IOException {
		mailExternalIP(getExternalIP(), mailAddress);
	}*/
	
	public static String getExternalIP() throws IOException {
		//URL whatismyip = new URL("http://agentgatech.appspot.com/");
		URL whatismyip = new URL(AdminServer.externalServerUrl+"?action=MY_IP");
		URLConnection connection = whatismyip.openConnection();
		connection.addRequestProperty("Protocol", "Http/1.1");
	    connection.addRequestProperty("Connection", "keep-alive");
	    connection.addRequestProperty("Keep-Alive", "1000");
	    connection.addRequestProperty("User-Agent", "Web-Agent");
	    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    String ip = in.readLine(); //you get the IP as a String
	    return ip;
	}
	
	public static String getExternalParam(String externalUrl, String param) throws IOException {
		//URL whatismyip = new URL("http://agentgatech.appspot.com/");
		URL whatismyip = new URL(externalUrl+"?action=get&param="+param);
		URLConnection connection = whatismyip.openConnection();
		connection.addRequestProperty("Protocol", "Http/1.1");
	    connection.addRequestProperty("Connection", "keep-alive");
	    connection.addRequestProperty("Keep-Alive", "1000");
	    connection.addRequestProperty("User-Agent", "Web-Agent");
	    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    String ip = in.readLine(); //you get the IP as a String
	    return ip;
	}
	
	public static String getIPFromMail(final String email, final String pass) throws MessagingException {

		Folder inbox;
		/*String email = "vickysengupta006@gmail.com";
		String pass = "chinat0wn";*/

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
			
		return messages[0].getSubject();
	}
	
	public static String getParam(String params[], int index) {
		return params[index].substring(params[index].indexOf("=") + 1);
	}
	
	public static byte[] createSalt() {
		SecureRandom sr = new SecureRandom();
		byte[] rndBytes = new byte[8];
		sr.nextBytes(rndBytes);
		return rndBytes;
	}
	
	public static int getAdminPort() {
		String suffix;
		int temp;
		try {
			suffix = InetAddress.getLocalHost().getHostAddress().toString();
			suffix = suffix.substring(suffix.lastIndexOf(".")+1);
			temp = Integer.parseInt(suffix);
			if(temp < 10) {
				suffix = "110"+suffix+"0";
			}
			else if(temp < 100) {
				suffix = "11"+suffix+"0";
			}
			else {
				suffix = "11"+suffix;
			}
			LOG.info("adminPort = "+suffix);
			return Integer.parseInt(suffix);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	//final static String publicUrl = "http://apps-pratiks.rhcloud.com/json/cs/ip";
	public static void updateExternalIP(String ip, int port) {
		final String USER_AGENT = "Mozilla/5.0";

		String localIP = UCIUtil.getLocalIP();
		try {
			URL url = new URL(AdminServer.externalServerUrl+"?action=set&ip="+ip+"&port="+port+"&localip="+localIP);
			HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			int responseCode = conn.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	 
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			//print result
			System.out.println(response.toString());
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getLocalIP() {
		Enumeration<NetworkInterface> interfaces;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()){
			    NetworkInterface current = interfaces.nextElement();
			    if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
			    Enumeration<InetAddress> addresses = current.getInetAddresses();
			    while (addresses.hasMoreElements()){
			        InetAddress current_addr = addresses.nextElement();
			        if (current_addr.isLoopbackAddress()) continue;
			        if(current_addr instanceof Inet4Address) {
			        	return current_addr.getHostAddress();
			        }
			    }
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	public static String getServerIP(String externalUrl) throws IOException {
		return getExternalParam(externalUrl, "ip");
	}

}
