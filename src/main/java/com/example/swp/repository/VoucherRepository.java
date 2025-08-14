package com.example.swp.repository;

import com.example.swp.entity.Voucher;
import com.example.swp.enums.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    // Lấy các voucher đủ điểm và trạng thái (ví dụ ACTIVE), sắp xếp tăng dần requiredPoint
    List<Voucher> findByStatusAndRequiredPointLessThanEqualOrderByRequiredPointAsc(VoucherStatus status, Integer customerPoint);

    // Lấy tất cả voucher theo trạng thái
    List<Voucher> findByStatus(VoucherStatus status);

    long countByStatus(VoucherStatus status);
}
