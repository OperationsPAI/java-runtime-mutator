package io.chaosmesh.mutator.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Runtime helper for mutating object fields using reflection.
 * This class is called from mutated bytecode at runtime.
 */
public class MutationHelper {

    /**
     * Mutates the fields of an object using pre-generated random values.
     *
     * @param obj          The object to mutate
     * @param randomValues Pre-generated random values (Map with int, long, float, double, string keys)
     * @param recursive    Whether to recursively mutate nested objects
     * @param excludeFields Set of field names to exclude from mutation
     * @return The mutated object (same instance)
     */
    @SuppressWarnings("unchecked")
    public static Object mutateObjectFields(Object obj, Object randomValues,
                                            boolean recursive, Set<String> excludeFields) {
        if (obj == null) {
            return null;
        }

        Map<String, Object> values = extractRandomValues(randomValues);
        Set<String> excluded = excludeFields != null ? excludeFields : Collections.emptySet();

        try {
            mutateFields(obj, values, recursive, excluded, new HashSet<>());
        } catch (Exception e) {
            System.out.println("Failed to mutate object fields: " + e.getMessage());
        }

        return obj;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractRandomValues(Object randomValues) {
        if (randomValues instanceof Map) {
            return (Map<String, Object>) randomValues;
        }
        // Return empty map if no pre-generated values
        return Collections.emptyMap();
    }

    private static void mutateFields(Object obj, Map<String, Object> values,
                                     boolean recursive, Set<String> excluded,
                                     Set<Object> visited) throws Exception {
        if (obj == null || visited.contains(obj)) {
            return;
        }
        visited.add(obj);

        Class<?> clazz = obj.getClass();
        StringBuilder mutationLog = new StringBuilder();
        mutationLog.append("Mutating ").append(clazz.getSimpleName()).append(": ");

        // Process all fields including inherited ones
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                // Skip static, final, and excluded fields
                if (Modifier.isStatic(field.getModifiers()) ||
                    Modifier.isFinal(field.getModifiers()) ||
                    excluded.contains(field.getName())) {
                    continue;
                }

                field.setAccessible(true);
                Object originalValue = field.get(obj);
                Object newValue = mutateFieldValue(field, originalValue, values, recursive, excluded, visited);

                if (newValue != originalValue) {
                    field.set(obj, newValue);
                    mutationLog.append(field.getName()).append("=").append(newValue).append(", ");
                }
            }
            clazz = clazz.getSuperclass();
        }

        System.out.println(mutationLog.toString());
    }

    private static Object mutateFieldValue(Field field, Object originalValue,
                                           Map<String, Object> values,
                                           boolean recursive, Set<String> excluded,
                                           Set<Object> visited) throws Exception {
        Class<?> type = field.getType();

        // Primitive types
        if (type == boolean.class || type == Boolean.class) {
            // Negate boolean
            if (originalValue instanceof Boolean) {
                return !((Boolean) originalValue);
            }
            return true;
        }

        if (type == int.class || type == Integer.class) {
            return getIntValue(values);
        }

        if (type == long.class || type == Long.class) {
            return getLongValue(values);
        }

        if (type == float.class || type == Float.class) {
            return getFloatValue(values);
        }

        if (type == double.class || type == Double.class) {
            return getDoubleValue(values);
        }

        if (type == short.class || type == Short.class) {
            return (short) getIntValue(values);
        }

        if (type == byte.class || type == Byte.class) {
            return (byte) getIntValue(values);
        }

        if (type == char.class || type == Character.class) {
            return (char) ('A' + Math.abs(getIntValue(values)) % 26);
        }

        // String
        if (type == String.class) {
            return getStringValue(values);
        }

        // Recursive mutation for nested objects
        if (recursive && originalValue != null && !type.isPrimitive() &&
            !type.getName().startsWith("java.") && !type.isArray() && !type.isEnum()) {
            mutateFields(originalValue, values, true, excluded, visited);
        }

        return originalValue;
    }

    private static int getIntValue(Map<String, Object> values) {
        Object val = values.get("int");
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    private static long getLongValue(Map<String, Object> values) {
        Object val = values.get("long");
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return 0L;
    }

    private static float getFloatValue(Map<String, Object> values) {
        Object val = values.get("float");
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        return 0.0f;
    }

    private static double getDoubleValue(Map<String, Object> values) {
        Object val = values.get("double");
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return 0.0;
    }

    private static String getStringValue(Map<String, Object> values) {
        Object val = values.get("string");
        if (val instanceof String) {
            return (String) val;
        }
        return UUID.randomUUID().toString();
    }
}
