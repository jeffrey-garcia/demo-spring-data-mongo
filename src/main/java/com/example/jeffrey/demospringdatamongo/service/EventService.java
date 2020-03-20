package com.example.jeffrey.demospringdatamongo.service;

import com.example.jeffrey.demospringdatamongo.dao.EventDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    @Autowired
    @Qualifier("EventDao")
    EventDao eventDao;

    public void create(String collectionName, String payload) {
        eventDao.createEvent(collectionName, payload);
    }

}
