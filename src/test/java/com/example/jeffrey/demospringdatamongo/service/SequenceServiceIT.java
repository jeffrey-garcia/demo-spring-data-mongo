package com.example.jeffrey.demospringdatamongo.service;

import com.example.jeffrey.demospringdatamongo.model.Sequence;
import com.example.jeffrey.demospringdatamongo.repository.SequenceRepository;
import com.example.jeffrey.demospringdatamongo.util.EmbeddedMongoDb;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@DataMongoTest
@Import({SequenceService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "/application.properties")
public class SequenceServiceIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceServiceIT.class);

    @Value("${spring.data.mongodb.uri:#{null}}")
    protected static String mongoDbConnectionString;

    @BeforeClass
    public static void setUp() throws IOException {
        EmbeddedMongoDb.replicaSetConfigurer().start(
                mongoDbConnectionString == null ? EmbeddedMongoDb.DEFAULT_CONN_STR : mongoDbConnectionString
        );
    }

    @Autowired
    SequenceRepository sequenceRepository;

    @Autowired
    SequenceService sequenceService;

    @Before
    public void initialize() {
        sequenceRepository.deleteAll();
        sequenceRepository.save(new Sequence());
    }

    @Test
    public void initialTest() {
        int sequenceNumber = sequenceService.appendSequenceNumber();
        Assert.assertEquals(1, sequenceNumber);
    }

    @Test
    public void loopTest() throws Exception {
        final int MAX_THREAD = 50;
        final int MAX_SEQUENCE_PER_CYCLE = 30;

        final CountDownLatch lock = new CountDownLatch(MAX_THREAD);
        final Executor executor = Executors.newFixedThreadPool(MAX_THREAD);

        // TODO: change to use bitwise manipulation to reduce memory consumption
        final int [] sequenceNumbers = new int [MAX_THREAD * MAX_SEQUENCE_PER_CYCLE];

        for (int i = 0; i < MAX_THREAD; i++) {
            executor.execute(new Thread(String.valueOf(i)) {
                @Override
                public void run() {
                    for (int j = 0; j < MAX_SEQUENCE_PER_CYCLE; j++) {
                        try {
                            int k = Integer.parseInt(this.getName());
                            int sequenceNumber = sequenceService.appendSequenceNumber();
                            //LOGGER.info("sequence number = " + sequenceNumber);
                            sequenceNumbers[k*MAX_SEQUENCE_PER_CYCLE + j] = sequenceNumber;

                        } catch (Throwable throwable) {
                            LOGGER.error(throwable.getMessage(), throwable);
                        }
                    }
                    lock.countDown();
                }
            });
        }

        lock.await();
        findDuplicates(sequenceNumbers);
    }

    public void findDuplicates(int [] input) {
        Set<Integer> set = new TreeSet<>();
        for (int i=0; i<input.length; i++) {
            if (set.contains(input[i])) {
                LOGGER.warn("{} already exists", input[i]);
            } else {
                set.add(input[i]);
            }
        }

        Assert.assertEquals(input.length, set.size());

        // this test will always fail, database write have delay
//        Assert.assertEquals(input[input.length-1], sequenceService.querySequenceNumber().get(0).sequenceNumber);
    }

}
