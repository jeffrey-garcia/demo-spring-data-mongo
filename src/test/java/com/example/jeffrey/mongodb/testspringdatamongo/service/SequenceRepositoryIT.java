package com.example.jeffrey.mongodb.testspringdatamongo.service;

import com.example.jeffrey.mongodb.testspringdatamongo.model.Sequence;
import com.example.jeffrey.mongodb.testspringdatamongo.repository.SequenceRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
public class SequenceRepositoryIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceRepositoryIT.class);

    @Autowired
    SequenceRepository sequenceRepository;

    @Autowired
    SequenceService sequenceService;

    @Before
    public void setUp() {
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
        final int MAX_THREAD = 10;
        final int MAX_SEQUENCE_PER_CYCLE = 10000;

        final CountDownLatch lock = new CountDownLatch(MAX_THREAD);
        final Executor executor = Executors.newFixedThreadPool(MAX_THREAD);

        final Set<Integer> sequenceNumberSet = new TreeSet<>();

        Runnable runnable = () -> {
            for (int i = 0; i < MAX_SEQUENCE_PER_CYCLE; i++) {
                try {
                    int sequenceNumber = sequenceService.appendSequenceNumber();
                    LOGGER.info("sequence number = " + sequenceNumber);
//                    sequenceNumberSet.add(sequenceNumber);
                } catch (Throwable throwable) {
                    LOGGER.error(throwable.getMessage(), throwable);
                }
            }

            lock.countDown();
        };

        for (int i=0; i<MAX_THREAD; i++) {
            executor.execute(runnable);
        }

        lock.await();

        Assert.assertEquals(MAX_THREAD * MAX_SEQUENCE_PER_CYCLE, sequenceNumberSet.size());
    }

}
