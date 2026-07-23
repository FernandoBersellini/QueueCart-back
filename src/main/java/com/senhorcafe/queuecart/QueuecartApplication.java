package com.senhorcafe.queuecart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = { RabbitAutoConfiguration.class })
public class QueuecartApplication {
    public static void main(String[] args) {
        SpringApplication.run(QueuecartApplication.class, args);
    }
}
