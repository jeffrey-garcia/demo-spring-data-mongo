package com.example.jeffrey.demospringdatamongo.config;

import com.example.jeffrey.demospringdatamongo.model.Account;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DemoMongoDbConfig extends AbstractMongoClientConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoMongoDbConfig.class);

    @Value("${spring.data.mongodb.uri:#{null}}")
    protected String mongoDbConnectionString;

    @Bean
    @Qualifier("mongoDbFactory")
    @Override
    public SimpleMongoClientDbFactory mongoDbFactory() {
        Assert.assertNotNull(mongoDbConnectionString);
        ConnectionString connectionString = new ConnectionString(mongoDbConnectionString);
        return new SimpleMongoClientDbFactory(connectionString);
    }

    @Bean
    @Qualifier("mongoTransactionManager")
    MongoTransactionManager transactionManager(
            @Autowired
            @Qualifier("mongoDbFactory")
            MongoDbFactory dbFactory
    ) {
        // MongoDB Rollback does not work with @Transactional and MongoTransactionManager.
        MongoTransactionManager transactionManager = new MongoTransactionManager(dbFactory);
        transactionManager.setRollbackOnCommitFailure(true);
        transactionManager.setOptions(
                TransactionOptions.builder()
                        .readPreference(ReadPreference.primary())
                        .readConcern(ReadConcern.LOCAL)
                        .writeConcern(WriteConcern.MAJORITY)
                .build());
        return transactionManager;
    }

    @Override
    protected String getDatabaseName() {
        String dbName = mongoDbFactory().getDb().getName();
        return dbName;
    }

    @Override
    public MongoClient mongoClient() {
        Assert.assertNotNull(mongoDbConnectionString);
        ConnectionString connectionString = new ConnectionString(mongoDbConnectionString);
        MongoClient mongoClient = MongoClients.create(connectionString);
        return mongoClient;
    }

    @Override
    public boolean autoIndexCreation() {
        // Automatic index creation will be turned OFF by default with the release of 3.x.
        // Let index creation to happen either out of band or as part of the application
        // startup using IndexOperations.
        return true;
    }

//	@EventListener(ApplicationReadyEvent.class)
//	public void initIndicesAfterStartup(
//	        @Autowired MongoMappingContext mongoMappingContext,
//            @Autowired MongoTemplate mongoTemplate)
//    {
//        // Although index creation via annotations comes in handy for many scenarios
//        // consider taking over more control by setting up indices manually via IndexOperations.
//        IndexOperations indexOps = mongoTemplate.indexOps(Account.class);
//        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
//        resolver.resolveIndexFor(Account.class).forEach(indexOps::ensureIndex);
//	}

}
