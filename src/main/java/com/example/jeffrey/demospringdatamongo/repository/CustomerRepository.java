package com.example.jeffrey.demospringdatamongo.repository;

import com.example.jeffrey.demospringdatamongo.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {

    public List<Customer> findByFirstName(String firstName);
    public List<Customer> findByLastName(String lastName);

}
