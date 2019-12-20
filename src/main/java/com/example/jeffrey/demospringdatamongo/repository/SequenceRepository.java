package com.example.jeffrey.demospringdatamongo.repository;

import com.example.jeffrey.demospringdatamongo.model.Sequence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SequenceRepository extends MongoRepository<Sequence, String> {

}
