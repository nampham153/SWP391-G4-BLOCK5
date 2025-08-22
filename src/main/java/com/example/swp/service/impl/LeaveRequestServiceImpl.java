package com.example.swp.service.impl;

import com.example.swp.entity.LeaveRequest;
import com.example.swp.entity.Staff;
import com.example.swp.repository.LeaveRequestRepository;
import com.example.swp.service.EmailService;
import com.example.swp.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public LeaveRequest createRequest(LeaveRequest request) {
        LeaveRequest savedRequest = leaveRequestRepository.save(request);

        // Gửi mail cho manager
        String managerEmail = "hongquanvjp@gmail.com"; // Cứng hoặc lấy từ DB
        String subject = "Đơn xin nghỉ phép mới từ " + request.getStaff().getFullname();
        String content = "Nhân viên: " + request.getStaff().getFullname() +
                "\nTừ ngày: " + request.getFromDate() +
                "\nĐến ngày: " + request.getToDate() +
                "\nLoại phép: " + request.getLeaveType() +
                "\nLý do: " + request.getReason();

        emailService.sendEmail(managerEmail, subject, content);

        return savedRequest;
    }

// Neu muon lay mail Manager dong
//    @Autowired
//    private StaffRepository staffRepository;
//
//    List<Staff> managers = staffRepository.findByRoleName(RoleName.MANAGER);
//for (Staff manager : managers) {
//        emailService.sendEmail(manager.getEmail(), subject, content);
//    }


    @Override
    public List<LeaveRequest> getRequestsByStaff(Staff staff) {
        return leaveRequestRepository.findByStaff(staff);
    }

    @Override
    public List<LeaveRequest> getPendingRequests() {
        return leaveRequestRepository.findByStatus(LeaveRequest.Status.CHO_DUYET);
    }

    @Override
    public List<LeaveRequest> getAllRequests() {
        return leaveRequestRepository.findAll();  // ✅ Load tất cả đơn nghỉ phép
    }

    @Override
    public List<LeaveRequest> getRequestsByStatus(String status) {
        LeaveRequest.Status enumStatus;
        try {
            enumStatus = LeaveRequest.Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + status);
        }
        return leaveRequestRepository.findByStatus(enumStatus);
    }


    @Override
    public LeaveRequest approveRequest(Long id, String managerNote) {
        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn nghỉ phép"));
        req.setStatus(LeaveRequest.Status.DUYET);
        req.setManagerNote(managerNote);
        return leaveRequestRepository.save(req);
    }

    @Override
    public LeaveRequest rejectRequest(Long id, String managerNote) {
        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn nghỉ phép"));
        req.setStatus(LeaveRequest.Status.TU_CHOI);
        req.setManagerNote(managerNote);
        return leaveRequestRepository.save(req);
    }
}
