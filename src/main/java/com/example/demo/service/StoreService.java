package com.example.demo.service;

import com.example.demo.entity.Store;
import java.util.List;

public interface StoreService {
    Store createStore(Store store);
    Store getStoreById(long id);
    Store getStoreById(Long id);
    List<Store> getAllStores();
    Store updateStore(Long id, Store update);
    void deactivateStore(Long id);
    List<Store> getStore(); // Keep for backward compatibility
    Store getById(long id); // Keep for backward compatibility
}
