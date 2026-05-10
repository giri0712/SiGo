package com.sigo.terminal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> getApiInfo() {
        Map<String, String> info = Map.of(
            "message", "SiGo India Commodity Terminal API",
            "version", "1.0.0",
            "endpoints", "/api/v1/snapshot, /api/v1/commodities, /api/v1/metals/{symbol}, /api/v1/commodities/precious, /api/v1/commodities/base, /api/v1/commodities/energy, /api/v1/stocks/metals, /api/v1/news",
            "websocket", "/ws for real-time updates"
        );
        return ResponseEntity.ok(info);
    }
}