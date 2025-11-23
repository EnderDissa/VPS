package com.example.warehouse.repository;

import com.example.warehouse.entity.Storage;
import com.example.warehouse.entity.Transportation;
import com.example.warehouse.enumeration.TransportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransportationRepository extends ReactiveCrudRepository<Transportation, Long> {

    Flux<Transportation> findByStatus(TransportStatus status, Pageable pageable);

    Flux<Transportation> findByItemId(Long itemId, Pageable pageable);

    Flux<Transportation> findByFromStorageId(Long fromStorageId, Pageable pageable);

    Flux<Transportation> findByToStorageId(Long toStorageId, Pageable pageable);

    Flux<Transportation> findByStatusAndItemId(TransportStatus status, Long itemId, Pageable pageable);

    Flux<Transportation> findByStatusAndFromStorageId(TransportStatus status, Long fromStorageId, Pageable pageable);

    Flux<Transportation> findByStatusAndToStorageId(TransportStatus status, Long toStorageId, Pageable pageable);

    Flux<Transportation> findByItemIdAndFromStorageId(Long itemId, Long fromStorageId, Pageable pageable);

    Flux<Transportation> findAllBy(Pageable pageable);

    Flux<Transportation> findByStatusAndItemIdAndFromStorageIdAndToStorageId(
            TransportStatus status, Long itemId, Long fromStorageId, Long toStorageId, Pageable pageable);

    @Query("SELECT t FROM Transportation t WHERE t.status = 'IN_PROGRESS' AND t.scheduledArrival < :now")
    Flux<Transportation> findOverdueTransportations(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(t) = 0 THEN true ELSE false END FROM Transportation t " +
            "WHERE t.driver.id = :driverId AND t.status IN ('PLANNED', 'IN_PROGRESS') " +
            "AND ((t.scheduledDeparture BETWEEN :start AND :end) OR (t.scheduledArrival BETWEEN :start AND :end))")
    Mono<Boolean> isDriverAvailable(@Param("driverId") Long driverId,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("SELECT CASE WHEN COUNT(t) = 0 THEN true ELSE false END FROM Transportation t " +
            "WHERE t.vehicle.id = :vehicleId AND t.status IN ('PLANNED', 'IN_PROGRESS') " +
            "AND ((t.scheduledDeparture BETWEEN :start AND :end) OR (t.scheduledArrival BETWEEN :start AND :end))")
    Mono<Boolean> isVehicleAvailable(@Param("vehicleId") Long vehicleId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    Mono<Long> countByStatus(TransportStatus status);
}