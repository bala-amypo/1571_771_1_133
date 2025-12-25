package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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

    @Column(name = "suggested_quantity")
    private Integer suggestedQuantity;

    @Column(name = "quantity")
    private Integer quantity;

    private String priority;
    private String reason;
    private String status;

    @Column(name = "suggested_at")
    private Timestamp suggestedAt;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        this.suggestedAt = new Timestamp(System.currentTimeMillis());
        this.generatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Store getSourceStore() {
        return sourceStore;
    }

    public void setSourceStore(Store sourceStore) {
        this.sourceStore = sourceStore;
    }

    public Store getTargetStore() {
        return targetStore;
    }

    public void setTargetStore(Store targetStore) {
        this.targetStore = targetStore;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getSuggestedQuantity() {
        return suggestedQuantity;
    }

    public void setSuggestedQuantity(Integer suggestedQuantity) {
        this.suggestedQuantity = suggestedQuantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getSuggestedAt() {
        return suggestedAt;
    }

    public void setSuggestedAt(Timestamp suggestedAt) {
        this.suggestedAt = suggestedAt;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}