package com.sigo.api.controller;
import com.sigo.api.model.GoldPrice;
import com.sigo.api.model.SilverPrice;
import com.sigo.api.repository.GoldPricerepository;
import com.sigo.api.repository.SilverPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class Pricecontroller {

    @Autowired
    private GoldPricerepository goldRepository;

    @Autowired
    private SilverPriceRepository silverRepository;

    @GetMapping("/gold")
    public List<GoldPrice> getAllGoldPrices() {
        return goldRepository.findAll();
    }

    @GetMapping("/silver")
    public List<SilverPrice> getAllSilverPrices() {
        return silverRepository.findAll();
    }
}
