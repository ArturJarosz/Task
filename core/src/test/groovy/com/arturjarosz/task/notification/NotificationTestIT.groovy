package com.arturjarosz.task.notification

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ArchitectUserLinkDto
import com.arturjarosz.task.dto.CalendarEventDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ContractDto
import com.arturjarosz.task.dto.ContractStatusDto
import com.arturjarosz.task.dto.InstallmentDto
import com.arturjarosz.task.dto.NotificationCountDto
import com.arturjarosz.task.dto.NotificationReadDto
import com.arturjarosz.task.dto.NotificationSettingDto
import com.arturjarosz.task.dto.NotificationTypeDto
import com.arturjarosz.task.dto.NotificationsDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.dto.ProjectDto
import com.arturjarosz.task.dto.StageDto
import com.arturjarosz.task.dto.TaskDto
import com.arturjarosz.task.dto.TaskStatusDto
import com.arturjarosz.task.notification.domain.NotificationTime
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import com.arturjarosz.task.utils.TestsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tools.jackson.databind.ObjectMapper

import java.time.LocalDate

/**
 * Notifications are created when an item is saved, so these tests don't wait for the nightly job.
 */
class NotificationTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String CONTRACTS_URI = "/contracts"
    static final String EVENTS_URI = "/calendars/events"
    static final String NOTIFICATIONS_URI = "/notifications"
    static final String SETTINGS_URI = "/notification-settings"
    static final String PROJECTS_URI = "/projects"
    static final String AUTH_ID = "auth0|notification-test"
    static final String OTHER_AUTH_ID = "auth0|notification-test-other"
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architectDto = this.readJson('json/architect/architect.json', ArchitectDto)
    final def clientDto = this.readJson('json/client/privateClient.json', ClientDto)
    final def projectCreateDto = this.readJson('json/project/properProject.json', ProjectCreateDto)

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private UserRepository userRepository

    private final LocalDate today = LocalDate.now(NotificationTime.ZONE)
    private final List<Long> eventIds = []
    private final List<Long> projectIds = []
    private final List<Long> architectIds = []
    private final List<Long> clientIds = []
    private User user
    private Long architectId

    def setup() {
        this.user = this.userRepository.save(new User("Notified", "notified@example.com", AUTH_ID))
        this.architectId = this.createArchitect()
        this.request(MockMvcRequestBuilders.put("$HOST:$port$ARCHITECTS_URI/$architectId/user"),
                new ArchitectUserLinkDto(userId: this.user.id))
        authenticate(AUTH_ID)
    }

    def cleanup() {
        eventIds.each { this.request(MockMvcRequestBuilders.delete("$HOST:$port$EVENTS_URI/$it")) }
        projectIds.each { this.request(MockMvcRequestBuilders.delete("$HOST:$port$PROJECTS_URI/$it")) }
        clientIds.each { this.request(MockMvcRequestBuilders.delete("$HOST:$port$CLIENTS_URI/$it")) }
        architectIds.each { this.request(MockMvcRequestBuilders.delete("$HOST:$port$ARCHITECTS_URI/$it")) }
        // Removing a user removes their notifications and settings
        [AUTH_ID, OTHER_AUTH_ID].each { authId -> this.userRepository.findByAuthId(authId).ifPresent { this.userRepository.delete(it) } }
        SecurityContextHolder.clearContext()
    }

    def "Adding a meeting tomorrow on a project of the user's architect should notify the user at once"() {
        given:
            def projectId = this.createProject(this.architectId)
        when:
            this.createEvent(projectId, null, this.today.plusDays(1))
        then:
            this.unreadCount() == 1
            def notification = this.notifications(true).notifications.find { it.type == NotificationTypeDto.MEETING }
            notification.itemName == "Site visit"
            notification.projectId == projectId
            notification.dueDate == this.today.plusDays(1)
            notification.dueDateTime != null
            !notification.read
    }

    def "Meeting further away than the notice period should not notify"() {
        given:
            def projectId = this.createProject(this.architectId)
        when:
            this.createEvent(projectId, null, this.today.plusDays(5))
        then:
            this.unreadCount() == 0
    }

    def "Meeting without a project or architect should notify every user"() {
        when:
            def eventId = this.createEvent(null, null, this.today.plusDays(1))
        then:
            this.notifications(true).notifications*.itemId.contains(eventId)
    }

    def "Meeting without a project should notify the user of its architect only"() {
        given:
            def otherArchitectId = this.createArchitect()
        when:
            def ownEventId = this.createEvent(null, this.architectId, this.today.plusDays(1))
            def otherEventId = this.createEvent(null, otherArchitectId, this.today.plusDays(1))
        then:
            def itemIds = this.notifications(true).notifications*.itemId
            itemIds.contains(ownEventId)
            !itemIds.contains(otherEventId)
    }

    def "Project of an architect without a linked user should notify no one"() {
        given:
            def projectId = this.createProject(this.createArchitect())
        when:
            this.createEvent(projectId, null, this.today.plusDays(1))
        then:
            this.unreadCount() == 0
    }

    def "Moving a stage deadline out of the notice period should remove its unread notification"() {
        given:
            def projectId = this.createProject(this.architectId)
            def stage = this.createStage(projectId, this.today.plusDays(2), null)
            def before = this.notifications(true).notifications.findAll { it.type == NotificationTypeDto.STAGE_DEADLINE }
        when:
            stage.deadline = this.today.plusDays(20)
            def response = this.request(MockMvcRequestBuilders.put("$HOST:$port$PROJECTS_URI/$projectId/stages/${stage.id}"), stage)
        then:
            response.status == HttpStatus.OK.value()
            before*.itemId == [stage.id]
            before[0].dueDate == this.today.plusDays(2)
            this.notifications(true).notifications.findAll { it.type == NotificationTypeDto.STAGE_DEADLINE }.isEmpty()
    }

    def "Removing a stage should remove its notifications, read ones too"() {
        given:
            def projectId = this.createProject(this.architectId)
            def stage = this.createStage(projectId, this.today.plusDays(1), new InstallmentDto(value: 1000,
                    dueDate: this.today.plusDays(1)))
            def stageNotification = this.notifications(true).notifications.find { it.type == NotificationTypeDto.STAGE_DEADLINE }
            this.markRead(stageNotification.id, true)
        when:
            def response = this.request(MockMvcRequestBuilders.delete("$HOST:$port$PROJECTS_URI/$projectId/stages/${stage.id}"))
        then:
            response.status == HttpStatus.OK.value()
            this.notifications(false).notifications.findAll {
                it.type in [NotificationTypeDto.STAGE_DEADLINE, NotificationTypeDto.INSTALLMENT_PAYMENT]
            }.isEmpty()
    }

    def "Finishing a stage's last task should remove its deadline notification, and reopening the task should bring it back"() {
        given:
            def projectId = this.createProject(this.architectId)
            this.acceptOffer(projectId)
            def stage = this.createStage(projectId, this.today.plusDays(2), null)
            def taskId = this.createTask(projectId, stage.id)
            this.changeTaskStatus(projectId, stage.id, taskId, TaskStatusDto.IN_PROGRESS)
        when:
            this.changeTaskStatus(projectId, stage.id, taskId, TaskStatusDto.DONE)
            def afterDone = this.stageNotifications()
            this.changeTaskStatus(projectId, stage.id, taskId, TaskStatusDto.IN_PROGRESS)
        then:
            afterDone.isEmpty()
            this.stageNotifications()*.itemId == [stage.id]
    }

    def "Unpaid installment past its due date should be overdue"() {
        given:
            def projectId = this.createProject(this.architectId)
        when:
            this.createStage(projectId, null, new InstallmentDto(value: 1000, dueDate: this.today.minusDays(1)))
        then:
            def notification = this.notifications(true).notifications.find { it.type == NotificationTypeDto.INSTALLMENT_OVERDUE }
            notification.dueDate == this.today.minusDays(1)
            notification.itemName == "Etap 01"
    }

    def "Marking notifications read and unread should change the count"() {
        given:
            def projectId = this.createProject(this.architectId)
            this.createEvent(projectId, null, this.today.plusDays(1))
            this.createEvent(projectId, null, this.today)
            def first = this.notifications(true).notifications[0]
        when:
            def readResponse = this.markRead(first.id, true)
            def afterRead = this.unreadCount()
            this.markRead(first.id, false)
            def afterUnread = this.unreadCount()
            def allResponse = this.request(MockMvcRequestBuilders.post("$HOST:$port$NOTIFICATIONS_URI/read-all"))
        then:
            readResponse.status == HttpStatus.OK.value()
            afterRead == 1
            afterUnread == 2
            allResponse.status == HttpStatus.OK.value()
            this.unreadCount() == 0
            this.notifications(false).notifications.size() == 2
            this.notifications(false).notifications.every { it.read }
    }

    def "Marking another user's notification should return code 404"() {
        given:
            def projectId = this.createProject(this.architectId)
            this.createEvent(projectId, null, this.today.plusDays(1))
            def notification = this.notifications(true).notifications[0]
            this.userRepository.save(new User("Other", "other@example.com", OTHER_AUTH_ID))
            authenticate(OTHER_AUTH_ID)
        when:
            def response = this.markRead(notification.id, true)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            authenticate(AUTH_ID)
            this.unreadCount() == 1
    }

    def "New user should get the default settings"() {
        when:
            def settings = this.settings()
        then:
            settings*.type == [NotificationTypeDto.MEETING, NotificationTypeDto.INSTALLMENT_PAYMENT,
                               NotificationTypeDto.CONTRACT_DEADLINE, NotificationTypeDto.STAGE_DEADLINE,
                               NotificationTypeDto.COMPANY_COST_PAYMENT]
            settings.find { it.type == NotificationTypeDto.MEETING }.daysBefore == [1]
            !settings.find { it.type == NotificationTypeDto.COMPANY_COST_PAYMENT }.enabled
    }

    def "Turning meetings off should remove their notifications, read ones too, and turning them on should bring them back"() {
        given:
            def projectId = this.createProject(this.architectId)
            this.createEvent(projectId, null, this.today.plusDays(1))
            this.markRead(this.notifications(true).notifications[0].id, true)
            def settings = this.settings()
        when:
            settings.find { it.type == NotificationTypeDto.MEETING }.enabled = false
            def offResponse = this.request(MockMvcRequestBuilders.put("$HOST:$port$SETTINGS_URI"), settings)
            def allWhenOff = this.notifications(false).notifications
            settings.find { it.type == NotificationTypeDto.MEETING }.enabled = true
            this.request(MockMvcRequestBuilders.put("$HOST:$port$SETTINGS_URI"), settings)
        then:
            offResponse.status == HttpStatus.OK.value()
            allWhenOff.isEmpty()
            this.unreadCount() == 1
            this.settings().find { it.type == NotificationTypeDto.MEETING }.enabled
    }

    def "Reaching a closer reminder should make the read notification unread again"() {
        given:
            def projectId = this.createProject(this.architectId)
            this.saveStageReminders([10])
            this.createStage(projectId, this.today.plusDays(1), null)
            this.markRead(this.notifications(true).notifications[0].id, true)
        when:
            def settings = this.saveStageReminders([10, 1, 1])
        then:
            settings.find { it.type == NotificationTypeDto.STAGE_DEADLINE }.daysBefore == [10, 1]
            def stageNotifications = this.notifications(false).notifications
                    .findAll { it.type == NotificationTypeDto.STAGE_DEADLINE }
            stageNotifications.size() == 1
            !stageNotifications[0].read
    }

    def "Saving reminders #daysBefore with the type #state should return code 400 and keep the settings"() {
        given:
            def settings = this.settings()
            def stageSetting = settings.find { it.type == NotificationTypeDto.STAGE_DEADLINE }
            stageSetting.daysBefore = daysBefore
            stageSetting.enabled = enabled
        when:
            def response = this.request(MockMvcRequestBuilders.put("$HOST:$port$SETTINGS_URI"), settings)
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.settings().find { it.type == NotificationTypeDto.STAGE_DEADLINE }.daysBefore == [3]
        where:
            daysBefore         | enabled
            [45]               | true
            [1, 2, 3, 4, 5, 6] | true
            []                 | true
            null               | false
            state = enabled ? "on" : "off"
    }

    def "Type turned off should keep no reminders"() {
        given:
            def settings = this.settings()
            def companyCostSetting = settings.find { it.type == NotificationTypeDto.COMPANY_COST_PAYMENT }
            companyCostSetting.daysBefore = []
        when:
            def response = this.request(MockMvcRequestBuilders.put("$HOST:$port$SETTINGS_URI"), settings)
        then:
            response.status == HttpStatus.OK.value()
            this.settings().find { it.type == NotificationTypeDto.COMPANY_COST_PAYMENT }.daysBefore == []
    }

    private List<NotificationSettingDto> saveStageReminders(List<Integer> daysBefore) {
        def settings = this.settings()
        settings.find { it.type == NotificationTypeDto.STAGE_DEADLINE }.daysBefore = daysBefore
        def response = this.request(MockMvcRequestBuilders.put("$HOST:$port$SETTINGS_URI"), settings)
        assert response.status == HttpStatus.OK.value()
        return MAPPER.readValue(response.contentAsString,
                MAPPER.getTypeFactory().constructCollectionType(List, NotificationSettingDto))
    }

    private Long createArchitect() {
        def id = TestsHelper.createArchitect(this.architectDto, "$HOST:$port$ARCHITECTS_URI", this.mockMvc).id
        this.architectIds.add(0, id)
        return id
    }

    private Long createProject(Long architectId) {
        def clientId = TestsHelper.createClient(this.clientDto, "$HOST:$port$CLIENTS_URI", this.mockMvc).id
        this.clientIds << clientId
        this.projectCreateDto.typeId = this.projectTypeId
        this.projectCreateDto.architectId = architectId
        this.projectCreateDto.clientId = clientId
        def id = TestsHelper.createProject(this.projectCreateDto, "$HOST:$port$PROJECTS_URI", this.mockMvc).id
        this.projectIds << id
        return id
    }

    private StageDto createStage(Long projectId, LocalDate deadline, InstallmentDto installment) {
        def stageDto = this.readJson('json/stage/properStage.json', StageDto)
        stageDto.typeId = this.stageTypeId
        stageDto.architectId = this.architectId
        stageDto.deadline = deadline
        stageDto.installment = installment
        def response = this.request(MockMvcRequestBuilders.post("$HOST:$port$PROJECTS_URI/$projectId/stages"), stageDto)
        assert response.status == HttpStatus.CREATED.value()
        return MAPPER.readValue(response.contentAsString, StageDto)
    }

    // Work on a project starts once its contract offer is accepted
    private void acceptOffer(Long projectId) {
        def project = MAPPER.readValue(this.request(MockMvcRequestBuilders.get("$HOST:$port$PROJECTS_URI/$projectId"))
                .contentAsString, ProjectDto)
        def response = this.request(MockMvcRequestBuilders.post("$HOST:$port$CONTRACTS_URI/${project.contract.id}/status"),
                new ContractDto(status: ContractStatusDto.ACCEPTED))
        assert response.status == HttpStatus.OK.value()
    }

    private Long createTask(Long projectId, Long stageId) {
        def taskDto = this.readJson('json/task/properTask.json', TaskDto)
        taskDto.typeId = this.taskTypeId
        taskDto.architectId = this.architectId
        def response = this.request(MockMvcRequestBuilders.post("$HOST:$port$PROJECTS_URI/$projectId/stages/$stageId/tasks"),
                taskDto)
        assert response.status == HttpStatus.CREATED.value()
        return MAPPER.readValue(response.contentAsString, TaskDto).id
    }

    private void changeTaskStatus(Long projectId, Long stageId, Long taskId, TaskStatusDto status) {
        def response = this.request(MockMvcRequestBuilders.post(
                "$HOST:$port$PROJECTS_URI/$projectId/stages/$stageId/tasks/$taskId/status"), new TaskDto(status: status))
        assert response.status == HttpStatus.OK.value()
    }

    private List stageNotifications() {
        return this.notifications(false).notifications.findAll { it.type == NotificationTypeDto.STAGE_DEADLINE }
    }

    private Long createEvent(Long projectId, Long architectId, LocalDate day) {
        def dto = new CalendarEventDto(name: "Site visit", startDateTime: day.atTime(23, 0).toString(),
                endDateTime: day.atTime(23, 30).toString(), projectId: projectId, architectId: architectId)
        def response = this.request(MockMvcRequestBuilders.post("$HOST:$port$EVENTS_URI"), dto)
        assert response.status == HttpStatus.CREATED.value()
        def id = MAPPER.readValue(response.contentAsString, CalendarEventDto).id
        this.eventIds << id
        return id
    }

    private long unreadCount() {
        def response = this.request(MockMvcRequestBuilders.get("$HOST:$port$NOTIFICATIONS_URI/count"))
        return MAPPER.readValue(response.contentAsString, NotificationCountDto).unread
    }

    private NotificationsDto notifications(boolean unread) {
        def response = this.request(MockMvcRequestBuilders.get("$HOST:$port$NOTIFICATIONS_URI?unread=$unread"))
        assert response.status == HttpStatus.OK.value()
        return MAPPER.readValue(response.contentAsString, NotificationsDto)
    }

    private List<NotificationSettingDto> settings() {
        def response = this.request(MockMvcRequestBuilders.get("$HOST:$port$SETTINGS_URI"))
        return MAPPER.readValue(response.contentAsString,
                MAPPER.getTypeFactory().constructCollectionType(List, NotificationSettingDto))
    }

    private MockHttpServletResponse markRead(Long notificationId, boolean read) {
        return this.request(MockMvcRequestBuilders.patch("$HOST:$port$NOTIFICATIONS_URI/$notificationId"),
                new NotificationReadDto(read: read))
    }

    private MockHttpServletResponse request(MockHttpServletRequestBuilder builder, Object body = null) {
        if (body != null) {
            builder.contentType("application/json").content(MAPPER.writeValueAsString(body))
        }
        return this.mockMvc.perform(builder).andReturn().response
    }

    private <T> T readJson(String fileName, Class<T> valueType) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(fileName).file), valueType)
    }
}
