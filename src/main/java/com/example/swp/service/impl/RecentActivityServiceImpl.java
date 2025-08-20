package com.example.swp.service.impl;

import com.example.swp.entity.RecentActivity;
import com.example.swp.repository.RecentActivityRepository;
import com.example.swp.service.RecentActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecentActivityServiceImpl implements RecentActivityService {

    @Autowired
    private RecentActivityRepository recentActivityRepository;

    @Override
    public List<RecentActivity> getAllActivities() {
        return recentActivityRepository.findAll();
    }

    @Override
    public RecentActivity save(RecentActivity activity) {
        return recentActivityRepository.save(activity);
    }

    @Override
    public void deleteById(Integer id) {
        recentActivityRepository.deleteById(id);
    }

    @Override
    public void logActivity(RecentActivity activity) {
        recentActivityRepository.save(activity);
    }

    @Override
    public List<RecentActivity> getLoginActivities() {
        return recentActivityRepository.findByActionOrderByTimestampDesc("Người dùng đăng nhập vào hệ thống");

    }

    @Override
    public List<RecentActivity> getVoucherActivities() {
        return recentActivityRepository.findByActionOrderByTimestampDesc("Tạo voucher mới");
    }

    @Override
    public List<RecentActivity> getStorageActivities() {
        return recentActivityRepository.findByActionOrderByTimestampDesc("Thêm kho hàng");
    }

    @Override
    public List<RecentActivity> getOrderActivities() {
        return recentActivityRepository.findByActionOrderByTimestampDesc("Đơn hàng được thêm mới");
    }
}