package conf;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
public class TestConfig {

    @Value("${mysql.url}")
    private String mysqlUrl;

    @Value("${mysql.username}")
    private String mysqlUsername;

    @Value("${mysql.password}")
    private String mysqlPassword;

    @Value("${mysql.driver}")
    private String mysqlDriver;

    @Value("${h2.sql.create-db}")
    private String h2SqlCreatDb;

    @Value("${h2.sql.insert-data}")
    private String h2SqlInsertData;

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("config.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    @Profile("mysql")
    public DataSource mysqlDataSourcembee() throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(mysqlDriver);
        dataSource.setJdbcUrl(mysqlUrl);
        dataSource.setUser(mysqlUsername);
        dataSource.setPassword(mysqlPassword);
        dataSource.setInitialPoolSize(1);
        dataSource.setAcquireIncrement(1);
        dataSource.setMinPoolSize(1);
        dataSource.setMaxPoolSize(10);
        dataSource.setMaxConnectionAge(500);
        return dataSource;
    }


    @Bean
    @Profile("h2")
    public EmbeddedDatabaseBuilder h2DataSource() throws PropertyVetoException {
        EmbeddedDatabaseBuilder dataSourceBuilder = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript(h2SqlCreatDb)
                .addScript(h2SqlInsertData);
        return dataSourceBuilder;
    }


}
