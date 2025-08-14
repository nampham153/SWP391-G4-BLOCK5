package com.example.swp.service.impl;

import com.example.swp.entity.VoucherUsage;
import com.example.swp.repository.VoucherUsageRepository;
import com.example.swp.service.VoucherUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoucherUsageServiceImpl extends VoucherUsageService {

    @Autowired
    private VoucherUsageRepository repository;

    @Override
    public void save(VoucherUsage history) {
        repository.save(history);
    }

    @Override
    public List<VoucherUsage> findAll() {
        return repository.findAll();
    }
}
