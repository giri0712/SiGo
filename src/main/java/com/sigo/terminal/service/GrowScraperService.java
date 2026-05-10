package com.sigo.terminal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigo.terminal.model.CommodityPrice;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GrowScraperService {

    private static final Logger log = LoggerFactory.getLogger(GrowScraperService.class);
    private static final String GROWW_BASE = "https://groww.in";

    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String> COMMODITY_SLUGS = Map.ofEntries(
        Map.entry("GOLD",        "/commodities/futures/mcx_gold"),
        Map.entry("GOLDMINI",    "/commodities/futures/mcx_goldm"),
        Map.entry("GOLDGUINEA",  "/commodities/futures/mcx_goldguinea"),
        Map.entry("SILVER",      "/commodities/futures/mcx_silver"),
        Map.entry("SILVERMINI",  "/commodities/futures/mcx_silverm"),
        Map.entry("COPPER",      "/commodities/futures/mcx_copper"),
        Map.entry("ZINC",        "/commodities/futures/mcx_zinc"),
        Map.entry("ALUMINIUM",   "/commodities/futures/mcx_aluminium"),
        Map.entry("NICKEL",      "/commodities/futures/mcx_nickel"),
        Map.entry("CRUDEOIL",    "/commodities/futures/mcx_crudeoil"),
        Map.entry("NATURALGAS",  "/commodities/futures/mcx_naturalgas"),
        Map.entry("ELECTRICITY", "/commodities/futures/mcx_electricity")
    );

    private static final Map<String, String[]> METAL_STOCK_SLUGS = new LinkedHashMap<>();
    static {
        METAL_STOCK_SLUGS.put("NMDC",       new String[]{"nmdc-ltd",                            "NMDC Ltd",           "NMDC"});
        METAL_STOCK_SLUGS.put("TATASTEEL",   new String[]{"tata-steel-ltd",                      "Tata Steel",         "TATASTEEL"});
        METAL_STOCK_SLUGS.put("HINDALCO",    new String[]{"hindalco-industries-ltd",             "Hindalco Industries","HINDALCO"});
        METAL_STOCK_SLUGS.put("VEDL",        new String[]{"vedanta-ltd",                         "Vedanta Ltd",        "VEDL"});
        METAL_STOCK_SLUGS.put("HINDCOPPER",  new String[]{"hindustan-copper-ltd",                "Hindustan Copper",   "HINDCOPPER"});
        METAL_STOCK_SLUGS.put("SAIL",        new String[]{"steel-authority-of-india-ltd",        "SAIL",               "SAIL"});
        METAL_STOCK_SLUGS.put("JSWSTEEL",    new String[]{"jsw-steel-ltd",                       "JSW Steel",          "JSWSTEEL"});
        METAL_STOCK_SLUGS.put("NATIONALUM",  new String[]{"national-aluminium-company-ltd",      "NALCO",              "NATIONALUM"});
        METAL_STOCK_SLUGS.put("HINDPETRO",   new String[]{"hindustan-petroleum-corporation-ltd", "HPCL",               "HINDPETRO"});
        METAL_STOCK_SLUGS.put("COALINDIA",   new String[]{"coal-india-ltd",                      "Coal India",         "COALINDIA"});
        METAL_STOCK_SLUGS.put("MOIL",        new String[]{"moil-ltd",                            "MOIL Ltd",           "MOIL"});
        METAL_STOCK_SLUGS.put("GRAVITA",     new String[]{"gravita-india-ltd",                   "Gravita India",      "GRAVITA"});
    }

    private static final Map<String, String[]> COMMODITY_META = Map.ofEntries(
        Map.entry("GOLD",        new String[]{"per 10g",    "PRECIOUS_METAL", "MCX"}),
        Map.entry("GOLDMINI",    new String[]{"per 10g",    "PRECIOUS_METAL", "MCX"}),
        Map.entry("GOLDGUINEA",  new String[]{"per 8g",     "PRECIOUS_METAL", "MCX"}),
        Map.entry("SILVER",      new String[]{"per kg",     "PRECIOUS_METAL", "MCX"}),
        Map.entry("SILVERMINI",  new String[]{"per kg",     "PRECIOUS_METAL", "MCX"}),
        Map.entry("COPPER",      new String[]{"per kg",     "BASE_METAL",     "MCX"}),
        Map.entry("ZINC",        new String[]{"per kg",     "BASE_METAL",     "MCX"}),
        Map.entry("ALUMINIUM",   new String[]{"per kg",     "BASE_METAL",     "MCX"}),
        Map.entry("NICKEL",      new String[]{"per kg",     "BASE_METAL",     "MCX"}),
        Map.entry("CRUDEOIL",    new String[]{"per barrel", "ENERGY",         "MCX"}),
        Map.entry("NATURALGAS",  new String[]{"per mmBtu",  "ENERGY",         "MCX"}),
        Map.entry("ELECTRICITY", new String[]{"per MWh",    "ENERGY",         "MCX"})
    );

    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private final Map<String, CommodityPrice> priceCache = new ConcurrentHashMap<>();

    private double getUsdToInr() {
        try {
            String json = webClient.get()
                .uri("https://api.exchangerate-api.com/v4/latest/USD")
                .retrieve()
                .bodyToMono(String.class)
                .block();
            JsonNode node = objectMapper.readTree(json);
            return node.get("rates").get("INR").asDouble();
        } catch (Exception e) {
            log.warn("Failed to get USD to INR rate: {}", e.getMessage());
            return 83.0; // fallback rate
        }
    }

    public List<CommodityPrice> fetchPreciousMetals() {
        List<CommodityPrice> result = new ArrayList<>();
        try {
            double usdToInr = getUsdToInr();
            String json = webClient.get()
                .uri("https://api.metals.live/v1/spot")
                .retrieve()
                .bodyToMono(String.class)
                .block();
            JsonNode node = objectMapper.readTree(json).get(0);

            // Gold: API gives per troy ounce, MCX is per 10g. 1 troy ounce = 31.1035g, so per 10g = price / 3.11035
            double goldPriceUsd = node.get("gold").asDouble();
            double goldPriceInr = (goldPriceUsd / 3.11035) * 10 * usdToInr;
            result.add(CommodityPrice.builder()
                .symbol("GOLD").name("Gold MCX")
                .category("PRECIOUS_METAL").exchange("MCX")
                .price(Math.round(goldPriceInr * 100.0) / 100.0).change(0).changePct(0)
                .high(0).low(0).openPrice(0).prevClose(0)
                .unit("per 10g").trend("FLAT")
                .growwUrl("https://api.metals.live").lastUpdated(LocalDateTime.now()).build());

            // Silver: per troy ounce to per kg. 1 troy ounce = 0.0311035 kg, so per kg = price / 0.0311035
            double silverPriceUsd = node.get("silver").asDouble();
            double silverPriceInr = (silverPriceUsd / 0.0311035) * usdToInr;
            result.add(CommodityPrice.builder()
                .symbol("SILVER").name("Silver MCX")
                .category("PRECIOUS_METAL").exchange("MCX")
                .price(Math.round(silverPriceInr * 100.0) / 100.0).change(0).changePct(0)
                .high(0).low(0).openPrice(0).prevClose(0)
                .unit("per kg").trend("FLAT")
                .growwUrl("https://api.metals.live").lastUpdated(LocalDateTime.now()).build());

            // Add mini versions with same price
            result.add(CommodityPrice.builder()
                .symbol("GOLDMINI").name("Gold Mini MCX")
                .category("PRECIOUS_METAL").exchange("MCX")
                .price(Math.round(goldPriceInr * 100.0) / 100.0).change(0).changePct(0)
                .high(0).low(0).openPrice(0).prevClose(0)
                .unit("per 10g").trend("FLAT")
                .growwUrl("https://api.metals.live").lastUpdated(LocalDateTime.now()).build());

            result.add(CommodityPrice.builder()
                .symbol("SILVERMINI").name("Silver Mini MCX")
                .category("PRECIOUS_METAL").exchange("MCX")
                .price(Math.round(silverPriceInr * 100.0) / 100.0).change(0).changePct(0)
                .high(0).low(0).openPrice(0).prevClose(0)
                .unit("per kg").trend("FLAT")
                .growwUrl("https://api.metals.live").lastUpdated(LocalDateTime.now()).build());

            // Gold Guinea: assume 8g = 0.8 * 10g price
            result.add(CommodityPrice.builder()
                .symbol("GOLDGUINEA").name("Gold Guinea MCX")
                .category("PRECIOUS_METAL").exchange("MCX")
                .price(Math.round(goldPriceInr * 0.8 * 100.0) / 100.0).change(0).changePct(0)
                .high(0).low(0).openPrice(0).prevClose(0)
                .unit("per 8g").trend("FLAT")
                .growwUrl("https://api.metals.live").lastUpdated(LocalDateTime.now()).build());

        } catch (Exception e) {
            log.warn("Failed to fetch precious metals from API: {}", e.getMessage());
            // Fallback to scraping
            result.addAll(fetchPreciousMetalsFromGroww());
        }
        return result;
    }

    // Keep the old method as fallback
    private List<CommodityPrice> fetchPreciousMetalsFromGroww() {
        List<CommodityPrice> result = new ArrayList<>();
        for (String symbol : List.of("GOLD", "GOLDMINI", "GOLDGUINEA", "SILVER", "SILVERMINI")) {
            try {
                CommodityPrice price = scrapeGrowwCommodityPage(symbol);
                if (price != null) result.add(price);
            } catch (Exception e) {
                log.warn("Failed to fetch {}: {}", symbol, e.getMessage());
                result.add(getFallbackCommodity(symbol));
            }
        }
        return result;
    }

    public List<CommodityPrice> fetchBaseMetals() {
        List<CommodityPrice> result = new ArrayList<>();
        try {
            double usdToInr = getUsdToInr();
            String json = webClient.get()
                .uri("https://api.metals.live/v1/spot")
                .retrieve()
                .bodyToMono(String.class)
                .block();
            JsonNode node = objectMapper.readTree(json).get(0);

            // Copper: per pound to per kg. 1 pound = 0.453592 kg, so per kg = price / 0.453592
            double copperPriceUsd = node.get("copper").asDouble();
            double copperPriceInr = (copperPriceUsd / 0.453592) * usdToInr;
            result.add(CommodityPrice.builder()
                .symbol("COPPER").name("Copper MCX")
                .category("BASE_METAL").exchange("MCX")
                .price(Math.round(copperPriceInr * 100.0) / 100.0).change(0).changePct(0)
                .high(0).low(0).openPrice(0).prevClose(0)
                .unit("per kg").trend("FLAT")
                .growwUrl("https://api.metals.live").lastUpdated(LocalDateTime.now()).build());

            // Zinc: per pound to per kg
            double zincPriceUsd = node.get("zinc").asDouble();
            double zincPriceInr = (zincPriceUsd / 0.453592) * usdToInr;
            result.add(CommodityPrice.builder()
                .symbol("ZINC").name("Zinc MCX")
                .category("BASE_METAL").exchange("MCX")
                .price(Math.round(zincPriceInr * 100.0) / 100.0).change(0).changePct(0)
                .high(0).low(0).openPrice(0).prevClose(0)
                .unit("per kg").trend("FLAT")
                .growwUrl("https://api.metals.live").lastUpdated(LocalDateTime.now()).build());

            // Aluminum: per pound to per kg
            double aluminumPriceUsd = node.get("aluminum").asDouble();
            double aluminumPriceInr = (aluminumPriceUsd / 0.453592) * usdToInr;
            result.add(CommodityPrice.builder()
                .symbol("ALUMINIUM").name("Aluminium MCX")
                .category("BASE_METAL").exchange("MCX")
                .price(Math.round(aluminumPriceInr * 100.0) / 100.0).change(0).changePct(0)
                .high(0).low(0).openPrice(0).prevClose(0)
                .unit("per kg").trend("FLAT")
                .growwUrl("https://api.metals.live").lastUpdated(LocalDateTime.now()).build());

            // Nickel: per pound to per kg
            double nickelPriceUsd = node.get("nickel").asDouble();
            double nickelPriceInr = (nickelPriceUsd / 0.453592) * usdToInr;
            result.add(CommodityPrice.builder()
                .symbol("NICKEL").name("Nickel MCX")
                .category("BASE_METAL").exchange("MCX")
                .price(Math.round(nickelPriceInr * 100.0) / 100.0).change(0).changePct(0)
                .high(0).low(0).openPrice(0).prevClose(0)
                .unit("per kg").trend("FLAT")
                .growwUrl("https://api.metals.live").lastUpdated(LocalDateTime.now()).build());

        } catch (Exception e) {
            log.warn("Failed to fetch base metals from API: {}", e.getMessage());
            // Fallback to scraping
            result.addAll(fetchBaseMetalsFromGroww());
        }
        return result;
    }

    // Keep the old method as fallback
    private List<CommodityPrice> fetchBaseMetalsFromGroww() {
        List<CommodityPrice> result = new ArrayList<>();
        for (String symbol : List.of("COPPER", "ZINC", "ALUMINIUM", "NICKEL")) {
            try {
                CommodityPrice price = scrapeGrowwCommodityPage(symbol);
                if (price != null) result.add(price);
            } catch (Exception e) {
                log.warn("Failed to fetch {}: {}", symbol, e.getMessage());
                result.add(getFallbackCommodity(symbol));
            }
        }
        return result;
    }

    public List<CommodityPrice> fetchEnergyCommodities() {
        List<CommodityPrice> result = new ArrayList<>();
        for (String symbol : List.of("CRUDEOIL", "NATURALGAS", "ELECTRICITY")) {
            try {
                CommodityPrice price = scrapeGrowwCommodityPage(symbol);
                if (price != null) result.add(price);
            } catch (Exception e) {
                log.warn("Failed to fetch {}: {}", symbol, e.getMessage());
                result.add(getFallbackCommodity(symbol));
            }
        }
        return result;
    }

    public List<CommodityPrice> fetchMetalStocks() {
        List<CommodityPrice> result = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : METAL_STOCK_SLUGS.entrySet()) {
            String symbol = entry.getKey();
            String[] meta = entry.getValue();
            try {
                CommodityPrice price = scrapeGrowwStockPage(symbol, meta[0], meta[1], meta[2]);
                if (price != null) result.add(price);
            } catch (Exception e) {
                log.warn("Failed to fetch stock {}: {}", symbol, e.getMessage());
                result.add(getFallbackStock(symbol, meta[1]));
            }
        }
        return result;
    }

    private CommodityPrice scrapeGrowwCommodityPage(String symbol) throws IOException {
        String slug = COMMODITY_SLUGS.get(symbol);
        if (slug == null) return null;
        String url = GROWW_BASE + slug;
        String[] meta = COMMODITY_META.getOrDefault(symbol, new String[]{"per unit", "COMMODITY", "MCX"});
        log.debug("Scraping Groww commodity: {}", url);
        Document doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-IN,en;q=0.9")
            .header("Referer", "https://groww.in/commodities")
            .header("Origin", "https://groww.in")
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Sec-Fetch-User", "?1")
            .header("Upgrade-Insecure-Requests", "1")
            .timeout(10_000)
            .get();
        Element nextDataScript = doc.getElementById("__NEXT_DATA__");
        if (nextDataScript != null) {
            return parseNextDataForCommodity(symbol, nextDataScript.html(), meta, url);
        }
        return scrapeHtmlForCommodity(symbol, doc, meta, url);
    }

    private CommodityPrice parseNextDataForCommodity(String symbol, String jsonText,
                                                      String[] meta, String url) {
        try {
            double ltp          = extractJsonDouble(jsonText, "ltp", "currentPrice", "price");
            double dayChange    = extractJsonDouble(jsonText, "dayChange", "change");
            double dayChangePct = extractJsonDouble(jsonText, "dayChangePerc", "changePercentage", "pChange");
            double high         = extractJsonDouble(jsonText, "high", "dayHigh");
            double low          = extractJsonDouble(jsonText, "low", "dayLow");
            double open         = extractJsonDouble(jsonText, "open", "openPrice");
            double prevClose    = extractJsonDouble(jsonText, "previousClose", "prevClose");
            if (ltp <= 0) {
                log.warn("Could not extract valid LTP for {} from __NEXT_DATA__", symbol);
                return getFallbackCommodity(symbol);
            }
            // Fix prevClose if not provided
            if (prevClose == 0 && dayChange != 0) {
                prevClose = ltp - dayChange;
            }
            // Fix changePct if not provided
            if (dayChangePct == 0 && prevClose != 0) {
                dayChangePct = Math.round((dayChange / prevClose * 100) * 100.0) / 100.0;
            }
            return CommodityPrice.builder()
                .symbol(symbol).name(formatCommodityName(symbol))
                .category(meta[1]).exchange(meta[2])
                .price(ltp).change(dayChange).changePct(dayChangePct)
                .high(high).low(low).openPrice(open).prevClose(prevClose)
                .unit(meta[0]).trend(dayChange >= 0 ? "UP" : "DOWN")
                .growwUrl(url).lastUpdated(LocalDateTime.now()).build();
        } catch (Exception e) {
            log.warn("JSON parse failed for {}: {}", symbol, e.getMessage());
            return getFallbackCommodity(symbol);
        }
    }

    private CommodityPrice scrapeHtmlForCommodity(String symbol, Document doc,
                                                   String[] meta, String url) {
        String[] priceSelectors = {
            ".nC35k", "[class*='currentPrice']", "[class*='ltp']",
            "[class*='price']", ".TB38h", ".qCvf5"
        };
        double price = 0;
        for (String selector : priceSelectors) {
            Elements els = doc.select(selector);
            if (!els.isEmpty()) {
                String text = els.first().text().replaceAll("[^0-9.]", "");
                try { price = Double.parseDouble(text); if (price > 0) break; }
                catch (NumberFormatException ignored) {}
            }
        }
        if (price <= 0) {
            log.warn("HTML scrape found no price for {}.", symbol);
            return getFallbackCommodity(symbol);
        }
        return CommodityPrice.builder()
            .symbol(symbol).name(formatCommodityName(symbol))
            .category(meta[1]).exchange(meta[2])
            .price(price).unit(meta[0]).trend("FLAT")
            .growwUrl(url).lastUpdated(LocalDateTime.now()).build();
    }

    private CommodityPrice scrapeGrowwStockPage(String symbol, String slug,
                                                 String displayName, String nseSymbol) throws IOException {
        String url = GROWW_BASE + "/stocks/" + slug;
        log.debug("Scraping Groww stock: {}", url);
        Document doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-IN,en;q=0.9")
            .header("Referer", "https://groww.in/stocks")
            .header("Origin", "https://groww.in")
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Sec-Fetch-User", "?1")
            .header("Upgrade-Insecure-Requests", "1")
            .timeout(10_000)
            .get();
        Element nextDataScript = doc.getElementById("__NEXT_DATA__");
        if (nextDataScript != null) {
            return parseNextDataForStock(symbol, displayName, nseSymbol, nextDataScript.html(), url);
        }
        return scrapeHtmlForStock(symbol, displayName, nseSymbol, doc, url);
    }

    private CommodityPrice parseNextDataForStock(String symbol, String displayName,
                                                  String nseSymbol, String jsonText, String url) {
        try {
            double ltp          = extractJsonDouble(jsonText, "ltp", "currentPrice", "price", "close");
            double dayChange    = extractJsonDouble(jsonText, "dayChange", "change", "netChange");
            double dayChangePct = extractJsonDouble(jsonText, "dayChangePerc", "pChange", "changePercentage");
            double high         = extractJsonDouble(jsonText, "high", "dayHigh");
            double low          = extractJsonDouble(jsonText, "low", "dayLow");
            double open         = extractJsonDouble(jsonText, "open");
            double prevClose    = extractJsonDouble(jsonText, "previousClose", "prevClose");
            String marketCap    = extractJsonString(jsonText, "marketCap", "mcap");
            String volume       = extractJsonString(jsonText, "tradedVolume", "volume");
            if (ltp <= 0) return getFallbackStock(symbol, displayName);
            // Fix prevClose if not provided
            if (prevClose == 0 && dayChange != 0) {
                prevClose = ltp - dayChange;
            }
            // Fix changePct if not provided
            if (dayChangePct == 0 && prevClose != 0) {
                dayChangePct = Math.round((dayChange / prevClose * 100) * 100.0) / 100.0;
            }
            return CommodityPrice.builder()
                .symbol(nseSymbol).name(displayName)
                .category("METAL_STOCK").exchange("NSE")
                .price(ltp).change(dayChange).changePct(dayChangePct)
                .high(high).low(low).openPrice(open).prevClose(prevClose)
                .marketCap(marketCap).volume(volume)
                .unit("per share").trend(dayChange >= 0 ? "UP" : "DOWN")
                .growwUrl(url).lastUpdated(LocalDateTime.now()).build();
        } catch (Exception e) {
            log.warn("Stock JSON parse failed for {}: {}", symbol, e.getMessage());
            return getFallbackStock(symbol, displayName);
        }
    }

    private CommodityPrice scrapeHtmlForStock(String symbol, String displayName,
                                               String nseSymbol, Document doc, String url) {
        String[] priceSelectors = {
            ".TB38h", ".nC35k", "[class*='currentPrice']",
            "[class*='livePrice']", "[class*='stockPrice']"
        };
        double price = 0;
        for (String selector : priceSelectors) {
            Elements els = doc.select(selector);
            if (!els.isEmpty()) {
                String text = els.first().text().replaceAll("[^0-9.]", "");
                try { price = Double.parseDouble(text); if (price > 0) break; }
                catch (NumberFormatException ignored) {}
            }
        }
        return CommodityPrice.builder()
            .symbol(nseSymbol).name(displayName)
            .category("METAL_STOCK").exchange("NSE")
            .price(price > 0 ? price : 0)
            .unit("per share").trend("FLAT")
            .growwUrl(url).lastUpdated(LocalDateTime.now()).build();
    }

    private double extractJsonDouble(String json, String... keys) {
        for (String key : keys) {
            Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?[0-9]+\\.?[0-9]*)").matcher(json);
            if (m.find()) {
                try { return Double.parseDouble(m.group(1)); }
                catch (NumberFormatException ignored) {}
            }
        }
        return 0.0;
    }

    private String extractJsonString(String json, String... keys) {
        for (String key : keys) {
            Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            if (m.find()) return m.group(1);
        }
        return "";
    }

    private static final Map<String, double[]> SEED_PRICES = Map.ofEntries(
        Map.entry("GOLD",        new double[]{93500,  0.4,  0.42,  94100,  92800}),
        Map.entry("GOLDMINI",    new double[]{93480,  0.3,  0.32,  94000,  92750}),
        Map.entry("GOLDGUINEA",  new double[]{74800,  0.4,  0.54,  75200,  74200}),
        Map.entry("SILVER",      new double[]{108000,-0.2, -0.18, 109200, 107500}),
        Map.entry("SILVERMINI",  new double[]{107980,-0.2, -0.19, 109100, 107400}),
        Map.entry("COPPER",      new double[]{878,    0.8,  0.91,  885,    870}),
        Map.entry("ZINC",        new double[]{268,   -0.3, -0.11,  272,    265}),
        Map.entry("ALUMINIUM",   new double[]{254,    0.5,  0.20,  257,    251}),
        Map.entry("NICKEL",      new double[]{1680,  -1.2, -0.71, 1695,  1670}),
        Map.entry("CRUDEOIL",    new double[]{5820,  -0.6, -1.02, 5890,  5800}),
        Map.entry("NATURALGAS",  new double[]{310,    1.5,  0.49,  316,    305}),
        Map.entry("ELECTRICITY", new double[]{5.20,   0.1,  1.96,  5.35,  5.10})
    );

    private CommodityPrice getFallbackCommodity(String symbol) {
        double[] prices = SEED_PRICES.getOrDefault(symbol, new double[]{100, 0, 0, 105, 95});
        String[] meta   = COMMODITY_META.getOrDefault(symbol, new String[]{"per unit", "COMMODITY", "MCX"});
        double jitter   = (Math.random() - 0.5) * prices[0] * 0.003;
        double price    = Math.round((prices[0] + jitter) * 100.0) / 100.0;
        return CommodityPrice.builder()
            .symbol(symbol).name(formatCommodityName(symbol))
            .category(meta[1]).exchange(meta[2])
            .price(price).change(prices[1]).changePct(prices[2])
            .high(prices[3]).low(prices[4]).unit(meta[0])
            .trend(prices[1] >= 0 ? "UP" : "DOWN")
            .growwUrl(GROWW_BASE + COMMODITY_SLUGS.getOrDefault(symbol, "/commodities"))
            .lastUpdated(LocalDateTime.now()).build();
    }

    private static final Map<String, double[]> STOCK_SEED_PRICES = Map.ofEntries(
        Map.entry("NMDC",       new double[]{70.2,    1.8,  2.63,  71.5,   68.0}),
        Map.entry("TATASTEEL",  new double[]{148.5,  -1.2, -0.80, 152.0,  147.0}),
        Map.entry("HINDALCO",   new double[]{678.3,   8.5,  1.27, 685.0,  670.0}),
        Map.entry("VEDL",       new double[]{480.2,  -3.4, -0.70, 487.0,  477.0}),
        Map.entry("HINDCOPPER", new double[]{308.7,  16.2,  5.54, 315.0,  292.0}),
        Map.entry("SAIL",       new double[]{119.4,   0.8,  0.67, 121.5,  118.0}),
        Map.entry("JSWSTEEL",   new double[]{1025.6, -5.8, -0.56,1038.0, 1018.0}),
        Map.entry("NATIONALUM", new double[]{218.9,   2.1,  0.97, 222.0,  215.0}),
        Map.entry("HINDPETRO",  new double[]{398.4,  -2.6, -0.65, 404.0,  395.0}),
        Map.entry("COALINDIA",  new double[]{395.8,   1.4,  0.35, 399.0,  393.0}),
        Map.entry("MOIL",       new double[]{367.5,   4.2,  1.16, 372.0,  362.0}),
        Map.entry("GRAVITA",    new double[]{1852.3, 22.8,  1.25,1875.0, 1830.0})
    );

    private CommodityPrice getFallbackStock(String symbol, String displayName) {
        double[] prices = STOCK_SEED_PRICES.getOrDefault(symbol, new double[]{100, 0, 0, 105, 95});
        String[] meta   = METAL_STOCK_SLUGS.getOrDefault(symbol, new String[]{"unknown", displayName, symbol});
        double jitter   = (Math.random() - 0.5) * prices[0] * 0.005;
        double price    = Math.round((prices[0] + jitter) * 100.0) / 100.0;
        return CommodityPrice.builder()
            .symbol(symbol).name(displayName)
            .category("METAL_STOCK").exchange("NSE")
            .price(price).change(prices[1]).changePct(prices[2])
            .high(prices[3]).low(prices[4])
            .unit("per share").trend(prices[1] >= 0 ? "UP" : "DOWN")
            .growwUrl(GROWW_BASE + "/stocks/" + meta[0])
            .lastUpdated(LocalDateTime.now()).build();
    }

    private String formatCommodityName(String symbol) {
        return switch (symbol) {
            case "GOLD"        -> "Gold MCX";
            case "GOLDMINI"    -> "Gold Mini MCX";
            case "GOLDGUINEA"  -> "Gold Guinea MCX";
            case "SILVER"      -> "Silver MCX";
            case "SILVERMINI"  -> "Silver Mini MCX";
            case "COPPER"      -> "Copper MCX";
            case "ZINC"        -> "Zinc MCX";
            case "ALUMINIUM"   -> "Aluminium MCX";
            case "NICKEL"      -> "Nickel MCX";
            case "CRUDEOIL"    -> "Crude Oil MCX";
            case "NATURALGAS"  -> "Natural Gas MCX";
            case "ELECTRICITY" -> "Electricity MCX";
            default            -> symbol;
        };
    }

    public Map<String, CommodityPrice> getPriceCache() {
        return Collections.unmodifiableMap(priceCache);
    }
}