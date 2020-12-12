package com.arturjarosz.task.sharedkernel.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class WorkTime extends AbstractValueObject<WorkTime> implements ValueObject<WorkTime> {
    private static final long serialVersionUID = -9057875053913061735L;

    /*
    TODO: For now it does not validate adding time. The idea is to have something like parsing of entered text
        1d 5h 10m and then translated to minutes and store as minutes amount. When no letter is provided,
        then it should tread it as minutes. No any other letters are allowed.
     */

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
