package com.authora.authorization.server;

import com.authora.authorization.server.config.properties.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AuthorizationServerApplication {

	 static void main(String[] args) {
		SpringApplication.run(AuthorizationServerApplication.class, args);
	}

}
