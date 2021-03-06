package com.example.jeffrey.demospringdatamongo.controller;

import com.example.jeffrey.demospringdatamongo.model.Customer;
import com.example.jeffrey.demospringdatamongo.model.Event;
import com.example.jeffrey.demospringdatamongo.service.CustomerService;
import com.example.jeffrey.demospringdatamongo.service.EventService;
import com.example.jeffrey.demospringdatamongo.service.SequenceService;
import com.example.jeffrey.demospringdatamongo.model.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MainController {

    @Autowired
    CustomerService customerService;

    @Autowired
    SequenceService sequenceService;

    @Autowired
    EventService eventService;

    @RequestMapping("/")
    public @ResponseBody
    String test() {
        return "test okay";
    }

    @GetMapping(path="/customer/add")
    public @ResponseBody
    Customer add(@RequestParam String firstName, @RequestParam String lastName) {
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

    @GetMapping(path="/event/add")
    public @ResponseBody ResponseEntity addEvent(@RequestParam String collectionName, @RequestParam String payload) {
        eventService.create(collectionName, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(HttpStatus.CREATED.toString());
    }
}
