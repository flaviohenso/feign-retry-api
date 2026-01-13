package com.example.feignretryapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FeignRetryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignRetryApiApplication.class, args);
    }
}
