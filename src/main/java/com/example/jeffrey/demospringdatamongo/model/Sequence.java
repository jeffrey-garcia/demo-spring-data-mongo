package com.example.jeffrey.demospringdatamongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "SequenceCollection") // specify the name of the collection in MongoDB
public class Sequence {

    // The id is mostly for internal use by MongoDB
    @Id
    public String id;

    public int sequenceNumber;

    public Sequence() {
        this.sequenceNumber = 0;
    }

}
