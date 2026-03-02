package com.sigo.api.controller;

import com.sigo.api.service.SigoScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/sigo")
public class MarketController {

    private static final String PENDING = "—";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss");

    private final SigoScraperService scraperService;

    public MarketController(SigoScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping(value = "/prices", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> getLivePrices() {
        Map<String, String> data = scraperService.getLatestMarketData();

        PricePair gold24 = parsePricePair(data, "GOLD 24K");
        PricePair gold22 = parsePricePair(data, "GOLD 22K");
        PricePair silver = parsePricePair(data, "SILVER");

        String updated = LocalDateTime.now().format(FORMATTER);

        return ResponseEntity.ok(buildHtml(gold24, gold22, silver, updated));
    }

    record PricePair(String metal, String perGram, String perKg, boolean isPending) {}

    private PricePair parsePricePair(Map<String, String> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return new PricePair(key, PENDING, PENDING, true);
        }
        try {
            String raw = data.get(key).trim();
            double pricePerGram = Double.parseDouble(raw.replaceAll("[^\\d.]", ""));
            double pricePerKg = pricePerGram * 1000;
            String symbol = raw.replaceAll("[\\d.,\\s/a-zA-Z]", "");
            if (symbol.isBlank()) symbol = "₹";

            return new PricePair(
                key,
                symbol + String.format("%,.2f", pricePerGram),
                symbol + String.format("%,.2f", pricePerKg),
                false
            );
        } catch (NumberFormatException e) {
            return new PricePair(key, data.get(key), "N/A", false);
        }
    }

    private String buildHtml(PricePair gold24, PricePair gold22, PricePair silver, String updated) {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SiGo Live Market Rates</title>
    <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@300;400;600;700&family=DM+Mono:wght@300;400;500&display=swap" rel="stylesheet">
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        :root {
            --gold: #c9a84c;
            --gold-lt: #e8c97a;
            --gold-dim: #8a6f2e;
            --silver: #b0bec5;
            --silver-lt: #e0e8ec;
            --bg: #0d0e10;
            --surface: #131519;
            --card-bg: #1a1d22;
            --text: #e8e4d8;
            --muted: #6b6a67;
            --border: rgba(201,168,76,0.2);
        }

        body {
            font-family: 'DM Mono', monospace;
            background: var(--bg);
            color: var(--text);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            overflow-x: hidden;
        }

        /* ── HERO ── */
        .hero {
            width: 100%%;
            padding: 40px 20px 20px;
            text-align: center;
        }

        .brand {
            font-family: 'Cormorant Garamond', serif;
            font-size: clamp(2rem, 6vw, 3.2rem);
            font-weight: 300;
            letter-spacing: 0.25em;
            text-transform: uppercase;
            color: var(--gold-lt);
        }
        .brand span { color: var(--gold); font-weight: 700; }

        /* ── LIVE TICKER ── */
        .ticker-wrap {
            width: 100%%;
            background: var(--gold-dim);
            height: 32px;
            display: flex;
            align-items: center;
            overflow: hidden;
        }

        .ticker-track {
            display: flex;
            animation: ticker 25s linear infinite;
            white-space: nowrap;
        }

        .ticker-track span {
            font-size: 0.7rem;
            color: #fff;
            letter-spacing: 0.2em;
            padding: 0 40px;
            text-transform: uppercase;
        }

        @keyframes ticker {
            0%% { transform: translateX(0); }
            100%% { transform: translateX(-50%%); }
        }

        /* ── TOP BARS SECTION ── */
        .bars-header {
            width: 100%%;
            max-width: 900px;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            padding: 30px 20px;
        }

        .bar-svg { width: 100%%; filter: drop-shadow(0 15px 30px rgba(0,0,0,0.6)); }

        /* ── MAIN CONTAINER ── */
        .container {
            width: 95%%;
            max-width: 1100px;
            padding: 20px 0 60px;
        }

        /* ── CARDS ── */
        .cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 20px;
            margin-bottom: 40px;
        }

        .card {
            background: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 25px;
        }

        .card-name {
            font-family: 'Cormorant Garamond', serif;
            font-size: 1.4rem;
            color: var(--gold);
            letter-spacing: 0.1em;
            border-bottom: 1px solid var(--border);
            padding-bottom: 8px;
            margin-bottom: 20px;
            text-transform: uppercase;
        }
        .card.silver .card-name { color: var(--silver); }

        .price-row {
            display: flex;
            justify-content: space-between;
            align-items: baseline;
            margin-bottom: 10px;
        }

        .price-label { font-size: 0.6rem; color: var(--muted); text-transform: uppercase; letter-spacing: 0.1em; }
        
        .price-val { 
            font-size: 1.35rem; 
            font-weight: 500; 
            color: var(--gold-lt); 
            white-space: nowrap; 
            padding-left: 10px;
        }
        .card.silver .price-val { color: var(--silver-lt); }

        /* ── TABLE ── */
        .table-section {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 16px;
            overflow: hidden;
        }

        table { width: 100%%; border-collapse: collapse; }
        th { padding: 15px; text-align: left; font-size: 0.6rem; color: var(--muted); text-transform: uppercase; background: rgba(255,255,255,0.02); }
        td { padding: 20px 15px; border-bottom: 1px solid rgba(255,255,255,0.03); font-size: 0.95rem; }
        
        .metal-cell { font-family: 'Cormorant Garamond', serif; font-size: 1.1rem; font-weight: 600; color: var(--gold); }
        .silver-row .metal-cell { color: var(--silver); }

        @media (max-width: 600px) {
            .bars-header { grid-template-columns: 1fr; }
            .brand { font-size: 2rem; }
        }
    </style>
</head>
<body>

<div class="hero">
    <div class="brand"><span>Si</span>Go Market</div>
</div>

<div class="ticker-wrap">
    <div class="ticker-track">
        <span>● LIVE MARKET RATES</span>
        <span>GOLD 24K DATA SCRAPED</span>
        <span>GOLD 22K DATA SCRAPED</span>
        <span>SILVER DATA SCRAPED</span>
        <span>● LIVE MARKET RATES</span>
        <span>GOLD 24K DATA SCRAPED</span>
        <span>GOLD 22K DATA SCRAPED</span>
        <span>SILVER DATA SCRAPED</span>
    </div>
</div>

<div class="bars-header">
    <svg class="bar-svg" viewBox="0 0 400 180">
        <defs>
            <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%%" stop-color="#f0d080"/><stop offset="50%%" stop-color="#c9a84c"/><stop offset="100%%" stop-color="#8a6020"/>
            </linearGradient>
        </defs>
        <path d="M50,160 L90,20 L310,20 L350,160 Z" fill="url(#g)" stroke="#5c4416" stroke-width="1.5"/>
        <text x="200" y="100" text-anchor="middle" font-family="'Cormorant Garamond'" font-weight="700" font-size="28" fill="#3a2e00">GOLD</text>
    </svg>

    <svg class="bar-svg" viewBox="0 0 400 180">
        <defs>
            <linearGradient id="s" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%%" stop-color="#e0e8ec"/><stop offset="50%%" stop-color="#90a4ae"/><stop offset="100%%" stop-color="#455a64"/>
            </linearGradient>
        </defs>
        <path d="M50,160 L90,20 L310,20 L350,160 Z" fill="url(#s)" stroke="#263238" stroke-width="1.5"/>
        <text x="200" y="100" text-anchor="middle" font-family="'Cormorant Garamond'" font-weight="700" font-size="28" fill="#1a252b">SILVER</text>
    </svg>
</div>

<div class="container">
    <div class="cards">
        <div class="card">
            <div class="card-name">Gold 24K</div>
            <div class="price-row"><span class="price-label">Gram</span><span class="price-val">%s</span></div>
            <div class="price-row"><span class="price-label">Kilo</span><span class="price-val">%s</span></div>
        </div>
        <div class="card">
            <div class="card-name">Gold 22K</div>
            <div class="price-row"><span class="price-label">Gram</span><span class="price-val">%s</span></div>
            <div class="price-row"><span class="price-label">Kilo</span><span class="price-val">%s</span></div>
        </div>
        <div class="card silver">
            <div class="card-name">Silver</div>
            <div class="price-row"><span class="price-label">Gram</span><span class="price-val">%s</span></div>
            <div class="price-row"><span class="price-label">Kilo</span><span class="price-val">%s</span></div>
        </div>
    </div>

    <div class="table-section">
        <table>
            <thead><tr><th>Metal</th><th>Per Gram</th><th>Per Kilogram</th></tr></thead>
            <tbody>
                <tr><td class="metal-cell">Gold 24K</td><td class="price-val">%s</td><td class="price-val">%s</td></tr>
                <tr><td class="metal-cell">Gold 22K</td><td class="price-val">%s</td><td class="price-val">%s</td></tr>
                <tr class="silver-row"><td class="metal-cell">Silver</td><td class="price-val">%s</td><td class="price-val">%s</td></tr>
            </tbody>
        </table>
        <div style="padding: 12px; font-size: 0.6rem; color: var(--muted); text-align: right;">Updated: %s</div>
    </div>
</div>

</body>
</html>
        """.formatted(
            gold24.perGram(), gold24.perKg(),
            gold22.perGram(), gold22.perKg(),
            silver.perGram(), silver.perKg(),
            gold24.perGram(), gold24.perKg(),
            gold22.perGram(), gold22.perKg(),
            silver.perGram(), silver.perKg(),
            updated
        );
    }
}