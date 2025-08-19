package com.example.swp.repository;

import com.example.swp.entity.RecentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecentActivityRepository extends JpaRepository<RecentActivity, Integer> {
    List<RecentActivity> findByActionOrderByTimestampDesc(String action);
}
