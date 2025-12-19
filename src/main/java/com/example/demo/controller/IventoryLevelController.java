package com.example.demo.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.demo.entity.InventoryLevel;
import com.example.demo.service.InventoryLevelService;

@RestController
@RequestMapping("/api/inventory")
public class InventoryLevelController {

    @Autowired
    InventoryLevelService inventoryLevelService;

    @PutMapping("/update")
    public InventoryLevel updateInventory(@RequestParam long storeId,@RequestParam long productId, @RequestParam int quantity) {
        return inventoryLevelService.updateInventory(storeId, productId, quantity);
    }

    @GetMapping("/store/{storeId}/product/{productId}")
    public InventoryLevel getInventory(@PathVariable long storeId, @PathVariable long productId) {
        return inventoryLevelService.getInventory(storeId, productId);
    }

    @GetMapping("/store/{storeId}")
    public List<InventoryLevel> getInventoryByStore(@PathVariable long storeId) {
        return inventoryLevelService.getInventoryByStore(storeId);
    }
}