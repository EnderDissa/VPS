package com.example.warehouse.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MappersSmokeTest {

    @Test
    void userMapper_toResponseDTO_smoke() throws Exception {
        UserMapper mapper = Mappers.getMapper(UserMapper.class);
        assertNotNull(mapper);
        Method toDto = null;
        for (Method m : UserMapper.class.getMethods()) {
            if (m.getName().equals("toResponseDTO") && m.getParameterCount() == 1) {
                toDto = m;
                break;
            }
        }
        assertNotNull(toDto);
        Class<?> paramType = toDto.getParameterTypes()[0];
        Object user = newInstance(paramType);
        setIfExists(user, "id", 1L);
        setIfExists(user, "name", "U");
        Object dto = toDto.invoke(mapper, user);
        assertNotNull(dto);
        Object id = getIfExists(dto, "id");
        if (id != null) assertEquals(1L, ((Number) id).longValue());
    }

    private static Object newInstance(Class<?> type) throws Exception {
        Constructor<?> c = type.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    private static void setIfExists(Object obj, String field, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (NoSuchFieldException ignored) {
        } catch (Exception e) {
            fail(e);
        }
    }

    private static Object getIfExists(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(obj);
        } catch (NoSuchFieldException ignored) {
            return null;
        } catch (Exception e) {
            fail(e);
            return null;
        }
    }
}
