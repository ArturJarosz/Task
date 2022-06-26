package com.arturjarosz.task.sharedkernel.testhelpers;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;

/**
 * Util class for building Entities for tests and sample data.
 */
public final class TestUtils {

    public static final String NO_SUCH_FIELD = "No such field {0} for class {1}";

    private TestUtils() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }

    /**
     * Sets given fieldValue of field with fieldName on given targetObject.
     *
     * @param targetObject
     * @param fieldName
     * @param fieldValue
     * @param <T>
     */
    public static <T> void setFieldForObject(Object targetObject, String fieldName, T fieldValue) {
        Class theClass = targetObject.getClass();
        Field field;
        field = getDeclaredField(theClass, fieldName);
        ensureFieldIsAccessible(field, targetObject);
        try {
            field.set(targetObject, fieldValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns field for given object. If field on the object does not exists, then it is checked if the superclass
     * of given class does have that field. It is called recursively until whether the field is found or class has
     * no superclass. If field was found then it is returned. In case there is not such field,
     * {@link IllegalArgumentException} is thrown.
     *
     * @param theClass
     * @param fieldName
     * @return
     */
    public static Field getDeclaredField(@NonNull Class<?> theClass, String fieldName) {
        Field field;
        try {
            field = theClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            theClass = theClass.getSuperclass();
            if (theClass == null) {
                throw new IllegalArgumentException(String.format(NO_SUCH_FIELD, fieldName, theClass.getName()));
            }
            field = getDeclaredField(theClass, fieldName);
        }
        return field;
    }

    /**
     * Returns value of field with fieldName for object of generic type C.
     *
     * @param object
     * @param fieldName
     * @param <C>
     * @return
     */
    public static <C extends Object> Object getFieldValue(C object, String fieldName) {
        Field field = getDeclaredField(object.getClass(), fieldName);
        ensureFieldIsAccessible(field, object);
        Object fieldValue = null;
        try {
            fieldValue = field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return fieldValue;
    }

    /**
     * Checks whether field is accessible. If not, then set field access to true, so that its value can be changed.
     *
     * @param field
     * @param targetObject
     */
    private static void ensureFieldIsAccessible(Field field, Object targetObject) {
        if (!field.canAccess(targetObject)) {
            field.setAccessible(true);
        }
    }
}
