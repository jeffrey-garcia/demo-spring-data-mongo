package com.example.jeffrey.demospringdatamongo.dao;

import com.example.jeffrey.demospringdatamongo.config.DemoMongoDbConfig;
import com.example.jeffrey.demospringdatamongo.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("EventDao")
@EnableMongoRepositories
public class EventDao {

    @Autowired
    DemoMongoDbConfig mongoDbConfig;

    @Autowired
    MongoTemplate mongoTemplate;

    public void deleteAll(String collectionName) {
        mongoTemplate.dropCollection(collectionName);
    }

    public void createEvent(String collectionName, String payload) {
        Event event = new Event(payload);
        mongoTemplate.save(event, collectionName);
    }

    public List<Event> findAllEvents(String collectionName) {
        return mongoTemplate.findAll(Event.class, collectionName);
    }

}
