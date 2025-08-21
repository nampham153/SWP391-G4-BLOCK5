package com.example.swp.repository;

import com.example.swp.entity.LeaveRequest;
import com.example.swp.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStaff(Staff staff);
    List<LeaveRequest> findByStatus(LeaveRequest.Status status);
}
