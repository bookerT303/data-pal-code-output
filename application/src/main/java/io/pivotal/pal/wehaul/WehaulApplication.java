package io.pivotal.pal.wehaul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WehaulApplication {

    public static void main(String[] args) {
        SpringApplication.run(WehaulApplication.class, args);
    }
}
