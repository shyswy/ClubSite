package com.example.clubsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing //BaseEntity 사용하기 위함
public class ClubSiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClubSiteApplication.class, args);
    }

}
