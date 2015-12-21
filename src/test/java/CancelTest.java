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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("mysql")
public class CancelTest {

    @Autowired
    private DataSource dataSource;

    private Database database;

    @Before
    public void setUp() throws Exception {
        database = Database.fromDataSource(dataSource);
    }

    @Test
    public void testCancellationQuery() throws Exception {
        System.out.println(database.select("select body from posts").getAs(String.class).toBlocking().first());
    }

    @Test
    public void testCancellationQuery2() throws Exception {
        Database adb = database.asynchronous();
        Observable observable = adb
                .select("SELECT * FROM big_table t1 JOIN big_table t2 ON (SUBSTR(t1.TEXT,RAND()*100,RAND()*100) = SUBSTR(t2.TEXT,RAND()*100,RAND()*100) AND SUBSTR(t2.TEXT,RAND()*100,RAND()*100) = 'tttt')")
                .getAs(String.class)
                .doOnNext(s -> {
                    System.out.println("TEST");
                });
        Subscription subscription = observable.subscribe();
        Thread.sleep(1000);
        subscription.unsubscribe();

    }


}
