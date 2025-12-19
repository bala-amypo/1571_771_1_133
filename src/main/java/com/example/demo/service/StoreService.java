package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Store;

public interface StoreService {

    Store createStore(Store store);
    List<Store> getStore();
    Store getById(long id);
    Store getStoreById(long storeId);
}
