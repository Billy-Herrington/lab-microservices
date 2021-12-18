package com.example.demo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@RestController
@EnableKafka
public class DemoApplication {
    static class Entity {
        private int id;
        private String message;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "id=" + id +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
    private JdbcTemplate jdbcTemplate;
    {
        final DataSource build = DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://postgres:5432/demo")
                .username("demo")
                .password("demo")
                .build();
        jdbcTemplate = new JdbcTemplate(build);
    }

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

    @KafkaListener(topics = "test-kafka-topic")
    public void listner(String message) {
        System.out.println("WRITING KAFKA MESSAGE: "+ message);
        jdbcTemplate.execute("INSERT INTO messages(\"message\") VALUES (\'"+ message +"\') ");
    }

    @GetMapping("/service1/db")
    public String listDataBase() {
        final List<Entity> query = jdbcTemplate.query("select * from \"messages\"",new BeanPropertyRowMapper<>(Entity.class));
        return query.toString();
    }

    @GetMapping("/service1")
    public String helloWorld(){
        return "Hello, World from service 1!" + (int)(Math.random()*500);
    }

    public static void main(String[] args) {
        System.out.println("TEST!!!");
        SpringApplication.run(DemoApplication.class, args);
    }

}
