package com.smartpark.repository;

import com.smartpark.entity.ParkingSlot;
import com.smartpark.entity.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    Optional<ParkingSlot> findBySlotNumber(String slotNumber);

    boolean existsBySlotNumber(String slotNumber);

    List<ParkingSlot> findByZoneId(Long zoneId);

    List<ParkingSlot> findByStatus(SlotStatus status);

    long countByStatus(SlotStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ParkingSlot s WHERE s.id = :id")
    Optional<ParkingSlot> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT s FROM ParkingSlot s " +
           "WHERE (:zoneId IS NULL OR s.zone.id = :zoneId) " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:search IS NULL OR LOWER(s.slotNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY s.slotNumber ASC")
    List<ParkingSlot> filterSlots(
            @Param("zoneId") Long zoneId,
            @Param("status") SlotStatus status,
            @Param("search") String search);
}
