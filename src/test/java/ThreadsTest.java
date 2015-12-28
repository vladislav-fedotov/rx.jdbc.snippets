import com.github.davidmoten.rx.jdbc.Database;
import conf.TestConfig;
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
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("h2")
public class ThreadsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadsTest.class);

    @Autowired
    private EmbeddedDatabaseBuilder dataSourceBuilder;

    private Database database;
    private EmbeddedDatabase dataSource;

    @Before
    public void setUp() throws Exception {
        dataSource = dataSourceBuilder.build();
        database = Database.fromDataSource(dataSource);
    }

    @Test
    public void testThreadPerOperation() throws InterruptedException {
        Observable<String> observable = Observable
                .create(subscriber -> {
                    subscriber.onNext("onNext-" + Thread.currentThread().getName());
                    subscriber.onCompleted();
                })
                .map(value -> {
                    System.out.println("+");
                    return "map_1-" + Thread.currentThread().getName() + " - " + value;
                })
                .map(value -> "map_2-" + Thread.currentThread().getName() + " - " + value)
                .doOnUnsubscribe(() -> System.out.println("uns-" + Thread.currentThread().getName()))
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .doOnNext(s -> {
                    System.out.println("doOnNext_1-" + Thread.currentThread().getName() + " - " + s);
                })
                .doOnNext(s -> {
                    System.out.println("doOnNext_2-" + Thread.currentThread().getName() + " - " + s);
                });
        observable.subscribe(value -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("sub_1-" + Thread.currentThread().getName() + " - " + value);
        });
        observable.subscribe(value -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("sub_2-" + Thread.currentThread().getName() + " - " + value);
        });
        TimeUnit.SECONDS.sleep(1);
    }
}
