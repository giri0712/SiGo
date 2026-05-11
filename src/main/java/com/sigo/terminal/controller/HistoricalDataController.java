package com.sigo.terminal.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@RestController
@RequestMapping("/api/v1/historical")
@CrossOrigin(origins = "*")
public class HistoricalDataController {

    private static final Logger log = LoggerFactory.getLogger(HistoricalDataController.class);
    private final WebClient webClient = WebClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    // Yahoo Finance symbol mapping
    private static final Map<String, String> YAHOO_SYMBOLS = Map.ofEntries(
        Map.entry("GOLD",      "GC=F"),
        Map.entry("SILVER",    "SI=F"),
        Map.entry("CRUDEOIL",  "CL=F"),
        Map.entry("NIFTY50",   "^NSEI"),
        Map.entry("SENSEX",    "^BSESN"),
        Map.entry("RELIANCE",  "RELIANCE.NS"),
        Map.entry("TCS",       "TCS.NS"),
        Map.entry("HDFCBANK",  "HDFCBANK.NS"),
        Map.entry("INFOSYS",   "INFY.NS"),
        Map.entry("ITC",       "ITC.NS"),
        Map.entry("TATASTEEL", "TATASTEEL.NS"),
        Map.entry("HINDALCO",  "HINDALCO.NS")
    );


    @GetMapping("/{symbol}")
    public ResponseEntity<Map<String, Object>> getHistorical(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1y") String range,
            @RequestParam(defaultValue = "1wk") String interval) {

        String yahooSym = YAHOO_SYMBOLS.getOrDefault(symbol.toUpperCase(), symbol + ".NS");
        String url = String.format(
            "https://query1.finance.yahoo.com/v8/finance/chart/%s?range=%s&interval=%s&events=history",
            yahooSym, range, interval);

        try {
            String json = webClient.get().uri(url)
                .header("User-Agent", "Mozilla/5.0")
                .retrieve().bodyToMono(String.class).block();

            JsonNode root    = mapper.readTree(json);
            JsonNode result  = root.path("chart").path("result").get(0);
            JsonNode meta    = result.path("meta");
            JsonNode tsNode  = result.path("timestamp");
            JsonNode closes  = result.path("indicators").path("quote").get(0).path("close");

            List<Long>   timestamps = new ArrayList<>();
            List<Double> prices     = new ArrayList<>();

            for (JsonNode ts : tsNode) timestamps.add(ts.asLong() * 1000L);
            for (JsonNode c  : closes) prices.add(c.isNull() ? null : c.asDouble());

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("symbol",    symbol.toUpperCase());
            resp.put("yahoo",     yahooSym);
            resp.put("currency",  meta.path("currency").asText("INR"));
            resp.put("range",     range);
            resp.put("interval",  interval);
            resp.put("timestamps", timestamps);
            resp.put("prices",    prices);
            resp.put("current",   meta.path("regularMarketPrice").asDouble());
            resp.put("prevClose", meta.path("previousClose").asDouble());

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            log.error("Historical fetch failed for {}: {}", symbol, e.getMessage());
            return ResponseEntity.ok(Map.of("symbol", symbol, "error", e.getMessage(),
                "timestamps", List.of(), "prices", List.of()));
        }
    }

    /**
     * GET /api/v1/historical/macro/india
     * Returns India CPI, GDP growth (static/approximate — upgrade to RBI API later)
     */
    @GetMapping("/macro/india")
    public ResponseEntity<Map<String, Object>> getIndiaMacro() {
        // India CPI YoY % and GDP growth % — sourced from RBI/MOSPI public data
        // These are approximate recent values; replace with live RBI API when needed
        Map<String, Object> macro = new LinkedHashMap<>();

        macro.put("cpi", List.of(
            Map.of("month", "Apr-24", "headline", 4.83, "core", 3.73),
            Map.of("month", "May-24", "headline", 4.75, "core", 3.60),
            Map.of("month", "Jun-24", "headline", 5.08, "core", 3.12),
            Map.of("month", "Jul-24", "headline", 3.54, "core", 3.40),
            Map.of("month", "Aug-24", "headline", 3.65, "core", 3.35),
            Map.of("month", "Sep-24", "headline", 5.49, "core", 3.51),
            Map.of("month", "Oct-24", "headline", 6.21, "core", 3.74),
            Map.of("month", "Nov-24", "headline", 5.48, "core", 3.66),
            Map.of("month", "Dec-24", "headline", 5.22, "core", 3.62),
            Map.of("month", "Jan-25", "headline", 4.31, "core", 3.67),
            Map.of("month", "Feb-25", "headline", 3.61, "core", 3.53),
            Map.of("month", "Mar-25", "headline", 3.34, "core", 3.64),
            Map.of("month", "Apr-25", "headline", 3.16, "core", 3.64)
        ));

        macro.put("gdp", List.of(
            Map.of("quarter", "Q1 FY24", "growth", 8.2),
            Map.of("quarter", "Q2 FY24", "growth", 6.7),
            Map.of("quarter", "Q3 FY24", "growth", 8.4),
            Map.of("quarter", "Q4 FY24", "growth", 7.8),
            Map.of("quarter", "Q1 FY25", "growth", 6.7),
            Map.of("quarter", "Q2 FY25", "growth", 5.4),
            Map.of("quarter", "Q3 FY25", "growth", 6.2),
            Map.of("quarter", "Q4 FY25", "growth", 6.5)
        ));

        macro.put("rbiRate",      6.25);
        macro.put("rbiRateDate", "Apr-2025");
        macro.put("target",       4.0);
        macro.put("source",      "RBI / MOSPI");

        return ResponseEntity.ok(macro);
    }
}