package com.example.demo.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.entity.InventoryLevel;
import com.example.demo.entity.Product;
import com.example.demo.entity.Store;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.InventoryLevelRepository;
import com.example.demo.service.InventoryLevelService;
import com.example.demo.service.ProductService;
import com.example.demo.service.StoreService;

@Service
public class InventoryLevelServiceimpl implements InventoryLevelService {

    @Autowired
    private InventoryLevelRepository inventoryLevelRepository;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Override
    public InventoryLevel updateInventory(long storeId, long productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }
        Store store = storeService.getStoreById(storeId);
        Product product = productService.getProductById(productId);

        InventoryLevel level = inventoryLevelRepository.findByStoreAndProduct(store, product)
                .orElse(new InventoryLevel());
        level.setStore(store);
        level.setProduct(product);
        level.setQuantity(quantity);

        return inventoryLevelRepository.save(level);
    }

    @Override
    public InventoryLevel getInventory(long storeId, long productId) {
        Store store = storeService.getStoreById(storeId);
        Product product = productService.getProductById(productId);
        return inventoryLevelRepository.findByStoreAndProduct(store, product)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }

    @Override
    public List<InventoryLevel> getInventoryByStore(long storeId) {
        Store store = storeService.getStoreById(storeId);
        return inventoryLevelRepository.findByStore(store);
    }
}