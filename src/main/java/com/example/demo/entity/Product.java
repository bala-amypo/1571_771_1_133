package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product", uniqueConstraints = {
    @UniqueConstraint(columnNames = "sku")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(nullable = false)
    private boolean active = true;

    public Product() {
        
    }
    public Product(long id, String sku, String name, String category, boolean active) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.active = active;
    }

    
    public long getId() {
        return id;
    }
    public String getSku() { 
        return sku; 
    }
    public String getName() {
       return name; 
    }
    public String getCategory() { 
        return category; 
    }
    public boolean isActive() { 
        return active; 
    }

    public void setId(long id) {
        this.id = id; 
    }
    public void setSku(String sku) {
        this.sku = sku; 
    }
    public void setName(String name) { 
        this.name = name;
    }
    public void setCategory(String category) {
        this.category = category; 
    }
    public void setActive(boolean active) { 
        this.active = active;
    }
}
