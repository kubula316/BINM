package com.BINM.listing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ListingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ListingApplication.class, args);
	}

}
