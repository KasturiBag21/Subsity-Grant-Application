package org.example.subsidyplatformtest;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "org.example.subsidyplatformtest",
        "com.platform"
})
@EntityScan(basePackages = {
        "com.platform"
})
@EnableJpaRepositories(basePackages = {
        "com.platform"
})
public class SubsidyPlatformTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                SubsidyPlatformTestApplication.class,
                args
        );
    }
}