package com.techsemina.msa.pointservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class PointServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(PointServiceApplication.class, args);
    }

}
