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

        List<InventoryLevel> inventoryLevels =
                inventoryLevelRepository.findByProduct_Id(productId);

        List<TransferSuggestion> suggestions = new ArrayList<>();

        for (InventoryLevel source : inventoryLevels) {

            List<DemandForecast> forecasts =
                    demandForecastRepository.findByStoreAndProductAndForecastDateAfter(
                            source.getStore(), product, LocalDate.now()
                    );

            if (forecasts.isEmpty()) continue;

            int demand = forecasts.get(0).getPredictedDemand();

            if (source.getQuantity() > demand) {

                for (InventoryLevel target : inventoryLevels) {

                    
                    if (target.getStore().getId() != source.getStore().getId()
                            && target.getQuantity() < demand) {

                        int qty = Math.min(
                                source.getQuantity() - demand,
                                demand - target.getQuantity()
                        );

                        if (qty > 0) {
                            TransferSuggestion ts = new TransferSuggestion();
                            ts.setSourceStore(source.getStore());
                            ts.setTargetStore(target.getStore());
                            ts.setProduct(product);
                            ts.setQuantity(qty);
                            ts.setPriority("MEDIUM");

                            suggestions.add(
                                    transferSuggestionRepository.save(ts)
                            );
                        }
                    }
                }
            }
        }

        if (suggestions.isEmpty()) {
            throw new BadRequestException("No forecast found");
        }

        return suggestions;
    }

    @Override
    public List<TransferSuggestion> getSuggestionsForStore(Long storeId) {
        return transferSuggestionRepository.findBySourceStoreId(storeId);
    }

    @Override
    public TransferSuggestion getSuggestionById(Long id) {
        return transferSuggestionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transfer suggestion not found"));
    }
}
