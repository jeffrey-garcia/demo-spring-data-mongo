package com.example.jeffrey.mongodb.testspringdatamongo.repository;

import com.example.jeffrey.mongodb.testspringdatamongo.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CustomerRepository extends MongoRepository<Customer, String> {

    public List<Customer> findByFirstName(String firstName);
    public List<Customer> findByLastName(String lastName);

}
