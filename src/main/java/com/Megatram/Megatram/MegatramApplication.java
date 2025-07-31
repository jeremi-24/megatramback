package com.Megatram.Megatram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class MegatramApplication {

    public static void main(String[] args) {
        // Forcer la timezone JVM à UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SpringApplication.run(MegatramApplication.class, args);
    }

}
