package com.example.swp.service.impl;

import com.example.swp.entity.Manager;
import com.example.swp.repository.ManageRepository;
import com.example.swp.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerServiceImpl implements ManagerService {
    @Autowired
    private ManageRepository managerRepository;
    @Override
    public Manager findByEmail(String email) {
        return managerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found with email: " + email));
    }
}
