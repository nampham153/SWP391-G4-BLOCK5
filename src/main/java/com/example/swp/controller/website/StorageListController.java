package com.example.swp.controller.website;

import com.example.swp.entity.Manager;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Storage;
import com.example.swp.service.StorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/SWP")
public class StorageListController {
    @Autowired
    private StorageService storageService;

    @GetMapping("/storages")
    public String listStorage(
            @RequestParam(required = false) String storageName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "storagename") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra xem có phải staff hoặc manager đang truy cập không
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        Manager manager = (Manager) session.getAttribute("loggedInManager");

        if (staff != null) {
            redirectAttributes.addFlashAttribute("error",
                    "Trang này chỉ dành cho khách hàng. Bạn đã được chuyển về trang quản lý khách hàng.");
            return "redirect:/SWP/customers";
        }

        if (manager != null) {
            redirectAttributes.addFlashAttribute("error",
                    "Trang này chỉ dành cho khách hàng. Bạn đã được chuyển về trang quản lý khách hàng.");
            return "redirect:/admin/manager-customer-list";
        }
        try {
            // Tạo Sort object
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            // Tạo Pageable object
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Lấy dữ liệu với phân trang và sắp xếp
            Page<Storage> storagePage;
            if ((storageName != null && !storageName.trim().isEmpty()) ||
                (city != null && !city.trim().isEmpty()) ||
                (status != null && !status.trim().isEmpty())) {
                storagePage = storageService.getStoragesWithFilters(storageName, city, status, pageable);
            } else {
                storagePage = storageService.getAllStorages(pageable);
            }
            
            System.out.println("Total storages: " + storagePage.getTotalElements());
            System.out.println("Current page: " + storagePage.getNumber());
            System.out.println("Total pages: " + storagePage.getTotalPages());

            // Lấy danh sách cities để hiển thị trong dropdown
            List<String> cities = storageService.findAllCities();

            // Thêm vào model
            model.addAttribute("storages", storagePage.getContent());
            model.addAttribute("storagePage", storagePage);
            model.addAttribute("cities", cities);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", storagePage.getTotalPages());
            model.addAttribute("totalElements", storagePage.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);

            // Keep search parameters for form persistence
            model.addAttribute("searchStorageName", storageName);
            model.addAttribute("searchCity", city);
            model.addAttribute("searchStatus", status);

            return "storage-list";
        } catch (Exception e) {
            System.err.println("Error loading storages: " + e.getMessage());
            e.printStackTrace();

            // Trả về trang với danh sách rỗng nếu có lỗi
            model.addAttribute("storages", new ArrayList<>());
            model.addAttribute("cities", new ArrayList<>());
            return "storage-list";
        }
    }

    // API endpoint for real-time search
    @GetMapping(value = "/storages/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<Storage>> searchStorages(
            @RequestParam(required = false) String storageName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status) {
        try {
            System.out.println("=== Search API called ===");
            System.out.println("- storageName: " + storageName);
            System.out.println("- city: " + city);
            System.out.println("- status: " + status);

            List<Storage> allStorages = storageService.getAll();
            System.out.println("Total storages from service: " + allStorages.size());

            List<Storage> filteredStorages = filterStorages(allStorages, storageName, city, status);
            System.out.println("Filtered storages: " + filteredStorages.size());

            // Log first storage for debugging
            if (!filteredStorages.isEmpty()) {
                Storage firstStorage = filteredStorages.get(0);
                System.out
                        .println("First storage: " + firstStorage.getStoragename() + " - " + firstStorage.getAddress());
            }

            return ResponseEntity.ok(filteredStorages);
        } catch (Exception e) {
            System.err.println("=== Error searching storages ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    private List<Storage> filterStorages(List<Storage> storages, String storageName, String city, String status) {
        return storages.stream()
                .filter(storage -> {
                    // Filter by storage name only (not address)
                    if (storageName != null && !storageName.trim().isEmpty()) {
                        String searchName = storageName.trim().toLowerCase();
                        String actualName = storage.getStoragename() != null ? storage.getStoragename().toLowerCase()
                                : "";
                        if (!actualName.contains(searchName)) {
                            return false;
                        }
                    }

                    // Filter by city
                    if (city != null && !city.trim().isEmpty()) {
                        String actualCity = storage.getCity() != null ? storage.getCity() : "";
                        if (!actualCity.equals(city)) {
                            return false;
                        }
                    }

                    // Filter by status
                    if (status != null && !status.trim().isEmpty()) {
                        boolean searchStatus = Boolean.parseBoolean(status);
                        if (storage.isStatus() != searchStatus) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

}