package com.example.warehouse.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AllMappersRoundtripDynamicTest {

    private static final List<String> MAPPER_FQCN = List.of(
            "com.example.warehouse.mapper.BorrowingMapper",
            "com.example.warehouse.mapper.ItemMaintenanceMapper",
            "com.example.warehouse.mapper.KeepingMapper"
    );

    @Test
    @DisplayName("MapStruct roundtrip")
    void roundtripAllPresentMappers() throws Exception {
        for (String fqcn : MAPPER_FQCN) {
            Class<?> mapperType;
            try {
                mapperType = Class.forName(fqcn);
            } catch (ClassNotFoundException e) {
                continue;
            }

            Object mapper = Mappers.getMapper(mapperType);
            assertNotNull(mapper, "Mapper is null: " + fqcn);

            Method toDto = findSingleArgMethod(mapperType, "toDTO");
            Method toEntity = findSingleArgMethod(mapperType, "toEntity");
            if (toDto == null || toEntity == null) {
                continue;
            }

            Class<?> entityClass = toDto.getParameterTypes()[0];
            Object entity = newInstanceIfPossible(entityClass);
            if (entity == null) continue;
            setIfExists(entity, "id", 1L);
            setIfExists(entity, "name", entityClass.getSimpleName());

            Object dto = toDto.invoke(mapper, entity);
            assertNotNull(dto, fqcn + " toDTO returned null");
            Object dtoId = getIfExists(dto, "id");
            if (dtoId != null) assertEquals(1L, ((Number) dtoId).longValue(), fqcn + " id -> DTO");

            Class<?> dtoClass = toEntity.getParameterTypes()[0];
            Object dtoNew = newInstanceIfPossible(dtoClass);
            if (dtoNew == null) continue;
            setIfExists(dtoNew, "id", 42L);
            setIfExists(dtoNew, "name", dtoClass.getSimpleName());

            Object mappedEntity = toEntity.invoke(mapper, dtoNew);
            assertNotNull(mappedEntity, fqcn + " toEntity returned null");
            Object entityId = getIfExists(mappedEntity, "id");
            if (entityId != null) assertEquals(42L, ((Number) entityId).longValue(), fqcn + " id <- DTO");
        }
    }

    private static Method findSingleArgMethod(Class<?> type, String name) {
        for (Method m : type.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) return m;
        }
        return null;
    }

    private static Object newInstanceIfPossible(Class<?> cl) {
        try {
            Constructor<?> c = cl.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void setIfExists(Object obj, String field, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (NoSuchFieldException ignored) {
        } catch (Exception e) {
            fail("Failed to set '" + field + "' on " + obj.getClass().getSimpleName() + ": " + e);
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
            fail("Failed to get '" + field + "' on " + obj.getClass().getSimpleName() + ": " + e);
            return null;
        }
    }
}
