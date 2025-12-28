package io.github.jiwontechinnovation.analysis.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @org.springframework.beans.factory.annotation.Value("${ANALYSIS_MONGO_URI:mongodb://host.docker.internal:27017/jiwon}")
    private String mongoUri;

    @org.springframework.beans.factory.annotation.Value("${MONGO_DB_NAME:jiwon}")
    private String databaseName;

    @Bean
    @org.springframework.context.annotation.Primary
    public MongoClient mongoClient() {
        logger.info("Initializing Manual MongoClient with URI: {}", mongoUri);
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        logger.info("Initializing Manual MongoTemplate with database: {}", databaseName);
        return new MongoTemplate(mongoClient, databaseName);
    }
}
