package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "demand_forecast")
public class DemandForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(name = "predicted_demand", nullable = false)
    private int predictedDemand;
    private Double confidenceScore;

    public DemandForecast() {
        
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
    public LocalDate getForecastDate() {
       return forecastDate;
    }
    public int getPredictedDemand() {
     return predictedDemand;
    }
    public Double getConfidenceScore() {
     return confidenceScore;
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
    public void setForecastDate(LocalDate forecastDate) {
        this.forecastDate = forecastDate; 
    }
    public void setPredictedDemand(int predictedDemand) {
        this.predictedDemand = predictedDemand;
    }
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore; 
    }
}
