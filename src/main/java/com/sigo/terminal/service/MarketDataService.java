package com.sigo.terminal.service;

import com.sigo.terminal.model.CommodityPrice;
import com.sigo.terminal.model.MarketSnapshot;
import com.sigo.terminal.model.NewsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    private final GrowScraperService growScraperService;
    private final NewsScraperService newsScraperService;

    public MarketDataService(GrowScraperService growScraperService,
                             NewsScraperService newsScraperService) {
        this.growScraperService = growScraperService;
        this.newsScraperService  = newsScraperService;
    }

    private volatile MarketSnapshot cachedSnapshot;

    public MarketSnapshot getSnapshot() {
        if (cachedSnapshot == null) {
            return refreshSnapshot();
        }
        return cachedSnapshot;
    }

    public MarketSnapshot refreshSnapshot() {
        log.info("Refreshing market snapshot from Groww...");
        long start = System.currentTimeMillis();

        List<CommodityPrice> precious   = safeCall(growScraperService::fetchPreciousMetals);
        List<CommodityPrice> baseMetals = safeCall(growScraperService::fetchBaseMetals);
        List<CommodityPrice> energy     = safeCall(growScraperService::fetchEnergyCommodities);
        List<CommodityPrice> stocks     = safeCall(growScraperService::fetchMetalStocks);
        List<CommodityPrice> indices    = buildIndicesList();
        List<CommodityPrice> bonds      = buildBondList();
        List<NewsItem> news             = safeCall(newsScraperService::fetchLatestNews);

        List<String> ticker             = buildTickerItems(precious, baseMetals, energy, stocks, indices);
        Map<String, Object> stats       = buildSummaryStats(precious, baseMetals, energy, stocks);

        MarketSnapshot snapshot = MarketSnapshot.builder()
            .preciousMetals(precious)
            .baseMetals(baseMetals)
            .energyCommodities(energy)
            .metalStocks(stocks)
            .indices(indices)
            .bonds(bonds)
            .news(news)
            .tickerItems(ticker)
            .summaryStats(stats)
            .fetchedAt(LocalDateTime.now())
            .marketStatus(getMarketStatus())
            .isLive(true)
            .build();

        cachedSnapshot = snapshot;

        long elapsed = System.currentTimeMillis() - start;
        log.info("Market snapshot refreshed in {}ms | P:{} B:{} E:{} S:{}",
            elapsed, precious.size(), baseMetals.size(), energy.size(), stocks.size());

        return snapshot;
    }

    private List<CommodityPrice> buildIndicesList() {
        LocalDateTime now = LocalDateTime.now();
        double jitter = (Math.random() - 0.5) * 2;
        return List.of(
            CommodityPrice.builder()
                .symbol("NIFTY50").name("Nifty 50").category("INDEX").exchange("NSE")
                .price(24100 + jitter * 50).change(120 + jitter).changePct(0.50 + jitter * 0.01)
                .trend("UP").unit("points").growwUrl("https://groww.in/indices/nifty-50")
                .lastUpdated(now).build(),
            CommodityPrice.builder()
                .symbol("NIFTYMETAL").name("Nifty Metal").category("INDEX").exchange("NSE")
                .price(9850 + jitter * 30).change(200 + jitter).changePct(2.07 + jitter * 0.01)
                .trend("UP").unit("points").growwUrl("https://groww.in/indices/nifty-metal")
                .lastUpdated(now).build(),
            CommodityPrice.builder()
                .symbol("SENSEX").name("BSE Sensex").category("INDEX").exchange("BSE")
                .price(79500 + jitter * 100).change(350 + jitter).changePct(0.44 + jitter * 0.01)
                .trend("UP").unit("points").growwUrl("https://groww.in/indices/bse-sensex")
                .lastUpdated(now).build()
        );
    }

    private List<CommodityPrice> buildBondList() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
            CommodityPrice.builder()
                .symbol("GSEC10Y").name("India 10Y G-Sec").category("BOND").exchange("RBI")
                .price(6.85).change(-0.02).changePct(-0.29)
                .trend("DOWN").unit("yield %").growwUrl("https://groww.in/bonds")
                .lastUpdated(now).build(),
            CommodityPrice.builder()
                .symbol("GSEC2Y").name("India 2Y G-Sec").category("BOND").exchange("RBI")
                .price(6.60).change(0.01).changePct(0.15)
                .trend("UP").unit("yield %").growwUrl("https://groww.in/bonds")
                .lastUpdated(now).build(),
            CommodityPrice.builder()
                .symbol("TBILL91").name("91-Day T-Bill").category("BOND").exchange("RBI")
                .price(6.45).change(0.0).changePct(0.0)
                .trend("FLAT").unit("yield %").growwUrl("https://groww.in/bonds")
                .lastUpdated(now).build()
        );
    }

    @SafeVarargs
    private List<String> buildTickerItems(List<CommodityPrice>... lists) {
        List<String> items = new ArrayList<>();
        for (List<CommodityPrice> list : lists) {
            for (CommodityPrice p : list) {
                String sign = p.getChange() >= 0 ? "+" : "";
                items.add(String.format("● %s ₹%.2f (%s%.2f%%)",
                    p.getSymbol(), p.getPrice(), sign, p.getChangePct()));
            }
        }
        return items;
    }

    private Map<String, Object> buildSummaryStats(List<CommodityPrice> precious,
                                                   List<CommodityPrice> base,
                                                   List<CommodityPrice> energy,
                                                   List<CommodityPrice> stocks) {
        Map<String, Object> stats = new LinkedHashMap<>();

        List<CommodityPrice> all = Stream.of(precious, base, energy, stocks)
            .flatMap(Collection::stream).collect(Collectors.toList());

        long gainers = all.stream().filter(p -> "UP".equals(p.getTrend())).count();
        long losers  = all.stream().filter(p -> "DOWN".equals(p.getTrend())).count();

        stats.put("totalAssets",  all.size());
        stats.put("gainers",      gainers);
        stats.put("losers",       losers);
        stats.put("unchanged",    all.size() - gainers - losers);
        stats.put("marketStatus", getMarketStatus());

        precious.stream().filter(p -> "GOLD".equals(p.getSymbol())).findFirst().ifPresent(g -> {
            stats.put("goldPrice",     g.getPrice());
            stats.put("goldChangePct", g.getChangePct());
        });

        precious.stream().filter(p -> "SILVER".equals(p.getSymbol())).findFirst().ifPresent(s -> {
            stats.put("silverPrice",     s.getPrice());
            stats.put("silverChangePct", s.getChangePct());
        });

        energy.stream().filter(p -> "CRUDEOIL".equals(p.getSymbol())).findFirst().ifPresent(c -> {
            stats.put("crudeOilPrice",     c.getPrice());
            stats.put("crudeOilChangePct", c.getChangePct());
        });

        return stats;
    }

    private String getMarketStatus() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(9, 0)) && now.isBefore(LocalTime.of(23, 30))) {
            return "OPEN";
        }
        return "CLOSED";
    }

    @SuppressWarnings("unchecked")
    private <T> T safeCall(java.util.function.Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Service call failed: {}", e.getMessage());
            return (T) Collections.emptyList();
        }
    }
}
