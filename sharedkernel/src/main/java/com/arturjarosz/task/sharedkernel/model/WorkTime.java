package com.arturjarosz.task.sharedkernel.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class WorkTime extends AbstractValueObject<WorkTime> implements ValueObject<WorkTime> {
    private static final long serialVersionUID = -9057875053913061735L;

    @Column(name = "WORK_TIME")
    private Long time;

    public WorkTime() {
        this.reset();
    }

    public WorkTime(Long time) {
        this.time = time;
    }

    public void reset() {
        this.time = 0L;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.time).toHashCode();
    }

    @Override
    public boolean hasSameValueAs(WorkTime other) {
        return new EqualsBuilder().append(this.time, other.time).isEquals();
    }

    @Override
    public WorkTime copy() {
        return new WorkTime(this.time);
    }
}
