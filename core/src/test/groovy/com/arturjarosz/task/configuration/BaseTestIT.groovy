package com.arturjarosz.task.configuration

import com.arturjarosz.task.DatabaseMain
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest(classes = DatabaseMain.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BaseTestIT extends Specification {
    private static final Logger LOG = LoggerFactory.getLogger(BaseTestIT.class);
    @Shared
    private static final PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer("postgres")
            .withDatabaseName('test').withUsername(
            'test').withPassword('test');

    def setupSpec() {
        startPostgresIfNeeded()
        ['spring.datasource.url'     : 'jdbc:tc:postgresql:///test',
         'spring.datasource.username': POSTGRES_CONTAINER.getUsername(),
         'spring.datasource.password': POSTGRES_CONTAINER.getPassword()
        ].each { k, v ->
            System.setProperty(k, v)
        }
    }

    private static void startPostgresIfNeeded() {
        if (!POSTGRES_CONTAINER.isRunning()) {
            LOG.info("[BASE-INTEGRATION-TEST] - Postgres is not started. Running...")
            POSTGRES_CONTAINER.start()
        }
    }

    def cleanupSpec() {
        if (POSTGRES_CONTAINER.isRunning()) {
            LOG.info("[BASE-INTEGRATION-TEST] - Stopping Postgres...")
            POSTGRES_CONTAINER.stop()
        }
    }
}
