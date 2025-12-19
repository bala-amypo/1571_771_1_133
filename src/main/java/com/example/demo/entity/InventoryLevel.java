package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "inventory_level")
public class InventoryLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    private Timestamp lastUpdated;

    @PrePersist
    @PreUpdate
    private void updateTimestamp() {
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }
    public InventoryLevel() {

    }
    public long getId() {
         return id;
     }
    public Store getStore() {
         return store;
     }
    public Product getProduct() { 
        return product;
    }
    public int getQuantity() {
         return quantity;
     }
    public Timestamp getLastUpdated() {
         return lastUpdated;
     }

    public void setId(long id) { 
        this.id = id;
     }
    public void setStore(Store store) { 
        this.store = store; 
    }
    public void setProduct(Product product) {
         this.product = product;
     }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
    }
    public void setLastUpdated(Timestamp lastUpdated) {
         this.lastUpdated = lastUpdated;
        
    }
}