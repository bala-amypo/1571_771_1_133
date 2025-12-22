package com.example.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Store;
import com.example.demo.repository.StoreRepository;
import com.example.demo.service.StoreService; 
@Service
public class StoreServiceimpl implements StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Override
    public Store createStore(Store store) {

        if (storeRepository.findByStoreName(store.getStoreName()).isPresent()) {
            throw new RuntimeException("Store name already exists");
        }

        return storeRepository.save(store);
    }

    @Override
    public List<Store> getStore() {
        return storeRepository.findAll();
    }

    @Override
    public Store getById(long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));
    }

   
    @Override
public Store getStoreById(Long storeId) {
    return storeRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Store not found"));
}
 @Override
    public Store getStoreById(long storeId) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getStoreById'");
    }

}
 
