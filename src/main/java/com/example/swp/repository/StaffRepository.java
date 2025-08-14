package com.example.swp.repository;

import com.example.swp.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByEmail(String email);

//    Staff findByStaffEmail(String email);

    Page<Staff> findAll(Pageable pageable);
    @Query("SELECT COUNT(s) FROM Staff s")
    int countAllStaff();


}
