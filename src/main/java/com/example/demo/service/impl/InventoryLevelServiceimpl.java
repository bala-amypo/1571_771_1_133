package com.example.demo.service.impl;

import com.example.demo.entity.InventoryLevel;
import com.example.demo.entity.Product;
import com.example.demo.entity.Store;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.InventoryLevelRepository;
import com.example.demo.service.InventoryLevelService;
import com.example.demo.service.ProductService;
import com.example.demo.service.StoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryLevelServiceimpl implements InventoryLevelService {

    private final InventoryLevelRepository inventoryLevelRepository;
    private final StoreService storeService;
    private final ProductService productService;

    public InventoryLevelServiceimpl(
            InventoryLevelRepository inventoryLevelRepository,
            StoreService storeService,
            ProductService productService) {
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.storeService = storeService;
        this.productService = productService;
    }

    @Override
    public InventoryLevel createOrUpdateInventory(InventoryLevel inventory) {

        if (inventory.getQuantity() < 0) {
            throw new BadRequestException("Quantity must be >= 0");
        }

        Store store = storeService.getStoreById(inventory.getStore().getId());
        Product product = productService.getProductById(inventory.getProduct().getId());

        InventoryLevel existing =
                inventoryLevelRepository.findByStoreAndProduct(store, product);

        if (existing != null) {
            existing.setQuantity(inventory.getQuantity());
            return inventoryLevelRepository.save(existing);
        }

        inventory.setStore(store);
        inventory.setProduct(product);
        return inventoryLevelRepository.save(inventory);
    }

    @Override
    public List<InventoryLevel> getInventoryForStore(Long storeId) {
        storeService.getStoreById(storeId);
        return inventoryLevelRepository.findByStore_Id(storeId);
    }

    @Override
    public List<InventoryLevel> getInventoryForProduct(Long productId) {
        productService.getProductById(productId);
        return inventoryLevelRepository.findByProduct_Id(productId);
    }

    @Override
    public InventoryLevel getInventory(Long storeId, Long productId) {

        Store store = storeService.getStoreById(storeId);
        Product product = productService.getProductById(productId);

        InventoryLevel inventory =
                inventoryLevelRepository.findByStoreAndProduct(store, product);

        if (inventory == null) {
            throw new ResourceNotFoundException("Inventory not found");
        }

        return inventory;
    }
}
