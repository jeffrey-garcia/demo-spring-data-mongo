package com.example.jeffrey.demospringdatamongo.repository;

import com.example.jeffrey.demospringdatamongo.model.Sequence;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SequenceRepository extends MongoRepository<Sequence, String> {

}
