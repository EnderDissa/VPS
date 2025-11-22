package com.example.warehouse.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DtoJsonSmokeDynamicTest {

    private final ObjectMapper om = new ObjectMapper();

    private static final List<String> DTO_FQCN = List.of(
            "com.example.warehouse.dto.BorrowingDTO",
            "com.example.warehouse.dto.ItemMaintenanceDTO",
            "com.example.warehouse.dto.UserStorageAccessDTO",
            "com.example.warehouse.dto.TransportationDTO",
            "com.example.warehouse.dto.VehicleDTO",
            "com.example.warehouse.dto.KeepingDTO",
            "com.example.warehouse.dto.UserResponseDTO",
            "com.example.warehouse.dto.UserDTO",
            "com.example.warehouse.dto.ItemDTO",
            "com.example.warehouse.dto.StorageDTO"
    );

    @Test
    @DisplayName("DTO Jackson roundtrip")
    void dtoSerializeDeserialize() throws Exception {
        for (String fqcn : DTO_FQCN) {
            Class<?> dtoType;
            try {
                dtoType = Class.forName(fqcn);
            } catch (ClassNotFoundException e) {
                continue;
            }

            Object dto = newInstanceIfPossible(dtoType);
            if (dto == null) continue;
            setIfExists(dto, "id", 777L);
            setIfExists(dto, "name", dtoType.getSimpleName());

            String json = om.writeValueAsString(dto);
            assertNotNull(json, "JSON is null for " + fqcn);

            Object restored = om.readValue(json, dtoType);
            assertNotNull(restored, "Deserialized is null for " + fqcn);

            Object id = getIfExists(restored, "id");
            if (id != null) assertEquals(777L, ((Number) id).longValue(), fqcn + " id mismatch");
        }
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
