package com.superinka.formex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FormexApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormexApplication.class, args);
    }


}
