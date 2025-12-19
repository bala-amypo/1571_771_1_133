package com.example.demo.service.impl;

import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.InventoryBalancerService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class InventoryBalancerServiceImpl implements InventoryBalancerService {  // Fixed: capital "I" in Impl

    private final TransferSuggestionRepository transferSuggestionRepository;
    private final InventoryLevelRepository inventoryLevelRepository;
    private final DemandForecastRepository demandForecastRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;  // Added: needed to fetch real Product

    public InventoryBalancerServiceImpl(
            TransferSuggestionRepository transferSuggestionRepository,
            InventoryLevelRepository inventoryLevelRepository,
            DemandForecastRepository demandForecastRepository,
            StoreRepository storeRepository,
            ProductRepository productRepository) {  // Added ProductRepository

        this.transferSuggestionRepository = transferSuggestionRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.demandForecastRepository = demandForecastRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<TransferSuggestion> generateSuggestions(long productId) {
        // Properly fetch the Product entity
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        List<Store> stores = storeRepository.findAll();

        Map<Store, Integer> surpl