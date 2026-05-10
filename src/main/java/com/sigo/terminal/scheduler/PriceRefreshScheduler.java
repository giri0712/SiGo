package com.sigo.terminal.scheduler;

import com.sigo.terminal.model.MarketSnapshot;
import com.sigo.terminal.service.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PriceRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceRefreshScheduler.class);
    private static final long REFRESH_INTERVAL_MS = 60_000;

    private final MarketDataService marketDataService;
    private final SimpMessagingTemplate messagingTemplate;

    public PriceRefreshScheduler(MarketDataService marketDataService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.marketDataService  = marketDataService;
        this.messagingTemplate  = messagingTemplate;
    }

    @Scheduled(fixedDelay = REFRESH_INTERVAL_MS, initialDelay = 5000)
    @SuppressWarnings("null")
    public void refreshAndBroadcast() {
        log.info("Scheduled price refresh triggered...");
        try {
            MarketSnapshot snapshot = marketDataService.refreshSnapshot();

            messagingTemplate.convertAndSend("/topic/prices", snapshot);
            messagingTemplate.convertAndSend("/topic/ticker", snapshot.getTickerItems());

            if (snapshot.getNews() != null && !snapshot.getNews().isEmpty()) {
                messagingTemplate.convertAndSend("/topic/news", snapshot.getNews());
            }

            log.info("Broadcast complete. Assets: {}, News: {}",
                countAssets(snapshot),
                snapshot.getNews() != null ? snapshot.getNews().size() : 0);

        } catch (Exception e) {
            log.error("Refresh failed: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    @SuppressWarnings("null")
    public void heartbeat() {
        try {
            messagingTemplate.convertAndSend("/topic/heartbeat",
                Map.of("ts", System.currentTimeMillis(), "status", "OK"));
        } catch (Exception e) {
            log.debug("Heartbeat failed: {}", e.getMessage());
        }
    }

    private int countAssets(MarketSnapshot s) {
        int count = 0;
        if (s.getPreciousMetals()    != null) count += s.getPreciousMetals().size();
        if (s.getBaseMetals()        != null) count += s.getBaseMetals().size();
        if (s.getEnergyCommodities() != null) count += s.getEnergyCommodities().size();
        if (s.getMetalStocks()       != null) count += s.getMetalStocks().size();
        return count;
    }
}
