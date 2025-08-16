package com.example.swp.controller.website;

import com.example.swp.entity.Storage;
import com.example.swp.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

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
            Model model) {
        try {
            // Lấy tất cả storage từ database
            List<Storage> allStorages = storageService.getAll();
            System.out.println("All storages from database: " + allStorages.size());

            // Filter storages based on search criteria
            List<Storage> filteredStorages = filterStorages(allStorages, storageName, city, status);
            System.out.println("Filtered storages: " + filteredStorages.size());

            // Lấy danh sách cities để hiển thị trong dropdown
            List<String> cities = storageService.findAllCities();
            System.out.println("Cities found: " + cities.size());
            System.out.println("Cities list: " + cities);

            // Thêm vào model
            model.addAttribute("storages", filteredStorages);
            model.addAttribute("cities", cities);

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