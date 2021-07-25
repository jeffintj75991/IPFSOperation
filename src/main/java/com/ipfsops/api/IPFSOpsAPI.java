package com.ipfsops.api;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ipfsops.controller.IPFSOpsController;



@RestController
public class IPFSOpsAPI {
	
	@Autowired
	private IPFSOpsController ipfsopsController;
	
	
	
	@Async
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/writeIPFS", method = RequestMethod.POST)
	public @ResponseBody String writeIPFS(@RequestBody String payload) throws IOException, ClassNotFoundException, SQLException,RuntimeException {
	
	return ipfsopsController.writeIPFSControl(payload);		
	}
	
	@Async
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/readIPFS", method = RequestMethod.POST)
	public @ResponseBody String readIPFS(@RequestBody String payload) throws ClassNotFoundException, IOException, SQLException {
	
	return ipfsopsController.readIPFSControl(payload);		
	}
	
	@Async
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/listIPFS", method = RequestMethod.POST)
	public @ResponseBody String listIPFS() throws ClassNotFoundException, IOException, SQLException {
	
	return ipfsopsController.listIPFSControl();		
	}

}
