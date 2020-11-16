package com.arturjarosz.task.sharedkernel.model;

import java.io.Serializable;

/**
 * Embeddable type. Stores abstract some type of values, measures like money, telephone numbers that should not be
 * stored without Entity context and provides methods to operate on those Valuse.
 *
 * @param <T>
 */
public interface ValueObject<T extends ValueObject<T>> extends Serializable {

    /**
     * Computerisation between two ValueObjects comes to comparing values their are storing.
     *
     * @param other The other ValueObject to compare.
     * @return <code>true</code> if both ValueObjects attributes are the same.
     */
    boolean hasSameValueAs(T other);

    /**
     * Makes deep copy of T
     *
     * @param t
     * @return Safe copy of ValueOject.
     */
    T copy(T t);
}

