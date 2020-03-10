package com.example.jeffrey.demospringdatamongo.service;

import com.example.jeffrey.demospringdatamongo.model.Customer;
import com.example.jeffrey.demospringdatamongo.repository.CustomerRepository;
import com.example.jeffrey.demospringdatamongo.util.EmbeddedMongoDb;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@DataMongoTest
@Import({CustomerService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "/application.properties")
public class CustomerServiceIT {

    @Value("${spring.data.mongodb.uri:#{null}}")
    protected static String mongoDbConnectionString;

    @BeforeClass
    public static void setUp() throws IOException {
        EmbeddedMongoDb.replicaSetConfigurer().start(
                mongoDbConnectionString == null ? EmbeddedMongoDb.DEFAULT_CONN_STR : mongoDbConnectionString
        );
    }

    @AfterClass
    public static void cleanUp() {
        EmbeddedMongoDb.replicaSetConfigurer().finish();
    }

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerRepository customerRepository;

    @Before
    public void initialize() {
        customerRepository.deleteAll();
    }

    @Test
    public void saveCustomer() {
        customerService.save(new Customer("steve","rogers"));
        List<Customer> customers = customerService.findByFirstName("steve");
        Assert.assertTrue( customers.size()>=1);
        Assert.assertEquals("steve", customers.get(0).firstName);
        Assert.assertEquals("rogers", customers.get(0).lastName);
    }

}
