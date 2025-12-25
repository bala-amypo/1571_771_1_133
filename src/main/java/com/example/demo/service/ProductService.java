package com.example.demo.service;

import com.example.demo.entity.Product;
import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product getProductById(long id);
    Product getProductById(Long id);
    List<Product> getAllProducts();
    void deactivateProduct(Long id);
}