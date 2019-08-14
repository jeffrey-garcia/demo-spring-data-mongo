package com.example.jeffrey.mongodb.testspringdatamongo.controller;

import com.example.jeffrey.mongodb.testspringdatamongo.model.Customer;
import com.example.jeffrey.mongodb.testspringdatamongo.model.Sequence;
import com.example.jeffrey.mongodb.testspringdatamongo.service.CustomerService;
import com.example.jeffrey.mongodb.testspringdatamongo.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MainController {

    @Autowired
    CustomerService customerService;

    @Autowired
    SequenceService sequenceService;

    @RequestMapping("/")
    public @ResponseBody
    String test() {
        return "test okay";
    }

    @GetMapping(path="/customer/add")
    public @ResponseBody Customer add(@RequestParam String firstName, @RequestParam String lastName) {
        Customer customer = new Customer(firstName, lastName);
        customerService.save(customer); // save to DB
        return customer;
    }

    @GetMapping(path="/customer/query")
    public @ResponseBody Iterable<Customer> queryByFirstName(@RequestParam(required = false) String firstName, @RequestParam(required = false) String lastName) {
        if (lastName == null) {
            // query by first name only
            return customerService.findByFirstName(firstName); // query from DB
        }
        if (firstName == null) {
            // query by last name only
            return customerService.findByLastName(lastName); // query from DB
        }
        return null;
    }

    @GetMapping(path="/sequence/append")
    public @ResponseBody int appendSequenceNumber() {
        return sequenceService.appendSequenceNumber();
    }

    @GetMapping(path="/sequence/query")
    public @ResponseBody List<Sequence> querySequenceNumber() {
        return sequenceService.querySequenceNumber();
    }

}
