# SiGo — Precious Metals Market Terminal

> A real-time precious metals market data API built with Spring Boot, serving live MCX commodity prices with WebSocket support and scheduled price refresh.

---

## What is SiGo?

SiGo is a backend market terminal that tracks live MCX (Multi Commodity Exchange) precious metals pricing — Gold, Gold Mini, Silver, and more. It scrapes live data, caches it in memory, and exposes it via a clean REST API and WebSocket feed. A lightweight static frontend is included for live visualization.I have vibe coded the front-end part with Claude code.

---

## Features

- Live MCX precious metals prices (Gold, Gold Mini, Silver, etc.)
- Scheduled background price refresh via Spring Scheduler
- WebSocket support for real-time price streaming to frontend clients
- REST API with structured JSON responses including trend, high/low, change, and open price
- Lightweight static HTML frontend served from Spring Boot
- News scraper for commodity-related market news
- Deployed and accessible via Render

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Real-time | WebSocket (Spring WebSocket) |
| Scheduling | Spring `@Scheduled` |
| Data Source | MCX via Groww commodity futures |
| Frontend | Vanilla HTML / CSS / JS |
| Deployment | Render |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/sigo/terminal/
│   │   ├── TerminalApplication.java
│   │   ├── config/
│   │   │   └── WebSocketConfig.java
│   │   ├── controller/
│   │   │   ├── MarketDataController.java
│   │   │   └── RootController.java
│   │   ├── model/
│   │   │   ├── CommodityPrice.java
│   │   │   ├── MarketSnapshot.java
│   │   │   └── NewsItem.java
│   │   ├── scheduler/
│   │   │   └── PriceRefreshScheduler.java
│   │   └── service/
│   │       ├── GrowScraperService.java
│   │       ├── MarketDataService.java
│   │       └── NewsScraperService.java
│   └── resources/
│       ├── application.properties
│       └── static/
│           └── index.html
```

---

## API Response Sample

```json
{
  "preciousMetals": [
    {
      "symbol": "GOLD",
      "name": "Gold MCX",
      "category": "PRECIOUS_METAL",
      "exchange": "MCX",
      "price": 152589,
      "change": 328,
      "changePct": 0.21,
      "high": 153440,
      "low": 152199,
      "unit": "per 10g",
      "openPrice": 152672,
      "trend": "UP",
      "trendArrow": "▲",
      "lastUpdated": "2026-05-10T00:34:44.538"
    }
  ]
}
```

---

## REST Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/metals` | Returns all live precious metal prices |
| GET | `/api/snapshot` | Returns full market snapshot |
| GET | `/api/news` | Returns latest commodity news |
| WS | `/ws` | WebSocket endpoint for live price feed |

---

## Running Locally

### Prerequisites
- Java 21+
- Maven 3.9+

### Steps

```bash
# Clone the repository
git clone https://github.com/giri0712/SiGo.git
cd SiGo

# Configure your API key in application.properties
# metals.api.key=YOUR_KEY_HERE

# Build and run
./mvnw spring-boot:run
```

App runs on `http://localhost:8081`

### With Docker

```bash
# Build image
docker build -t sigo-api .

# Run container
docker run -p 8081:8081 sigo-api
```

---

## Environment Variables

| Variable | Description |
|---|---|
| `metals.api.key` | API key for metals data source |
| `metals.api.base.url` | Base URL for metals API |
| `SPRING_PROFILES_ACTIVE` | Spring profile (dev/prod) |

---

## Roadmap

- [ ] Add Redis caching with 5-minute TTL
- [ ] Migrate to stable metals REST API (metals-api.com)
- [ ] Add PostgreSQL for historical price storage
- [ ] Add proper DTOs and response validation
- [ ] Write JUnit + Mockito unit tests
- [ ] Full Docker + docker-compose setup

---

## Author

**Giri** — BTech CSE (AI/ML), VIT-AP University  
GitHub: [@giri0712](https://github.com/giri0712)
Website is live at:https://sigo-dwk4.onrender.com/
