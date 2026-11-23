package com.arturjarosz.task.notification.query.impl;

import com.arturjarosz.task.calendar.model.CalendarEvent;
import com.arturjarosz.task.calendar.model.CalendarEventSyncStatus;
import com.arturjarosz.task.calendar.model.QCalendarEvent;
import com.arturjarosz.task.companycost.domain.ChargeSchedule;
import com.arturjarosz.task.companycost.model.CompanyCost;
import com.arturjarosz.task.companycost.model.QCompanyCost;
import com.arturjarosz.task.contract.model.QContract;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.finance.model.QInstallment;
import com.arturjarosz.task.finance.model.QProjectFinancialData;
import com.arturjarosz.task.notification.domain.DueItem;
import com.arturjarosz.task.notification.domain.ItemDueDates;
import com.arturjarosz.task.notification.domain.ItemRef;
import com.arturjarosz.task.notification.model.NotificationItemType;
import com.arturjarosz.task.notification.model.NotificationType;
import com.arturjarosz.task.notification.model.QNotification;
import com.arturjarosz.task.notification.query.DueItemQueryService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.project.model.QStage;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Finder
public class DueItemQueryServiceImpl extends AbstractQueryService<QNotification> implements DueItemQueryService {

    private static final QCalendarEvent EVENT = QCalendarEvent.calendarEvent;
    private static final QCompanyCost COMPANY_COST = QCompanyCost.companyCost;
    private static final QContract CONTRACT = QContract.contract;
    private static final QInstallment INSTALLMENT = QInstallment.installment;
    private static final QNotification NOTIFICATION = QNotification.notification;
    private static final QProject PROJECT = QProject.project;
    private static final QProjectFinancialData PROJECT_FINANCIAL_DATA = QProjectFinancialData.projectFinancialData;
    private static final QStage STAGE = QStage.stage;

    // Work on a finished or rejected project is no longer due, though its unpaid installments still are
    private static final Set<ProjectStatus> CLOSED_PROJECT_STATUSES = Set.of(ProjectStatus.COMPLETED,
            ProjectStatus.REJECTED);
    private static final Set<ContractStatus> OPEN_CONTRACT_STATUSES = Set.of(ContractStatus.OFFER,
            ContractStatus.ACCEPTED, ContractStatus.SIGNED);
    private static final Set<StageStatus> OPEN_STAGE_STATUSES = Set.of(StageStatus.TO_DO, StageStatus.IN_PROGRESS);

    public DueItemQueryServiceImpl() {
        super(NOTIFICATION);
    }

    @Override
    public ItemDueDates getDueDates(ItemRef item, LocalDate today, LocalDateTime now, LocalDate since) {
        return switch (item.itemType()) {
            case CALENDAR_EVENT -> this.getMeetingDueDates(item.itemId(), now);
            case INSTALLMENT -> this.getInstallmentDueDates(item.itemId(), today);
            case CONTRACT -> this.getContractDueDates(item.itemId());
            case STAGE -> this.getStageDueDates(item.itemId());
            case COMPANY_COST -> this.getCompanyCostDueDates(item.itemId(), today, since);
        };
    }

    @Override
    public Set<ItemRef> getItemsToCheck(Set<NotificationItemType> itemTypes, LocalDate today, LocalDateTime now) {
        var lastDueDate = today.plusDays(NotificationType.MAX_DAYS_BEFORE);
        var items = new HashSet<ItemRef>();
        if (itemTypes.contains(NotificationItemType.CALENDAR_EVENT)) {
            this.addItems(items, NotificationItemType.CALENDAR_EVENT, this.query().select(EVENT.id).from(EVENT)
                    .where(EVENT.syncStatus.ne(CalendarEventSyncStatus.DELETE_PENDING), EVENT.endDateTime.after(now),
                            EVENT.startDateTime.before(lastDueDate.plusDays(1).atStartOfDay()))
                    .fetch());
        }
        if (itemTypes.contains(NotificationItemType.INSTALLMENT)) {
            this.addItems(items, NotificationItemType.INSTALLMENT, this.query().select(INSTALLMENT.id).from(INSTALLMENT)
                    .where(INSTALLMENT.dueDate.loe(lastDueDate), INSTALLMENT.financialData.paid.isFalse())
                    .fetch());
        }
        if (itemTypes.contains(NotificationItemType.CONTRACT)) {
            this.addItems(items, NotificationItemType.CONTRACT, this.query().select(CONTRACT.id).from(CONTRACT)
                    .where(CONTRACT.deadline.between(today, lastDueDate)).fetch());
        }
        if (itemTypes.contains(NotificationItemType.STAGE)) {
            this.addItems(items, NotificationItemType.STAGE, this.query().select(STAGE.id).from(STAGE)
                    .where(STAGE.deadline.between(today, lastDueDate), STAGE.status.in(OPEN_STAGE_STATUSES)).fetch());
        }
        // Charges aren't stored, they come from each cost's schedule, and a company has few costs
        if (itemTypes.contains(NotificationItemType.COMPANY_COST)) {
            this.addItems(items, NotificationItemType.COMPANY_COST,
                    this.query().select(COMPANY_COST.id).from(COMPANY_COST).fetch());
        }
        this.query().select(NOTIFICATION.itemType, NOTIFICATION.itemId).distinct().from(NOTIFICATION)
                .where(NOTIFICATION.itemType.in(itemTypes)).fetch()
                .forEach(row -> items.add(new ItemRef(row.get(NOTIFICATION.itemType), row.get(NOTIFICATION.itemId))));
        return items;
    }

    @Override
    public Set<ItemRef> getItemsOfProject(long projectId) {
        var items = new HashSet<ItemRef>();
        var project = this.query().selectFrom(PROJECT).where(PROJECT.id.eq(projectId)).fetchOne();
        if (project != null) {
            this.addProjectItems(items, project);
        }
        this.query().select(NOTIFICATION.itemType, NOTIFICATION.itemId).distinct().from(NOTIFICATION)
                .where(NOTIFICATION.projectId.eq(projectId)).fetch()
                .forEach(row -> items.add(new ItemRef(row.get(NOTIFICATION.itemType), row.get(NOTIFICATION.itemId))));
        return items;
    }

    @Override
    public Set<ItemRef> getItemsOfArchitect(long architectId) {
        var items = new HashSet<ItemRef>();
        this.query().selectFrom(PROJECT).where(PROJECT.architectId.eq(architectId)).fetch()
                .forEach(project -> this.addProjectItems(items, project));
        this.addItems(items, NotificationItemType.STAGE,
                this.query().select(STAGE.id).from(STAGE).where(STAGE.architectId.eq(architectId)).fetch());
        this.addItems(items, NotificationItemType.CALENDAR_EVENT,
                this.query().select(EVENT.id).from(EVENT).where(EVENT.architectId.eq(architectId)).fetch());
        return items;
    }

    private void addProjectItems(Set<ItemRef> items, Project project) {
        items.add(new ItemRef(NotificationItemType.CONTRACT, project.getContractId()));
        project.getStages().forEach(stage -> items.add(new ItemRef(NotificationItemType.STAGE, stage.getId())));
        this.addItems(items, NotificationItemType.INSTALLMENT, this.query().select(INSTALLMENT.id)
                .from(PROJECT_FINANCIAL_DATA).join(PROJECT_FINANCIAL_DATA.installments, INSTALLMENT)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(project.getId())).fetch());
        this.addItems(items, NotificationItemType.CALENDAR_EVENT,
                this.query().select(EVENT.id).from(EVENT).where(EVENT.projectId.eq(project.getId())).fetch());
    }

    private ItemDueDates getMeetingDueDates(long eventId, LocalDateTime now) {
        var event = this.query().selectFrom(EVENT).where(EVENT.id.eq(eventId)).fetchOne();
        if (event == null || event.getSyncStatus() == CalendarEventSyncStatus.DELETE_PENDING
                || !event.getEndDateTime().isAfter(now)) {
            return ItemDueDates.itemGone();
        }
        var project = event.getProjectId() == null ? null : this.findProject(event.getProjectId());
        // A meeting with a project is for the project's architect; one with neither is for everyone
        var architectId = project != null ? project.getArchitectId() : event.getArchitectId();
        return ItemDueDates.of(List.of(new DueItem(NotificationType.MEETING, NotificationItemType.CALENDAR_EVENT,
                eventId, event.getProjectId(), null, event.getStartDateTime().toLocalDate(), meetingTime(event),
                event.getName(), project != null ? project.getName() : null, architectId, architectId == null)));
    }

    private ItemDueDates getInstallmentDueDates(long installmentId, LocalDate today) {
        var installment = this.query().selectFrom(INSTALLMENT).where(INSTALLMENT.id.eq(installmentId)).fetchOne();
        if (installment == null) {
            return ItemDueDates.itemGone();
        }
        if (installment.isPaid() || installment.getDueDate() == null) {
            return ItemDueDates.of(List.of());
        }
        var projectId = this.query().select(PROJECT_FINANCIAL_DATA.projectId).from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.installments, INSTALLMENT).where(INSTALLMENT.id.eq(installmentId))
                .fetchOne();
        var project = this.findProject(projectId);
        if (project == null) {
            return ItemDueDates.itemGone();
        }
        // A deposit has no stage, so it has no name
        var stageName = installment.getStageId() == null ? null : this.query().select(STAGE.name).from(STAGE)
                .where(STAGE.id.eq(installment.getStageId())).fetchOne();
        var type = today.isAfter(installment.getDueDate()) ? NotificationType.INSTALLMENT_OVERDUE
                : NotificationType.INSTALLMENT_PAYMENT;
        return ItemDueDates.of(List.of(new DueItem(type, NotificationItemType.INSTALLMENT, installmentId,
                projectId, installment.getStageId(), installment.getDueDate(), null, stageName, project.getName(),
                project.getArchitectId(), false)));
    }

    private ItemDueDates getContractDueDates(long contractId) {
        var contract = this.query().selectFrom(CONTRACT).where(CONTRACT.id.eq(contractId)).fetchOne();
        var project = this.query().selectFrom(PROJECT).where(PROJECT.contractId.eq(contractId)).fetchOne();
        if (contract == null || project == null) {
            return ItemDueDates.itemGone();
        }
        if (contract.getDeadline() == null || !OPEN_CONTRACT_STATUSES.contains(contract.getStatus())
                || isClosed(project)) {
            return ItemDueDates.of(List.of());
        }
        return ItemDueDates.of(List.of(new DueItem(NotificationType.CONTRACT_DEADLINE, NotificationItemType.CONTRACT,
                contractId, project.getId(), null, contract.getDeadline(), null, null, project.getName(),
                project.getArchitectId(), false)));
    }

    private ItemDueDates getStageDueDates(long stageId) {
        var project = this.findProjectOfStage(stageId);
        if (project == null) {
            return ItemDueDates.itemGone();
        }
        var stage = findStage(project.getStages(), stageId);
        if (stage.getDeadline() == null || !OPEN_STAGE_STATUSES.contains(stage.getStatus()) || isClosed(project)) {
            return ItemDueDates.of(List.of());
        }
        var architectId = stage.getArchitectId() != null ? stage.getArchitectId() : project.getArchitectId();
        return ItemDueDates.of(List.of(new DueItem(NotificationType.STAGE_DEADLINE, NotificationItemType.STAGE,
                stageId, project.getId(), stageId, stage.getDeadline(), null, stage.getName(), project.getName(),
                architectId, false)));
    }

    private ItemDueDates getCompanyCostDueDates(long companyCostId, LocalDate today, LocalDate since) {
        var companyCost = this.query().selectFrom(COMPANY_COST).where(COMPANY_COST.id.eq(companyCostId)).fetchOne();
        if (companyCost == null) {
            return ItemDueDates.itemGone();
        }
        var schedule = ChargeSchedule.of(companyCost);
        var lastDueDate = today.plusDays(NotificationType.MAX_DAYS_BEFORE);
        var lastMonth = CompanyCost.monthOf(lastDueDate);
        // A company cost is not about a project, so everyone who wants to know about these hears about them
        var dueItems = schedule.chargeMonths()
                .takeWhile(month -> !month.isAfter(lastMonth))
                .filter(month -> {
                    var chargeDate = schedule.chargeDate(month);
                    return !chargeDate.isBefore(since) && !chargeDate.isAfter(lastDueDate);
                })
                .filter(month -> companyCost.findPaymentOfCharge(month).isEmpty())
                .map(month -> new DueItem(NotificationType.COMPANY_COST_PAYMENT, NotificationItemType.COMPANY_COST,
                        companyCostId, null, null, schedule.chargeDate(month), null, companyCost.getName(), null,
                        null, true))
                .toList();
        return ItemDueDates.of(dueItems);
    }

    private Project findProject(Long projectId) {
        return projectId == null ? null : this.query().selectFrom(PROJECT).where(PROJECT.id.eq(projectId)).fetchOne();
    }

    private Project findProjectOfStage(long stageId) {
        return this.query().select(PROJECT).from(PROJECT).join(PROJECT.stages, STAGE).where(STAGE.id.eq(stageId))
                .fetchOne();
    }

    private static Stage findStage(Collection<Stage> stages, long stageId) {
        return stages.stream().filter(stage -> stage.getId() == stageId).findFirst().orElseThrow();
    }

    private static boolean isClosed(Project project) {
        return CLOSED_PROJECT_STATUSES.contains(project.getStatus());
    }

    // The hour is shown only for a meeting that isn't all day
    private static LocalDateTime meetingTime(CalendarEvent event) {
        return event.isAllDay() ? null : event.getStartDateTime();
    }

    private void addItems(Set<ItemRef> items, NotificationItemType itemType, List<Long> ids) {
        ids.forEach(id -> items.add(new ItemRef(itemType, id)));
    }
}
