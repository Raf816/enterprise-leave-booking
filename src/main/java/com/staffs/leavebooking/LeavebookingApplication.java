package com.staffs.leavebooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRabbit
@EnableAsync
@EnableRetry
@EnableScheduling
@SpringBootApplication
public class LeavebookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeavebookingApplication.class, args);
    }
}
