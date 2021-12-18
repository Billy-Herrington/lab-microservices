package com.example.demo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
@RestController
@EnableKafka
public class DemoApplication {
    private static boolean isBroken = false;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "kafka:9092");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "test");
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "test");
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @KafkaListener(topics = "test2")
    public void listner(String message) {
        System.out.println("KAFKA MESSAGE: "+ message);
    }


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
