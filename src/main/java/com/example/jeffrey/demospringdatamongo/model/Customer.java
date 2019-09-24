package com.example.jeffrey.demospringdatamongo.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Document(collection = "CustomerCollection") // specify the name of the collection in MongoDB
public class Customer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Customer.class);

    // The id is mostly for internal use by MongoDB
    @Id
    public String id;

    public String firstName;
    public String lastName;

    public LocalDateTime createdOn;

    public Customer() {}

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;

        // simulate WRONG date/time conversion when writing to DB, resulting in loss of date/time precision
        //this.createdOn = generateDateTimeStamp_WRONG();

        // simulate CORRECT date/time conversion when writing to DB, robust against loss of date/time precision
        this.createdOn = generateDateTimeStamp_CORRECT();
    }

    private LocalDateTime generateDateTimeStamp_WRONG() {
        LocalDateTime localDateTime_Tha = LocalDateTime.ofInstant(
                Instant.now(),
                ZonedDateTime.now(ZoneId.of("Asia/Bangkok")).getOffset()
        );
        ZonedDateTime zonedDateTime_Tha = ZonedDateTime.of(localDateTime_Tha, ZoneId.of("Asia/Bangkok"));

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX:00");
        String rfc3339 = dateTimeFormatter.format(zonedDateTime_Tha);
        LOGGER.debug("created on: {}", rfc3339);

        // LocalDateTime will be using the date/time value in Thailand's timezone, but the timezone information
        // is no longer preserved, so later on when MongoDB driver proceed conversion, it will be converted as
        // local system date/time, thus causing precision loss.
        //
        // For example:
        // 2019-01-01T07:00:00+07:00 will be converted to 2018-12:31 23:00:00 UTC
        LocalDateTime localDateTime = LocalDateTime.ofInstant(
                ZonedDateTime.parse(rfc3339, dateTimeFormatter).toInstant(),
                ZoneId.of("Asia/Bangkok")
        );
        return localDateTime;
    }

    private LocalDateTime generateDateTimeStamp_CORRECT() {
        LocalDateTime localDateTime_Tha = LocalDateTime.ofInstant(
                Instant.now(),
                ZonedDateTime.now(ZoneId.of("Asia/Bangkok")).getOffset()
        );
        ZonedDateTime zonedDateTime_Tha = ZonedDateTime.of(localDateTime_Tha, ZoneId.of("Asia/Bangkok"));

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX:00");
        String rfc3339 = dateTimeFormatter.format(zonedDateTime_Tha);
        LOGGER.debug("created on: {}", rfc3339);

        // LocalDateTime will be using the date/time value in local system timezone, so later on
        // when MongoDB driver proceed conversion using local system timezone there will NOT be
        // any precision loss
        LocalDateTime localDateTime = LocalDateTime.ofInstant(
                ZonedDateTime.parse(rfc3339, dateTimeFormatter).toInstant(),
                ZoneId.systemDefault()
        );
        return localDateTime;
    }
    
    @Override
    public String toString() {
        return String.format(
                "Customer[id=%s, firstName='%s', lastName='%s']",
                id, firstName, lastName);
    }
}
