package com.smartpark.repository;

import com.smartpark.entity.ParkingSession;
import com.smartpark.entity.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    Optional<ParkingSession> findFirstByUserIdAndStatusOrderByEntryTimeDesc(Long userId, SessionStatus status);

    Optional<ParkingSession> findFirstByVehicleIdAndStatus(Long vehicleId, SessionStatus status);

    Optional<ParkingSession> findFirstBySlotIdAndStatus(Long slotId, SessionStatus status);

    boolean existsByUserIdAndStatus(Long userId, SessionStatus status);

    boolean existsByVehicleIdAndStatus(Long vehicleId, SessionStatus status);

    boolean existsBySlotIdAndStatus(Long slotId, SessionStatus status);

    List<ParkingSession> findByStatusOrderByEntryTimeDesc(SessionStatus status);

    Page<ParkingSession> findByUserIdOrderByEntryTimeDesc(Long userId, Pageable pageable);

    List<ParkingSession> findTop5ByUserIdOrderByEntryTimeDesc(Long userId);

    long countByStatus(SessionStatus status);

    long countByEntryTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByExitTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM ParkingSession s " +
           "WHERE s.user.id = :userId " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:search IS NULL OR LOWER(s.vehicle.registrationNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.slot.slotNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:fromDate IS NULL OR s.entryTime >= :fromDate) " +
           "AND (:toDate IS NULL OR s.entryTime <= :toDate) " +
           "ORDER BY s.entryTime DESC")
    Page<ParkingSession> filterStudentSessions(
            @Param("userId") Long userId,
            @Param("status") SessionStatus status,
            @Param("search") String search,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query("SELECT s FROM ParkingSession s " +
           "WHERE (:status IS NULL OR s.status = :status) " +
           "AND (:search IS NULL OR LOWER(s.vehicle.registrationNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.slot.slotNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.user.studentId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:fromDate IS NULL OR s.entryTime >= :fromDate) " +
           "AND (:toDate IS NULL OR s.entryTime <= :toDate) " +
           "ORDER BY s.entryTime DESC")
    Page<ParkingSession> filterAdminSessions(
            @Param("status") SessionStatus status,
            @Param("search") String search,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query("SELECT s FROM ParkingSession s " +
           "WHERE (:fromDate IS NULL OR s.entryTime >= :fromDate) " +
           "AND (:toDate IS NULL OR s.entryTime <= :toDate) " +
           "ORDER BY s.entryTime DESC")
    List<ParkingSession> findAllForReport(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}
