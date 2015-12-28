import com.github.davidmoten.rx.jdbc.Database;
import conf.TestConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;
import rx.Subscription;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("h2")
public class TimeoutTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutTest.class);

    @Autowired
    private EmbeddedDatabaseBuilder dataSourceBuilder;

    private Database database;

    @Before
    public void setUp() throws Exception {
        EmbeddedDatabase dataSource = dataSourceBuilder.build();
        database = Database.fromDataSource(dataSource);
    }

    @Test
    public void testTimeoutQuery() throws InterruptedException {
        Observable<String> observable = database.asynchronous()
                                                .select("SELECT email FROM users WHERE name = 'alex'")
                                                .getAs(String.class)
                                                .timeout(10, TimeUnit.MILLISECONDS)
                ;

        LOGGER.info("Subscribe to observable and start query execution...");
        observable.subscribe(LOGGER::info, throwable -> LOGGER.error(throwable.getMessage()));
        LOGGER.info("After subscribing to observable...");
        TimeUnit.SECONDS.sleep(2);
    }
}
