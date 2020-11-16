package com.arturjarosz.task.sharedkernel.model;

/**
 * Abstraction for all abstract values. They don't have own tables
 * and should not exist without context within other Object.
 *
 * @param <T>
 */

public abstract class AbstractValueObject<T extends ValueObject<T>> implements ValueObject<T> {
    private static final long serialVersionUID = -8491331532881822544L;

    protected AbstractValueObject() {
        //needed by Hibernate
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != this.getClass()) {
            return false;
        }

        T other = (T) object;

        return this.hasSameValueAs(other);
    }

    @Override
    public abstract int hashCode();
}


