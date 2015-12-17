import com.github.davidmoten.rx.jdbc.Database;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class SyncTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncTest.class);
    private Connection connection;
    private Database db;

    @Before
    public void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        String url = "jdbc:h2:mem:test";
        String urlWithInitScripts = url+";DB_CLOSE_DELAY=-1;INIT=runscript from '" +
                "./src/test/resources/db/sql/create-db" +
                ".sql'\\;runscript from './src/test/resources/db/sql/insert-data.sql'";
        connection = DriverManager.getConnection(urlWithInitScripts);
        db = Database.from(url);
    }

    @Test
    public void testSimpleSyncSelect() throws Exception {
        String selectedEmail = db.select("SELECT email FROM users")
                .getAs(String.class)
                .toBlocking()
                .first();
        LOGGER.info(selectedEmail);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }
}
