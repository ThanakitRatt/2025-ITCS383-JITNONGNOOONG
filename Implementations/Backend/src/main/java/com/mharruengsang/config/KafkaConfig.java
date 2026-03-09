package com.mharruengsang.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {
    // Kafka configuration for async event processing
    // Topics: order-paid-topic, delivery-assigned-topic, order-delivered-topic
}
