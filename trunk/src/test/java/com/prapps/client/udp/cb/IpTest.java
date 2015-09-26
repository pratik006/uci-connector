package com.prapps.client.udp.cb;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.prapps.chess.Starter;
import com.prapps.chess.uci.share.UCIUtil;

public class IpTest {

	@Test
	public void testIPTransfer() throws IOException, JAXBException {
		Starter starter = new Starter();
		starter.main(null);
		String expectedIp = UCIUtil.getExternalIP();
		Assert.assertNotNull(expectedIp);
		Properties config = new Properties();
		config.load(new FileInputStream("clientConfig.ini"));
		String externalUrl = config.getProperty("external_server_ip");
		String actualIp = UCIUtil.getExternalParam(externalUrl, "ip");
		Assert.assertEquals(expectedIp, actualIp);
	}
}
