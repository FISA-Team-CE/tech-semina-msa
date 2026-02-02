package com.techsemina.msa.pointservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class PointServiceApplication {

    /**
     * Application entry point that launches the Point Service Spring Boot application.
     */
    public static void main(String[] args) {

        SpringApplication.run(PointServiceApplication.class, args);
    }

}