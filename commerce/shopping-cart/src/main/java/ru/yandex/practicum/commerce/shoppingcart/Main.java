package ru.yandex.practicum.commerce.shoppingcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.yandex.practicum.commerce.interactionapi.client")
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}