package com.arturjarosz.task.sharedkernel.exceptions;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Provides validation methods.
 */
public abstract class BaseValidator<T> {

    private static final String DELIMITER = ".";

    protected T validatedObject;

    protected BaseValidator(T validatedObject) {
        this.validatedObject = validatedObject;
    }

    /**
     * Creates message code.
     *
     * @param elements
     * @return
     */
    public static String createMessageCode(String... elements) {
        return StringUtils.join(elements, DELIMITER);
    }

    public static void assertIsTrue(boolean value, String messageCode, Object... parameters) {
        if (!value) {
            throw new IllegalArgumentException(messageCode, parameters);
        }
    }

    public static void assertIsFalse(boolean value, String messageCode, Object... parameters) {
        if (value) {
            throw new IllegalArgumentException(messageCode, parameters);
        }
    }

    public static void assertNotEmpty(String value, String messageCode, Object... parameters) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(messageCode, parameters);
        }
    }

    public static void assertNotEmpty(Collection<?> collection, String messageCode, Object... parameters) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(messageCode, parameters);
        }
    }

    public static void assertNotEmpty(Map<?, ?> map, String messageCode, Object... parameters) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException(messageCode, parameters);
        }
    }

    public static void assertNotEmpty(Object[] array, String messageCode, Object... parameters) {
        if (ArrayUtils.isEmpty(array)) {
            throw new IllegalArgumentException(messageCode, parameters);
        }
    }

    public static void assertNotNull(Object object, String messageCode, Object... parameters) {
        if (object == null) {
            throw new IllegalArgumentException(messageCode, parameters);
        }
    }

    public static void assertIsEmpty(String value, String messageCode, Object... parameters) {
        if (StringUtils.isNotBlank(value)) {
            throw new IllegalArgumentException(messageCode, parameters);
        }
    }

    public static void assertIsEmpty(Collection<?> collection, String messageCode, Object... parameters) {
        if (CollectionUtils.isNotEmpty(collection)) {
            throw new IllegalArgumentException(messageCode, parameters);
        }
    }

    public static <T> void assertContains(Collection<T> collection, T element, String messageCode,
                                          Object... parameters) {
        if (!collection.contains(element)) {
            throw new IllegalStateException(messageCode, parameters);
        }
    }

    public static <T> void assertNotContains(Collection<T> collection, T element, String messageCode,
                                             Object... parameters) {
        if (collection.contains(element)) {
            throw new IllegalStateException(messageCode, parameters);
        }
    }
}
