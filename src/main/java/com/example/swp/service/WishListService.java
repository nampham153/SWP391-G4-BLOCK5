package com.example.swp.service;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Storage;
import com.example.swp.entity.WishList;

import java.util.List;

public interface WishListService {
    void addToWishList(Customer customer, Storage storage);
    void removeFromWishList(Customer customer, Storage storage);
    List<WishList> getWishListByCustomer(Customer customer);

}
