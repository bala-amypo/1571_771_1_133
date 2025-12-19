package com.example.demo.service.impl;

import com.example.demo.entity.InventoryLevel;
import com.example.demo.entity.Store;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.InventoryLevelRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.service.InventoryLevelService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryLevelServiceimpl implements InventoryLevelService {

    private final InventoryLevelRepository inventoryLevelRepository;
    private final StoreRepository storeRepository;

    public InventoryLevelServiceimpl(
            InventoryLevelRepository inventoryLevelRepository,
            StoreRepository storeRepository) {
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public List<InventoryLevel> getInventoryByStore(Long storeId) {

        // ✅ 1. Fetch store
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Store not found"));

        // ✅ 2. THIS IS WHERE YOUR CODE GOES
        List<InventoryLevel> inventoryLevels =
                inventoryLevelRepository.findByStore(store);

        if (inventoryLevels.isEmpty()) {
            throw new ResourceNotFoundException("Inventory not found");
        }

        return inventoryLevels;
    }
}
