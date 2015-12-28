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
import rx.Subscriber;
import rx.functions.Func1;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("mysql")
public class InfinityTest {

    @Autowired
    private DataSource dataSource;

    private Database database;

    @Before
    public void setUp() throws Exception {
        database = Database.fromDataSource(dataSource);
    }

    @Test
    public void testSimpleAsyncSelect() throws InterruptedException {
        Long timestamp = database
                .select("SELECT UNIX_TIMESTAMP(NOW())+100 AS now FROM dual")
                .getAs(Long.class)
                .toBlocking()
                .first();
//        Timestamp timestampInFuture = Timestamp.from(Instant.ofEpochMilli(timestamp.getTime()));


        Func1<Object, Observable<Object>> throwException = new Func1<Object, Observable<Object>>() {
            @Override
            public Observable<Object> call(Object n) {
                return Observable.create(new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        System.out.println("In call method");
                        subscriber.onNext(100);
                        subscriber.onError(new RuntimeException("boo"));
                    }
                });
            }
        };

        System.out.println("SELECT NOW() AS now FROM dual WHERE UNIX_TIMESTAMP(NOW()) > " + timestamp);
        database
                .select("SELECT NOW() AS now FROM dual WHERE UNIX_TIMESTAMP(NOW()) > ?")
                .parameter(timestamp)
                .getAs(Long.class)
//                .map(aLong1 -> {
//                    System.out.println("In map");
//                    throw new RuntimeException();
//                })
//                .concatMap(throwException)
//                .retry().count()
                .isEmpty()
                .map(aBoolean -> {
                    System.out.println(aBoolean);
                    return aBoolean;
                })
                .map(aBoolean1 -> {
                    if (aBoolean1) {
                        throw new RuntimeException();
                    } else {

                    }
                    return aBoolean1;
                })
                .retry()
//                .retryWhen(observable -> {
//                    System.out.println("retry");
//                    return observable;
//                })
                .subscribe(aLong -> {
                    System.out.println(aLong);
                }, Throwable::printStackTrace, () -> {
                    System.out.println("completed");
//                    throw new RuntimeException();
                });

//                .concatMap(throwException)
//                .retry()
//                .doOnRequest(aLong1 -> {
//                    System.out.println("On request");
//                })
//                .doOnEach(notification -> {
//                    System.out.println("notif");
//                })
//                .doOnUnsubscribe(() -> {
//                    System.out.println("unsubscribe");
//                })
//                .finallyDo(() -> {
//                    System.out.println("finally");
//                })
//                .doOnError((throwable) -> {
//                    System.out.println("-");
//                })
//                .doOnTerminate(() -> {
//                    System.out.println("!");
//                })
//                .doOnNext(aLong -> {
//                    System.out.println("!!");
//                })
//                .doOnCompleted(() -> {
//                    System.out.println("!!!");
//                })
//                .toBlocking();

//        System.out.println(l);

        TimeUnit.SECONDS.sleep(30);


    }
}
