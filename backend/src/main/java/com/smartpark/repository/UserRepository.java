package com.smartpark.repository;

import com.smartpark.entity.Role;
import com.smartpark.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByStudentId(String studentId);

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    long countByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (" +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.studentId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsersByRole(@Param("role") Role role, @Param("search") String search, Pageable pageable);

    Page<User> findAllByRole(Role role, Pageable pageable);
}
