package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.client.infrastructure.repository.ClientRepository;
import com.arturjarosz.task.contract.application.ContractService;
import com.arturjarosz.task.contractor.infrastructure.ContractorRepository;
import com.arturjarosz.task.contractor.model.Contractor;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.data.initializer.SampleDateUpdater;
import com.arturjarosz.task.data.model.ContractorJobSample;
import com.arturjarosz.task.data.model.CostSample;
import com.arturjarosz.task.data.model.FinanceItemSample;
import com.arturjarosz.task.data.model.InstallmentSample;
import com.arturjarosz.task.data.model.ProjectSample;
import com.arturjarosz.task.data.model.StageSample;
import com.arturjarosz.task.data.model.SupplySample;
import com.arturjarosz.task.data.model.TaskSample;
import com.arturjarosz.task.dto.ContractDto;
import com.arturjarosz.task.dto.ContractStatusDto;
import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.dto.TaskStatusDto;
import com.arturjarosz.task.dto.UpdateStatusRequestDto;
import com.arturjarosz.task.exception.SampleDataInitializingException;
import com.arturjarosz.task.finance.application.ContractorJobApplicationService;
import com.arturjarosz.task.finance.application.CostApplicationService;
import com.arturjarosz.task.finance.application.InstallmentApplicationService;
import com.arturjarosz.task.finance.application.SupplyApplicationService;
import com.arturjarosz.task.project.application.ProjectApplicationService;
import com.arturjarosz.task.project.application.StageApplicationService;
import com.arturjarosz.task.project.application.TaskApplicationService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository;
import com.arturjarosz.task.project.model.ProjectType;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.supplier.infrastructure.SupplierRepository;
import com.arturjarosz.task.supplier.model.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Loads sample Projects with Stages, Tasks and financial data. Objects are created through application services, and
 * statuses are reached by regular status transitions, so all validators and status listeners are applied. Has to be
 * run after architects, clients, contractors, suppliers and project types are loaded. Dates that can't be set through
 * application services (passed deadlines, start and end dates of work, creation time) are changed by {@link SampleDateUpdater}.
 */
@Slf4j
@Component
@Order(8)
@RequiredArgsConstructor
public class ProjectDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/projectsSample.json";
    private static final Map<TaskStatusDto, List<TaskStatusDto>> TASK_STATUS_PATHS = Map.of(TaskStatusDto.TO_DO,
            List.of(), TaskStatusDto.IN_PROGRESS, List.of(TaskStatusDto.IN_PROGRESS), TaskStatusDto.DONE,
            List.of(TaskStatusDto.IN_PROGRESS, TaskStatusDto.DONE), TaskStatusDto.REJECTED,
            List.of(TaskStatusDto.REJECTED));
    private static final Set<ContractStatusDto> SIGNED_CONTRACT_STATUSES = Set.of(ContractStatusDto.SIGNED,
            ContractStatusDto.TERMINATED, ContractStatusDto.COMPLETED);

    // repositories are used for lookups, as list methods of application services also calculate projects data
    private final ArchitectRepository architectRepository;
    private final ClientRepository clientRepository;
    private final ContractorRepository contractorRepository;
    private final SupplierRepository supplierRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectApplicationService projectApplicationService;
    private final StageApplicationService stageApplicationService;
    private final TaskApplicationService taskApplicationService;
    private final ContractService contractService;
    private final InstallmentApplicationService installmentApplicationService;
    private final CostApplicationService costApplicationService;
    private final ContractorJobApplicationService contractorJobApplicationService;
    private final SupplyApplicationService supplyApplicationService;
    private final SampleDateUpdater sampleDateUpdater;

    @Override
    public void initializeData() {
        LOG.info("Starting importing projects.");
        var referencedIds = this.loadReferencedIds();
        this.prepareProjects().forEach(projectSample -> this.importProject(projectSample, referencedIds));
        LOG.info("Projects added to the database.");
    }

    private ReferencedIds loadReferencedIds() {
        return new ReferencedIds(idsByKey(this.architectRepository.findAll(),
                architect -> architect.getPersonName().getFirstName() + " " + architect.getPersonName()
                        .getLastName()),
                idsByKey(this.clientRepository.findAll(),
                        client -> client.getEmail() == null ? null : client.getEmail().getValue()),
                idsByKey(this.contractorRepository.findAll(), Contractor::getName),
                idsByKey(this.supplierRepository.findAll(), Supplier::getName),
                idsByKey(this.projectTypeRepository.findAll(), ProjectType::getName));
    }

    /**
     * Objects without a key are skipped. When the key is not unique (e.g. data loaded more than once), the object
     * with the lowest id is used.
     */
    private static <T extends AbstractEntity> Map<String, Long> idsByKey(List<T> objects,
            Function<T, String> keyMapper) {
        return objects.stream()
                .filter(object -> keyMapper.apply(object) != null)
                .collect(Collectors.toMap(keyMapper, AbstractEntity::getId, Math::min));
    }

    private void importProject(ProjectSample projectSample, ReferencedIds referencedIds) {
        LOG.info("Importing project {}.", projectSample.getName());
        var architectId = getId(referencedIds.architectIdsByName(), projectSample.getArchitect(), "architect");
        var projectCreateDto = new ProjectCreateDto().name(projectSample.getName())
                .typeId(getId(referencedIds.projectTypeIdsByName(), projectSample.getType(), "project type"))
                .architectId(architectId)
                .clientId(getId(referencedIds.clientIdsByEmail(), projectSample.getClientEmail(), "client"))
                .offerValue(projectSample.getOfferValue())
                .deadline(validContractDeadline(projectSample));
        var projectDto = this.projectApplicationService.createProject(projectCreateDto);
        var projectId = projectDto.getId();

        // Stages and tasks are created first, as creating work objects is allowed in all non-final contract statuses.
        var stageIds = new ArrayList<Long>();
        var taskIds = new IdentityHashMap<TaskSample, Long>();
        for (StageSample stageSample : projectSample.getStages()) {
            stageIds.add(this.createStage(projectId, architectId, stageSample, taskIds));
        }
        projectSample.getCosts().forEach(cost -> this.createCost(projectId, cost));
        projectSample.getContractorJobs().forEach(job -> this.createContractorJob(projectId, job, referencedIds));
        projectSample.getSupplies().forEach(supply -> this.createSupply(projectId, supply, referencedIds));

        // Contract has to allow working before work statuses are changed, final contract statuses are set after that.
        var contractId = projectDto.getContract().getId();
        this.moveContractToWorkingStatus(contractId, projectSample);
        this.changeWorkStatuses(projectId, projectSample.getStages(), stageIds, taskIds);
        this.moveContractToFinalStatus(contractId, projectSample);
        if (projectSample.isRejected()) {
            this.projectApplicationService.rejectProject(projectId);
        }
        this.changeDates(projectId, contractId, projectSample, taskIds);
    }

    private void changeDates(Long projectId, Long contractId, ProjectSample projectSample,
            Map<TaskSample, Long> taskIds) {
        var startDateByTaskId = new HashMap<Long, LocalDate>();
        var endDateByTaskId = new HashMap<Long, LocalDate>();
        taskIds.forEach((taskSample, taskId) -> {
            var startDate = daysAgo(taskSample.getStartedDaysAgo());
            var endDate = daysAgo(taskSample.getFinishedDaysAgo());
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                throw new SampleDataInitializingException(
                        String.format("Task '%1$s' cannot be finished before it is started in %2$s",
                                taskSample.getName(), FILE_NAME), null);
            }
            if (startDate != null) {
                startDateByTaskId.put(taskId, startDate);
            }
            if (endDate != null) {
                endDateByTaskId.put(taskId, endDate);
            }
        });
        this.sampleDateUpdater.setWorkDates(projectId, startDateByTaskId, endDateByTaskId);

        var deadline = daysFromNow(projectSample.getDeadlineInDays());
        if (deadline != null && !deadline.equals(validContractDeadline(projectSample))) {
            this.sampleDateUpdater.setContractDeadline(contractId, deadline);
        }
        this.sampleDateUpdater.moveCreationTime(projectId, contractId, projectSample.getCreatedDaysAgo());
    }

    /**
     * Contract deadline has to be in the future, passed deadline is set later directly in the database.
     */
    private static LocalDate validContractDeadline(ProjectSample projectSample) {
        var deadline = daysFromNow(projectSample.getDeadlineInDays());
        return deadline == null || deadline.isAfter(LocalDate.now()) ? deadline : LocalDate.now().plusDays(1);
    }

    private Long createStage(Long projectId, Long architectId, StageSample stageSample,
            Map<TaskSample, Long> taskIds) {
        var stageDto = this.stageApplicationService.createStage(projectId, new StageDto().name(stageSample.getName())
                .type(stageSample.getType())
                .note(stageSample.getNote())
                .architectId(architectId)
                .deadline(daysFromNow(stageSample.getDeadlineInDays())));
        var stageId = stageDto.getId();

        for (TaskSample taskSample : stageSample.getTasks()) {
            var taskDto = this.taskApplicationService.createTask(projectId, stageId,
                    new TaskDto().name(taskSample.getName())
                            .type(taskSample.getType())
                            .note(taskSample.getNote())
                            .architectId(architectId));
            taskIds.put(taskSample, taskDto.getId());
        }
        if (stageSample.getInstallment() != null) {
            this.createInstallment(projectId, stageId, stageSample.getInstallment());
        }
        return stageId;
    }

    private void createInstallment(Long projectId, Long stageId, InstallmentSample installmentSample) {
        this.installmentApplicationService.createInstallment(projectId, stageId, toInstallmentDto(installmentSample));
    }

    private static InstallmentDto toInstallmentDto(InstallmentSample installmentSample) {
        return new InstallmentDto().value(installmentSample.getValue())
                .note(installmentSample.getNote())
                .hasInvoice(installmentSample.isHasInvoice())
                .paid(installmentSample.isPaid())
                .paymentDate(paymentDate(installmentSample.isPaid(), installmentSample.getPaymentDaysAgo()));
    }

    private void createCost(Long projectId, CostSample costSample) {
        this.costApplicationService.createCost(projectId, new CostDto().name(costSample.getName())
                .value(costSample.getValue())
                .category(costSample.getCategory())
                .date(daysAgo(costSample.getDateDaysAgo()))
                .note(costSample.getNote())
                .hasInvoice(costSample.isHasInvoice())
                .payable(costSample.isPayable())
                .paid(costSample.isPaid())
                .paymentDate(paymentDate(costSample)));
    }

    private void createContractorJob(Long projectId, ContractorJobSample jobSample,
            ReferencedIds referencedIds) {
        this.contractorJobApplicationService.createContractorJob(projectId,
                new ContractorJobDto().name(jobSample.getName())
                        .contractorId(getId(referencedIds.contractorIdsByName(), jobSample.getContractor(), "contractor"))
                        .value(jobSample.getValue())
                        .note(jobSample.getNote())
                        .hasInvoice(jobSample.isHasInvoice())
                        .payable(jobSample.isPayable())
                        .paid(jobSample.isPaid())
                        .paymentDate(paymentDate(jobSample)));
    }

    private void createSupply(Long projectId, SupplySample supplySample, ReferencedIds referencedIds) {
        this.supplyApplicationService.createSupply(projectId, new SupplyDto().name(supplySample.getName())
                .supplierId(getId(referencedIds.supplierIdsByName(), supplySample.getSupplier(), "supplier"))
                .value(supplySample.getValue())
                .note(supplySample.getNote())
                .hasInvoice(supplySample.isHasInvoice())
                .payable(supplySample.isPayable())
                .paid(supplySample.isPaid())
                .paymentDate(paymentDate(supplySample)));
    }

    /**
     * Moves Contract to ACCEPTED or SIGNED, when target status requires it, or when work on the Project has to be
     * started before the Contract is rejected.
     */
    private void moveContractToWorkingStatus(Long contractId, ProjectSample projectSample) {
        var targetStatus = projectSample.getContractStatus();
        var isAcceptedOrFurther = targetStatus == ContractStatusDto.ACCEPTED || SIGNED_CONTRACT_STATUSES.contains(
                targetStatus);
        var isRejectedAfterWork = targetStatus == ContractStatusDto.REJECTED && hasStartedWork(projectSample);
        if (!isAcceptedOrFurther && !isRejectedAfterWork) {
            return;
        }
        this.contractService.changeStatus(contractId, new ContractDto().status(ContractStatusDto.ACCEPTED));
        if (SIGNED_CONTRACT_STATUSES.contains(targetStatus)) {
            var signingDate = daysAgo(projectSample.getSignedDaysAgo());
            if (signingDate == null) {
                throw new SampleDataInitializingException(
                        String.format("Signed contract of project '%1$s' requires signedDaysAgo in %2$s",
                                projectSample.getName(), FILE_NAME), null);
            }
            // Signing requires the deposit
            if (projectSample.getDeposit() == null) {
                throw new SampleDataInitializingException(
                        String.format("Signed contract of project '%1$s' requires deposit in %2$s",
                                projectSample.getName(), FILE_NAME), null);
            }
            this.contractService.changeStatus(contractId, new ContractDto().status(ContractStatusDto.SIGNED)
                    .offerValue(projectSample.getOfferValue())
                    .signingDate(signingDate)
                    .startDate(signingDate)
                    .deadline(validContractDeadline(projectSample))
                    .deposit(toInstallmentDto(projectSample.getDeposit())));
        }
    }

    private void moveContractToFinalStatus(Long contractId, ProjectSample projectSample) {
        var targetStatus = projectSample.getContractStatus();
        if (targetStatus == ContractStatusDto.REJECTED) {
            this.contractService.changeStatus(contractId, new ContractDto().status(ContractStatusDto.REJECTED));
        } else if (targetStatus == ContractStatusDto.TERMINATED || targetStatus == ContractStatusDto.COMPLETED) {
            this.contractService.changeStatus(contractId,
                    new ContractDto().status(targetStatus)
                            .endDate(projectSample.getContractEndedDaysAgo() == null
                                    ? LocalDate.now()
                                    : daysAgo(projectSample.getContractEndedDaysAgo())));
        }
    }

    private static boolean hasStartedWork(ProjectSample projectSample) {
        return projectSample.getStages()
                .stream()
                .filter(stageSample -> !stageSample.isRejected())
                .flatMap(stageSample -> stageSample.getTasks().stream())
                .anyMatch(taskSample -> !statusPath(taskSample).isEmpty());
    }

    private static List<TaskStatusDto> statusPath(TaskSample taskSample) {
        return taskSample.getStatus() == null ? List.of() : TASK_STATUS_PATHS.get(taskSample.getStatus());
    }

    private void changeWorkStatuses(Long projectId, List<StageSample> stageSamples, List<Long> stageIds,
            Map<TaskSample, Long> taskIds) {
        for (int i = 0; i < stageSamples.size(); i++) {
            var stageSample = stageSamples.get(i);
            if (!stageSample.isRejected()) {
                continue;
            }
            if (stageSample.getTasks().stream().anyMatch(taskSample -> !statusPath(taskSample).isEmpty())) {
                throw new SampleDataInitializingException(
                        String.format("Tasks of rejected stage '%1$s' cannot have status other than TO_DO in %2$s",
                                stageSample.getName(), FILE_NAME), null);
            }
            this.stageApplicationService.rejectStage(projectId, stageIds.get(i));
        }
        for (int i = 0; i < stageSamples.size(); i++) {
            var stageSample = stageSamples.get(i);
            if (stageSample.isRejected()) {
                continue;
            }
            for (TaskSample taskSample : stageSample.getTasks()) {
                for (TaskStatusDto status : statusPath(taskSample)) {
                    this.taskApplicationService.updateTaskStatus(projectId, stageIds.get(i), taskIds.get(taskSample),
                            new UpdateStatusRequestDto().status(status));
                }
            }
        }
    }

    private List<ProjectSample> prepareProjects() {
        return SampleDataFileReader.readList(FILE_NAME, ProjectSample.class);
    }

    private static Long getId(Map<String, Long> idsByKey, String key, String objectName) {
        var id = idsByKey.get(key);
        if (id == null) {
            throw new SampleDataInitializingException(
                    String.format("Sample %1$s '%2$s' referenced in %3$s does not exist", objectName, key, FILE_NAME),
                    null);
        }
        return id;
    }

    private static LocalDate paymentDate(FinanceItemSample financeItemSample) {
        return paymentDate(financeItemSample.isPaid(), financeItemSample.getPaymentDaysAgo());
    }

    private static LocalDate paymentDate(boolean paid, Integer paymentDaysAgo) {
        return paid ? daysAgo(paymentDaysAgo) : null;
    }

    private static LocalDate daysFromNow(Integer days) {
        return days == null ? null : LocalDate.now().plusDays(days);
    }

    private static LocalDate daysAgo(Integer days) {
        return days == null ? null : LocalDate.now().minusDays(days);
    }

    private record ReferencedIds(Map<String, Long> architectIdsByName, Map<String, Long> clientIdsByEmail,
            Map<String, Long> contractorIdsByName, Map<String, Long> supplierIdsByName,
            Map<String, Long> projectTypeIdsByName) {
    }
}
