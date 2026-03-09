package com.sigo.api.service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Service
public class SigoScraperService {

    private String lastGold24k = "Loading...";
    private String lastGold22k = "Loading...";
    private String lastSilver  = "Loading...";

    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";

    @PostConstruct
    public void init() {
        scrapeAll();
    }

    public Map<String, String> getLatestMarketData() {
        Map<String, String> data = new HashMap<>();
        data.put("GOLD 24K", lastGold24k);
        data.put("GOLD 22K", lastGold22k);
        data.put("SILVER",   lastSilver);
        return data;
    }

    @Scheduled(fixedRate = 1800000) // every 30 minutes
    public void scrapeAll() {
        scrapeGold();
        scrapeSilver();
    }
    private void scrapeGold() {
    try {
        Document doc = Jsoup.connect("https://groww.in/gold-rates")
                .userAgent(USER_AGENT)
                .referrer("https://www.google.com/")
                .timeout(30000)
                .get();

        boolean found24 = false;
        boolean found22 = false;

        Elements tables = doc.select("table");

        for (Element table : tables) {
            Elements rows = table.select("tr");
            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() >= 2 && cols.get(0).text().trim().equals("1 Gram")) {
                    String price = cleanPrice(cols.get(1).text());

                    if (!found24) {
                        lastGold24k = price;
                        found24 = true;
                        System.out.println("Gold 24K: " + lastGold24k + "/g");

                    } else if (!found22) {
                        lastGold22k = price;
                        found22 = true;
                        System.out.println("Gold 22K: " + lastGold22k + "/g");
                        break;
                    }
                }
            }
            if (found24 && found22) break;
        }
        if (!found22) {
            lastGold22k = calculate22kFrom24k(lastGold24k);
            System.out.println("Gold 22K (calculated): " + lastGold22k);
        }

    } catch (Exception e) {
        System.err.println("Gold scraping failed: " + e.getMessage());
    }
}
    private void scrapeSilver() {
        try {
            Document doc = Jsoup.connect("https://groww.in/silver-rates")
                    .userAgent(USER_AGENT)
                    .referrer("https://www.google.com/")
                    .timeout(30000)
                    .get();


            Elements tables = doc.select("table");
            for (Element table : tables) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    Elements cols = row.select("td");
                    if (cols.size() >= 2 && cols.get(0).text().trim().equals("1 Gram")) {
                        lastSilver = cleanPrice(cols.get(1).text());
                        System.out.println("Silver scraped: " + lastSilver + "/g");
                        return;
                    }
                }
            }

            System.err.println("Silver: '1 Gram' row not found on page.");

        } catch (Exception e) {
            System.err.println("Silver scraping failed: " + e.getMessage());
        }
    }


    private String cleanPrice(String raw) {
        if (raw == null || raw.isBlank()) return "N/A";
        String clean = raw.split("\\(")[0].trim();
        if (!clean.startsWith("₹")) clean = "₹" + clean;
        return clean;
    }

    private String calculate22kFrom24k(String gold24kPrice) {
        try {
            String numeric = gold24kPrice.replace("₹", "").replace(",", "").trim();
            double value22k = Double.parseDouble(numeric) * (22.0 / 24.0);
            return "₹" + String.format("%.2f", value22k);
        } catch (Exception e) {
            return "N/A";
        }
    }
}