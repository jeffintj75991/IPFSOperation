package com.ipfsops.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan(basePackages="com")
@SpringBootApplication
public class IPFSOpsApplication {

	public static void main(String[] args) {
		SpringApplication.run(IPFSOpsApplication.class, args);
	}

}
