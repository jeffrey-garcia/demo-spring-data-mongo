package com.example.jeffrey.demospringdatamongo.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "AccountCollection") // specify the name of the collection in MongoDB
public class Account {
    private static final Logger LOGGER = LoggerFactory.getLogger(Account.class);

    // The id is mostly for internal use by MongoDB
    @Id
    public String id;

    public String accountNumber;

    public Long accountBalance;

    public Long ledgerBalance;

    public Account() {
        this.accountNumber = UUID.randomUUID().toString();
        this.accountBalance = 100L;
        this.ledgerBalance = accountBalance;
    }

}
