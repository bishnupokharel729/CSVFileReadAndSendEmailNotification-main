package com.example.csvprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CsvFileReadAndSendEmailApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsvFileReadAndSendEmailApplication.class, args);
    }
}
