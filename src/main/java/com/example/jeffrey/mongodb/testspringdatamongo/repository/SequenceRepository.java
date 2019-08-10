package com.example.jeffrey.mongodb.testspringdatamongo.repository;

import com.example.jeffrey.mongodb.testspringdatamongo.model.Sequence;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SequenceRepository extends MongoRepository<Sequence, String> {

}
