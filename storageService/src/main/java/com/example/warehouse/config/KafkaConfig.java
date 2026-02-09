package com.example.warehouse.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }



    @Bean
    public NewTopic storageRequestsTopic() {
        return new NewTopic("storage-requests", 3, (short) 2)
                .configs(Map.of(
                        "retention.ms", "604800000"
                ));
    }

    @Bean
    public NewTopic storageRequestsResponsesTopic() {
        return new NewTopic("storage-requests-responses", 3, (short) 2)
                .configs(Map.of(
                        "retention.ms", "86400000"
                ));
    }
}
