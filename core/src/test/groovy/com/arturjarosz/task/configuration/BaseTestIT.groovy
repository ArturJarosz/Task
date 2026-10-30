package com.arturjarosz.task.configuration

import com.arturjarosz.task.DatabaseMain
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

/**
 * PostgreSQL is started by Testcontainers from the jdbc:tc: datasource url in the test application.yaml.
 */
@SpringBootTest(classes = DatabaseMain.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
class BaseTestIT extends Specification {
    protected static final String HOST = "http://localhost"
    protected static String baseUrl;

    @Value('${server.port}')
    protected String port

    def setup() {
        baseUrl = "$HOST:$port"
    }
}
