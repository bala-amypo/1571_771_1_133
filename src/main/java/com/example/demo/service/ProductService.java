package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Product;

public interface ProductService {
    Product createProduct(Product product);
    Product getProductById(long id);
    List<Product> getAllProducts();
}
