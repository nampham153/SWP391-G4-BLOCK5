package com.example.swp.service.impl;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Feedback;
import com.example.swp.entity.Storage;
import com.example.swp.repository.CustomerRepository;
import com.example.swp.repository.FeedbackRepository;
import com.example.swp.repository.OrderRepository;
import com.example.swp.repository.StorageRepository;
import com.example.swp.service.ActivityLogService;
import com.example.swp.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private StorageRepository storageRepo;
    @Autowired
    private ActivityLogService activityLogService;


    @Override
    public void deleteFeedback(int id) {
        feedbackRepository.deleteById(id);
    }

    @Override
    public boolean hasCustomerFeedbacked(int customerId, int storageId) {
        return feedbackRepository.existsByCustomer_IdAndStorage_Storageid(customerId, storageId);
    }
    @Override
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Override
    public Optional<Feedback> getFeedbackById(int id) {
        return feedbackRepository.findById(id);
    }

    @Override
    public List<Feedback> findByCustomerId(int customerId) {
        return feedbackRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Feedback> findByStorageId(int storageId) {
        return feedbackRepository.findByStorageStorageid(storageId);
    }
    @Override
    public Feedback save(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }
    @Override
    public Feedback createFeedback(int storageId, int customerId, String content, int rating) {
        Feedback feedback = new Feedback();
        feedback.setCustomer(customerRepo.findById(customerId).orElseThrow());
        feedback.setStorage(storageRepo.findById(storageId).orElseThrow());
        feedback.setContent(content);
        feedback.setRating(rating);
        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Ghi nhật ký hoạt động
        activityLogService.logActivity(
                "Gửi phản hồi",
                "Khách hàng " + savedFeedback.getCustomer().getFullname()
                        + " gửi feedback cho kho #" + savedFeedback.getStorage().getStorageid()
                        + " với nội dung: " + savedFeedback.getContent()
                        + " - Đánh giá: " + savedFeedback.getRating() + "/5 sao",
                savedFeedback.getCustomer(),
                null, null, null, savedFeedback, null
        );

        return savedFeedback;
    }


}
