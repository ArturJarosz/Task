package com.arturjarosz.application.model;

/**
 * Abstraction for all
 *
 * @param <T>
 */

public abstract class AbstractValueObject<T extends Value<T>> implements Value<T> {
    private static final long serialVersionUID = -8491331532881822544L;

    protected AbstractValueObject() {
    }
}


