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

        Product product = inventoryLevelRepository.findByProduct_Id(productId)
                .stream()
                .findFirst()
                .map(InventoryLevel::getProduct)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.isActive()) {
            throw new BadRequestException("Cannot generate suggestions for inactive product");
        }

        List<Store> stores = storeRepository.findAll();
        Map<Store, Integer> surpluses = new HashMap<>();
        Map<Store, Integer> deficits = new HashMap<>();

        for (Store store : stores) {

            int inventory = inventoryLevelRepository
                    .findByStoreAndProduct(store, product)
                    .map(InventoryLevel::getQuantity)
                    .orElse(0);

            List<DemandForecast> forecasts =
                    demandForecastRepository.findByStoreAndProductAndForecastDateAfter(
                            store, product, LocalDate.now());

            if (forecasts.isEmpty()) {
                throw new BadRequestException("No forecast found");
            }

            int demand = forecasts.get(0).getPredictedDemand();
            int diff = inventory - demand;

            if (diff > 0) {
                surpluses.put(store, diff);
            } else if (diff < 0) {
                deficits.put(store, -diff);
            }
        }

        List<TransferSuggestion> suggestions = new ArrayList<>();

        for (Map.Entry<Store, Integer> surplus : surpluses.entrySet()) {

            Store source = surplus.getKey();
            int remainingSurplus = surplus.getValue();

            Iterator<Map.Entry<Store, Integer>> deficitIterator =
                    deficits.entrySet().iterator();

            while (remainingSurplus > 0 && deficitIterator.hasNext()) {

                Map.Entry<Store, Integer> deficit = deficitIterator.next();
                Store target = deficit.getKey();
                int remainingDeficit = deficit.getValue();

                int transferQty = Math.min(remainingSurplus, remainingDeficit);

                TransferSuggestion suggestion = new TransferSuggestion();
                suggestion.setSourceStore(source);
                suggestion.setTargetStore(target);
                suggestion.setProduct(product);
                suggestion.setQuantity(transferQty);
                suggestion.setPriority(determinePriority(transferQty));
                suggestion.setStatus("PENDING");

                suggestions.add(suggestion);

                remainingSurplus -= transferQty;
                deficit.setValue(remainingDeficit - transferQty);

                if (deficit.getValue() <= 0) {
                    deficitIterator.remove();
                }
            }
        }

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
    public List<TransferSuggestion> getSuggestionsForStore(Long storeId) {

        List<TransferSuggestion> outgoing =
                transferSuggestionRepository.findBySourceStoreId(storeId);

        List<TransferSuggestion> incoming =
                transferSuggestionRepository.findByTargetStoreId(storeId);

        List<TransferSuggestion> all = new ArrayList<>(outgoing);
        all.addAll(incoming);
        return all;
    }

    @Override
    public TransferSuggestion getSuggestionById(Long id) {
        return transferSuggestionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Suggestion not found"));
    }
}
