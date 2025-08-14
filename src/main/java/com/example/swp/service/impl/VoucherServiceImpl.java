package com.example.swp.service.impl;

import com.example.swp.entity.Voucher;
import com.example.swp.enums.VoucherStatus;
import com.example.swp.repository.VoucherRepository;
import com.example.swp.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Optional<Voucher> getVoucherById(int id) {
        return voucherRepository.findById(id);
    }

    @Override
    public Voucher saveVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    public void deleteVoucher(int id) {
        voucherRepository.deleteById(id);
    }

    @Override
    public List<Voucher> getAvailableVouchersForCustomer(int customerPoint) {
        // Lấy các voucher status ACTIVE, đủ điểm
        return voucherRepository.findByStatusAndRequiredPointLessThanEqualOrderByRequiredPointAsc(
                VoucherStatus.ACTIVE, customerPoint);
    }

    @Override
    public List<Voucher> getVouchersByStatus(VoucherStatus status) {
        return voucherRepository.findByStatus(status);
    }

    @Override
    public long countByStatus(VoucherStatus status) {
        return voucherRepository.countByStatus(status);
    }

    @Override
    public Optional<Voucher> getBestEligibleVoucher(int customerPoint) {
        List<Voucher> eligibleVouchers = getAvailableVouchersForCustomer(customerPoint);
        return eligibleVouchers.stream()
                .sorted(Comparator.comparing(Voucher::getDiscountAmount).reversed())
                .findFirst(); // Ưu tiên giảm giá nhiều nhất
    }
}