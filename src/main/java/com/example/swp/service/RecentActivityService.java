package com.example.swp.service;

import com.example.swp.entity.RecentActivity;

import java.util.List;

public interface RecentActivityService {
    List<RecentActivity> getAllActivities();
    RecentActivity save(RecentActivity activity);
    void deleteById(Integer id);
    void logActivity(RecentActivity activity);
    List<RecentActivity> getLoginActivities();
    List<RecentActivity> getVoucherActivities();

    List<RecentActivity> getStorageActivities();

    List<RecentActivity> getOrderActivities();
}
