package com.example.grpc.client.grpcclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class GrpcClientApplication /*extends SpringBootServletInitializer*/ {

	public static void main(String[] args) {
		System.out.println("Client application starting ... ");
		SpringApplication.run(GrpcClientApplication.class, args);
	}

}
