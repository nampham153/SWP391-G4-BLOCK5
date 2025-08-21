package com.example.swp.controller.api;

import com.example.swp.dto.ManagerNoteDTO;
import com.example.swp.entity.LeaveRequest;
import com.example.swp.entity.Staff;
import com.example.swp.repository.StaffRepository;
import com.example.swp.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private StaffRepository staffReponsitory;

    private Staff getCurrentStaff(Principal principal) {
        String email = principal.getName();
        return staffReponsitory.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy staff"));
    }

//    private Staff getCurrentStaff(Principal principal) {
//        // Tạm test: lấy staff có email cố định
//        return staffReponsitory.findByEmail("hongquanvjp@gmail.com")
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy staff test"));
//    }

    // Staff gửi đơn nghỉ phép
    @PostMapping
    public LeaveRequest createRequest(@RequestBody LeaveRequest request, Principal principal) {
        Staff staff = getCurrentStaff(principal);
        request.setStaff(staff);
        return leaveRequestService.createRequest(request);
    }

    // Staff xem đơn nghỉ phép của mình
    @GetMapping("/my")
    public List<LeaveRequest> myRequests(Principal principal) {
        Staff staff = getCurrentStaff(principal);
        return leaveRequestService.getRequestsByStaff(staff);
    }

    @GetMapping("/api/leave-requests")
    public List<LeaveRequest> getRequests(@RequestParam(required = false) String status) {
        if (status == null) {
            return leaveRequestService.getAllRequests();
        } else {
            return leaveRequestService.getRequestsByStatus(status);
        }
    }



    // Manager xem tất cả đơn nghỉ phép đang chờ duyệt
    @GetMapping("/pending")
    public List<LeaveRequest> pendingRequests() {
        return leaveRequestService.getPendingRequests();
    }

    // Manager duyệt đơn
    @PutMapping("/{id}/approve")
    public LeaveRequest approveRequest(@PathVariable Long id, @RequestBody ManagerNoteDTO noteDto) {
        return leaveRequestService.approveRequest(id, noteDto.getManagerNote());
    }

    // Manager từ chối đơn
    @PutMapping("/{id}/reject")
    public LeaveRequest rejectRequest(@PathVariable Long id, @RequestBody ManagerNoteDTO noteDto) {
        return leaveRequestService.rejectRequest(id, noteDto.getManagerNote());
    }

    @GetMapping
    public List<LeaveRequest> getAllRequests() {
        return leaveRequestService.getAllRequests();
    }
}
