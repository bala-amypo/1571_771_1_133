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
    private final ProductRepository productRepository;

    public InventoryBalancerServiceimpl(
            TransferSuggestionRepository transferSuggestionRepository,
            InventoryLevelRepository inventoryLevelRepository,
            DemandForecastRepository demandForecastRepository,
            StoreRepository storeRepository,
            ProductRepository productRepository) {
        this.transferSuggestionRepository = transferSuggestionRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.demandForecastRepository = demandForecastRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<TransferSuggestion> generateSuggestions(long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        List<Store> stores = storeRepository.findAll();

        Map<Store, Integer> surpluses = new HashMap<>();
        Map<Store, Integer> deficits = new HashMap<>();

        for (Store store : stores) {
            int inventory = inventoryLevelRepository.findByStoreAndProduct(store, product)
                    .map(InventoryLevel::getQuantity)
                    .orElse(0);

            DemandForecast forecast = demandForecastRepository
                    .findByStoreAndProductAndForecastDateAfter(store, product, LocalDate.now())
                    .orElseThrow(() -> new BadRequestException("No forecast found for store: " + store.getId()));

            int demand = forecast.getPredictedDemand();
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
                deficitEntry.setValue(remainingDeficit - transferQty);

                if (remainingDeficit - transferQty <= 0) {
                    deficitIterator.remove();
                }
            }
        }

        // THIS IS THE IMPORTANT LINE – we only return TransferSuggestion objects
        return suggestions.isEmpty() 
                ? Collections.emptyList() 
                : transferSuggestionRepository.saveAll(suggestions);
    }

    private String determinePriority(int quantity) {
        if (quantity > 100) return "HIGH";
        if (quantity > 20) return "MEDIUM";
        return "LOW";
    }

    @Override
    public List<TransferSuggestion> getSuggestionsForStore(long storeId) {
        List<TransferSuggestion> outgoing = transferSuggestionRepository.findBySourceStoreId(storeId);
        List<TransferSuggestion> incoming = transferSuggestionRepository.findByTargetStoreId(storeId);

        List<TransferSuggestion> all = new ArrayList<>(outgoing);
        all.addAll(incoming);
        return all;
    }

    @Override
    public TransferSuggestion getSuggestionById(long id) {
        return transferSuggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found: " + id));
    }
}