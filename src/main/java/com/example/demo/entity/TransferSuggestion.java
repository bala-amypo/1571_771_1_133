package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "transfer_suggestion")
public class TransferSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_store_id", nullable = false)
    private Store sourceStore;

    @ManyToOne
    @JoinColumn(name = "target_store_id", nullable = false)
    private Store targetStore;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantity;

    private String priority;

    private String status;

    private Timestamp suggestedAt;

    @PrePersist
    protected void onCreate() {
        this.suggestedAt = new Timestamp(System.currentTimeMillis());
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Store getSourceStore() { return sourceStore; }
    public void setSourceStore(Store sourceStore) { this.sourceStore = sourceStore; }

    public Store getTargetStore() { return targetStore; }
    public void setTargetStore(Store targetStore) { this.targetStore = targetStore; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getSuggestedAt() { return suggestedAt; }
    public void setSuggestedAt(Timestamp suggestedAt) { this.suggestedAt = suggestedAt; }
}