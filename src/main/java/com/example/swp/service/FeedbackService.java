package com.example.swp.service;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Feedback;
import com.example.swp.entity.Storage;

import java.util.List;
import java.util.Optional;

public interface FeedbackService {
    List<Feedback> getAllFeedbacks();
    Optional<Feedback> getFeedbackById(int id);
    List<Feedback> findByCustomerId(int customerId);
    List<Feedback> findByStorageId(int storageId);   // thêm dòng này
    Feedback save(Feedback feedback);
    Feedback createFeedback(int storageId, int customerId, String content, int rating);
    void deleteFeedback(int id);
    boolean hasCustomerFeedbacked(int customerId, int storageId);


}

