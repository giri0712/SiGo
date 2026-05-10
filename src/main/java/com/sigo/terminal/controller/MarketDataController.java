package com.sigo.terminal.controller;

import com.sigo.terminal.model.CommodityPrice;
import com.sigo.terminal.model.MarketSnapshot;
import com.sigo.terminal.model.NewsItem;
import com.sigo.terminal.service.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class MarketDataController {

    private static final Logger log = LoggerFactory.getLogger(MarketDataController.class);

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> getApiInfo() {
        Map<String, String> info = Map.of(
            "message", "SiGo India Commodity Terminal API",
            "version", "1.0.0",
            "endpoints", "/api/v1/snapshot, /api/v1/commodities, /api/v1/metals/{symbol}, /api/v1/commodities/precious, /api/v1/commodities/base, /api/v1/commodities/energy, /api/v1/stocks/metals, /api/v1/news",
            "websocket", "/ws for real-time updates"
        );
        return ResponseEntity.ok(info);
    }

    @GetMapping("/snapshot")
    public ResponseEntity<MarketSnapshot> getFullSnapshot() {
        log.debug("GET /snapshot");
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
            .body(marketDataService.getSnapshot());
    }

    @GetMapping("/commodities")
    public ResponseEntity<List<CommodityPrice>> getAllCommodities() {
        MarketSnapshot snap = marketDataService.getSnapshot();
        List<CommodityPrice> all = new ArrayList<>();
        if (snap.getPreciousMetals()    != null) all.addAll(snap.getPreciousMetals());
        if (snap.getBaseMetals()        != null) all.addAll(snap.getBaseMetals());
        if (snap.getEnergyCommodities() != null) all.addAll(snap.getEnergyCommodities());
        return ResponseEntity.ok(all);
    }

    @GetMapping("/metals/{symbol}")
    public ResponseEntity<CommodityPrice> getMetal(@PathVariable String symbol) {
        MarketSnapshot snap = marketDataService.getSnapshot();
        List<CommodityPrice> all = new ArrayList<>();
        if (snap.getPreciousMetals() != null) all.addAll(snap.getPreciousMetals());
        if (snap.getBaseMetals() != null) all.addAll(snap.getBaseMetals());
        if (snap.getEnergyCommodities() != null) all.addAll(snap.getEnergyCommodities());
        if (snap.getMetalStocks() != null) all.addAll(snap.getMetalStocks());
        return all.stream()
            .filter(p -> symbol.equalsIgnoreCase(p.getSymbol()))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/commodities/precious")
    public ResponseEntity<List<CommodityPrice>> getPreciousMetals() {
        return ResponseEntity.ok(marketDataService.getSnapshot().getPreciousMetals());
    }

    @GetMapping("/commodities/base")
    public ResponseEntity<List<CommodityPrice>> getBaseMetals() {
        return ResponseEntity.ok(marketDataService.getSnapshot().getBaseMetals());
    }

    @GetMapping("/commodities/energy")
    public ResponseEntity<List<CommodityPrice>> getEnergyCommodities() {
        return ResponseEntity.ok(marketDataService.getSnapshot().getEnergyCommodities());
    }

    @GetMapping("/stocks/metals")
    public ResponseEntity<List<CommodityPrice>> getMetalStocks() {
        return ResponseEntity.ok(marketDataService.getSnapshot().getMetalStocks());
    }

    @GetMapping("/indices")
    public ResponseEntity<List<CommodityPrice>> getIndices() {
        return ResponseEntity.ok(marketDataService.getSnapshot().getIndices());
    }

    @GetMapping("/bonds")
    public ResponseEntity<List<CommodityPrice>> getBonds() {
        return ResponseEntity.ok(marketDataService.getSnapshot().getBonds());
    }

    @GetMapping("/news")
    public ResponseEntity<List<NewsItem>> getNews(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String symbol) {

        List<NewsItem> news = marketDataService.getSnapshot().getNews();
        if (news == null) return ResponseEntity.ok(List.of());

        if (category != null) {
            news = news.stream()
                .filter(n -> category.equalsIgnoreCase(n.getCategory()))
                .toList();
        }
        if (symbol != null) {
            news = news.stream()
                .filter(n -> symbol.equalsIgnoreCase(n.getRelatedSymbol()))
                .toList();
        }

        return ResponseEntity.ok(news);
    }

    @PostMapping("/refresh")
    public ResponseEntity<MarketSnapshot> forceRefresh() {
        log.info("Manual refresh triggered via API");
        return ResponseEntity.ok(marketDataService.refreshSnapshot());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "SiGo India Terminal",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
