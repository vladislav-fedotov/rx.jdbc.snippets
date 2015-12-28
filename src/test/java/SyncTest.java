import com.github.davidmoten.rx.jdbc.Database;
import conf.TestConfig;
import org.junit.After;
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
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("h2")
public class SyncTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncTest.class);

    private static final int INITIAL_STATE = 0;
    private static final int AFTER_QUERY_EXECUTION_STATE = 1;
    private static final int BEFORE_QUERY_EXECUTION = 2;

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
    public void testSimpleAsyncSelect() throws InterruptedException {
        AtomicInteger state = new AtomicInteger(INITIAL_STATE);
        AtomicReference<String> selectedEmail = new AtomicReference<>();
        database.asynchronous()
                .select("SELECT email FROM users")
                .getAs(String.class)
                .first()
                .map(selectedItem -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    assertEquals(BEFORE_QUERY_EXECUTION, state.get());
                    state.set(AFTER_QUERY_EXECUTION_STATE);
                    return selectedItem;
                })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(selectedEmail::set, Throwable::printStackTrace);


        assertEquals(INITIAL_STATE, state.get());
        state.set(BEFORE_QUERY_EXECUTION);

        int timer = 0;
        while (state.get() != AFTER_QUERY_EXECUTION_STATE && timer != 20) {
            try {
                TimeUnit.SECONDS.sleep(1);
                timer++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (state.get() == AFTER_QUERY_EXECUTION_STATE) {
            LOGGER.info("Selected email: {}", selectedEmail.get());
            assertEquals("mkyong@gmail.com", selectedEmail.get());
        }
    }

    @Test
    public void testSimpleSyncSelect() {
        AtomicInteger state = new AtomicInteger(INITIAL_STATE);
        String selectedEmail = database
                .select("SELECT email FROM users")
                .getAs(String.class)
                .map(selectedItem -> {
                    assertEquals(INITIAL_STATE, state.get());
                    state.set(AFTER_QUERY_EXECUTION_STATE);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return selectedItem;
                })
                .toBlocking()
                .first();

        assertEquals("mkyong@gmail.com", selectedEmail);
        assertEquals(AFTER_QUERY_EXECUTION_STATE, state.get());
    }

    @After
    public void tearDown() throws Exception {
        dataSource.shutdown();
    }
}
