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

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("h2")
public class SyncTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncTest.class);

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
    public void testSimpleSyncSelect() throws Exception {
        LOGGER.info("Executed first test");
        Integer affectedRows = database.update("update users set email = 'changed' where name = 'mkyong'")
                .count()
                .toBlocking()
                .first();
        assertEquals(1, affectedRows.intValue());
    }

    @Test
    public void testSimpleSyncSelect2() throws Exception {
        LOGGER.info("Executed second test");
        String selectedEmail = database.select("SELECT email FROM users")
                .getAs(String.class)
                .toBlocking()
                .first();
        assertEquals("mkyong@gmail.com", selectedEmail);
    }

    @After
    public void tearDown() throws Exception {
        dataSource.shutdown();
    }
}
