package com.example.swp.controller.website;

import com.example.swp.service.InventoryRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffInventoryViewController {

    private final InventoryRequestService service;

    // Danh sách request chờ xử lý
    @GetMapping("/{staffId}/requests")
    public String viewPendingRequests(@PathVariable Integer staffId, Model model) {
        model.addAttribute("requests", service.getPendingRequests());
        model.addAttribute("staffId", staffId);
        return "staff_requests"; // file staff_requests.html
    }

    // Duyệt request
    @PostMapping("/{staffId}/requests/{requestId}/approve")
    public String approveRequest(@PathVariable Integer staffId, @PathVariable Integer requestId) {
        service.approveRequest(requestId, staffId);
        return "redirect:/staff/" + staffId + "/requests";
    }

    // Từ chối request
    @PostMapping("/{staffId}/requests/{requestId}/reject")
    public String rejectRequest(@PathVariable Integer staffId,
                                @PathVariable Integer requestId,
                                @RequestParam String reason) {
        service.rejectRequest(requestId, reason, staffId);
        return "redirect:/staff/" + staffId + "/requests";
    }
}

