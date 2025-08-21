package com.example.swp.service;

import com.example.swp.entity.Customer;
import com.example.swp.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CustomerService {

    List<Customer> getAll();

    Customer getCustomer(int id);

    Optional<Customer> findByEmail1(String email);

    // Thêm nếu muốn search/filter
    List<Customer> searchByName(String name);

    List<Customer> filterByRole(RoleName roleName);

    // Pagination methods
    Page<Customer> getAllWithPagination(Pageable pageable);

    Page<Customer> searchByNameWithPagination(String name, Pageable pageable);

    Page<Customer> filterByRoleWithPagination(RoleName roleName, Pageable pageable);

    Customer save(Customer customer);

    void delete(int id);

    Customer findByEmail(String email);

    // Trong CustomerService.java
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // Khóa/mở khóa tài khoản
    void toggleAccountStatus(int customerId);


}
