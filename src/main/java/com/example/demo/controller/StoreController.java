package com.example.Multi.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Multi.Model.Store;
import com.example.Multi.Service.StoreService;

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
        return ResponseEntity.status(201).body(st);
    }

 
    @GetMapping("/{id}")
    public ResponseEntity<Store> getById(@PathVariable long id) {
        Store st = storeService.getById(id);
        return ResponseEntity.ok(st);
    }
}
