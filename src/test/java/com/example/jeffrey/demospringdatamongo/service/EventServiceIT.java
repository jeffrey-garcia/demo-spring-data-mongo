package com.example.jeffrey.demospringdatamongo.service;

import com.example.jeffrey.demospringdatamongo.config.DemoMongoDbConfig;
import com.example.jeffrey.demospringdatamongo.dao.EventDao;
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
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@DataMongoTest
@Import({DemoMongoDbConfig.class, EventService.class, EventDao.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "/application.properties")
public class EventServiceIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceIT.class);

    private static Random random;

    @Value("${spring.data.mongodb.uri:#{null}}")
    protected static String mongoDbConnectionString;

    @BeforeClass
    public static void setUp() throws IOException {
        random = new Random();
        EmbeddedMongoDb.replicaSetConfigurer().start(
                mongoDbConnectionString == null ? EmbeddedMongoDb.DEFAULT_CONN_STR : mongoDbConnectionString
        );
    }

    @Autowired
    EventService eventService;

    @Autowired
    EventDao eventDao;

    private final String[] collections = new String[] {
            "event-collection-0",
            "event-collection-1",
            "event-collection-2"
    };

    @Before
    public void initialize() {
        for (String collection:collections) {
            eventDao.deleteAll(collection);
        }
    }

    @Test
    public void loop() throws InterruptedException {
        final int MAX_CYCLE = 20; // sampling size
        for (int i=1; i<=MAX_CYCLE; i++) {
            System.out.println("cycle: " + i);
            parallelWrites(i);
        }
    }

    public void parallelWrites(int cycle) throws InterruptedException {
        /**
         * Suppose a single thread write of X records yields Y ms, writing the same amount
         * of records by doubling the thread count should spend half the time, until adding
         * more threads can no longer decrease the running time, the maximum tolerable write
         * capacity can be identified.
         *
         * For example, sequentially writing 100 records yields 20000 ms in single thread,
         * doubling the thread to write the same amount of records should approximately
         * decrease the running time by half (same number of disk and network I/O), where
         * adding more than 40 threads cannot further decrease the running time lesser
         * than 500 ms, we can conclude that at any given time, 40 concurrent write
         * requests can be made to the system where each one of them should not be
         * taking longer than 250 ms to finish. 
         *
         * The latency of the write operation is therefore 250 ms, while the contention
         * is limited by the network time to propagate the mutated data among all the
         * nodes in the cluster, disk I/O and size of the database connection pool.
         */
        final int MAX_THREAD = 10;
        final int MAX_WRITE = 100;
        final int WAIT_TIME_IN_MS = 200 * MAX_WRITE / MAX_THREAD;

        final CountDownLatch lock = new CountDownLatch(MAX_WRITE);
        final Executor executor = Executors.newFixedThreadPool(MAX_THREAD);

        for (int i = 0; i < MAX_WRITE; i++) {
            executor.execute(() -> {
                String collectionName = collections[random.nextInt(collections.length)];
                eventDao.createEvent(collectionName, String.format(
                        "%s-%s-%d", collectionName,
                        Thread.currentThread().getName(),
                        Thread.currentThread().getId()
                ));
                lock.countDown();
            });
        }
        Assert.assertTrue(lock.await(WAIT_TIME_IN_MS, TimeUnit.MILLISECONDS));

        int totalEvents = 0;
        for (String collection:collections) {
            totalEvents += eventDao.findAllEvents(collection).size();
        }
        Assert.assertEquals(MAX_WRITE * cycle, totalEvents);
    }

}
