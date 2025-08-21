package com.example.swp.service.impl;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Storage;
import com.example.swp.entity.WishList;
import com.example.swp.repository.WishListRepository;
import com.example.swp.service.WishListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class WishListServiceImpl implements WishListService {

    @Autowired
    private WishListRepository wishListRepository;

    @Override
    public void addToWishList(Customer customer, Storage storage) {
        if (!wishListRepository.existsByCustomerAndStorage(customer, storage)) {
            WishList wishList = new WishList();
            wishList.setCustomer(customer);
            wishList.setStorage(storage);
            wishList.setDate(new Date());
            wishListRepository.save(wishList);
        }
    }
    @Transactional // <-- Bổ sung dòng này


    @Override
    public void removeFromWishList(Customer customer, Storage storage) {
        wishListRepository.deleteByCustomerAndStorage(customer, storage);
    }

    @Override
    public List<WishList> getWishListByCustomer(Customer customer) {
        return wishListRepository.findByCustomer(customer);
    }
}
