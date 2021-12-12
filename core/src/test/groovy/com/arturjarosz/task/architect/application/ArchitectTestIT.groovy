package com.arturjarosz.task.architect.application

import com.arturjarosz.task.DatabaseMain
import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.configuration.BaseTestIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.spock.Testcontainers

@SpringBootTest(classes = DatabaseMain.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ArchitectTestIT extends BaseTestIT {
    private static final String NAME = "name";
    private static final String LAST_NAME = "last_name";

    @Autowired
    private TestRestTemplate restTemplate;

    @Transactional
    def "Creating Architect with proper DTO should return created architect and 201 code"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto();
            architectBasicDto.setFirstName(NAME);
            architectBasicDto.setLastName(LAST_NAME);
            HttpEntity entity = new HttpEntity(architectBasicDto);
        when:
            def exchange = restTemplate.exchange("/architects", HttpMethod.POST, entity, ArchitectBasicDto.class);
        then:
            exchange.statusCode == HttpStatus.CREATED;
            exchange.getBody().getFirstName() == NAME;
            exchange.getBody().getLastName() == LAST_NAME;

    }

    @Transactional
    def "Creating Architect with not proper DTO should return code 400 and not create new Architect"() {
        given:
            ArchitectBasicDto architectBasicDto = new ArchitectBasicDto();
            architectBasicDto.setFirstName(null);
            architectBasicDto.setLastName(LAST_NAME);
            HttpEntity entity = new HttpEntity(architectBasicDto);
        when:
            def exchange = restTemplate.exchange("/architects", HttpMethod.POST, entity, ArchitectBasicDto.class);
        then:
            exchange.statusCode == HttpStatus.BAD_REQUEST;
    }
}
