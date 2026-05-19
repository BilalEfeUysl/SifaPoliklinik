package com.sifa.poliklinik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SifaPoliklinikApplication {

    public static void main(String[] args) {
        SpringApplication.run(SifaPoliklinikApplication.class, args);
    }
}
