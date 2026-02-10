package com.rubin.insurance.policy_management_service.configuration.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import com.rubin.insurance.policy_management_service.events.EventEnvelope;

@Configuration
@EnableKafka
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, EventEnvelope<?>> producerFactory() {
        return new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                new JacksonJsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, EventEnvelope<?>> consumerFactory() {
        JacksonJsonDeserializer<EventEnvelope<?>> valueDeserializer = new JacksonJsonDeserializer<>(EventEnvelope.class);
        valueDeserializer.addTrustedPackages("com.rubin.insurance.policy_management_service.events",
                "com.rubin.insurance.policy_management_service.events.payload",
                "com.rubin.insurance.policy_management_service.model");

        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> kafkaListenerContainerFactory(
            KafkaTemplate<String, EventEnvelope<?>> template) {
        ConcurrentKafkaListenerContainerFactory<String, EventEnvelope<?>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(errorHandler(template));
        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, EventEnvelope<?>> template) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(template,
                        (record, ex) -> new TopicPartition(record.topic() + ".dlq", record.partition()));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(500L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(5000L);


        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean
    public NewTopic policyEventsTopic() {
        return TopicBuilder.name("policy.events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic claimEventsTopic() {
        return TopicBuilder.name("claim.events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic policyEventsDlqTopic() {
        return TopicBuilder.name("policy.events.dlq").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic claimEventsDlqTopic() {
        return TopicBuilder.name("claim.events.dlq").partitions(1).replicas(1).build();
    }
}
