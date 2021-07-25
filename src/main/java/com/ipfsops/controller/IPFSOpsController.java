package com.ipfsops.controller;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ipfsops.processor.IPFSOpsProcessor;

@Component

public class IPFSOpsController {
	
	@Autowired
	private IPFSOpsProcessor iPFSOpsProcessor;
	
	public String writeIPFSControl(String payload) throws IOException, ClassNotFoundException, SQLException,RuntimeException {
		
		return iPFSOpsProcessor.writeIPFS(payload);
	
	}
	
	public String readIPFSControl(String payload) throws ClassNotFoundException, IOException, SQLException {
		return iPFSOpsProcessor.readIPFS(payload);
	}
	
	public String listIPFSControl() throws ClassNotFoundException, IOException, SQLException {
		return iPFSOpsProcessor.listIPFS();
		
	}

}
