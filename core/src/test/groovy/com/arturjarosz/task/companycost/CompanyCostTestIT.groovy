package com.arturjarosz.task.companycost

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.dto.CompanyCostAmountDto
import com.arturjarosz.task.dto.CompanyCostCategoryDto
import com.arturjarosz.task.dto.CompanyCostChargeDto
import com.arturjarosz.task.dto.CompanyCostDto
import com.arturjarosz.task.dto.CompanyCostKindDto
import com.arturjarosz.task.dto.CompanyCostPaymentDto
import com.arturjarosz.task.dto.CompanyCostRepeatsDto
import com.arturjarosz.task.dto.FinancialReportDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

import java.time.LocalDate
import java.time.ZoneId

class CompanyCostTestIT extends BaseTestIT {
    static final String COSTS_URI = "/company-costs"
    static final String CATEGORIES_URI = "/company-cost-categories"
    static final String CONTENT_TYPE = "Content-Type"
    static final String APPLICATION_JSON = "application/json"
    static final long NOT_EXISTING_ID = 10000L
    static final LocalDate TODAY = LocalDate.now(ZoneId.of("Europe/Warsaw"))
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    private MockMvc mockMvc

    private Long categoryId

    def setup() {
        this.categoryId = this.createCategory("Software").id
    }

    @Transactional
    def "Creating a recurring cost should return code 201, the cost with one amount and nothing paid"() {
        when:
            def response = this.post(COSTS_URI, this.recurringDto(startMonth: TODAY.minusMonths(3)))
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def companyCost = MAPPER.readValue(response.contentAsString, CompanyCostDto)
            response.getHeader("Location") == "$COSTS_URI/${companyCost.id}"
            companyCost.startMonth == TODAY.minusMonths(3).withDayOfMonth(1)
            companyCost.amounts*.netValue == [289.0]
            companyCost.paidToDate == 0.0
            companyCost.nextCharge.chargeMonth == TODAY.minusMonths(3).withDayOfMonth(1)
            companyCost.overdueCharges >= 2
    }

    @Transactional
    def "Creating a recurring cost with markPastChargesAsPaid should pay every charge before today"() {
        when:
            def companyCost = this.createCost(this.recurringDto(startMonth: TODAY.minusMonths(3), chargeDay: 1,
                    markPastChargesAsPaid: true))
        then: "the charges of the three months before and, unless today is the 1st, of this month"
            def paidCharges = TODAY.dayOfMonth == 1 ? 3 : 4
            companyCost.paidToDate == paidCharges * 289.0
            companyCost.overdueCharges == 0
    }

    @Transactional
    def "Creating a one-time cost with paid should create its payment"() {
        when:
            def companyCost = this.createCost(new CompanyCostDto(kind: CompanyCostKindDto.ONE_TIME, name: "Laptop",
                    categoryId: categoryId, netValue: 7999.0, date: TODAY, hasInvoice: true, paid: true,
                    paymentDate: TODAY))
        then:
            companyCost.paidToDate == 7999.0
            companyCost.nextCharge == null
            companyCost.amounts.isEmpty()
    }

    @Transactional
    def "Creating a cost with not existing category should return code 404 and error message"() {
        when:
            def response = this.post(COSTS_URI, this.recurringDto(categoryId: NOT_EXISTING_ID))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.message(response) == "Company cost category with id 10,000 does not exist."
    }

    @Transactional
    def "Creating a cost with an end month before its start should return code 400 and error message"() {
        when:
            def response = this.post(COSTS_URI, this.recurringDto(startMonth: LocalDate.of(2026, 3, 1),
                    endMonth: LocalDate.of(2026, 2, 1)))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.message(response) == "End month of the company cost cannot be before its start month."
    }

    @Transactional
    def "Getting company costs should return them by name"() {
        given:
            this.createCost(this.recurringDto(name: "Office rent"))
            this.createCost(this.recurringDto(name: "Accounting office"))
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(COSTS_URI)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
            def names = MAPPER.readValue(response.contentAsString, CompanyCostDto[])*.name
            names.indexOf("Accounting office") < names.indexOf("Office rent")
    }

    @Transactional
    def "Getting company costs for a period should return only the costs charged or paid in it, with its figures"() {
        given:
            def monthly = this.createCost(this.recurringDto(name: "Rent 1999", startMonth: LocalDate.of(1999, 1, 1),
                    endMonth: LocalDate.of(1999, 12, 1), chargeDay: 10, netValue: 2400.0))
            this.createCost(this.recurringDto(name: "Licence 1999", repeats: CompanyCostRepeatsDto.YEARLY,
                    startMonth: LocalDate.of(1999, 8, 1), chargeDay: 1))
            this.createCost(new CompanyCostDto(kind: CompanyCostKindDto.ONE_TIME, name: "Insurance 1999",
                    categoryId: categoryId, netValue: 900.0, date: LocalDate.of(1999, 2, 20), hasInvoice: true,
                    paid: true, paymentDate: LocalDate.of(1999, 3, 2)))
            this.post("$COSTS_URI/${monthly.id}/payments",
                    new CompanyCostPaymentDto(chargeMonth: LocalDate.of(1999, 2, 1), paymentDate: LocalDate.of(1999, 3, 5)))
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(COSTS_URI)
                    .param("startDate", "1999-03-01").param("endDate", "1999-06-30")).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
            def costs = MAPPER.readValue(response.contentAsString, CompanyCostDto[]).findAll { it.name.endsWith("1999") }
            costs*.name == ["Insurance 1999", "Rent 1999"]
        and: "charged before the period, paid in it"
            costs[0].periodCharges == 0
            costs[0].periodPaidNetValue == 900.0
        and:
            costs[1].periodCharges == 4
            costs[1].periodChargesNetValue == 4 * 2400.0
            costs[1].periodPaidNetValue == 2400.0
            costs[1].periodOverdueCharges == 4
    }

    @Transactional
    def "Getting company costs with half a period should return code 400 and error message"() {
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(COSTS_URI).param("startDate", "2026-01-01"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.message(response) == "Give both the first and the last day of the period, or neither."
    }

    @Transactional
    def "Getting a not existing cost should return code 404 and error message"() {
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$COSTS_URI/$NOT_EXISTING_ID"))
                    .andReturn().response
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            this.message(response) == "Company cost with id 10,000 does not exist."
    }

    @Transactional
    def "Paying a charge should use its scheduled amount and show it as paid in the charges of its year"() {
        given:
            def companyCost = this.createCost(this.recurringDto(startMonth: LocalDate.of(2025, 1, 1)))
            this.post("$COSTS_URI/${companyCost.id}/amounts",
                    new CompanyCostAmountDto(validFrom: LocalDate.of(2025, 6, 1), netValue: 319.0))
        when:
            def response = this.post("$COSTS_URI/${companyCost.id}/payments",
                    new CompanyCostPaymentDto(chargeMonth: LocalDate.of(2025, 7, 1), paymentDate: LocalDate.of(2025, 7, 16)))
        then:
            response.status == HttpStatus.CREATED.value()
            def payment = MAPPER.readValue(response.contentAsString, CompanyCostPaymentDto)
            payment.netValue == 319.0
            payment.chargeDate == LocalDate.of(2025, 7, 15)
        and:
            def charges = this.charges(companyCost.id, 2025)
            charges.size() == 12
            def july = charges.find { it.chargeMonth == LocalDate.of(2025, 7, 1) }
            july.paid
            july.paymentId == payment.id
            charges.find { it.chargeMonth == LocalDate.of(2025, 5, 1) }.netValue == 289.0
    }

    @Transactional
    def "Paying a charge twice or a month without a charge should return code 400 and error message"() {
        given:
            def companyCost = this.createCost(this.recurringDto(repeats: CompanyCostRepeatsDto.QUARTERLY,
                    startMonth: LocalDate.of(2025, 1, 1)))
            this.pay(companyCost.id, LocalDate.of(2025, 4, 1))
        expect:
            this.message(this.post("$COSTS_URI/${companyCost.id}/payments", new CompanyCostPaymentDto(
                    chargeMonth: LocalDate.of(2025, 4, 1), paymentDate: TODAY))) == "The charge of 04.2025 is already paid."
            this.message(this.post("$COSTS_URI/${companyCost.id}/payments", new CompanyCostPaymentDto(
                    chargeMonth: LocalDate.of(2025, 5, 1), paymentDate: TODAY))) == "The company cost has no charge in 05.2025."
    }

    @Transactional
    def "A paid charge should keep its amount when the amount history changes"() {
        given:
            def companyCost = this.createCost(this.recurringDto(startMonth: LocalDate.of(2025, 1, 1)))
            this.pay(companyCost.id, LocalDate.of(2025, 3, 1))
        when:
            def response = this.put("$COSTS_URI/${companyCost.id}/amounts/${companyCost.amounts.first().id}",
                    new CompanyCostAmountDto(validFrom: LocalDate.of(2025, 1, 1), netValue: 299.0))
        then:
            response.status == HttpStatus.OK.value()
            MAPPER.readValue(response.contentAsString, CompanyCostDto).amounts*.netValue == [299.0]
            this.charges(companyCost.id, 2025).find { it.chargeMonth == LocalDate.of(2025, 3, 1) }.netValue == 289.0
    }

    @Transactional
    def "The first amount should not move or be removed, and a later one should be removable"() {
        given:
            def companyCost = this.createCost(this.recurringDto(startMonth: LocalDate.of(2025, 1, 1)))
            def changed = MAPPER.readValue(this.post("$COSTS_URI/${companyCost.id}/amounts",
                    new CompanyCostAmountDto(validFrom: LocalDate.of(2025, 6, 10), netValue: 319.0)).contentAsString,
                    CompanyCostDto)
            def firstId = changed.amounts.last().id
            def laterId = changed.amounts.first().id
        expect:
            changed.amounts*.validFrom == [LocalDate.of(2025, 6, 1), LocalDate.of(2025, 1, 1)]
            this.message(this.put("$COSTS_URI/${companyCost.id}/amounts/$firstId",
                    new CompanyCostAmountDto(validFrom: LocalDate.of(2025, 2, 1), netValue: 289.0))) ==
                    "The first amount always starts with the company cost, so it cannot be moved or removed."
            this.delete("$COSTS_URI/${companyCost.id}/amounts/$firstId").status == HttpStatus.BAD_REQUEST.value()
        and:
            def response = this.delete("$COSTS_URI/${companyCost.id}/amounts/$laterId")
            response.status == HttpStatus.OK.value()
            MAPPER.readValue(response.contentAsString, CompanyCostDto).amounts*.netValue == [289.0]
    }

    @Transactional
    def "Ending a cost before a paid charge should return code 400, and ending it after should work"() {
        given:
            def companyCost = this.createCost(this.recurringDto(startMonth: LocalDate.of(2025, 1, 1)))
            this.pay(companyCost.id, LocalDate.of(2025, 8, 1))
        when:
            def refused = this.put("$COSTS_URI/${companyCost.id}", this.recurringDto(startMonth: LocalDate.of(2025, 1, 1),
                    endMonth: LocalDate.of(2025, 6, 1)))
        then:
            refused.status == HttpStatus.BAD_REQUEST.value()
            this.message(refused) == "Charges of 08.2025 are paid. Mark them as not paid first."
        when:
            def ended = this.put("$COSTS_URI/${companyCost.id}", this.recurringDto(startMonth: LocalDate.of(2025, 1, 1),
                    endMonth: LocalDate.of(2025, 8, 1)))
        then:
            ended.status == HttpStatus.OK.value()
            MAPPER.readValue(ended.contentAsString, CompanyCostDto).endMonth == LocalDate.of(2025, 8, 1)
    }

    @Transactional
    def "Marking a charge as not paid and changing a payment date should work"() {
        given:
            def companyCost = this.createCost(this.recurringDto(startMonth: LocalDate.of(2025, 1, 1)))
            def payment = this.pay(companyCost.id, LocalDate.of(2025, 2, 1))
        when:
            def updated = this.put("$COSTS_URI/${companyCost.id}/payments/${payment.id}",
                    new CompanyCostPaymentDto(paymentDate: LocalDate.of(2025, 2, 20)))
        then:
            updated.status == HttpStatus.OK.value()
            MAPPER.readValue(updated.contentAsString, CompanyCostPaymentDto).paymentDate == LocalDate.of(2025, 2, 20)
        when:
            def deleted = this.delete("$COSTS_URI/${companyCost.id}/payments/${payment.id}")
        then:
            deleted.status == HttpStatus.OK.value()
            !this.charges(companyCost.id, 2025).find { it.chargeMonth == LocalDate.of(2025, 2, 1) }.paid
    }

    @Transactional
    def "Deleting a cost should remove it with its payments"() {
        given:
            def companyCost = this.createCost(this.recurringDto(startMonth: LocalDate.of(2025, 1, 1)))
            this.pay(companyCost.id, LocalDate.of(2025, 1, 1))
        when:
            def response = this.delete("$COSTS_URI/${companyCost.id}")
        then:
            response.status == HttpStatus.OK.value()
            this.mockMvc.perform(MockMvcRequestBuilders.get("$COSTS_URI/${companyCost.id}")).andReturn().response
                    .status == HttpStatus.NOT_FOUND.value()
    }

    @Transactional
    def "The financial report should count paid company costs as expenses on their payment date"() {
        given: "a cost without invoice, paid in two months of 2001, and a charge not paid"
            def companyCost = this.createCost(this.recurringDto(name: "Salary", hasInvoice: false,
                    startMonth: LocalDate.of(2001, 1, 1), endMonth: LocalDate.of(2001, 3, 1), netValue: 8500.0))
            this.post("$COSTS_URI/${companyCost.id}/payments",
                    new CompanyCostPaymentDto(chargeMonth: LocalDate.of(2001, 1, 1), paymentDate: LocalDate.of(2001, 1, 10)))
            this.post("$COSTS_URI/${companyCost.id}/payments",
                    new CompanyCostPaymentDto(chargeMonth: LocalDate.of(2001, 2, 1), paymentDate: LocalDate.of(2001, 3, 2)))
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("/financial-data")
                    .param("startDate", "2001-01-01").param("endDate", "2001-03-31").param("periodType", "MONTHLY"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
            def report = MAPPER.readValue(response.contentAsString, FinancialReportDto)
            report.periods*.expense*.companyCost*.netValue == [8500.0d, 0.0d, 8500.0d]
            report.periods[0].expense.companyCost.grossValue == 8500.0d
            report.periods[0].expense.total.netValue == 8500.0d
            report.totalExpense.netValue == 17000.0d
            report.totalBalance.netValue == -17000.0d
            report.periods[0].income.companyCost.netValue == 0.0d
    }

    @Transactional
    def "Company cost categories should count their costs, and a used one should not be deleted"() {
        given:
            this.createCost(this.recurringDto())
        when:
            def categories = MAPPER.readValue(this.mockMvc.perform(MockMvcRequestBuilders.get(CATEGORIES_URI))
                    .andReturn().response.contentAsString, CompanyCostCategoryDto[])
            def deleted = this.delete("$CATEGORIES_URI/$categoryId")
        then:
            categories.find { it.id == categoryId }.numberOfCompanyCosts == 1
            deleted.status == HttpStatus.BAD_REQUEST.value()
            this.message(deleted) == "Company cost category used by company costs cannot be removed."
    }

    @Transactional
    def "Creating a company cost category with a taken name should return code 400 and error message"() {
        when:
            def response = this.post(CATEGORIES_URI, new CompanyCostCategoryDto(name: "SOFTWARE"))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.message(response) == "Company cost category with name SOFTWARE already exists."
    }

    @Transactional
    def "Application configuration should list company cost categories with their id and name"() {
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("/configuration")).andReturn().response
        then:
            def configuration = MAPPER.readValue(response.contentAsString, ApplicationConfigurationDto)
            configuration.companyCostCategories.find { it.id == String.valueOf(categoryId) }.label == "Software"
    }

    private CompanyCostDto recurringDto(Map overrides = [:]) {
        def properties = [kind      : CompanyCostKindDto.RECURRING, name: "Adobe Creative Cloud", categoryId: categoryId,
                          repeats   : CompanyCostRepeatsDto.MONTHLY, startMonth: LocalDate.of(2024, 3, 1),
                          chargeDay : 15, netValue: 289.0, hasInvoice: true] + overrides
        return new CompanyCostDto(properties)
    }

    private CompanyCostDto createCost(CompanyCostDto companyCostDto) {
        def response = this.post(COSTS_URI, companyCostDto)
        assert response.status == HttpStatus.CREATED.value()
        return MAPPER.readValue(response.contentAsString, CompanyCostDto)
    }

    private CompanyCostPaymentDto pay(Long companyCostId, LocalDate chargeMonth) {
        def response = this.post("$COSTS_URI/$companyCostId/payments",
                new CompanyCostPaymentDto(chargeMonth: chargeMonth, paymentDate: TODAY))
        assert response.status == HttpStatus.CREATED.value()
        return MAPPER.readValue(response.contentAsString, CompanyCostPaymentDto)
    }

    private List<CompanyCostChargeDto> charges(Long companyCostId, int year) {
        def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$COSTS_URI/$companyCostId/charges")
                .param("year", String.valueOf(year))).andReturn().response
        return MAPPER.readValue(response.contentAsString, CompanyCostChargeDto[]).toList()
    }

    // found when an earlier test already created it
    private CompanyCostCategoryDto createCategory(String name) {
        def categories = MAPPER.readValue(this.mockMvc.perform(MockMvcRequestBuilders.get(CATEGORIES_URI))
                .andReturn().response.contentAsString, CompanyCostCategoryDto[])
        return categories.find { it.name == name } ?: MAPPER.readValue(
                this.post(CATEGORIES_URI, new CompanyCostCategoryDto(name: name)).contentAsString, CompanyCostCategoryDto)
    }

    private def post(String uri, Object body) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(uri).header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(body))).andReturn().response
    }

    private def put(String uri, Object body) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put(uri).header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(body))).andReturn().response
    }

    private def delete(String uri) {
        return this.mockMvc.perform(MockMvcRequestBuilders.delete(uri)).andReturn().response
    }

    private static String message(def response) {
        return MAPPER.readValue(response.contentAsString, ErrorMessage).message
    }
}
