package com.example.warehouse.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EntitiesReflectionSmokeTest {

    @Autowired
    EntityManager em;

    @Test
    void allEntities_haveNoArgsCtor_and_surviveBasicSetGet() throws Exception {
        Set<EntityType<?>> entities = em.getMetamodel().getEntities();
        assertFalse(entities.isEmpty());
        for (EntityType<?> et : entities) {
            Class<?> c = et.getJavaType();
            Constructor<?> ctor = c.getDeclaredConstructor();
            ctor.setAccessible(true);
            Object instance = ctor.newInstance();
            assertNotNull(instance);
            for (Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                Class<?> t = f.getType();
                Object v = sampleValue(t);
                if (v == null) continue;
                boolean set = trySetter(instance, f.getName(), t, v);
                if (!set) {
                    f.setAccessible(true);
                    f.set(instance, v);
                }
                Object read = tryGetter(instance, f.getName());
                if (read == null) {
                    f.setAccessible(true);
                    read = f.get(instance);
                }
                assertEquals(v.getClass(), read.getClass());
            }
        }
    }

    private static boolean trySetter(Object target, String field, Class<?> type, Object value) {
        String name = "set" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
        try {
            Method m = target.getClass().getMethod(name, type);
            m.invoke(target, value);
            return true;
        } catch (NoSuchMethodException e) {
            for (Method m : target.getClass().getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(type)) {
                    try {
                        m.invoke(target, value);
                        return true;
                    } catch (Exception ignored) {}
                }
            }
            return false;
        } catch (Exception e) {
            fail(e);
            return false;
        }
    }

    private static Object tryGetter(Object target, String field) {
        String base = Character.toUpperCase(field.charAt(0)) + field.substring(1);
        try {
            Method m = target.getClass().getMethod("get" + base);
            return m.invoke(target);
        } catch (NoSuchMethodException e) {
            try {
                Method m = target.getClass().getMethod("is" + base);
                return m.invoke(target);
            } catch (NoSuchMethodException ex) {
                return null;
            } catch (Exception ex) {
                fail(ex);
                return null;
            }
        } catch (Exception e) {
            fail(e);
            return null;
        }
    }

    private static Object sampleValue(Class<?> t) {
        if (t == String.class) return "x";
        if (t == Long.class || t == long.class) return 1L;
        if (t == Integer.class || t == int.class) return 1;
        if (t == Boolean.class || t == boolean.class) return true;
        if (t == Double.class || t == double.class) return 1.0d;
        if (t == Float.class || t == float.class) return 1.0f;
        if (t == BigDecimal.class) return new BigDecimal("1.00");
        if (t == LocalDate.class) return LocalDate.of(2020, 1, 1);
        if (t == LocalDateTime.class) return LocalDateTime.of(2020, 1, 1, 0, 0);
        return null;
    }
}
