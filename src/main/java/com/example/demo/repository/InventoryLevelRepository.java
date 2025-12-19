package com.example.demo.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.InventoryLevel;
import com.example.demo.entity.Product;
import com.example.demo.entity.Store;

public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, Long> {
    Optional<InventoryLevel> findByStoreAndProduct(Store store, Product product);
    List<InventoryLevel> findByStore(Store store);
    InventoryLevel save(InventoryLevel level);
}

