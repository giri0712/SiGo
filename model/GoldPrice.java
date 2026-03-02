package com.sigo.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "precious_metals")
public class GoldPrice {
    @Id
    private String id;
    private String metal;      
    private double pricePerGm;
    private double pricePerKg;
    private LocalDateTime timestamp;

    // Required by Spring Data
    public GoldPrice() {
    }

    public GoldPrice(String metal, double pricePerGm, double pricePerKg) {
        this.metal = metal;
        this.pricePerGm = pricePerGm;
        this.pricePerKg = pricePerKg;
        this.timestamp = LocalDateTime.now();
    }

    // --- LOGICAL FIX: GETTERS AND SETTERS ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMetal() { return metal; }
    public void setMetal(String metal) { this.metal = metal; }

    public double getPricePerGm() { return pricePerGm; }
    public void setPricePerGm(double pricePerGm) { this.pricePerGm = pricePerGm; }

    public double getPricePerKg() { return pricePerKg; }
    public void setPricePerKg(double pricePerKg) { this.pricePerKg = pricePerKg; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}