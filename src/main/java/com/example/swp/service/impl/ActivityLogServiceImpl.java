package com.example.swp.service.impl;

import com.example.swp.entity.*;
import com.example.swp.enums.ActivityType;
import com.example.swp.repository.ActivityLogRepository;
import com.example.swp.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Override
    public void logActivity(String action, String description, Customer customer,
                            Order order, StorageTransaction transaction,
                            Payment payment, Feedback feedback, Issue issue) {
        ActivityLog log = new ActivityLog();
        log.setAction(action);
        log.setDescription(description);
        log.setCustomer(customer);
        log.setOrder(order);
        log.setTransaction(transaction);
        log.setPayment(payment);
        log.setFeedback(feedback);
        log.setIssue(issue);
        log.setTimestamp(LocalDateTime.now());

        // Gán ActivityType theo object không null
        if (order != null) log.setType(ActivityType.ORDER);
        else if (transaction != null) log.setType(ActivityType.TRANSACTION);
        else if (payment != null) log.setType(ActivityType.PAYMENT);
        else if (feedback != null) log.setType(ActivityType.FEEDBACK);
        else if (issue != null) log.setType(ActivityType.ISSUE);
        else log.setType(ActivityType.ACCOUNT); // fallback

        activityLogRepository.save(log);
    }

    @Override
    public List<ActivityLog> getRecentActivities(Customer customer, int limit) {
        List<ActivityLog> all = activityLogRepository.findByCustomerOrderByTimestampDesc(customer);
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    @Override
    public List<ActivityLog> getAllByCustomer(Customer customer) {
        return activityLogRepository.findByCustomerOrderByTimestampDesc(customer);
    }

    @Override
    public List<ActivityLog> getByOrder(Customer customer) {
        return activityLogRepository.findByCustomerAndTypeOrderByTimestampDesc(customer, ActivityType.ORDER);
    }

    @Override
    public List<ActivityLog> getByFeedback(Customer customer) {
        return activityLogRepository.findByCustomerAndTypeOrderByTimestampDesc(customer, ActivityType.FEEDBACK);
    }

    @Override
    public List<ActivityLog> getByTransaction(Customer customer) {
        return activityLogRepository.findByCustomerAndTypeOrderByTimestampDesc(customer, ActivityType.TRANSACTION);
    }

    @Override
    public List<ActivityLog> getByIssue(Customer customer) {
        return activityLogRepository.findByCustomerAndTypeOrderByTimestampDesc(customer, ActivityType.ISSUE);
    }

    @Override
    public List<ActivityLog> getByAccount(Customer customer) {
        return activityLogRepository.findByCustomerAndTypeOrderByTimestampDesc(customer, ActivityType.ACCOUNT);
    }
}
