package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {
    private static boolean isBroken = false;

    @GetMapping("/service2")
    public String helloWorld() throws InterruptedException {
        if (isBroken) {
            Thread.sleep(10_000L);
        }
        return "Hello, sWorld from service 2!" + (int)(Math.random()*500);
    }

    @GetMapping("/service2/broken")
    public String broken() {
        isBroken = !isBroken;
        return "Service was broken!";
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
