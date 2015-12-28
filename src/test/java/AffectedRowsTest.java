import com.github.davidmoten.rx.jdbc.Database;
import conf.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("mysql")
public class AffectedRowsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutTest.class);

    @Autowired
    private DataSource dataSource;

    private Database database;

    @Before
    public void setUp() throws Exception {
        database = Database.fromDataSource(dataSource);
    }

    @Test
    public void testRowsAffected() {
        int affectedRows = database.update("update big_table set ID = concat(ID,'test')")
                                   .count()
                                   .toBlocking()
                                   .first();
        System.out.println(affectedRows);
    }
}
