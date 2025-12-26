package com.example.demo.service.impl;

import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.InventoryBalancerService;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryBalancerServiceimpl implements InventoryBalancerService {

    private final TransferSuggestionRepository transferSuggestionRepository;
    private final InventoryLevelRepository inventoryLevelRepository;
    private final DemandForecastRepository demandForecastRepository;
    private final StoreRepository storeRepository;
    
    @Autowired
    private ProductService productService;

    @Autowired
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
        Product product = productService.getProductById(productId);

        if (!product.isActive()) {
            throw new BadRequestException("Product is inactive");
        }

        List<InventoryLevel> inventoryLevels = inventoryLevelRepository.findByProduct_Id(productId);
        if (inventoryLevels.size() < 2) {
            return new ArrayList<>();
        }

        List<TransferSuggestion> suggestions = new ArrayList<>();

        for (InventoryLevel source : inventoryLevels) {
            // Use the CORRECT repository method
            List<DemandForecast> forecasts = demandForecastRepository
                .findByStoreAndProductAndForecastDateAfter(
                    source.getStore(), 
                    product, 
                    LocalDate.now().minusDays(1)  // Today or future
                );

            if (forecasts.isEmpty()) {
                continue;
            }

            int demand = forecasts.get(0).getForecastedDemand();
            int excess = source.getQuantity() - demand;

            // Only suggest if we have significant excess (>20% of demand)
            if (excess > (demand * 0.2)) {
                for (InventoryLevel target : inventoryLevels) {
                    // Compare primitive longs using ==
                    if (target.getStore().getId() == source.getStore().getId()) {
                        continue;
                    }

                    List<DemandForecast> targetForecasts = demandForecastRepository
                        .findByStoreAndProductAndForecastDateAfter(
                            target.getStore(), 
                            product, 
                            LocalDate.now().minusDays(1)  // Today or future
                        );

                    if (targetForecasts.isEmpty()) {
                        continue;
                    }

                    int targetDemand = targetForecasts.get(0).getForecastedDemand();
                    int deficit = targetDemand - target.getQuantity();

                    // Only suggest if significant deficit (>30% of demand)
                    if (deficit > (targetDemand * 0.3)) {
                        int transferQty = Math.min(excess, deficit);
                        
                        // Ensure minimum transfer quantity (at least 5 units)
                        if (transferQty >= 5) {
                            TransferSuggestion ts = new TransferSuggestion();
                            ts.setSourceStore(source.getStore());
                            ts.setTargetStore(target.getStore());
                            ts.setProduct(product);
                            ts.setSuggestedQuantity(transferQty);
                            ts.setReason("Inventory rebalancing needed");
                            ts.setGeneratedAt(LocalDateTime.now());
                            ts.setStatus("PENDING");

                            // Save and add to list
                            TransferSuggestion saved = transferSuggestionRepository.save(ts);
                            suggestions.add(saved);
                            
                            excess -= transferQty;
                        }
                    }
                }
            }
        }

        return suggestions;
    }

    @Override
    public List<TransferSuggestion> getSuggestionsForStore(Long storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        return transferSuggestionRepository.findBySourceStoreId(storeId);
    }

    @Override
    public TransferSuggestion getSuggestionById(Long id) {
        return transferSuggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer suggestion not found"));
    }
}