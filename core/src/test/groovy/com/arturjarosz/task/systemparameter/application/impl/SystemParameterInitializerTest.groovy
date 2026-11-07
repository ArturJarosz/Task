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
    def userProperties = new UserProperties(vatTax: 0.23D, incomeTax: 0.18D, themeColor: "#415cc8")
    def googleCalendarProperties = new GoogleCalendarProperties(true, null, "Task app", "Office", null, null)

    def systemParameterInitializer = new SystemParameterInitializer(systemParameterRepository,
            systemParameterValidatorService, userProperties, googleCalendarProperties)

    def "run should save default parameters when they are not in the database"() {
        given:
            this.systemParameterRepository.existsByName(_ as String) >> false
            this.systemParameterRepository.findByName(_ as String) >> Optional.empty()
            List<SystemParameter> saved = []
        when:
            this.systemParameterInitializer.run(null)
        then:
            11 * this.systemParameterRepository.save(_ as SystemParameter) >> { SystemParameter parameter ->
                saved << parameter
                parameter
            }
        and:
            saved.collectEntries { [(it.name): it.value] } ==
                    [(SystemParameters.VAT_TAX)                         : "0.23",
                     (SystemParameters.INCOME_TAX)                      : "0.18",
                     (SystemParameters.THEME_COLOR)                     : "#415cc8",
                     (SystemParameters.UPCOMING_EVENTS_SHOWN)           : "10",
                     (SystemParameters.PAST_EVENTS_SHOWN)               : "10",
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
                     (SystemParameters.UPCOMING_EVENTS_SHOWN)           : SystemParameterType.NUMBER,
                     (SystemParameters.PAST_EVENTS_SHOWN)               : SystemParameterType.NUMBER,
                     (SystemParameters.GOOGLE_CALENDAR_CREDENTIALS)     : SystemParameterType.SECRET,
                     (SystemParameters.GOOGLE_CALENDAR_NAME)            : SystemParameterType.TEXT,
                     (SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME): SystemParameterType.TEXT,
                     (SystemParameters.GOOGLE_CALENDAR_POLL_INTERVAL)   : SystemParameterType.NUMBER,
                     (SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL): SystemParameterType.NUMBER,
                     (SystemParameters.GOOGLE_CALENDAR_SYNC)            : SystemParameterType.BOOLEAN]
            saved.every { it.singleValue }
            saved.find { it.name == SystemParameters.GOOGLE_CALENDAR_CREDENTIALS }.defaultValue == ""
            saved.find { it.name == SystemParameters.THEME_COLOR }.defaultValue == "#415cc8"
    }

    def "run should not override parameters that are already in the database"() {
        given:
            this.systemParameterRepository.existsByName(SystemParameters.VAT_TAX) >> true
            this.systemParameterRepository.existsByName(SystemParameters.INCOME_TAX) >> false
            this.systemParameterRepository.existsByName(SystemParameters.THEME_COLOR) >> true
            this.systemParameterRepository.existsByName(_ as String) >> true
            this.systemParameterRepository.findByName(_ as String) >> Optional.empty()
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

    def "run should refresh default values of Google Calendar names from configuration and keep their values"() {
        given:
            def calendarName = new SystemParameter(SystemParameters.GOOGLE_CALENDAR_NAME, "Custom", "Old",
                    SystemParameterType.TEXT, true)
            def applicationName = new SystemParameter(SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME, "Custom app",
                    "Old app", SystemParameterType.TEXT, true)
            this.systemParameterRepository.existsByName(_ as String) >> true
            this.systemParameterRepository.findByName(SystemParameters.GOOGLE_CALENDAR_NAME) >> Optional.of(calendarName)
            this.systemParameterRepository.findByName(SystemParameters.GOOGLE_CALENDAR_APPLICATION_NAME) >>
                    Optional.of(applicationName)
        when:
            this.systemParameterInitializer.run(null)
        then:
            1 * this.systemParameterRepository.save(calendarName)
            1 * this.systemParameterRepository.save(applicationName)
            calendarName.defaultValue == "Office"
            calendarName.value == "Custom"
            applicationName.defaultValue == "Task app"
            applicationName.value == "Custom app"
    }

    def "run should not save Google Calendar names whose default value matches configuration"() {
        given:
            def calendarName = new SystemParameter(SystemParameters.GOOGLE_CALENDAR_NAME, "Custom", "Office",
                    SystemParameterType.TEXT, true)
            this.systemParameterRepository.existsByName(_ as String) >> true
            this.systemParameterRepository.findByName(SystemParameters.GOOGLE_CALENDAR_NAME) >> Optional.of(calendarName)
            this.systemParameterRepository.findByName(_ as String) >> Optional.empty()
        when:
            this.systemParameterInitializer.run(null)
        then:
            0 * this.systemParameterRepository.save(_)
    }

    def "run should keep the stored default value and start when the configured name is not valid"() {
        given:
            def calendarName = new SystemParameter(SystemParameters.GOOGLE_CALENDAR_NAME, "Custom", "Old",
                    SystemParameterType.TEXT, true)
            this.systemParameterRepository.existsByName(_ as String) >> true
            this.systemParameterRepository.findByName(SystemParameters.GOOGLE_CALENDAR_NAME) >> Optional.of(calendarName)
            this.systemParameterRepository.findByName(_ as String) >> Optional.empty()
            this.systemParameterValidatorService.validateOnUpdate(_) >> { throw new IllegalArgumentException("") }
        when:
            this.systemParameterInitializer.run(null)
        then:
            noExceptionThrown()
            calendarName.defaultValue == "Old"
            0 * this.systemParameterRepository.save(_)
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
            this.systemParameterRepository.findByName(_ as String) >> Optional.empty()
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
            this.systemParameterRepository.findByName(_ as String) >> Optional.empty()
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
