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
public class InventoryBalancerServiceimpl implements InventoryBalancerService {

    private final TransferSuggestionRepository transferSuggestionRepository;
    private final InventoryLevelRepository inventoryLevelRepository;
    private final DemandForecastRepository demandForecastRepository;
    private final StoreRepository storeRepository;

    // Constructor order exactly as required by the helper document
    public InventoryBalancerServiceimpl(
            TransferSuggestionRepository transferSuggestionRepository,
            InventoryLevelRepository inventoryLevelRepository,
            DemandForecastRepository demandForecastRepository,
            StoreRepository storeRepository) {
        this.transferSuggestionRepository = transferSuggestionRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.demandForecastRepository = demandForecastRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public List<TransferSuggestion> generateSuggestions(Long productId) {
        // Fetch product - will throw ResourceNotFoundException if not exists
        Product product = inventoryLevelRepository.findByProduct_Id(productId)
                .stream()
                .findFirst()
                .map(InventoryLevel::getProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Validate product is active - required by t61_balancer_generateSuggestions_inactiveProduct_throwsBadRequest
        if (!product.isActive()) {
            throw new BadRequestException("Cannot generate suggestions for inactive product");
        }

        List<Store> stores = storeRepository.findAll();

        Map<Store, Integer> surpluses = new HashMap<>();
        Map<Store, Integer> deficits = new HashMap<>();

        for (Store store : stores) {
            // Get current inventory quantity (default 0 if no record)
            int inventory = inventoryLevelRepository.findByStoreAndProduct(store, product)
                    .map(InventoryLevel::getQuantity)
                    .orElse(0);

            // Get future forecasts - uses exact repository method from helper doc
            List<DemandForecast> forecasts = demandForecastRepository
                    .findByStoreAndProductAndForecastDateAfter(store, product, LocalDate.now());

            if (forecasts.isEmpty()) {
                throw new BadRequestException("No forecast found");
            }

            // Use the first (or sum if multiple - but spec implies one relevant forecast)
            int demand = forecasts.get(0).getPredictedDemand();

            int difference = inventory - demand;

            if (difference > 0) {
                surpluses.put(store, difference);
            } else if (difference < 0) {
                deficits.put(store, -difference);
            }
        }

        List<TransferSuggestion> suggestions = new ArrayList<>();

        for (Map.Entry<Store, Integer> surplusEntry : surpluses.entrySet()) {
            Store source = surplusEntry.getKey();
            int remainingSurplus = surplusEntry.getValue();

            Iterator<Map.Entry<Store, Integer>> deficitIterator = deficits.entrySet().iterator();

            while (remainingSurplus > 0 && deficitIterator.hasNext()) {
                Map.Entry<Store, Integer> deficitEntry = deficitIterator.next();
                Store target = deficitEntry.getKey();
                int remainingDeficit = deficitEntry.getValue();

                int transferQty = Math.min(remainingSurplus, remainingDeficit);

                if (transferQty > 0) {
                    TransferSuggestion suggestion = new TransferSuggestion();
                    suggestion.setSourceStore(source);
                    suggestion.setTargetStore(target);
                    suggestion.setProduct(product);
                    suggestion.setQuantity(transferQty);
                    suggestion.setPriority(determinePriority(transferQty));
                    suggestion.setStatus("PENDING");

                    suggestions.add(suggestion);
                }

                remainingSurplus -= transferQty;
                deficitEntry.setValue(