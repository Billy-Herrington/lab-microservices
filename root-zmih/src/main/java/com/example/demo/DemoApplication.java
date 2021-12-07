package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@RestController
public class DemoApplication {
    private static final String DEF_URL = "http://%s-service/%s";
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/root-service/{serviceName}/{size}")
    public String ddosSerive(@PathVariable String serviceName, @PathVariable int size) throws InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        for (int i = 0; i < size; i++) {
            executorService.submit(() -> {
                long start = System.currentTimeMillis();
                try {
                    final String format = String.format(DEF_URL, serviceName, serviceName);
                    System.out.println("request started to " + format);
                    final ResponseEntity<String> forEntity = restTemplate.getForEntity(format, String.class);
                    long end = System.currentTimeMillis();
                    System.out.println(" Request to {" + serviceName + "} ended up in " + (end - start) + " mills, RESP: " + forEntity);
                } catch (HttpStatusCodeException ex) {
                    long end = System.currentTimeMillis();
                    System.out.println(" Request to {" + serviceName + "} failed up in " + (end - start) + " mills, Response CODE: " + ex.getStatusCode() );
                }
                    }
            );
        }
        return "Started !";
    }

}
