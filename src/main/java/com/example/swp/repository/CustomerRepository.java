package com.example.swp.repository;

import com.example.swp.entity.Customer;
import com.example.swp.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);

    List<Customer> findByFullnameContainingIgnoreCase(String fullname);

    List<Customer> findByRoleName(RoleName roleName);

    // Pagination methods
    Page<Customer> findAll(Pageable pageable);

    Page<Customer> findByFullnameContainingIgnoreCase(String fullname, Pageable pageable);

    Page<Customer> findByRoleName(RoleName roleName, Pageable pageable);

    // Trong CustomerRepository.java
    boolean existsByEmail(String email);

    // Nếu muốn validate phone, hãy bổ sung:
    boolean existsByPhone(String phone);

}
