package com.example.demo.controller;

import com.example.demo.entity.TransferSuggestion;
import com.example.demo.service.InventoryBalancerService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@SecurityRequirement(name = "bearerAuth")

public class TransferSuggestionController {

    private final InventoryBalancerService inventoryBalancerService;

    public TransferSuggestionController(
            InventoryBalancerService inventoryBalancerService) {
        this.inventoryBalancerService = inventoryBalancerService;
    }

    @PostMapping("/generate/{productId}")
    public List<TransferSuggestion> generate(@PathVariable Long productId) {
        return inventoryBalancerService.generateSuggestions(productId);
    }

    @GetMapping("/store/{storeId}")
    public List<TransferSuggestion> getForStore(@PathVariable Long storeId) {
        return inventoryBalancerService.getSuggestionsForStore(storeId);
    }

    @GetMapping("/{id}")
    public TransferSuggestion getById(@PathVariable Long id) {
        return inventoryBalancerService.getSuggestionById(id);
    }
}
