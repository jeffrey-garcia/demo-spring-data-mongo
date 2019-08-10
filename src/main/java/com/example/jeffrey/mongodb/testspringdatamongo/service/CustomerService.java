package com.example.jeffrey.mongodb.testspringdatamongo.service;

import com.example.jeffrey.mongodb.testspringdatamongo.model.Customer;
import com.example.jeffrey.mongodb.testspringdatamongo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    public void save(Customer customer) {
        customerRepository.save(customer);
    }

    public List<Customer> findByFirstName(String firstName) {
        return customerRepository.findByFirstName(firstName);
    }

    public List<Customer> findByLastName(String lastName) {
        return customerRepository.findByFirstName(lastName);
    }

}
