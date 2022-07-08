package com.arturjarosz.task.sharedkernel.model;

import java.io.Serializable;

/**
 * Embeddable type. Stores abstract some type of values, measures like money, telephone numbers that should not be
 * stored without Entity context and provides methods to operate on those Values.
 *
 * @param <T>
 */
public interface ValueObject<T extends ValueObject<T>> extends Serializable {

    /**
     * Comparison between two ValueObjects comes to comparing values they are storing.
     *
     * @param other The other ValueObject to compare.
     * @return <code>true</code> if both ValueObjects attributes are the same.
     */
    boolean hasSameValueAs(T other);

    /**
     * Makes deep copy of T
     *
     * @return Safe copy of ValueObject.
     */
    T copy();
}

