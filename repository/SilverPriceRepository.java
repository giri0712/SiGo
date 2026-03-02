package com.sigo.api.repository;

import com.sigo.api.model.SilverPrice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SilverPriceRepository extends MongoRepository<SilverPrice, String> {
}
