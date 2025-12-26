package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Store;
import com.example.demo.service.StoreService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
@RestController
@RequestMapping("/api/stores")

public class StoreController {

    @Autowired
    StoreService storeService;
    
    @GetMapping
    public List<Store> getAll() {
        return storeService.getStore();
    }
    
    @PostMapping
    public ResponseEntity<Store> createAll(@RequestBody Store store) {
        Store st = storeService.createStore(store);
        return ResponseEntity.ok(st);  // CHANGED from .status(201) to .ok()
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<Store> getById(@PathVariable long id) {
        Store st = storeService.getById(id);
        return ResponseEntity.ok(st);
    }
}