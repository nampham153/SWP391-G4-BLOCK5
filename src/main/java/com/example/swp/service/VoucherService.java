package com.example.swp.service;

import com.example.swp.entity.Voucher;
import com.example.swp.enums.VoucherStatus;

import java.util.List;
import java.util.Optional;

public interface VoucherService {

    List<Voucher> getAllVouchers();

    Optional<Voucher> getVoucherById(int id);

    Voucher saveVoucher(Voucher voucher);

    void deleteVoucher(int id);

    // Lấy các voucher khách đủ điểm để đổi (status = ACTIVE)
    List<Voucher> getAvailableVouchersForCustomer(int customerPoint);

    List<Voucher> getVouchersByStatus(VoucherStatus status);

    long countByStatus(VoucherStatus status);

    Optional<Voucher> getBestEligibleVoucher(int customerPoint);
}