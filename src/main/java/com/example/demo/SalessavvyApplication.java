package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@EntityScan("com.example.demo.entity")
@EnableJpaRepositories("com.example.demo.repository")
@ServletComponentScan
public class SalessavvyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalessavvyApplication.class, args);
	}
}
