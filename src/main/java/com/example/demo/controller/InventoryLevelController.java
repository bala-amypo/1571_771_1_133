// package com.example.demo.controller;

// import com.example.demo.entity.InventoryLevel;
// import com.example.demo.service.InventoryLevelService;
// import org.springframework.web.bind.annotation.*;
// import java.util.List;
// import io.swagger.v3.oas.annotations.security.SecurityRequirement;

// @RestController
// @RequestMapping("/api/inventory")
// @SecurityRequirement(name = "bearerAuth")
// public class InventoryLevelController {

//     private final InventoryLevelService inventoryLevelService;

//     public InventoryLevelController(InventoryLevelService inventoryLevelService) {
//         this.inventoryLevelService = inventoryLevelService;
//     }

//     @PostMapping
//     public InventoryLevel createOrUpdate(@RequestBody InventoryLevel inventory) {
//         return inventoryLevelService.createOrUpdateInventory(inventory);
//     }

//     @GetMapping("/store/{storeId}")
//     public List<InventoryLevel> getInventoryForStore(@PathVariable Long storeId) {
//         return inventoryLevelService.getInventoryForStore(storeId);
//     }

//     @GetMapping("/product/{productId}")
//     public List<InventoryLevel> getInventoryForProduct(@PathVariable Long productId) {
//         return inventoryLevelService.getInventoryForProduct(productId);
//     }
// }
package com.example.demo.controller;

import com.example.demo.entity.InventoryLevel;
import com.example.demo.service.InventoryLevelService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/inventory")
@SecurityRequirement(name = "bearerAuth")
public class InventoryLevelController {

    private final InventoryLevelService inventoryLevelService;

    public InventoryLevelController(InventoryLevelService inventoryLevelService) {
        this.inventoryLevelService = inventoryLevelService;
    }

    @Operation(summary = "Create or update inventory level")
    @PostMapping
    public InventoryLevel createOrUpdate(
            @RequestBody InventoryLevel inventory) {

        return inventoryLevelService.createOrUpdateInventory(inventory);
    }

    @Operation(summary = "Get inventory by store ID")
    @GetMapping("/store/{storeId}")
    public List<InventoryLevel> getInventoryForStore(
            @Parameter(description = "Store ID")
            @PathVariable("storeId") Long storeId) {

        return inventoryLevelService.getInventoryForStore(storeId);
    }

    @Operation(summary = "Get inventory by product ID")
    @GetMapping("/product/{productId}")
    public List<InventoryLevel> getInventoryForProduct(
            @Parameter(description = "Product ID")
            @PathVariable("productId") Long productId) {

        return inventoryLevelService.getInventoryForProduct(productId);
    }
}
