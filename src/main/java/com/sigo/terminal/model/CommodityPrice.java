package com.sigo.terminal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommodityPrice {

    private String symbol;
    private String name;
    private String category;
    private String exchange;
    private double price;
    private double change;
    private double changePct;
    private double high;
    private double low;
    private String unit;
    private double openPrice;
    private double prevClose;
    private String volume;
    private String marketCap;
    private String trend;
    private String growwUrl;
    private LocalDateTime lastUpdated;

    public CommodityPrice() {}

    private CommodityPrice(Builder b) {
        this.symbol      = b.symbol;
        this.name        = b.name;
        this.category    = b.category;
        this.exchange    = b.exchange;
        this.price       = b.price;
        this.change      = b.change;
        this.changePct   = b.changePct;
        this.high        = b.high;
        this.low         = b.low;
        this.unit        = b.unit;
        this.openPrice   = b.openPrice;
        this.prevClose   = b.prevClose;
        this.volume      = b.volume;
        this.marketCap   = b.marketCap;
        this.trend       = b.trend;
        this.growwUrl    = b.growwUrl;
        this.lastUpdated = b.lastUpdated;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String symbol, name, category, exchange, unit, volume, marketCap, trend, growwUrl;
        private double price, change, changePct, high, low, openPrice, prevClose;
        private LocalDateTime lastUpdated;

        public Builder symbol(String v)      { this.symbol = v; return this; }
        public Builder name(String v)        { this.name = v; return this; }
        public Builder category(String v)    { this.category = v; return this; }
        public Builder exchange(String v)    { this.exchange = v; return this; }
        public Builder price(double v)       { this.price = v; return this; }
        public Builder change(double v)      { this.change = v; return this; }
        public Builder changePct(double v)   { this.changePct = v; return this; }
        public Builder high(double v)        { this.high = v; return this; }
        public Builder low(double v)         { this.low = v; return this; }
        public Builder unit(String v)        { this.unit = v; return this; }
        public Builder openPrice(double v)   { this.openPrice = v; return this; }
        public Builder prevClose(double v)   { this.prevClose = v; return this; }
        public Builder volume(String v)      { this.volume = v; return this; }
        public Builder marketCap(String v)   { this.marketCap = v; return this; }
        public Builder trend(String v)       { this.trend = v; return this; }
        public Builder growwUrl(String v)    { this.growwUrl = v; return this; }
        public Builder lastUpdated(LocalDateTime v) { this.lastUpdated = v; return this; }
        public CommodityPrice build()        { return new CommodityPrice(this); }
    }

    // Getters
    public String getSymbol()          { return symbol; }
    public String getName()            { return name; }
    public String getCategory()        { return category; }
    public String getExchange()        { return exchange; }
    public double getPrice()           { return price; }
    public double getChange()          { return change; }
    public double getChangePct()       { return changePct; }
    public double getHigh()            { return high; }
    public double getLow()             { return low; }
    public String getUnit()            { return unit; }
    public double getOpenPrice()       { return openPrice; }
    public double getPrevClose()       { return prevClose; }
    public String getVolume()          { return volume; }
    public String getMarketCap()       { return marketCap; }
    public String getTrend()           { return trend; }
    public String getGrowwUrl()        { return growwUrl; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    // Setters
    public void setSymbol(String v)       { this.symbol = v; }
    public void setName(String v)         { this.name = v; }
    public void setCategory(String v)     { this.category = v; }
    public void setExchange(String v)     { this.exchange = v; }
    public void setPrice(double v)        { this.price = v; }
    public void setChange(double v)       { this.change = v; }
    public void setChangePct(double v)    { this.changePct = v; }
    public void setHigh(double v)         { this.high = v; }
    public void setLow(double v)          { this.low = v; }
    public void setUnit(String v)         { this.unit = v; }
    public void setOpenPrice(double v)    { this.openPrice = v; }
    public void setPrevClose(double v)    { this.prevClose = v; }
    public void setVolume(String v)       { this.volume = v; }
    public void setMarketCap(String v)    { this.marketCap = v; }
    public void setTrend(String v)        { this.trend = v; }
    public void setGrowwUrl(String v)     { this.growwUrl = v; }
    public void setLastUpdated(LocalDateTime v) { this.lastUpdated = v; }

    public String getTrendArrow() {
        if ("UP".equals(trend))   return "▲";
        if ("DOWN".equals(trend)) return "▼";
        return "▬";
    }
}
