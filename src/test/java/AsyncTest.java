import com.github.davidmoten.rx.jdbc.Database;
import static org.junit.Assert.*;

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

/**
 * Created by vfedotov on 12/16/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("mysql")
public class AsyncTest {
    @Autowired
    private DataSource dataSource;

    private Database database;

    @Before
    public void setUp() throws Exception {
        database = Database.fromDataSource(dataSource);
    }

    @Test
    public void testAsyncQuery() throws Exception {
        Database adb = database.asynchronous();
        final Object[] timestamp = {null};
        Observable observable = adb
                .select("select now() from dual")
                .getAs(Object.class)
                .doOnNext(s -> timestamp[0] = s)
                .doOnError(throwable -> System.out.println(throwable));

        assertNull(timestamp[0]);

        Subscription subscription = observable.subscribe();
        Thread.sleep(1000);

        assertNotNull(timestamp[0]);

        subscription.unsubscribe();
    }
}
