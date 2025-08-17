package com.example.swp.service;

import com.example.swp.entity.Manager;
import org.springframework.stereotype.Service;

@Service
public interface ManagerService {
    Manager findByEmail(String email);
}
