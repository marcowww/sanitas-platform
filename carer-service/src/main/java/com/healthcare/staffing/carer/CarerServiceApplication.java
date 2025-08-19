package com.healthcare.staffing.carer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class CarerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarerServiceApplication.class, args);
    }
}
