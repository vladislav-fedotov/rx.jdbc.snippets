import com.github.davidmoten.rx.jdbc.Database;
import conf.TestConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("mysql")
public class CancelTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncTest.class);

    @Autowired
    private DataSource dataSource;

    private Database database;

    @Before
    public void setUp() throws Exception {
        database = Database.fromDataSource(dataSource);
    }

    @Test
    public void testQueryCancelattion() throws Exception {
        int beforeQueryNumberOfOpenConnections = getNumberOfOpenConnections();

        Observable<Timestamp> observable = database.asynchronous()
                                                            .select("SELECT * FROM big_table t1\n" +
                                                                    "JOIN big_table t2 ON (SUBSTR(t1.TEXT,RAND()*100," +
                                                                    "RAND()*100) = " +
                                                                    "SUBSTR(t2.TEXT,RAND()*100,RAND()*100) )\n" +
                                                                    "JOIN big_table t3 ON (SUBSTR(t1.TEXT,RAND()*100," +
                                                                    "RAND()*100) = " +
                                                                    "SUBSTR(t3.TEXT,RAND()*100,RAND()*100) );")
                                                            .getAs(Timestamp.class)
                                                            .subscribeOn(Schedulers.newThread())
                                                            .unsubscribeOn(Schedulers.newThread());


        LOGGER.info("Call subscribe() method to start query execution...");
        Subscription subscription = observable.subscribe(timestamp -> LOGGER.info(timestamp.toString()),
                throwable -> LOGGER.error("ERROR occurred: " + throwable.getMessage()),
                () -> LOGGER.info("Query execution finished"));
        LOGGER.info("Method subscribe() was called...");

        TimeUnit.SECONDS.sleep(20);
        LOGGER.info("Call unsubscribe() method to cancel query execution... ");
        subscription.unsubscribe();
        LOGGER.info("Method unsubscribe() was called...");
        TimeUnit.SECONDS.sleep(20);

        int afterQueryNumberOfOpenConnections = getNumberOfOpenConnections();

        Assert.assertEquals(beforeQueryNumberOfOpenConnections, afterQueryNumberOfOpenConnections);
    }

    private int getNumberOfOpenConnections() {
        return database.select("SELECT ID FROM information_schema.processlist limit 0,1000")
                       .getAs(Integer.class)
                       .count()
                       .toBlocking()
                       .first();
    }


}
