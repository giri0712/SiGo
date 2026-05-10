package com.sigo.terminal.model;

import java.time.LocalDateTime;

public class NewsItem {

    private String title;
    private String summary;
    private String source;
    private String url;
    private String relatedSymbol;
    private String category;
    private String sentiment;
    private LocalDateTime publishedAt;

    public NewsItem() {}

    private NewsItem(Builder b) {
        this.title         = b.title;
        this.summary       = b.summary;
        this.source        = b.source;
        this.url           = b.url;
        this.relatedSymbol = b.relatedSymbol;
        this.category      = b.category;
        this.sentiment     = b.sentiment;
        this.publishedAt   = b.publishedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title, summary, source, url, relatedSymbol, category, sentiment;
        private LocalDateTime publishedAt;

        public Builder title(String v)         { this.title = v; return this; }
        public Builder summary(String v)       { this.summary = v; return this; }
        public Builder source(String v)        { this.source = v; return this; }
        public Builder url(String v)           { this.url = v; return this; }
        public Builder relatedSymbol(String v) { this.relatedSymbol = v; return this; }
        public Builder category(String v)      { this.category = v; return this; }
        public Builder sentiment(String v)     { this.sentiment = v; return this; }
        public Builder publishedAt(LocalDateTime v) { this.publishedAt = v; return this; }
        public NewsItem build()                { return new NewsItem(this); }
    }

    // Getters
    public String getTitle()           { return title; }
    public String getSummary()         { return summary; }
    public String getSource()          { return source; }
    public String getUrl()             { return url; }
    public String getRelatedSymbol()   { return relatedSymbol; }
    public String getCategory()        { return category; }
    public String getSentiment()       { return sentiment; }
    public LocalDateTime getPublishedAt() { return publishedAt; }

    // Setters
    public void setTitle(String v)           { this.title = v; }
    public void setSummary(String v)         { this.summary = v; }
    public void setSource(String v)          { this.source = v; }
    public void setUrl(String v)             { this.url = v; }
    public void setRelatedSymbol(String v)   { this.relatedSymbol = v; }
    public void setCategory(String v)        { this.category = v; }
    public void setSentiment(String v)       { this.sentiment = v; }
    public void setPublishedAt(LocalDateTime v) { this.publishedAt = v; }
}
