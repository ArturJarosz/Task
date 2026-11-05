package com.arturjarosz.task.project.infrastructure.repositor;

import com.arturjarosz.task.project.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The deletes are bulk statements that run at once, so the entries are gone before Hibernate removes the task they
 * reference.
 */
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    @Modifying
    @Query("delete from TimeEntry timeEntry where timeEntry.taskId = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Query("""
            delete from TimeEntry timeEntry where timeEntry.taskId in
            (select task.id from Stage stage join stage.tasks task where stage.id = :stageId)""")
    void deleteByStageId(@Param("stageId") Long stageId);

    @Modifying
    @Query("""
            delete from TimeEntry timeEntry where timeEntry.taskId in
            (select task.id from Project project join project.stages stage join stage.tasks task
            where project.id = :projectId)""")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
