package fr.zayzx.zayzutils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtils {

    private ReflectionUtils() {}

    /* =========================
       FIELD GETTER / SETTER
       ========================= */

    public static Object getField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field '" + fieldName + "'", e);
        }
    }

    public static void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field '" + fieldName + "'", e);
        }
    }

    /* =========================
       METHOD INVOCATION
       ========================= */

    public static Object invoke(Object obj, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method '" + methodName + "'", e);
        }
    }

    public static Object invoke(Object obj, String methodName) {
        return invoke(obj, methodName, new Class<?>[0]);
    }
}
