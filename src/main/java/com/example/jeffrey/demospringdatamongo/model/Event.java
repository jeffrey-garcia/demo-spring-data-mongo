package com.example.jeffrey.demospringdatamongo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.ZonedDateTime;

@Document
public class Event {

    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("createdOn")
    private Instant createdOn;

    @JsonProperty("payload")
    private String payload;

    public Event(String payload) {
        this.createdOn = ZonedDateTime.now().toInstant();
        this.payload = payload;
    }

}
