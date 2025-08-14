package com.example.swp.service;

import com.example.swp.entity.VoucherUsage;

import java.util.List;

public abstract class VoucherUsageService {
    public abstract void save(VoucherUsage history);

    public abstract List<VoucherUsage> findAll();
}
