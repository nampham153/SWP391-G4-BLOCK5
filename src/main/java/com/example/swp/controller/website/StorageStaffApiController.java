package com.example.swp.controller.website;

import com.example.swp.entity.Storage;
import com.example.swp.entity.Staff;
import com.example.swp.service.StorageService;
import com.example.swp.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/admin/api")
public class StorageStaffApiController {

    private static final Logger logger = Logger.getLogger(StorageStaffApiController.class.getName());

    @Autowired
    private StorageService storageService;

    @Autowired
    private StaffService staffService;

    /**
     * Assign staff to storage
     */
    @PostMapping("/storages/{storageId}/assign-staff")
    public ResponseEntity<Map<String, Object>> assignStaffToStorage(
            @PathVariable int storageId,
            @RequestBody Map<String, Integer> request) {
        
        logger.info("Assigning staff to storage: " + storageId);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer staffId = request.get("staffId");
            if (staffId == null) {
                logger.warning("Staff ID is missing in request");
                response.put("success", false);
                response.put("message", "Staff ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("Looking for storage with ID: " + storageId);
            // Find storage
            Optional<Storage> storageOpt = storageService.findByID(storageId);
            if (storageOpt.isEmpty()) {
                logger.warning("Storage not found with ID: " + storageId);
                response.put("success", false);
                response.put("message", "Storage not found");
                return ResponseEntity.notFound().build();
            }

            logger.info("Looking for staff with ID: " + staffId);
            // Find staff
            Optional<Staff> staffOpt = staffService.findById(staffId);
            if (staffOpt.isEmpty()) {
                logger.warning("Staff not found with ID: " + staffId);
                response.put("success", false);
                response.put("message", "Staff not found");
                return ResponseEntity.badRequest().body(response);
            }

            Storage storage = storageOpt.get();
            Staff staff = staffOpt.get();
            
            logger.info("Assigning staff " + staff.getFullname() + " to storage " + storage.getStoragename());
            // Assign staff to storage
            storage.setStaff(staff);
            storageService.save(storage);

            response.put("success", true);
            response.put("message", "Staff assigned successfully");
            response.put("storageId", storageId);
            response.put("staffId", staffId);
            response.put("staffName", staff.getFullname());
            
            logger.info("Staff assignment successful");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Error assigning staff: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error assigning staff: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Remove staff from storage
     */
    @PostMapping("/storages/{storageId}/remove-staff")
    public ResponseEntity<Map<String, Object>> removeStaffFromStorage(
            @PathVariable int storageId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find storage
            Optional<Storage> storageOpt = storageService.findByID(storageId);
            if (storageOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Storage not found");
                return ResponseEntity.notFound().build();
            }

            Storage storage = storageOpt.get();
            
            // Remove staff assignment
            storage.setStaff(null);
            storageService.save(storage);

            response.put("success", true);
            response.put("message", "Staff removed successfully");
            response.put("storageId", storageId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error removing staff: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
