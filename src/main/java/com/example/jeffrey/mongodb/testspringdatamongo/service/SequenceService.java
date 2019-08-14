package com.example.jeffrey.mongodb.testspringdatamongo.service;

import com.example.jeffrey.mongodb.testspringdatamongo.model.Sequence;
import com.example.jeffrey.mongodb.testspringdatamongo.repository.SequenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SequenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceService.class);

    @Autowired
    SequenceRepository sequenceRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    public int appendSequenceNumber() {
        Query query = new Query();
        query.addCriteria(Criteria.where("sequenceNumber").gte(0));
        Update update = new Update();
        update.inc("sequenceNumber", 1);
        Sequence sequence = mongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true),
                Sequence.class);
        return sequence.sequenceNumber;
    }

    public List<Sequence> querySequenceNumber() {
        return sequenceRepository.findAll();
    }

}
