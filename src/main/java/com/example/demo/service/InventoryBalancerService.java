package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.InventoryLevel;
import com.example.demo.entity.TransferSuggestion;

public interface InventoryBalancerService {
    List<TransferSuggestion> generateSuggestions(long productId);
    List<TransferSuggestion> getSuggestionsForStore(long storeId);
    TransferSuggestion getSuggestionById(long id);
}