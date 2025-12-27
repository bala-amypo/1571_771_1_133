package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Get all products")
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @Operation(summary = "Create a new product")
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {

        Product pr = productService.createProduct(product);
        return ResponseEntity.ok(pr);
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID")
            @PathVariable("id") long id) {

        Product pr = productService.getProductById(id);
        return ResponseEntity.ok(pr);
    }
}
