package com.example.demo.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.demo.entity.TransferSuggestion;
import com.example.demo.service.InventoryBalancerService;

@RestController
@RequestMapping("/api/suggestions")
public class TransferSuggestionController {

    @Autowired
    InventoryBalancerService inventoryBalancerService;

    @PostMapping("/generate/{productId}")
    public List<TransferSuggestion> generateSuggestions(@PathVariable long productId) {
        return inventoryBalancerService.generateSuggestions(productId);
    }

    @GetMapping("/store/{storeId}")
    public List<TransferSuggestion> getSuggestionsForStore(@PathVariable long storeId) {
        return inventoryBalancerService.getSuggestionsForStore(storeId);
    }

    @GetMapping("/{id}")
    public TransferSuggestion getSuggestionById(@PathVariable long id) {
        return inventoryBalancerService.getSuggestionById(id);
    }
}