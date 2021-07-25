package com.ipfsops.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class IPFSOpsDAO {
	String result = "";
	InputStream inputStream;

	public String getPropValues(String property) throws IOException  {
		

			
			Properties prop = readPropertiesFile("ipfsops.properties");

			String dburl = prop.getProperty("dburl");
			String jdbcdrver = prop.getProperty("jdbcdrver");
			String user = prop.getProperty("user");
			String password = prop.getProperty("password");
			String ipfsURL = prop.getProperty("ipfsURL");
			//System.out.println("dburl:" + dburl);
			//System.out.println("jdbcdrver:" + jdbcdrver);
			//System.out.println("user:" + user);
			//System.out.println("password:" + password);
			if (property.equals("dburl")) {
				return dburl;
			} else if (property.equals("jdbcdrver")) {
				return jdbcdrver;
			} else if (property.equals("user")) {
				return user;
			} else if (property.equals("password")) {
				return password;
			}else if (property.equals("ipfsURL")) {
				return ipfsURL;
			}
		
		return null;
	}

	public static Properties readPropertiesFile(String filename) throws IOException {
		FileInputStream fileInputStream=null;
		Properties prop=null ;
		try {
			fileInputStream=new FileInputStream(filename);
			prop= new Properties();
			prop.load(fileInputStream);
			
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			fileInputStream.close();
		}
		return prop;
	}
}
