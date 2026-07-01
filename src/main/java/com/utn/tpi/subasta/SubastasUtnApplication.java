package com.utn.tpi.subasta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubastasUtnApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubastasUtnApplication.class, args);
    }
}
