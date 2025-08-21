package com.example.swp.controller.website;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.swp.entity.Staff;
import com.example.swp.service.StaffService;

@RestController
@RequestMapping("/admin/api")
public class StaffApiController {

    private static final Logger logger = Logger.getLogger(StaffApiController.class.getName());

    @Autowired
    private StaffService staffService;

    /**
     * Get list of all staff members
     */
    @GetMapping("/staff/list")
    public ResponseEntity<List<Staff>> getAllStaff() {
        try {
            logger.info("Fetching all staff members");
            List<Staff> staffList = staffService.getAllStaff();
            logger.info("Found " + staffList.size() + " staff members");
            return ResponseEntity.ok(staffList);
        } catch (Exception e) {
            logger.severe("Error fetching staff list: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
