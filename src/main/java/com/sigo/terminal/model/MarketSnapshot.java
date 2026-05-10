package com.sigo.terminal.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MarketSnapshot {

    private List<CommodityPrice> preciousMetals;
    private List<CommodityPrice> baseMetals;
    private List<CommodityPrice> energyCommodities;
    private List<CommodityPrice> metalStocks;
    private List<CommodityPrice> indices;
    private List<CommodityPrice> bonds;
    private List<NewsItem> news;
    private List<String> tickerItems;
    private LocalDateTime fetchedAt;
    private String marketStatus;
    private boolean isLive;
    private Map<String, Object> summaryStats;

    public MarketSnapshot() {}

    private MarketSnapshot(Builder b) {
        this.preciousMetals    = b.preciousMetals;
        this.baseMetals        = b.baseMetals;
        this.energyCommodities = b.energyCommodities;
        this.metalStocks       = b.metalStocks;
        this.indices           = b.indices;
        this.bonds             = b.bonds;
        this.news              = b.news;
        this.tickerItems       = b.tickerItems;
        this.fetchedAt         = b.fetchedAt;
        this.marketStatus      = b.marketStatus;
        this.isLive            = b.isLive;
        this.summaryStats      = b.summaryStats;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<CommodityPrice> preciousMetals, baseMetals, energyCommodities, metalStocks, indices, bonds;
        private List<NewsItem> news;
        private List<String> tickerItems;
        private LocalDateTime fetchedAt;
        private String marketStatus;
        private boolean isLive;
        private Map<String, Object> summaryStats;

        public Builder preciousMetals(List<CommodityPrice> v)    { this.preciousMetals = v; return this; }
        public Builder baseMetals(List<CommodityPrice> v)        { this.baseMetals = v; return this; }
        public Builder energyCommodities(List<CommodityPrice> v) { this.energyCommodities = v; return this; }
        public Builder metalStocks(List<CommodityPrice> v)       { this.metalStocks = v; return this; }
        public Builder indices(List<CommodityPrice> v)           { this.indices = v; return this; }
        public Builder bonds(List<CommodityPrice> v)             { this.bonds = v; return this; }
        public Builder news(List<NewsItem> v)                    { this.news = v; return this; }
        public Builder tickerItems(List<String> v)               { this.tickerItems = v; return this; }
        public Builder fetchedAt(LocalDateTime v)                { this.fetchedAt = v; return this; }
        public Builder marketStatus(String v)                    { this.marketStatus = v; return this; }
        public Builder isLive(boolean v)                         { this.isLive = v; return this; }
        public Builder summaryStats(Map<String, Object> v)       { this.summaryStats = v; return this; }
        public MarketSnapshot build()                            { return new MarketSnapshot(this); }
    }

    // Getters
    public List<CommodityPrice> getPreciousMetals()    { return preciousMetals; }
    public List<CommodityPrice> getBaseMetals()        { return baseMetals; }
    public List<CommodityPrice> getEnergyCommodities() { return energyCommodities; }
    public List<CommodityPrice> getMetalStocks()       { return metalStocks; }
    public List<CommodityPrice> getIndices()           { return indices; }
    public List<CommodityPrice> getBonds()             { return bonds; }
    public List<NewsItem> getNews()                    { return news; }
    public List<String> getTickerItems()               { return tickerItems; }
    public LocalDateTime getFetchedAt()                { return fetchedAt; }
    public String getMarketStatus()                    { return marketStatus; }
    public boolean isLive()                            { return isLive; }
    public Map<String, Object> getSummaryStats()       { return summaryStats; }

    // Setters
    public void setPreciousMetals(List<CommodityPrice> v)    { this.preciousMetals = v; }
    public void setBaseMetals(List<CommodityPrice> v)        { this.baseMetals = v; }
    public void setEnergyCommodities(List<CommodityPrice> v) { this.energyCommodities = v; }
    public void setMetalStocks(List<CommodityPrice> v)       { this.metalStocks = v; }
    public void setIndices(List<CommodityPrice> v)           { this.indices = v; }
    public void setBonds(List<CommodityPrice> v)             { this.bonds = v; }
    public void setNews(List<NewsItem> v)                    { this.news = v; }
    public void setTickerItems(List<String> v)               { this.tickerItems = v; }
    public void setFetchedAt(LocalDateTime v)                { this.fetchedAt = v; }
    public void setMarketStatus(String v)                    { this.marketStatus = v; }
    public void setLive(boolean v)                           { this.isLive = v; }
    public void setSummaryStats(Map<String, Object> v)       { this.summaryStats = v; }
}
