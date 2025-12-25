package com.example.demo.service;

import com.example.demo.entity.TransferSuggestion;
import java.util.List;

public interface InventoryBalancerService {
    List<TransferSuggestion> generateSuggestions(Long productId);
    List<TransferSuggestion> getSuggestionsForStore(Long storeId);
    TransferSuggestion getSuggestionById(Long id);
}