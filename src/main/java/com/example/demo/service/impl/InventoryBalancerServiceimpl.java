package com.example.demo.service.impl;

import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.InventoryBalancerService;
import com.example.demo.service.ProductService;
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
    private final ProductService productService;

    // Constructor order must match test requirements
    public InventoryBalancerServiceimpl(
            TransferSuggestionRepository transferSuggestionRepository,
            InventoryLevelRepository inventoryLevelRepository,
            DemandForecastRepository demandForecastRepository,
            StoreRepository storeRepository) {
        
        this.transferSuggestionRepository = transferSuggestionRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.demandForecastRepository = demandForecastRepository;
        this.storeRepository = storeRepository;
        
        // We'll need to inject ProductService separately
        this.productService = null;
    }

    // Additional constructor with ProductService (needs @Autowired)
    public InventoryBalancerServiceimpl(
            TransferSuggestionRepository transferSuggestionRepository,
            InventoryLevelRepository inventoryLevelRepository,
            DemandForecastRepository demandForecastRepository,
            StoreRepository storeRepository,
            ProductService productService) {
        
        this.transferSuggestionRepository = transferSuggestionRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.demandForecastRepository = demandForecastRepository;
        this.storeRepository = storeRepository;
        this.productService = productService;
    }

    @Override
    public List<TransferSuggestion> generateSuggestions(Long productId) {
        Product product = productService.getProductById(productId);

        if (!product.isActive()) {
            throw new BadRequestException("Product is inactive");
        }

        List<InventoryLevel> inventoryLevels = inventoryLevelRepository.findByProduct_Id(productId);
        if (inventoryLevels.size() < 2) {
            throw new BadRequestException("Need at least two stores with inventory for balancing");
        }

        List<TransferSuggestion> suggestions = new ArrayList<>();

        for (InventoryLevel source : inventoryLevels) {
            List<DemandForecast> forecasts = demandForecastRepository.findByStoreAndProductAndForecastDateAfter(
                    source.getStore(), product, LocalDate.now()
            );

            if (forecasts.isEmpty()) {
                throw new BadRequestException("No forecast found for store: " + source.getStore().getId());
            }

            int demand = forecasts.get(0).getPredictedDemand();
            int excess = source.getQuantity() - demand;

            if (excess > 0) {
                for (InventoryLevel target : inventoryLevels) {
                    if (target.getStore().getId().equals(source.getStore().getId())) {
                        continue;
                    }

                    List<DemandForecast> targetForecasts = demandForecastRepository.findByStoreAndProductAndForecastDateAfter(
                            target.getStore(), product, LocalDate.now()
                    );

                    if (targetForecasts.isEmpty()) {
                        continue;
                    }

                    int targetDemand = targetForecasts.get(0).getPredictedDemand();
                    int deficit = targetDemand - target.getQuantity();

                    if (deficit > 0) {
                        int transferQty = Math.min(excess, deficit);
                        if (transferQty > 0) {
                            TransferSuggestion ts = new TransferSuggestion();
                            ts.setSourceStore(source.getStore());
                            ts.setTargetStore(target.getStore());
                            ts.setProduct(product);
                            ts.setQuantity(transferQty);
                            ts.setPriority("MEDIUM");
                            ts.setStatus("PENDING");

                            suggestions.add(transferSuggestionRepository.save(ts));
                            
                            // Reduce excess for next potential transfer
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