package com.arturjarosz.task.systemparameter.domain.impl

import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto
import com.arturjarosz.task.systemparameter.domain.validator.SystemParameterValidator
import com.arturjarosz.task.systemparameter.query.impl.SystemParameterQueryServiceImpl
import spock.lang.Specification

class SystemParameterValidatorServiceImplTest extends Specification {
    private static final String PARAMETER_NAME_1 = "parameter1"
    private static final String PARAMETER_NAME_2 = "parameter2"
    private static final String PARAMETER_NAME_3 = "parameter3"
    private static final List<String> PARAMETERS = Arrays.asList(PARAMETER_NAME_1, PARAMETER_NAME_2, PARAMETER_NAME_3)

    def systemParameterQueryService = Mock(SystemParameterQueryServiceImpl) {
        getSystemParametersNames() >> PARAMETERS
    }
    def parameterValidator1 = Mock(SystemParameterValidator) {
        getSystemParameterName() >> PARAMETER_NAME_1
    }
    def parameterValidator2 = Mock(SystemParameterValidator) {
        getSystemParameterName() >> PARAMETER_NAME_2
    }
    def parameterValidator3 = Mock(SystemParameterValidator) {
        getSystemParameterName() >> PARAMETER_NAME_3
    }
    def systemParameterQueryServiceNotFullList = Mock(SystemParameterQueryServiceImpl) {
        getSystemParametersNames() >> Arrays.asList(PARAMETER_NAME_1, PARAMETER_NAME_2)
    }

    List<SystemParameterValidator> validators =
            Arrays.asList(parameterValidator1, parameterValidator2, parameterValidator3)

    List<SystemParameterValidator> notFullListOfValidators =
            Arrays.asList(parameterValidator1, parameterValidator2)

    def systemParameterValidatorService = new SystemParameterValidatorServiceImpl(validators,
            systemParameterQueryService)

    def "when validators for all system parameters are present no exceptions are thrown"() {
        given:
        when:
            this.systemParameterValidatorService.validateSystemParameters()
        then:
            noExceptionThrown()
    }

    def "validateSystemParameters should call getSystemParameterNames from systemParameterQueryService"() {
        given:
        when:
            this.systemParameterValidatorService.validateSystemParameters()
        then:
            1 * this.systemParameterQueryService.getSystemParametersNames() >> {
                Arrays.asList(PARAMETER_NAME_1, PARAMETER_NAME_2, PARAMETER_NAME_3)
            }
    }

    def "validateSystemParameters should call validate on each validator"() {
        given:
        when:
            this.systemParameterValidatorService.validateSystemParameters()
        then:
            3 * _.validate(_ as String)
    }

    def "when validator for system parameter is missing, exception will be thrown"() {
        given:
        when:
            new SystemParameterValidatorServiceImpl(notFullListOfValidators, systemParameterQueryService)
        then:
            Exception exception = thrown()
            exception.message == "notExist.systemParameter.validator"
    }

    def "validateOnUpdate should throw an exception when parameter has no validator"() {
        given:
            SystemParameterDto systemParameterDto = new SystemParameterDto(name: PARAMETER_NAME_3)
            def parameterValidatorServiceImpl = new SystemParameterValidatorServiceImpl(notFullListOfValidators,
                    systemParameterQueryServiceNotFullList)
        when:
            parameterValidatorServiceImpl.validateOnUpdate(systemParameterDto)
        then:
            Exception exception = thrown()
            exception.message == "notExist.systemParameter.validator"
    }

    def "validateOnUpdate should call validate on validator for parameter"() {
        given:
            SystemParameterDto systemParameterDto = new SystemParameterDto(name: PARAMETER_NAME_1)
        when:
            this.systemParameterValidatorService.validateOnUpdate(systemParameterDto)
        then:
            1 * parameterValidator1.validateOnUpdate(systemParameterDto)
    }
}
