package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.dto.HistoryActionDto;
import com.arturjarosz.task.dto.HistoryChangeDto;
import com.arturjarosz.task.dto.HistoryEntryDto;
import com.arturjarosz.task.dto.HistoryFieldDto;
import com.arturjarosz.task.dto.HistoryRecordTypeDto;
import com.arturjarosz.task.dto.UserSummaryDto;
import com.arturjarosz.task.project.application.ProjectHistoryApplicationService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository;
import com.arturjarosz.task.project.infrastructure.repositor.StageTypeRepository;
import com.arturjarosz.task.project.infrastructure.repositor.TaskTypeRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.query.ProjectHistoryQueryService;
import com.arturjarosz.task.project.query.RecordRevision;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.namedentry.AbstractNamedEntry;
import com.arturjarosz.task.user.application.mapper.UserMapper;
import com.arturjarosz.task.user.infrastructure.repository.UserRepository;
import com.arturjarosz.task.user.model.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ProjectHistoryApplicationServiceImpl implements ProjectHistoryApplicationService {

    private static final List<TrackedField<Contract>> CONTRACT_FIELDS = List.of(
            new TrackedField<>(HistoryFieldDto.OFFER_VALUE,
                    contract -> moneyValue(contract.getOfferValue().getValue())),
            new TrackedField<>(HistoryFieldDto.STATUS, contract -> contract.getStatus().name()),
            new TrackedField<>(HistoryFieldDto.SIGNING_DATE, contract -> dateValue(contract.getSigningDate())),
            new TrackedField<>(HistoryFieldDto.DEADLINE, contract -> dateValue(contract.getDeadline())),
            new TrackedField<>(HistoryFieldDto.START_DATE, contract -> dateValue(contract.getStartDate())),
            new TrackedField<>(HistoryFieldDto.END_DATE, contract -> dateValue(contract.getEndDate())));

    @NonNull
    private final ProjectRepository projectRepository;
    @NonNull
    private final ProjectValidator projectValidator;
    @NonNull
    private final ProjectHistoryQueryService projectHistoryQueryService;
    @NonNull
    private final ArchitectRepository architectRepository;
    @NonNull
    private final ProjectTypeRepository projectTypeRepository;
    @NonNull
    private final StageTypeRepository stageTypeRepository;
    @NonNull
    private final TaskTypeRepository taskTypeRepository;
    @NonNull
    private final UserRepository userRepository;
    @NonNull
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public List<HistoryEntryDto> getProjectHistory(Long projectId, Long stageId, Long taskId) {
        LOG.info("Loading history of Project with id {}.", projectId);

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);

        var projectStageIds = this.projectHistoryQueryService.getStageIdsOfProject(projectId);
        var stageIdsByTaskId = this.projectHistoryQueryService.getStageIdsByTaskId(projectStageIds).entrySet()
                .stream()
                .filter(task -> (stageId == null || task.getValue().equals(stageId))
                        && (taskId == null || task.getKey().equals(taskId)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // the history of a task leaves out its stage, which is read only for its name
        var shownStageIds = projectStageIds.stream()
                .filter(id -> taskId == null && (stageId == null || id.equals(stageId)))
                .collect(Collectors.toSet());
        var stageRevisions = this.projectHistoryQueryService.getRevisions(Stage.class,
                Stream.concat(shownStageIds.stream(), stageIdsByTaskId.values().stream()).collect(Collectors.toSet()));
        var stageNames = latestNames(stageRevisions, Stage::getName);
        var architectNames = this.architectRepository.findAll().stream()
                .collect(Collectors.toMap(Architect::getId, ProjectHistoryApplicationServiceImpl::fullName));

        var taskEntries = this.entriesOf(HistoryRecordTypeDto.TASK,
                this.projectHistoryQueryService.getRevisions(Task.class, stageIdsByTaskId.keySet()),
                taskFields(architectNames, namesById(this.taskTypeRepository.findAll())), Task::getName);
        taskEntries.forEach(revisionEntry -> {
            var taskStageId = stageIdsByTaskId.get(revisionEntry.entry().getRecordId());
            revisionEntry.entry().stageId(taskStageId).stageName(stageNames.get(taskStageId));
        });
        var stageEntries = this.entriesOf(HistoryRecordTypeDto.STAGE, stageRevisions.stream()
                        .filter(revision -> shownStageIds.contains(revision.snapshot().getId()))
                        .toList(),
                stageFields(architectNames, namesById(this.stageTypeRepository.findAll())), Stage::getName);
        var revisionEntries = Stream.of(
                        stageId == null && taskId == null ? this.projectAndContractEntries(project, architectNames)
                                : List.<RevisionEntry>of(),
                        stageEntries,
                        taskEntries)
                .flatMap(List::stream)
                .toList();
        var authors = this.authorsByAuthId(revisionEntries);

        LOG.info("History of Project with id {} loaded.", projectId);
        // a change to a project and its parts in one go is one revision; the project comes first, the tasks last
        return revisionEntries.stream()
                .sorted(Comparator.comparingInt(RevisionEntry::revision).reversed()
                        .thenComparing(revisionEntry -> revisionEntry.entry().getRecordType()))
                .map(revisionEntry -> revisionEntry.entry().author(authors.get(revisionEntry.authorAuthId())))
                .toList();
    }

    private List<RevisionEntry> projectAndContractEntries(Project project, Map<Long, String> architectNames) {
        return Stream.concat(
                        this.entriesOf(HistoryRecordTypeDto.PROJECT,
                                this.projectHistoryQueryService.getRevisions(Project.class, List.of(project.getId())),
                                projectFields(architectNames, namesById(this.projectTypeRepository.findAll())),
                                Project::getName).stream(),
                        this.entriesOf(HistoryRecordTypeDto.CONTRACT,
                                this.projectHistoryQueryService.getRevisions(Contract.class,
                                        List.of(project.getContractId())), CONTRACT_FIELDS, _ -> null).stream())
                .toList();
    }

    /**
     * Turns the revisions of records of one type, oldest first, into history entries. A change that leaves every
     * tracked field as it was, such as adding a stage to a project, gives no entry, and neither does a change to a
     * record created before its history began, as there is nothing to compare it with. The contract is created with
     * its project, so its creation gives no entry either.
     */
    private <T extends AbstractEntity> List<RevisionEntry> entriesOf(HistoryRecordTypeDto recordType,
            List<RecordRevision<T>> revisions, List<TrackedField<T>> fields, Function<T, String> recordName) {
        Map<Long, T> previousSnapshots = new HashMap<>();
        var revisionEntries = new ArrayList<RevisionEntry>();
        for (var revision : revisions) {
            var previous = previousSnapshots.put(revision.snapshot().getId(), revision.snapshot());
            var entry = switch (revision.type()) {
                case ADD -> recordType == HistoryRecordTypeDto.CONTRACT ? null : new HistoryEntryDto()
                        .action(HistoryActionDto.CREATED);
                case MOD -> changedEntry(previous, revision.snapshot(), fields);
                case DEL -> new HistoryEntryDto().action(HistoryActionDto.REMOVED);
            };
            if (entry != null) {
                entry.dateTime(revision.dateTime().toString())
                        .recordType(recordType)
                        .recordId(revision.snapshot().getId())
                        .recordName(recordName.apply(revision.snapshot()));
                revisionEntries.add(new RevisionEntry(revision.revision(), revision.authorAuthId(), entry));
            }
        }
        return revisionEntries;
    }

    private static <T> HistoryEntryDto changedEntry(T previous, T current, List<TrackedField<T>> fields) {
        if (previous == null) {
            return null;
        }
        var changes = fields.stream()
                .map(field -> new HistoryChangeDto()
                        .field(field.field())
                        .oldValue(field.value().apply(previous))
                        .newValue(field.value().apply(current)))
                .filter(change -> !Objects.equals(change.getOldValue(), change.getNewValue()))
                .toList();
        return changes.isEmpty() ? null : new HistoryEntryDto().action(HistoryActionDto.CHANGED).changes(changes);
    }

    private Map<String, UserSummaryDto> authorsByAuthId(List<RevisionEntry> revisionEntries) {
        var authIds = revisionEntries.stream()
                .map(RevisionEntry::authorAuthId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return this.userRepository.findByAuthIdIn(authIds).stream()
                .collect(Collectors.toMap(User::getAuthId, this.userMapper::mapToSummaryDto));
    }

    private static <T extends AbstractEntity> Map<Long, String> latestNames(List<RecordRevision<T>> revisions,
            Function<T, String> name) {
        // revisions are oldest first, so the last name put stays
        Map<Long, String> names = new HashMap<>();
        revisions.forEach(revision -> names.put(revision.snapshot().getId(), name.apply(revision.snapshot())));
        return names;
    }

    private static Map<Long, String> namesById(List<? extends AbstractNamedEntry> namedEntries) {
        return namedEntries.stream().collect(Collectors.toMap(AbstractEntity::getId, AbstractNamedEntry::getName));
    }

    private static String fullName(Architect architect) {
        return architect.getPersonName().getFirstName() + " " + architect.getPersonName().getLastName();
    }

    private static List<TrackedField<Project>> projectFields(Map<Long, String> architectNames,
            Map<Long, String> typeNames) {
        return List.of(
                new TrackedField<>(HistoryFieldDto.NAME, Project::getName),
                new TrackedField<>(HistoryFieldDto.TYPE, project -> typeNames.get(project.getProjectTypeId())),
                new TrackedField<>(HistoryFieldDto.ARCHITECT,
                        project -> architectNames.get(project.getArchitectId())),
                new TrackedField<>(HistoryFieldDto.STATUS, project -> project.getStatus().name()),
                new TrackedField<>(HistoryFieldDto.START_DATE, project -> dateValue(project.getStartDate())),
                new TrackedField<>(HistoryFieldDto.END_DATE, project -> dateValue(project.getEndDate())),
                new TrackedField<>(HistoryFieldDto.NOTE, Project::getNote));
    }

    private static List<TrackedField<Stage>> stageFields(Map<Long, String> architectNames,
            Map<Long, String> typeNames) {
        return List.of(
                new TrackedField<>(HistoryFieldDto.NAME, Stage::getName),
                new TrackedField<>(HistoryFieldDto.TYPE, stage -> typeNames.get(stage.getStageTypeId())),
                new TrackedField<>(HistoryFieldDto.ARCHITECT, stage -> architectNames.get(stage.getArchitectId())),
                new TrackedField<>(HistoryFieldDto.STATUS, stage -> stage.getStatus().name()),
                new TrackedField<>(HistoryFieldDto.START_DATE, stage -> dateValue(stage.getStartDate())),
                new TrackedField<>(HistoryFieldDto.END_DATE, stage -> dateValue(stage.getEndDate())),
                new TrackedField<>(HistoryFieldDto.DEADLINE, stage -> dateValue(stage.getDeadline())),
                new TrackedField<>(HistoryFieldDto.NOTE, Stage::getNote));
    }

    private static List<TrackedField<Task>> taskFields(Map<Long, String> architectNames,
            Map<Long, String> typeNames) {
        return List.of(
                new TrackedField<>(HistoryFieldDto.NAME, Task::getName),
                new TrackedField<>(HistoryFieldDto.TYPE, task -> typeNames.get(task.getTaskTypeId())),
                new TrackedField<>(HistoryFieldDto.ARCHITECT, task -> architectNames.get(task.getArchitectId())),
                new TrackedField<>(HistoryFieldDto.STATUS, task -> task.getStatus().name()),
                new TrackedField<>(HistoryFieldDto.START_DATE, task -> dateValue(task.getStartDate())),
                new TrackedField<>(HistoryFieldDto.END_DATE, task -> dateValue(task.getEndDate())),
                new TrackedField<>(HistoryFieldDto.NOTE, Task::getNote));
    }

    private static String dateValue(LocalDate date) {
        return date == null ? null : date.toString();
    }

    // stripped of trailing zeros, so the same amount read with another scale is not a change
    private static String moneyValue(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }

    /**
     * A field shown in the history, read from a snapshot of a record as the text the history shows.
     */
    private record TrackedField<T>(HistoryFieldDto field, Function<T, String> value) {
    }

    // the revision orders entries and the author is resolved once for all of them
    private record RevisionEntry(int revision, String authorAuthId, HistoryEntryDto entry) {
    }
}
