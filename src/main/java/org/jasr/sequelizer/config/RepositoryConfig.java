package org.jasr.sequelizer.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jasr.sequelizer.entities.SqlJob;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableConfigurationProperties
@EnableTransactionManagement
@PropertySource("classpath:databases.properties")
@ConfigurationProperties(prefix = "db")
public class RepositoryConfig extends RepositoryRestMvcConfiguration {

    private List<String> name       = new ArrayList<>();
    private List<String> db_name    = new ArrayList<>();
    private List<String> test_query = new ArrayList<>();
    private List<String> host       = new ArrayList<>();
    private List<String> port       = new ArrayList<>();
    private List<String> user       = new ArrayList<>();
    private List<String> password   = new ArrayList<>();

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<String> getDb_name() {
        return db_name;
    }

    public void setDb_name(List<String> db_name) {
        this.db_name = db_name;
    }

    public List<String> getTest_query() {
        return test_query;
    }

    public void setTest_query(List<String> test_query) {
        this.test_query = test_query;
    }

    public List<String> getHost() {
        return host;
    }

    public void setHost(List<String> host) {
        this.host = host;
    }

    public List<String> getPort() {
        return port;
    }

    public void setPort(List<String> port) {
        this.port = port;
    }

    public List<String> getUser() {
        return user;
    }

    public void setUser(List<String> user) {
        this.user = user;
    }

    public List<String> getPassword() {
        return password;
    }

    public void setPassword(List<String> password) {
        this.password = password;
    }

    @Bean
    public Map<String, NamedParameterJdbcTemplate> templates() {
        Map<String, NamedParameterJdbcTemplate> templates = new HashMap<>();
        for (int i = 0; i < host.size(); i++) {
            templates.put(name.get(i), template(name.get(i), db_name.get(i), test_query.get(i), host.get(i), port.get(i),
                    user.get(i), password.get(i)));
        }
        return templates;
    }

    private NamedParameterJdbcTemplate template(String name, String db_name, String test_query, String host, String port,
            String user, String password) {
        Locale.setDefault(Locale.US);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        config.setJdbcUrl("jdbc:oracle:thin:@" + host + ":" + port + ":" + db_name);
        config.setUsername(user);
        config.setReadOnly(true);
        config.setPassword(password);
        config.addDataSourceProperty("user.country", "us");
        config.addDataSourceProperty("user.language", "en");
        return new NamedParameterJdbcTemplate(new HikariDataSource(config));
    }

    @Override
    protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(SqlJob.class);
    }

    @Override
    protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
        v.addValidator("beforeSave", validator);
        v.addValidator("beforeCreate", validator);
    }

    private Validator validator = new Validator() {

        @Override
        public boolean supports(Class<?> clazz) {
            return SqlJob.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name cannot be empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "csvName", "csvName cannot be empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dbName", "dbName cannot be empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "project", "project cannot be empty");
        }
    };

}
