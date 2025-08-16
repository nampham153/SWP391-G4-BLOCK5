package com.example.swp.controller.api;

import com.example.swp.entity.Storage;
import com.example.swp.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StorageApiController {
    
    @Autowired
    private StorageService storageService;

    @GetMapping("/storages/search")
    public ResponseEntity<List<Storage>> searchStorages(
            @RequestParam(required = false) String storageName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status) {
        try {
            System.out.println("API Search called with params:");
            System.out.println("- storageName: " + storageName);
            System.out.println("- city: " + city);
            System.out.println("- status: " + status);

            List<Storage> allStorages = storageService.getAll();
            System.out.println("Total storages from service: " + allStorages.size());

            List<Storage> filteredStorages = filterStorages(allStorages, storageName, city, status);
            System.out.println("Filtered storages: " + filteredStorages.size());

            return ResponseEntity.ok(filteredStorages);
        } catch (Exception e) {
            System.err.println("Error searching storages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    private List<Storage> filterStorages(List<Storage> storages, String storageName, String city, String status) {
        return storages.stream()
                .filter(storage -> {
                    // Filter by storage name
                    if (storageName != null && !storageName.trim().isEmpty()) {
                        String searchName = storageName.trim().toLowerCase();
                        String actualName = storage.getStoragename() != null ? storage.getStoragename().toLowerCase()
                                : "";
                        String actualAddress = storage.getAddress() != null ? storage.getAddress().toLowerCase() : "";
                        if (!actualName.contains(searchName) && !actualAddress.contains(searchName)) {
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
