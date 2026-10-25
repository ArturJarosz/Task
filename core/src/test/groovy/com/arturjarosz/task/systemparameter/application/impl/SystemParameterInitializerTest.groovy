package com.arturjarosz.task.systemparameter.application.impl

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.properties.GoogleCalendarProperties
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.systemparameter.domain.SystemParameterValidatorService
import com.arturjarosz.task.systemparameter.domain.SystemParameters
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository
import com.arturjarosz.task.systemparameter.model.SystemParameter
import com.arturjarosz.task.systemparameter.model.SystemParameterType
import spock.lang.Specification

class SystemParameterInitializerTest extends Specification {

    def systemParameterRepository = Mock(SystemParameterRepository)
    def systemParameterValidatorService = Mock(SystemParameterValidatorService)
    def userProperties = new UserProperties(vatTax: 0.23D, incomeTax: 0.18D, themeColor: "#465fff")
    def googleCalendarProperties = new GoogleCalendarProperties(true, null, "Task app", "Office", null, null)

    def systemParameterInitializer = new SystemParameterInitializer(systemParameterRepository,
            systemParameterValidatorService, userProperties, googleCalendarProperties)

    def "run should save default parameters when they are not in the database"() {
        given:
            this.systemParameterRepository.existsByName(_ as String) >> false
            List<SystemParameter> saved = []
        when:
            this.systemParameterInitializer.run(null)
        then:
            9 * this.systemParameterRepository.save(_ as SystemParameter) >> { SystemParameter parameter ->
                saved << parameter
                parameter
            }
        and:
            saved.collectEntries { [(it.name): it.value] } ==
                    [(SystemParameters.VAT_TAX)                         : "0.23",
                     (SystemParameters.INCOME_TAX)                      : "0.18",
                     (SystemParameters.THEME_COLOR)                     : "#465fff",
                     (SystemParameters.GOOGLE_CALENDAR_CREDENTIALS)     : "",
                     (SystemParameters.GOOGLE_CALENDAR_NAME)            : "Office",
                     (SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME): "Task app",
                     (SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL)   : "15",
                     (SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL): "1",
                     (SystemParameters.GOOGLE_CALENDAR_SYNC)            : "false"]
            saved.collectEntries { [(it.name): it.type] } ==
                    [(SystemParameters.VAT_TAX)                         : SystemParameterType.DECIMAL_NUMBER,
                     (SystemParameters.INCOME_TAX)                      : SystemParameterType.DECIMAL_NUMBER,
                     (SystemParameters.THEME_COLOR)                     : SystemParameterType.TEXT,
                     (SystemParameters.GOOGLE_CALENDAR_CREDENTIALS)     : SystemParameterType.SECRET,
                     (SystemParameters.GOOGLE_CALENDAR_NAME)            : SystemParameterType.TEXT,
                     (SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME): SystemParameterType.TEXT,
                     (SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL)   : SystemParameterType.NUMBER,
                     (SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL): SystemParameterType.NUMBER,
                     (SystemParameters.GOOGLE_CALENDAR_SYNC)            : SystemParameterType.BOOLEAN]
            saved.every { it.singleValue }
            saved.find { it.name == SystemParameters.GOOGLE_CALENDAR_CREDENTIALS }.defaultValue == ""
    }

    def "run should not override parameters that are already in the database"() {
        given:
            this.systemParameterRepository.existsByName(SystemParameters.VAT_TAX) >> true
            this.systemParameterRepository.existsByName(SystemParameters.INCOME_TAX) >> false
            this.systemParameterRepository.existsByName(SystemParameters.THEME_COLOR) >> true
            this.systemParameterRepository.existsByName(_ as String) >> true
        when:
            this.systemParameterInitializer.run(null)
        then:
            1 * this.systemParameterRepository.save({ SystemParameter parameter ->
                parameter.name == SystemParameters.INCOME_TAX
            })
            0 * this.systemParameterRepository.save({ SystemParameter parameter ->
                parameter.name == SystemParameters.VAT_TAX
            })
    }

    def "run should validate default value before saving it"() {
        given:
            this.systemParameterRepository.existsByName(_ as String) >> false
            this.systemParameterValidatorService.validateOnUpdate(_) >> { throw new IllegalArgumentException("") }
        when:
            this.systemParameterInitializer.run(null)
        then:
            thrown(IllegalArgumentException)
            0 * this.systemParameterRepository.save(_)
    }

    def "run should store the Google Calendar key from the credentials file and turn sync on"() {
        given:
            def keyFile = File.createTempFile("service-account", ".json")
            keyFile.text = '{"type": "service_account"}'
            def subject = new SystemParameterInitializer(this.systemParameterRepository,
                    this.systemParameterValidatorService, this.userProperties,
                    new GoogleCalendarProperties(true, keyFile.absolutePath, "Task", "Task", null, null))
            this.systemParameterRepository.existsByName(_ as String) >> false
            List<SystemParameter> saved = []
            this.systemParameterRepository.save(_ as SystemParameter) >> { SystemParameter parameter ->
                saved << parameter
                parameter
            }
        when:
            subject.run(null)
        then:
            def credentials = saved.find { it.name == SystemParameters.GOOGLE_CALENDAR_CREDENTIALS }
            credentials.value == '{"type": "service_account"}'
            credentials.defaultValue == ""
            saved.find { it.name == SystemParameters.GOOGLE_CALENDAR_SYNC }.value == "true"
        cleanup:
            keyFile.delete()
    }

    def "run should store empty Google Calendar key and keep sync off when the credentials file is not a valid key"() {
        given:
            def keyFile = File.createTempFile("service-account", ".json")
            keyFile.text = "not a key"
            def subject = new SystemParameterInitializer(this.systemParameterRepository,
                    this.systemParameterValidatorService, this.userProperties,
                    new GoogleCalendarProperties(true, keyFile.absolutePath, "Task", "Task", null, null))
            this.systemParameterRepository.existsByName(_ as String) >> false
            this.systemParameterValidatorService.validateOnUpdate({ it.value == "not a key" }) >> {
                throw new IllegalArgumentException("")
            }
            List<SystemParameter> saved = []
            this.systemParameterRepository.save(_ as SystemParameter) >> { SystemParameter parameter ->
                saved << parameter
                parameter
            }
        when:
            subject.run(null)
        then:
            noExceptionThrown()
            saved.find { it.name == SystemParameters.GOOGLE_CALENDAR_CREDENTIALS }.value == ""
            saved.find { it.name == SystemParameters.GOOGLE_CALENDAR_SYNC }.value == "false"
        cleanup:
            keyFile.delete()
    }
}
