package com.hhz.hhztestboot.util;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * <p class="detail">
 * 功能: 反射工具类
 * </p>
 *
 * @author HuangHuizhou
 * @ClassName Reflection util.
 * @Version V1.0.
 * @date 2019.05.21 09:36:04
 */
public class ReflectionUtil {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);


    public static List<Field> getFieldsList(final Class<?> cls) {
        return getAllFieldsList(cls, false, null, false);
    }

    /**
     * <p class="detail">
     * 功能：获得类的所有字段
     * </p>
     *
     * @param cls the cls
     * @return the all fields list
     */
    public static List<Field> getAllFieldsList(final Class<?> cls) {
        return getAllFieldsList(cls, true, null, false);
    }

    /**
     * Gets all fields list.
     *
     * @param cls           the cls
     * @param recursive     the recursive
     * @param filter        Field -> return or not
     * @param breakOnFilter the break on filter
     * @return the all fields list
     */
    public static List<Field> getAllFieldsList(final Class<?> cls, boolean recursive, Function<Field, Boolean> filter, boolean breakOnFilter) {
        if (cls == null) {
            throw new IllegalArgumentException("cls can not be null");
        }

        final List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        do {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            if (filter == null) {
                allFields.addAll(Arrays.asList(declaredFields));
            } else {
                for (Field field : declaredFields) {
                    if (filter.apply(field)) {
                        allFields.add(field);
                        if (breakOnFilter) {
                            return allFields;
                        }
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null && recursive);
        return allFields;
    }


    public static Field getFieldByName(final Class<?> cls, String fieldName) {
        if (Strings.isNullOrEmpty(fieldName)) {
            return null;
        }
        List<Field> fields = getAllFieldsList(cls, true, q -> q.getName().equals(fieldName), true);
        if (fields.size() != 0) {
            return fields.get(0);
        } else {
            return null;
        }
    }


    public static Object getValueFromField(Field field, Object object) {
        field.setAccessible(true);
        Object value;
        try {
            value = field.get(object);
        } catch (IllegalAccessException e) {
            logger.error("Failed to get field {} value of object {}", field.getName(), object);
            throw new RuntimeException(e);
        }
        return value;
    }


    public static void setValueByField(Object object, Field field, Object newValue) {
        field.setAccessible(true);
        try {
            field.set(object, newValue);
        } catch (IllegalAccessException e) {
            logger.error("Failed to set field {} value {} of object {}", field, newValue, object);
            throw new RuntimeException(e);
        }
    }
}
