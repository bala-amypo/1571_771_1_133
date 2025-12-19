package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "transfer_suggestion")
public class TransferSuggestion {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long id;

   @ManyToOne
   @JoinColumn(name = "source_store_id", nullable = false)
    private Store sourceStore;

   @ManyToOne
   @JoinColumn(name = "target_store_id", nullable = false)
   private Store targetStore;

   @ManyToOne
   @JoinColumn(name = "product_id", nullable = false)
   private Product product;

   private int quantity;

   private String priority;
   private Timestamp suggestedAt;

   private String status = "PENDING";

   @PrePersist
   private void setSuggestedAt() {
      this.suggestedAt = new Timestamp(System.currentTimeMillis());
   }

   public long getId() {
      return id;
   }
   public Store getSourceStore() { 
      return sourceStore; 
   }
   public Store getTargetStore() {
     return targetStore; 
   }
   public Product getProduct() { 
      return product; 
   }
   public int getQuantity() {
      return quantity; 
   }
   public String getPriority() {
     return priority; 
   }
   public Timestamp getSuggestedAt() {
     return suggestedAt; 
   }
   public String getStatus() {
    return status;
   }
   public void setId(long id) {
      this.id = id; 
   }
   public void setSourceStore(Store sourceStore) {
     this.sourceStore = sourceStore; 
   }
   public void setTargetStore(Store targetStore) { 
      this.targetStore = targetStore; 
   }
   public void setProduct(Product product) { 
      this.product = product;
   }
   public void setQuantity(int quantity) { 
      this.quantity = quantity; 
   }
   public void setPriority(String priority) {
      this.priority = priority;
   }
   public void setStatus(String status) {
      this.status = status; 
   }
}

