package com.example.swp.repository;

import com.example.swp.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManageRepository extends JpaRepository<Manager, Integer> {
    Optional<Manager> findByEmail(String email);
    Optional<Manager> findFirstByOnDutyTrue();

}
