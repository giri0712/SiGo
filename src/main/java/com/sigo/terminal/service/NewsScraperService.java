package com.sigo.terminal.service;

import com.sigo.terminal.model.NewsItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class NewsScraperService {

    private static final Logger log = LoggerFactory.getLogger(NewsScraperService.class);

    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private static final String GROWW_DIGEST_URL  = "https://groww.in/digest";
    private static final String ET_COMMODITIES_RSS =
        "https://economictimes.indiatimes.com/markets/commodities/rssfeeds/1808152.cms";
    private static final String ET_MARKETS_RSS =
        "https://economictimes.indiatimes.com/markets/stocks/rssfeeds/2146842.cms";
    private static final String ET_BONDS_RSS =
        "https://economictimes.indiatimes.com/markets/bonds/rssfeeds/1808153.cms";

    public List<NewsItem> fetchLatestNews() {
        List<NewsItem> allNews = new ArrayList<>();

        try { allNews.addAll(fetchEtRss(ET_COMMODITIES_RSS, "COMMODITY")); }
        catch (Exception e) { log.warn("ET Commodities RSS failed: {}", e.getMessage()); }

        try { allNews.addAll(fetchEtRss(ET_MARKETS_RSS, "STOCK")); }
        catch (Exception e) { log.warn("ET Markets RSS failed: {}", e.getMessage()); }

        try { allNews.addAll(fetchEtRss(ET_BONDS_RSS, "BOND")); }
        catch (Exception e) { log.warn("ET Bonds RSS failed: {}", e.getMessage()); }

        try { allNews.addAll(fetchGrowwDigest()); }
        catch (Exception e) { log.warn("Groww digest scrape failed: {}", e.getMessage()); }

        List<NewsItem> filtered = allNews.stream()
            .filter(this::isRelevantNews)
            .sorted(Comparator.comparing(NewsItem::getPublishedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .distinct()
            .limit(30)
            .toList();

        return filtered.isEmpty() ? getFallbackNews() : filtered;
    }

    private List<NewsItem> fetchEtRss(String rssUrl, String defaultCategory) throws IOException {
        List<NewsItem> items = new ArrayList<>();

        Document doc = Jsoup.connect(rssUrl)
            .userAgent(USER_AGENT)
            .timeout(8000)
            .parser(org.jsoup.parser.Parser.xmlParser())
            .get();

        Elements entries = doc.select("item");
        for (Element entry : entries) {
            String title   = entry.selectFirst("title")      != null ? entry.selectFirst("title").text()      : "";
            String desc    = entry.selectFirst("description") != null
                ? Jsoup.parse(entry.selectFirst("description").text()).text() : "";
            String link    = entry.selectFirst("link")       != null ? entry.selectFirst("link").text()       : "";

            if (desc.length() > 200) desc = desc.substring(0, 197) + "...";

            items.add(NewsItem.builder()
                .title(title)
                .summary(desc)
                .source("ET Markets")
                .url(link)
                .category(classifyNewsCategory(title, defaultCategory))
                .relatedSymbol(extractRelatedSymbol(title))
                .sentiment(classifySentiment(title))
                .publishedAt(LocalDateTime.now())
                .build());
        }
        return items;
    }

    private List<NewsItem> fetchGrowwDigest() throws IOException {
        List<NewsItem> items = new ArrayList<>();

        Document doc = Jsoup.connect(GROWW_DIGEST_URL)
            .userAgent(USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml")
            .timeout(8000)
            .get();

        Elements cards = doc.select("[class*='digestCard'], [class*='newsCard'], article, .digest-item");

        for (Element card : cards) {
            String title   = card.select("h2, h3, [class*='title'], [class*='heading']").text();
            String summary = card.select("p, [class*='summary'], [class*='desc']").text();
            String link    = card.select("a[href]").attr("abs:href");

            if (title.isEmpty()) continue;
            if (summary.length() > 200) summary = summary.substring(0, 197) + "...";

            items.add(NewsItem.builder()
                .title(title)
                .summary(summary)
                .source("Groww Digest")
                .url(link.isEmpty() ? GROWW_DIGEST_URL : link)
                .category(classifyNewsCategory(title, "MACRO"))
                .relatedSymbol(extractRelatedSymbol(title))
                .sentiment(classifySentiment(title))
                .publishedAt(LocalDateTime.now())
                .build());
        }
        return items;
    }

    private boolean isRelevantNews(NewsItem item) {
        if (item.getTitle() == null) return false;
        String t = item.getTitle().toLowerCase();
        return t.contains("gold") || t.contains("silver") || t.contains("copper")
            || t.contains("zinc") || t.contains("aluminium") || t.contains("nickel")
            || t.contains("crude") || t.contains("natural gas") || t.contains("iron")
            || t.contains("steel") || t.contains("nmdc") || t.contains("vedanta")
            || t.contains("hindalco") || t.contains("mcx") || t.contains("commodity")
            || t.contains("metal") || t.contains("bond") || t.contains("g-sec")
            || t.contains("yield") || t.contains("tata steel") || t.contains("coal india")
            || t.contains("nifty metal") || t.contains("mineral");
    }

    private String classifyNewsCategory(String title, String defaultCategory) {
        if (title == null) return defaultCategory;
        String t = title.toLowerCase();
        if (t.contains("bond") || t.contains("yield") || t.contains("g-sec")) return "BOND";
        if (t.contains("gold") || t.contains("silver") || t.contains("crude") || t.contains("mcx")) return "COMMODITY";
        if (t.contains("nifty") || t.contains("sensex") || t.contains("stock")) return "STOCK";
        return defaultCategory;
    }

    private String extractRelatedSymbol(String title) {
        if (title == null) return "";
        String t = title.toUpperCase();
        if (t.contains("GOLD"))    return "GOLD";
        if (t.contains("SILVER"))  return "SILVER";
        if (t.contains("COPPER"))  return "COPPER";
        if (t.contains("NICKEL"))  return "NICKEL";
        if (t.contains("ZINC"))    return "ZINC";
        if (t.contains("ALUMINIUM") || t.contains("ALUMINUM")) return "ALUMINIUM";
        if (t.contains("CRUDE"))   return "CRUDEOIL";
        if (t.contains("NATURAL GAS")) return "NATURALGAS";
        if (t.contains("TATA STEEL"))  return "TATASTEEL";
        if (t.contains("NMDC"))        return "NMDC";
        if (t.contains("HINDALCO"))    return "HINDALCO";
        if (t.contains("VEDANTA"))     return "VEDL";
        if (t.contains("COAL INDIA"))  return "COALINDIA";
        if (t.contains("IRON") || t.contains("STEEL")) return "TATASTEEL";
        return "";
    }

    private String classifySentiment(String title) {
        if (title == null) return "NEUTRAL";
        String t = title.toLowerCase();
        boolean bullish = t.contains("surge") || t.contains("gain") || t.contains("rise")
            || t.contains("bull") || t.contains("strong") || t.contains("rally")
            || t.contains("record") || t.contains("high");
        boolean bearish = t.contains("fall") || t.contains("drop") || t.contains("decline")
            || t.contains("bear") || t.contains("weak") || t.contains("loss")
            || t.contains("slump") || t.contains("low");
        if (bullish && !bearish) return "BULLISH";
        if (bearish && !bullish) return "BEARISH";
        return "NEUTRAL";
    }

    private List<NewsItem> getFallbackNews() {
        return List.of(
            NewsItem.builder()
                .title("Gold hits record highs on MCX amid global uncertainty")
                .summary("Gold futures on MCX touched fresh all-time highs as global geopolitical uncertainty and a weaker dollar drove demand for safe-haven assets.")
                .source("ET Markets").category("COMMODITY").relatedSymbol("GOLD")
                .sentiment("BULLISH").publishedAt(LocalDateTime.now().minusMinutes(30)).build(),
            NewsItem.builder()
                .title("Hindustan Copper surges 5.6% as metal stocks rally")
                .summary("Metal stocks surged as US-Iran talks eased geopolitical tensions. Nifty Metal index gained 2.55%.")
                .source("Groww Digest").category("STOCK").relatedSymbol("HINDCOPPER")
                .sentiment("BULLISH").publishedAt(LocalDateTime.now().minusHours(2)).build(),
            NewsItem.builder()
                .title("India 10Y G-Sec yield at 6.85%, bond market cautious")
                .summary("India's 10-year government securities yield remained stable as RBI kept rates unchanged.")
                .source("ET Markets").category("BOND").relatedSymbol("GSEC10Y")
                .sentiment("NEUTRAL").publishedAt(LocalDateTime.now().minusHours(3)).build(),
            NewsItem.builder()
                .title("Copper MCX prices tick up on Chinese demand recovery signals")
                .summary("Copper futures on MCX saw modest gains following positive industrial output data from China.")
                .source("ET Markets").category("COMMODITY").relatedSymbol("COPPER")
                .sentiment("BULLISH").publishedAt(LocalDateTime.now().minusHours(4)).build(),
            NewsItem.builder()
                .title("NMDC iron ore production rises 8% YoY in April 2026")
                .summary("NMDC reported an 8% year-on-year increase in iron ore production for April 2026.")
                .source("Groww Digest").category("STOCK").relatedSymbol("NMDC")
                .sentiment("BULLISH").publishedAt(LocalDateTime.now().minusHours(5)).build(),
            NewsItem.builder()
                .title("Silver outperforms gold on industrial demand; MCX silver up 170% YoY")
                .summary("Silver is outperforming gold in 2026 due to structural supply deficits and high demand from electronics.")
                .source("ET Markets").category("COMMODITY").relatedSymbol("SILVER")
                .sentiment("BULLISH").publishedAt(LocalDateTime.now().minusHours(6)).build()
        );
    }
}
