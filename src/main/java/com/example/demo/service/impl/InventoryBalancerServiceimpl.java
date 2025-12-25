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
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryBalancerServiceimpl implements InventoryBalancerService {

    private final TransferSuggestionRepository transferSuggestionRepository;
    private final InventoryLevelRepository inventoryLevelRepository;
    private final DemandForecastRepository demandForecastRepository;
    private final StoreRepository storeRepository;
    
    @Autowired
    private ProductService productService;  // Injected separately

    // Constructor with EXACTLY 4 parameters as required by your test
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
        // Get product - this will throw ResourceNotFoundException if not found
        Product product = productService.getProductById(productId);

        // Check if product is active
        if (!product.isActive()) {
            throw new BadRequestException("Product is inactive");
        }

        // Get inventory levels for this product across all stores
        List<InventoryLevel> inventoryLevels = inventoryLevelRepository.findByProduct_Id(productId);
        
        // Need at least 2 stores with inventory for balancing
        if (inventoryLevels.size() < 2) {
            return new ArrayList<>(); // Return empty list instead of exception
        }

        List<TransferSuggestion> suggestions = new ArrayList<>();

        // Check each store as potential source
        for (InventoryLevel source : inventoryLevels) {
            // Get forecast for source store
            List<DemandForecast> sourceForecasts = demandForecastRepository.findByStoreAndProductAndForecastDateAfter(
                    source.getStore(), product, LocalDate.now()
            );

            if (sourceForecasts.isEmpty()) {
                continue; // No forecast for this store, skip
            }

            int sourceDemand = sourceForecasts.get(0).getPredictedDemand();
            int excess = source.getQuantity() - sourceDemand;

            // If source has excess inventory
            if (excess > 0) {
                // Check all other stores as potential targets
                for (InventoryLevel target : inventoryLevels) {
                    // Skip same store
                    if (target.getStore().getId().equals(source.getStore().getId())) {
                        continue;
                    }

                    // Get forecast for target store
                    List<DemandForecast> targetForecasts = demandForecastRepository.findByStoreAndProductAndForecastDateAfter(
                            target.getStore(), product, LocalDate.now()
                    );

                    if (targetForecasts.isEmpty()) {
                        continue; // No forecast for target store, skip
                    }

                    int targetDemand = targetForecasts.get(0).getPredictedDemand();
                    int deficit = targetDemand - target.getQuantity();

                    // If target has deficit
                    if (deficit > 0) {
                        // Calculate transfer quantity (minimum of excess and deficit)
                        int transferQty = Math.min(excess, deficit);
                        
                        if (transferQty > 0) {
                            // Create transfer suggestion
                            TransferSuggestion ts = new TransferSuggestion();
                            ts.setSourceStore(source.getStore());
                            ts.setTargetStore(target.getStore());
                            ts.setProduct(product);
                            ts.setQuantity(transferQty);
                            ts.setPriority("MEDIUM");
                            ts.setStatus("PENDING");

                            // Save and add to suggestions
                            suggestions.add(transferSuggestionRepository.save(ts));
                            
                            // Reduce excess for next potential transfer from this source
                            excess -= transferQty;
                            
                            // If no more excess, break from inner loop
                            if (excess <= 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        return suggestions;
    }

    @Override
    public List<TransferSuggestion> getSuggestionsForStore(Long storeId) {
        // Verify store exists
        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        
        // Return suggestions where this store is the source
        return transferSuggestionRepository.findBySourceStoreId(storeId);
    }

    @Override
    public TransferSuggestion getSuggestionById(Long id) {
        return transferSuggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer suggestion not found"));
    }
}