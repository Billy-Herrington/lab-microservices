package com.example.demo;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@RestController
public class DemoApplication {


    private static final String DEF_URL = "http://%s-service/%s";
    private static final String DEF_TOPIC = "test-kafka-topic";
    private KafkaProducer<String, String> kafkaProducer;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public DemoApplication() {
        kafkaProducer();
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    void kafkaProducer() {
        String bootstrapServers = "kafka:9092";
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("client.id", "test");
        properties.put("group.id", "test");
        kafkaProducer = new KafkaProducer<>(properties, new StringSerializer(), new StringSerializer());
    }

    @GetMapping("/root-service/kafkaSend/{topic}/{message}")
    public String kafkaSend(@PathVariable String topic, @PathVariable String message) {
        kafkaProducer.send(new ProducerRecord<>(topic, message));
        return "Sent";
    }

    @GetMapping("/root-service/sendHttp/{serviceName}/{size}")
    public String ddosSerive(@PathVariable String serviceName, @PathVariable int size) {
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
