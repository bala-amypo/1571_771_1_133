// package com.example.demo.controller;

// import com.example.demo.entity.TransferSuggestion;
// import com.example.demo.service.InventoryBalancerService;
// import org.springframework.web.bind.annotation.*;
// import java.util.List;
// import io.swagger.v3.oas.annotations.security.SecurityRequirement;
// @RestController
// @RequestMapping("/api/suggestions")
// @SecurityRequirement(name = "bearerAuth")


// public class TransferSuggestionController {

//     private final InventoryBalancerService inventoryBalancerService;

//     public TransferSuggestionController(
//             InventoryBalancerService inventoryBalancerService) {
//         this.inventoryBalancerService = inventoryBalancerService;
//     }

//     @PostMapping("/generate/{productId}")
//     public List<TransferSuggestion> generate(@PathVariable Long productId) {
//         return inventoryBalancerService.generateSuggestions(productId);
//     }

//     @GetMapping("/store/{storeId}")
//     public List<TransferSuggestion> getForStore(@PathVariable Long storeId) {
//         return inventoryBalancerService.getSuggestionsForStore(storeId);
//     }

//     @GetMapping("/{id}")
//     public TransferSuggestion getById(@PathVariable Long id) {
//         return inventoryBalancerService.getSuggestionById(id);
//     }

// }

package com.example.demo.controller;

import com.example.demo.entity.TransferSuggestion;
import com.example.demo.service.InventoryBalancerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/suggestions")
@SecurityRequirement(name = "bearerAuth")
public class TransferSuggestionController {

    private final InventoryBalancerService inventoryBalancerService;

    public TransferSuggestionController(
            InventoryBalancerService inventoryBalancerService) {
        this.inventoryBalancerService = inventoryBalancerService;
    }

    @Operation(summary = "Generate transfer suggestions for a product")
    @PostMapping("/generate/{productId}")
    public List<TransferSuggestion> generate(
            @Parameter(description = "Product ID for which transfer suggestions are generated")
            @PathVariable("productId") Long productId) {

        return inventoryBalancerService.generateSuggestions(productId);
    }

    @Operation(summary = "Get transfer suggestions for a store")
    @GetMapping("/store/{storeId}")
    public List<TransferSuggestion> getForStore(
            @Parameter(description = "Store ID")
            @PathVariable("storeId") Long storeId) {

        return inventoryBalancerService.getSuggestionsForStore(storeId);
    }

    @Operation(summary = "Get transfer suggestion by ID")
    @GetMapping("/{id}")
    public TransferSuggestion getById(
            @Parameter(description = "Transfer Suggestion ID")
            @PathVariable("id") Long id) {

        return inventoryBalancerService.getSuggestionById(id);
    }
}
