package com.fisa.core_payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableJpaAuditing
@SpringBootApplication
public class CorePaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CorePaymentServiceApplication.class, args);
	}

}
