package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.VehicleDTO;
import com.example.warehouse.enumeration.VehicleStatus;
import org.springframework.data.domain.Page;

public interface VehicleService {
    VehicleDTO create(VehicleDTO dto);
    VehicleDTO getById(Long id);
    void update(Long id, VehicleDTO dto);
    void delete(Long id);
    Page<VehicleDTO> findPage(int page, int size, VehicleStatus status, String brand, String model);
}
