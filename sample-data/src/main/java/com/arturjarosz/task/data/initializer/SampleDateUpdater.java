package com.arturjarosz.task.data.initializer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Changes dates of already loaded sample Project directly in the database. Dates set by status transitions are always
 * today, and validators do not allow passed contract deadlines, so such dates can't be set through application
 * services. Persistence context is flushed before and cleared after changes, so loaded entities do not override them.
 * Dates kept in the history are changed too, so the history shows the same dates as the objects.
 */
@Component
@RequiredArgsConstructor
public class SampleDateUpdater {
    private static final String PROJECT_STAGES = "select id from %1$s.stage where project_id = ?";
    private static final String PROJECT_FINANCIAL_DATA = "select id from %1$s.project_financial_data where project_id = ?";
    private static final List<String> FINANCIAL_ITEM_TABLES = List.of("cost", "installment", "contractor_job",
            "supply");

    private final JdbcTemplate jdbcTemplate;
    private final SampleSchemaProperties sampleSchemaProperties;
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Sets start and end dates of Tasks, and aligns dates of Stages with their Tasks and of Project with its Stages.
     * Only dates already set by status transitions are changed.
     */
    @Transactional
    public void setWorkDates(Long projectId, Map<Long, LocalDate> startDateByTaskId,
            Map<Long, LocalDate> endDateByTaskId) {
        this.entityManager.flush();
        for (String table : List.of("task", "task_aud")) {
            startDateByTaskId.forEach((taskId, date) -> this.update(
                    "update %1$s." + table + " set start_date = ? where id = ? and start_date is not null", date,
                    taskId));
            endDateByTaskId.forEach((taskId, date) -> this.update(
                    "update %1$s." + table + " set end_date = ? where id = ? and end_date is not null", date,
                    taskId));
        }
        this.update("""
                update %1$s.stage s set start_date = coalesce(t.start_date, s.start_date),
                    end_date = case when s.end_date is null then null else coalesce(t.end_date, s.end_date) end
                from (select stage_id, min(start_date) start_date, max(end_date) end_date from %1$s.task
                    group by stage_id) t
                where s.id = t.stage_id and s.project_id = ? and s.start_date is not null""", projectId);
        this.update("""
                update %1$s.project p set start_date = coalesce(s.start_date, p.start_date),
                    end_date = case when p.end_date is null then null else coalesce(s.end_date, p.end_date) end
                from (select project_id, min(start_date) start_date, max(end_date) end_date from %1$s.stage
                    group by project_id) s
                where p.id = s.project_id and p.id = ? and p.start_date is not null""", projectId);
        // every date set in the history becomes the date set above, which is the one the history ends with
        this.update("""
                update %1$s.stage_aud a set start_date = case when a.start_date is null then null else s.start_date end,
                    end_date = case when a.end_date is null then null else s.end_date end
                from %1$s.stage s where a.id = s.id and s.project_id = ?""", projectId);
        this.update("""
                update %1$s.project_aud a set start_date = case when a.start_date is null then null else p.start_date end,
                    end_date = case when a.end_date is null then null else p.end_date end
                from %1$s.project p where a.id = p.id and p.id = ?""", projectId);
        this.entityManager.clear();
    }

    @Transactional
    public void setContractDeadline(Long contractId, LocalDate deadline) {
        this.entityManager.flush();
        this.update("update %1$s.contract set deadline = ? where id = ?", deadline, contractId);
        this.update("update %1$s.contract_aud set deadline = ? where id = ? and deadline is not null", deadline,
                contractId);
        this.entityManager.clear();
    }

    /**
     * Moves creation and modification time of the Project, its Contract, Stages, Tasks and financial objects back by
     * given number of days.
     */
    @Transactional
    public void moveCreationTime(Long projectId, Long contractId, int days) {
        if (days == 0) {
            return;
        }
        this.entityManager.flush();
        this.moveHistory("project", "id = ?", days, projectId);
        this.moveHistory("contract", "id = ?", days, contractId);
        this.moveHistory("stage", "project_id = ?", days, projectId);
        this.moveHistory("task", "stage_id in (" + PROJECT_STAGES + ")", days, projectId);
        this.moveHistory("project_financial_data", "project_id = ?", days, projectId);
        for (String table : FINANCIAL_ITEM_TABLES) {
            this.moveHistory(table, "project_financial_data_id in (" + PROJECT_FINANCIAL_DATA + ")", days,
                    projectId);
        }
        var financialDataIds = FINANCIAL_ITEM_TABLES.stream()
                .map(table -> "select financial_data_id from %1$s." + table + " where project_financial_data_id in ("
                        + PROJECT_FINANCIAL_DATA + ")")
                .collect(Collectors.joining(" union "));
        this.moveHistory("financial_data", "id in (" + financialDataIds + ")", days,
                FINANCIAL_ITEM_TABLES.stream().map(table -> projectId).toArray());
        this.entityManager.clear();
    }

    private void moveHistory(String table, String condition, int days, Object... params) {
        this.update("update %1$s." + table + " set created_datetime = created_datetime - interval '" + days
                + " days', last_modified_datetime = last_modified_datetime - interval '" + days + " days' where "
                + condition, params);
    }

    private void update(String sql, Object... params) {
        this.jdbcTemplate.update(String.format(sql, this.sampleSchemaProperties.defaultSchema()), params);
    }
}
