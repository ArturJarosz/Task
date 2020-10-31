package com.arturjarosz.application.model;

/**
 * Abstraction for all abstract values. They don't have own tables
 * and should not exist without context within other Object.
 *
 * @param <T>
 */

public abstract class AbstractValueObject<T extends Value<T>> implements Value<T> {
    private static final long serialVersionUID = -8491331532881822544L;

    protected AbstractValueObject() {
    }
}


