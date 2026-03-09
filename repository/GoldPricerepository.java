package com.sigo.api.repository;

import com.sigo.api.model.GoldPrice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoldPricerepository extends MongoRepository<GoldPrice, String> {
    // Spring Data MongoDB generates the save/find methods for you
}
