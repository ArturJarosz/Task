package com.arturjarosz.application.model;

import java.io.Serializable;

public interface Value<T extends Value<T>> extends Serializable {

    /**
     * Makes deep copy of T
     *
     * @param t
     * @return
     */
    T copy(T t);
}

