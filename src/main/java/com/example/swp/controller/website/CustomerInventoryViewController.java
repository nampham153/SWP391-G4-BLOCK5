package com.example.swp.controller.website;

import com.example.swp.dto.InventoryRequestCreateDTO;
import com.example.swp.service.InventoryRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerInventoryViewController {

    private final InventoryRequestService service;

    // Danh sách request của customer
    @GetMapping("/{customerId}/requests")
    public String viewRequests(@PathVariable Integer customerId, Model model) {
        model.addAttribute("requests", service.getRequestsByCustomer(customerId));
        model.addAttribute("customerId", customerId);
        return "customer_requests"; // file customer_requests.html
    }

    // Danh sách hàng trong kho
    @GetMapping("/{customerId}/items")
    public String viewItems(@PathVariable Integer customerId, Model model) {
        model.addAttribute("items", service.getStorageItems(customerId));
        model.addAttribute("customerId", customerId);
        return "customer_items"; // file customer_items.html
    }

    // Form tạo request nhập/xuất
    @GetMapping("/{customerId}/requests/new")
    public String showCreateForm(@PathVariable Integer customerId, Model model) {
        model.addAttribute("request", new InventoryRequestCreateDTO());
        model.addAttribute("customerId", customerId);
        return "customer_create_request"; // file customer_create_request.html
    }

    // Submit tạo request
    @PostMapping("/{customerId}/requests")
    public String createRequest(@PathVariable Integer customerId,
                                @ModelAttribute("request") InventoryRequestCreateDTO dto) {
        service.createRequest(customerId, dto);
        return "redirect:/customer/" + customerId + "/requests";
    }
}
