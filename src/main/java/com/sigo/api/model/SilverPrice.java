package com.sigo.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor // Required by MongoDB to recreate objects from the DB
@Document(collection = "silver_prices")
public class SilverPrice {
    @Id
    private String id;
    private Double pricePerGram;
    private Double pricePerKg;
    private LocalDateTime timestamp;
    public SilverPrice(Double rawPrice, String inputUnit) {
        this.timestamp = LocalDateTime.now();
        
        if (inputUnit.equalsIgnoreCase("kg")) {
            this.pricePerKg = rawPrice;
            this.pricePerGram = rawPrice / 1000.0;
        } else {
            this.pricePerGram = rawPrice;
            this.pricePerKg = rawPrice * 1000.0;
        }
    }
}
