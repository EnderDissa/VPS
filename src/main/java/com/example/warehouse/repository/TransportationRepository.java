package com.example.warehouse.repository;

import com.example.warehouse.entity.Transportation;
import com.example.warehouse.enumeration.TransportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransportationRepository extends JpaRepository<Transportation, Long> {

    Page<Transportation> findByStatus(TransportStatus status, Pageable pageable);

    Page<Transportation> findByItemId(Long itemId, Pageable pageable);

    Page<Transportation> findByFromStorageId(Long fromStorageId, Pageable pageable);

    Page<Transportation> findByToStorageId(Long toStorageId, Pageable pageable);

    Page<Transportation> findByStatusAndItemId(TransportStatus status, Long itemId, Pageable pageable);

    Page<Transportation> findByStatusAndFromStorageId(TransportStatus status, Long fromStorageId, Pageable pageable);

    Page<Transportation> findByStatusAndToStorageId(TransportStatus status, Long toStorageId, Pageable pageable);

    Page<Transportation> findByItemIdAndFromStorageId(Long itemId, Long fromStorageId, Pageable pageable);

    Page<Transportation> findByStatusAndItemIdAndFromStorageIdAndToStorageId(
            TransportStatus status, Long itemId, Long fromStorageId, Long toStorageId, Pageable pageable);

    @Query("SELECT t FROM Transportation t WHERE t.status = 'IN_PROGRESS' AND t.scheduledArrival < :now")
    Page<Transportation> findOverdueTransportations(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transportation t WHERE t.driver.id = :driverId AND t.status IN ('PLANNED', 'IN_PROGRESS') " +
            "AND ((t.scheduledDeparture BETWEEN :start AND :end) OR (t.scheduledArrival BETWEEN :start AND :end))")
    long countDriverTransportationsInPeriod(@Param("driverId") Long driverId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Transportation t WHERE t.vehicle.id = :vehicleId AND t.status IN ('PLANNED', 'IN_PROGRESS') " +
            "AND ((t.scheduledDeparture BETWEEN :start AND :end) OR (t.scheduledArrival BETWEEN :start AND :end))")
    long countVehicleTransportationsInPeriod(@Param("vehicleId") Long vehicleId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query("SELECT CASE WHEN COUNT(t) = 0 THEN true ELSE false END FROM Transportation t " +
            "WHERE t.driver.id = :driverId AND t.status IN ('PLANNED', 'IN_PROGRESS') " +
            "AND ((t.scheduledDeparture BETWEEN :start AND :end) OR (t.scheduledArrival BETWEEN :start AND :end))")
    boolean isDriverAvailable(@Param("driverId") Long driverId,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("SELECT CASE WHEN COUNT(t) = 0 THEN true ELSE false END FROM Transportation t " +
            "WHERE t.vehicle.id = :vehicleId AND t.status IN ('PLANNED', 'IN_PROGRESS') " +
            "AND ((t.scheduledDeparture BETWEEN :start AND :end) OR (t.scheduledArrival BETWEEN :start AND :end))")
    boolean isVehicleAvailable(@Param("vehicleId") Long vehicleId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    long countByStatus(TransportStatus status);

    // ДОБАВЛЕННЫЙ МЕТОД
    long countByVehicleIdAndStatusIn(Long vehicleId, List<TransportStatus> statuses);

    List<Transportation> findByDriverIdAndStatusIn(Long driverId, List<TransportStatus> statuses);

    List<Transportation> findByVehicleIdAndStatusIn(Long vehicleId, List<TransportStatus> statuses);
}