package com.example.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Store;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.StoreRepository;
import com.example.demo.service.StoreService; 

@Service
public class StoreServiceimpl implements StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Override
    public Store createStore(Store store) {
        if (storeRepository.findByStoreName(store.getStoreName()).isPresent()) {
            throw new BadRequestException("Store name already exists");
        }
        return storeRepository.save(store);
    }

    @Override
    public Store getStoreById(long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
    }

    @Override
    public Store getStoreById(Long id) {
        return getStoreById(id.longValue());
    }

    @Override
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    @Override
    public List<Store> getStore() {
        return getAllStores();
    }

    @Override
    public Store getById(long id) {
        return getStoreById(id);
    }

    @Override
    public Store updateStore(Long id, Store update) {
        Store store = getStoreById(id);
        
        // Check if new store name already exists (if changed)
        if (!store.getStoreName().equals(update.getStoreName()) && 
            storeRepository.findByStoreName(update.getStoreName()).isPresent()) {
            throw new BadRequestException("Store name already exists");
        }
        
        store.setStoreName(update.getStoreName());
        store.setAddress(update.getAddress());
        store.setRegion(update.getRegion());
        store.setActive(update.isActive());
        
        return storeRepository.save(store);
    }

    @Override
    public void deactivateStore(Long id) {
        Store store = getStoreById(id);
        store.setActive(false);
        storeRepository.save(store);
    }
}