package com.example.jeffrey.demospringdatamongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "CustomerCollection") // specify the name of the collection in MongoDB
public class Customer {

    // The id is mostly for internal use by MongoDB
    @Id
    public String id;

    public String firstName;
    public String lastName;

    public Customer() {}

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

//    @Override
//    public String toString() {
//        return String.format(
//                "Customer[id=%s, firstName='%s', lastName='%s']",
//                id, firstName, lastName);
//    }
}
