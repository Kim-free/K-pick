package com.example.kpick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KpickApplication {

    public static void main(String[] args) {
        SpringApplication.run(KpickApplication.class, args);
    }

}
