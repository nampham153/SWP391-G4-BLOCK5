package com.example.swp.repository;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Storage;
import com.example.swp.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {
    List<WishList> findByCustomer(Customer customer);
    boolean existsByCustomerAndStorage(Customer customer, Storage storage);
    void deleteByCustomerAndStorage(Customer customer, Storage storage);

}

