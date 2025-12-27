package com.example.demo.controller;

import com.example.demo.entity.InventoryLevel;
import com.example.demo.service.InventoryLevelService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/inventory")
@SecurityRequirement(name = "bearerAuth")
public class InventoryLevelController {

    private final InventoryLevelService inventoryLevelService;

    public InventoryLevelController(InventoryLevelService inventoryLevelService) {
        this.inventoryLevelService = inventoryLevelService;
    }

    @PostMapping
    public InventoryLevel createOrUpdate(@RequestBody InventoryLevel inventory) {
        return inventoryLevelService.createOrUpdateInventory(inventory);
    }

    @GetMapping("/store/{storeId}")
    public List<InventoryLevel> getInventoryForStore(@PathVariable Long storeId) {
        return inventoryLevelService.getInventoryForStore(storeId);
    }

    @GetMapping("/product/{productId}")
    public List<InventoryLevel> getInventoryForProduct(@PathVariable Long productId) {
        return inventoryLevelService.getInventoryForProduct(productId);
    }
}
