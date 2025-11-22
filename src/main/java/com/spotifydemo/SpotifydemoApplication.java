package com.spotifydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SpotifydemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotifydemoApplication.class, args);
    }

}
