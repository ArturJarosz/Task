package com.arturjarosz.task.sharedkernel.testhelpers;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;

/**
 * Util class for building Entities for tests and sample data.
 */
@Slf4j
public final class TestUtils {

    private TestUtils() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }

    /**
     * Sets given fieldValue of field with fieldName on given targetObject.
     */
    @SuppressWarnings("java:S3011") // whole idea is for this method is to set field via reflection
    public static <T> void setFieldForObject(Object targetObject, String fieldName, T fieldValue) {
        Class<? extends Object> theClass = targetObject.getClass();
        Field field;
        field = getDeclaredField(theClass, fieldName);
        ensureFieldIsAccessible(field, targetObject);
        try {
            field.set(targetObject, fieldValue);
        } catch (IllegalAccessException e) {
            LOG.error("Cannot set field for object. ", e);
        }
    }

    /**
     * Returns field for given object. If field on the object does not exist, then it is checked if the superclass
     * of given class does have that field. It is called recursively until whether the field is found or class has
     * no superclass. If field was found then it is returned. In case there is not such field,
     * {@link IllegalArgumentException} is thrown.
     */
    public static Field getDeclaredField(@NonNull Class<?> theClass, String fieldName) {
        Field field;
        try {
            field = theClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            theClass = theClass.getSuperclass();
            if (theClass == null) {
                throw new IllegalArgumentException("Given class is top class, no superclass available.");
            }
            field = getDeclaredField(theClass, fieldName);
        }
        return field;
    }

    /**
     * Checks whether field is accessible. If not, then set field access to true, so that its value can be changed.
     */
    @SuppressWarnings("java:S3011") // whole point of this method is to make field accessible
    private static void ensureFieldIsAccessible(Field field, Object targetObject) {
        if (!field.canAccess(targetObject)) {
            field.setAccessible(true);
        }
    }
}
