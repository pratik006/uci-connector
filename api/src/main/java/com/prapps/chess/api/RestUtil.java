package com.prapps.chess.api;

import java.util.Calendar;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class RestUtil {
	
	public static void updateNatDetails(String externalHost, String id, String host, int port) {
		NatDetail detail = new NatDetail();
		detail.setHost(host);
		detail.setPort(port);
		detail.setId(id);
		detail.setLastUpdated(Calendar.getInstance().getTime().getTime());
		detail.setLastUpdatedTime(Calendar.getInstance().getTime().toString());
		
		ClientConfig cc = new DefaultClientConfig();
		cc.getClasses().add(JacksonJsonProvider.class);
		Client client = Client.create(cc);
		WebResource webResource = client.resource(externalHost+id+".json");
		ClientResponse response = webResource.type("application/json")
		   .put(ClientResponse.class, detail);

		if (response.getStatus() != 201 && response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			     + response.getStatus());
		}
		
		//System.out.println(response.getEntity(NatDetail.class));
	}
	
	
	public static NatDetail getOtherNatDetails(String id) {
		return getOtherNatDetails("https://speech-translator-44168.firebaseio.com/", id);
	}
	
	public static NatDetail getOtherNatDetails(String externalHost, String id) {
		ClientConfig cc = new DefaultClientConfig();
		cc.getClasses().add(JacksonJsonProvider.class);
		Client client = Client.create(cc);
		WebResource webResource = client
		   .resource(externalHost+id+".json");
		ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

		if (response.getStatus() != 201 && response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			     + response.getStatus());
		}
		
		return response.getEntity(NatDetail.class);
	}
}
