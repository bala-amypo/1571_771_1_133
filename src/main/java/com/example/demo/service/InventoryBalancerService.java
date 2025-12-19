package com.example.demo.service;

import java.util.List;
import com.example.demo.entity.TransferSuggestion;

public interface InventoryBalancerService {

    List<TransferSuggestion> generateSuggestions(Long productId);

    List<TransferSuggestion> getSuggestionsForStore(Long storeId);

    TransferSuggestion getSuggestionById(Long id);
}
