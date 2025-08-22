package com.example.swp.service;

import com.example.swp.entity.LeaveRequest;
import com.example.swp.entity.Staff;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequest createRequest(LeaveRequest request);
    List<LeaveRequest> getRequestsByStaff(Staff staff);
    List<LeaveRequest> getPendingRequests();
    List<LeaveRequest> getAllRequests();
    List<LeaveRequest> getRequestsByStatus(String status);
    LeaveRequest approveRequest(Long id, String managerNote);
    LeaveRequest rejectRequest(Long id, String managerNote);
}
