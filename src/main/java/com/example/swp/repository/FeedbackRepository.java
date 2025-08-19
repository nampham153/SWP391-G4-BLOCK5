package com.example.swp.repository;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Feedback;
import com.example.swp.entity.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findByCustomerId(int id);        // id của Customer
    List<Feedback> findByStorageStorageid(int id);  // id của Storage
    List<Feedback> findByCustomer(Customer customer);
    boolean existsByCustomerAndStorage(Customer customer, Storage storage);
    Optional<Feedback> findByCustomerAndStorage(Customer customer, Storage storage);
    boolean existsByCustomer_IdAndStorage_Storageid(int customerId, int storageId);

}
