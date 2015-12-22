import com.github.davidmoten.rx.jdbc.Database;
import conf.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;
import rx.Subscription;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * Created by vfedotov on 12/16/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("mysql")
public class TimeoutTest {

    @Autowired
    private DataSource dataSource;

    private Database database;

    @Before
    public void setUp() throws Exception {
        database = Database.fromDataSource(dataSource);
    }

    @Test
    public void testTimeoutQuery() {
        Database adb = database.asynchronous();
        Observable observable = adb
                .select("SELECT * FROM big_table t1 JOIN big_table t2 ON (SUBSTR(t1.TEXT,RAND()*100,RAND()*100) = SUBSTR(t2.TEXT,RAND()*100,RAND()*100) AND SUBSTR(t2.TEXT,RAND()*100,RAND()*100) = 'tttt')")
                .getAs(String.class)
                .doOnNext(s -> System.out.println(s))
                .timeout(2, TimeUnit.SECONDS)
                .doOnError(throwable -> System.out.println(throwable));
        Subscription subscription = observable.subscribe();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        subscription.unsubscribe();

    }
}
