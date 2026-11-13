package com.arturjarosz.task.data.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Changes the history of an already loaded sample Project directly in the database. Sample data is loaded in moments
 * and by nobody, so its revisions are moved to the time the Project has existed and given authors.
 */
@Component
@RequiredArgsConstructor
public class SampleHistoryUpdater {
    private static final String PROJECT_STAGES = "select stage_id from %1$s.project_stage_aud where project_id = ?";
    private static final String PROJECT_TASKS = "select task_id from %1$s.stage_task_aud where stage_id in (%2$s)";
    private static final String PROJECT_REVISIONS = """
            select rev from %1$s.project_aud where id = ?
            union select rev from %1$s.contract_aud where id = ?
            union select rev from %1$s.project_stage_aud where project_id = ?
            union select rev from %1$s.stage_aud where id in (%2$s)
            union select rev from %1$s.stage_task_aud where stage_id in (%2$s)
            union select rev from %1$s.task_aud where id in (%3$s)
            order by rev""";
    // the date a revision sets where there was none before: a start, end or signing date
    private static final String SET_DATES = """
            select rev, max(set_date) set_date from (
                select rev, revtype, set_date, lag(set_date) over (partition by record order by rev) previous_date
                from (
                    select 'task start ' || id record, rev, revtype, start_date set_date from %1$s.task_aud
                        where id in (%3$s)
                    union all select 'task end ' || id, rev, revtype, end_date from %1$s.task_aud where id in (%3$s)
                    union all select 'stage start ' || id, rev, revtype, start_date from %1$s.stage_aud
                        where id in (%2$s)
                    union all select 'stage end ' || id, rev, revtype, end_date from %1$s.stage_aud where id in (%2$s)
                    union all select 'project start', rev, revtype, start_date from %1$s.project_aud where id = ?
                    union all select 'project end', rev, revtype, end_date from %1$s.project_aud where id = ?
                    union all select 'contract signing', rev, revtype, signing_date from %1$s.contract_aud
                        where id = ?
                    union all select 'contract end', rev, revtype, end_date from %1$s.contract_aud where id = ?
                ) dates
            ) changes
            where revtype = 1 and set_date is not null and previous_date is null
            group by rev""";
    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final Duration MIN_GAP = Duration.ofMinutes(10);

    private final JdbcTemplate jdbcTemplate;
    private final SampleSchemaProperties sampleSchemaProperties;

    /**
     * Moves the revisions of the Project, its Contract, Stages and Tasks to the time the Project has existed, keeping
     * their order. A revision that sets a start, end or signing date is moved to that day, the others are spread evenly
     * between them, from given number of days ago until now. The authors take turns in the order given, one per
     * revision.
     */
    @Transactional
    public void spreadHistory(Long projectId, Long contractId, int createdDaysAgo, List<String> authorAuthIds) {
        var schema = this.sampleSchemaProperties.defaultSchema();
        var stages = String.format(PROJECT_STAGES, schema);
        var tasks = String.format(PROJECT_TASKS, schema, stages);
        var revisions = this.jdbcTemplate.queryForList(String.format(PROJECT_REVISIONS, schema, stages, tasks),
                Integer.class, projectId, contractId, projectId, projectId, projectId, projectId);
        var setDates = this.jdbcTemplate.query(String.format(SET_DATES, schema, stages, tasks),
                        (row, _) -> Map.entry(row.getInt("rev"), row.getObject("set_date", LocalDate.class)),
                        projectId, projectId, projectId, projectId, projectId, projectId, contractId, contractId)
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var now = LocalDateTime.now();
        var times = revisionTimes(revisions, setDates, now.minusDays(createdDaysAgo), now);
        for (int i = 0; i < revisions.size(); i++) {
            this.jdbcTemplate.update(
                    String.format("update %1$s.revinfo set revtstmp = ?, author_auth_id = ? where rev = ?", schema),
                    times.get(i), authorAuthIds.get(i % authorAuthIds.size()), revisions.get(i));
        }
    }

    /**
     * The first revision, which creates the Project, is at the start. A revision setting a date is on that day, but
     * never before the revision preceding it; the revisions between are spread evenly.
     */
    private static List<LocalDateTime> revisionTimes(List<Integer> revisions, Map<Integer, LocalDate> setDates,
            LocalDateTime start, LocalDateTime end) {
        var times = new ArrayList<LocalDateTime>(List.of(start));
        var previous = start;
        var withoutDate = 0;
        for (Integer revision : revisions.subList(1, revisions.size())) {
            var setDate = setDates.get(revision);
            if (setDate == null) {
                withoutDate++;
                continue;
            }
            var time = latest(setDate.atTime(WORK_START), previous.plus(MIN_GAP));
            times.addAll(evenlyBetween(previous, time, withoutDate));
            times.add(time);
            previous = time;
            withoutDate = 0;
        }
        times.addAll(evenlyBetween(previous, latest(end, previous.plus(MIN_GAP.multipliedBy(withoutDate + 1L))),
                withoutDate));
        return times;
    }

    private static List<LocalDateTime> evenlyBetween(LocalDateTime from, LocalDateTime to, int count) {
        var step = Duration.between(from, to).dividedBy(count + 1L);
        return IntStream.rangeClosed(1, count).mapToObj(n -> from.plus(step.multipliedBy(n))).toList();
    }

    private static LocalDateTime latest(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }
}
