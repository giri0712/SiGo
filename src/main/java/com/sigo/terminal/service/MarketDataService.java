package com.sigo.terminal.service;

import com.sigo.terminal.model.CommodityPrice;
import com.sigo.terminal.model.MarketSnapshot;
import com.sigo.terminal.model.NewsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    // MCX hours IST: Mon-Fri 09:00-23:30, Sat 09:00-23:55, Sun CLOSED
    private static final ZoneId IST            = ZoneId.of("Asia/Kolkata");
    private static final LocalTime MCX_OPEN    = LocalTime.of(9, 0);
    private static final LocalTime MCX_CLOSE_WD  = LocalTime.of(23, 30);
    private static final LocalTime MCX_CLOSE_SAT = LocalTime.of(23, 55);

    private final GrowScraperService growScraperService;
    private final NewsScraperService newsScraperService;

    public MarketDataService(GrowScraperService growScraperService,
                             NewsScraperService newsScraperService) {
        this.growScraperService = growScraperService;
        this.newsScraperService = newsScraperService;
    }

    private volatile MarketSnapshot cachedSnapshot;

    public MarketSnapshot getSnapshot() {
        if (cachedSnapshot == null) return refreshSnapshot();
        return cachedSnapshot;
    }

    public MarketSnapshot refreshSnapshot() {
        log.info("Refreshing market snapshot...");
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
            .preciousMetals(precious).baseMetals(baseMetals)
            .energyCommodities(energy).metalStocks(stocks)
            .indices(indices).bonds(bonds).news(news)
            .tickerItems(ticker).summaryStats(stats)
            .fetchedAt(LocalDateTime.now())
            .marketStatus(getMcxMarketStatus())
            .isLive(true).build();

        cachedSnapshot = snapshot;
        log.info("Snapshot refreshed in {}ms", System.currentTimeMillis() - start);
        return snapshot;
    }

    public String getMcxMarketStatus() {
        ZonedDateTime now = ZonedDateTime.now(IST);
        DayOfWeek day     = now.getDayOfWeek();
        LocalTime time    = now.toLocalTime();
        if (day == DayOfWeek.SUNDAY) return "CLOSED";
        LocalTime close = (day == DayOfWeek.SATURDAY) ? MCX_CLOSE_SAT : MCX_CLOSE_WD;
        return (time.isAfter(MCX_OPEN) && time.isBefore(close)) ? "OPEN" : "CLOSED";
    }

    private List<CommodityPrice> buildIndicesList() {
        LocalDateTime now = LocalDateTime.now();
        double j = (Math.random() - 0.5) * 2;
        return List.of(
            CommodityPrice.builder().symbol("NIFTY50").name("Nifty 50").category("INDEX").exchange("NSE")
                .price(24100+j*50).change(120+j).changePct(0.50+j*0.01).trend("UP")
                .unit("points").growwUrl("https://groww.in/indices/nifty-50").lastUpdated(now).build(),
            CommodityPrice.builder().symbol("NIFTYMETAL").name("Nifty Metal").category("INDEX").exchange("NSE")
                .price(9850+j*30).change(200+j).changePct(2.07+j*0.01).trend("UP")
                .unit("points").growwUrl("https://groww.in/indices/nifty-metal").lastUpdated(now).build(),
            CommodityPrice.builder().symbol("SENSEX").name("BSE Sensex").category("INDEX").exchange("BSE")
                .price(79500+j*100).change(350+j).changePct(0.44+j*0.01).trend("UP")
                .unit("points").growwUrl("https://groww.in/indices/bse-sensex").lastUpdated(now).build()
        );
    }

    private List<CommodityPrice> buildBondList() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
            CommodityPrice.builder().symbol("GSEC10Y").name("India 10Y G-Sec").category("BOND").exchange("RBI")
                .price(6.85).change(-0.02).changePct(-0.29).trend("DOWN").unit("yield %")
                .growwUrl("https://groww.in/bonds").lastUpdated(now).build(),
            CommodityPrice.builder().symbol("GSEC2Y").name("India 2Y G-Sec").category("BOND").exchange("RBI")
                .price(6.60).change(0.01).changePct(0.15).trend("UP").unit("yield %")
                .growwUrl("https://groww.in/bonds").lastUpdated(now).build(),
            CommodityPrice.builder().symbol("TBILL91").name("91-Day T-Bill").category("BOND").exchange("RBI")
                .price(6.45).change(0.0).changePct(0.0).trend("FLAT").unit("yield %")
                .growwUrl("https://groww.in/bonds").lastUpdated(now).build()
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
        Map<String, Object> s = new LinkedHashMap<>();
        List<CommodityPrice> all = Stream.of(precious, base, energy, stocks)
            .flatMap(Collection::stream).collect(Collectors.toList());
        long gainers = all.stream().filter(p -> "UP".equals(p.getTrend())).count();
        long losers  = all.stream().filter(p -> "DOWN".equals(p.getTrend())).count();
        s.put("totalAssets", all.size()); s.put("gainers", gainers);
        s.put("losers", losers); s.put("unchanged", all.size()-gainers-losers);
        s.put("marketStatus", getMcxMarketStatus());
        precious.stream().filter(p->"GOLD".equals(p.getSymbol())).findFirst().ifPresent(g->{
            s.put("goldPrice", g.getPrice()); s.put("goldChangePct", g.getChangePct());});
        precious.stream().filter(p->"SILVER".equals(p.getSymbol())).findFirst().ifPresent(sv->{
            s.put("silverPrice", sv.getPrice()); s.put("silverChangePct", sv.getChangePct());});
        energy.stream().filter(p->"CRUDEOIL".equals(p.getSymbol())).findFirst().ifPresent(c->{
            s.put("crudeOilPrice", c.getPrice()); s.put("crudeOilChangePct", c.getChangePct());});
        return s;
    }

    @SuppressWarnings("unchecked")
    private <T> T safeCall(java.util.function.Supplier<T> supplier) {
        try { return supplier.get(); }
        catch (Exception e) { log.error("Call failed: {}", e.getMessage()); return (T) Collections.emptyList(); }
    }
}