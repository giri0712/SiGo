
<div align="center">

```
███████╗██╗ ██████╗  ██████╗
██╔════╝██║██╔════╝ ██╔═══██╗
███████╗██║██║  ███╗██║   ██║
╚════██║██║██║   ██║██║   ██║
███████║██║╚██████╔╝╚██████╔╝
╚══════╝╚═╝ ╚═════╝  ╚═════╝
```

### `v1.0` — **INDIA MARKET TERMINAL**

*A high-performance market data engine for Indian commodities, indices & bonds*

[![Live Demo](https://img.shields.io/badge/🟢_LIVE-sigo--dwk4.onrender.com-00ff88?style=for-the-badge&labelColor=0d1117)](https://sigo-dwk4.onrender.com)
[![Java](https://img.shields.io/badge/Java_21-LTS-f89820?style=for-the-badge&logo=openjdk&logoColor=white&labelColor=0d1117)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6db33f?style=for-the-badge&logo=springboot&logoColor=white&labelColor=0d1117)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ed?style=for-the-badge&logo=docker&logoColor=white&labelColor=0d1117)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Deployed_on-Render-46e3b7?style=for-the-badge&logo=render&logoColor=white&labelColor=0d1117)](https://render.com)

</div>

---

## `>_ OVERVIEW`

**SiGo** *(Signal Go)* is a full-stack financial terminal that aggregates live market data for Indian exchanges — MCX, NSE, BSE, and ET Markets. It scrapes and structures complex data from multiple sources and pushes real-time updates via WebSocket to a responsive multi-category frontend.

```
NSEX ₹79473.22 (+0.44%)  •  GOLD ₹152589.00 (+0.22%)  •  SILVER ₹261999.00 (+1.34%)
```

> Built by a CS (AI/ML) student at VIT-AP — because Bloomberg Terminal costs $24,000/year.

---

## `>_ LIVE DATA COVERAGE`

| Category | Instruments |
|---|---|
| 💎 **Precious Metals** | Gold · GoldMini · GoldGuinea · Silver · SilverMini |
| 🔩 **Base Metals** | Copper · Zinc · Aluminium · Nickel |
| ⚡ **Energy** | Crude Oil · Natural Gas · Electricity |
| 📈 **Indices** | NIFTY 50 · NIFTY Metal · BSE Sensex · Bank Nifty |
| 📰 **Market News** | Live commodity news feed + scrolling global ticker |
| 🏦 **Bonds** | GSEC 10Y yield and real-time bond pricing |
| 🏭 **Equity Proxies** | NMDC · SAIL · Tata Steel · JSW Steel |

---

## `>_ KEY FEATURES`

```
┌─────────────────────────────────────────────────────────────────┐
│  ⚡  REAL-TIME STREAMING   Spring WebSocket (STOMP/SockJS)       │
│  🕸  MULTI-SOURCE DATA     Scalable scraper + API integration    │
│  🔄  AUTO SCHEDULING       @Scheduled refresh & data integrity   │
│  🐳  DOCKERIZED            Consistent deployment anywhere        │
│  🧱  CLEAN ARCHITECTURE    DTOs · Services · Controllers         │
│  🛡  ERROR RESILIENT       Graceful fallbacks on scrape failure  │
└─────────────────────────────────────────────────────────────────┘
```

---

## `>_ TECH STACK`

<div align="center">

| Layer | Technology | Purpose |
|---|---|---|
| **Language** | `Java 21 (LTS)` | Core runtime |
| **Framework** | `Spring Boot 3.x` | Application backbone |
| **Real-Time** | `Spring WebSocket` | STOMP/SockJS live feed |
| **Parsing** | `Jsoup` + `Jackson` | HTML scraping + JSON |
| **Infrastructure** | `Docker` + `Maven` | Build & containerization |
| **Deployment** | `Render (PaaS)` | Cloud hosting |
| **Frontend** | `Vanilla JS (ES6+)` | CSS Grid/Flexbox UI |

</div>

---

## `>_ PROJECT ARCHITECTURE`

```
sigo-terminal/
│
└── src/main/java/com/sigo/terminal/
    │
    ├── config/             ── WebSocket & Security configurations
    │
    ├── controller/         ── REST Endpoints
    │   ├── MarketController    (snapshot, metals, indices)
    │   ├── NewsController      (live news feed)
    │   └── RootController      (health / root)
    │
    ├── model/              ── Domain Entities
    │   ├── Commodity           (name, price, change, unit)
    │   ├── Snapshot            (full market state)
    │   └── NewsItem            (headline, source, timestamp)
    │
    ├── scheduler/          ── Background Workers
    │   └── PriceRefreshTask    (@Scheduled price sync)
    │
    └── service/            ── Core Business Logic
        ├── MetalService        (MCX Gold, Silver, etc.)
        ├── IndexService        (NSE/BSE indices)
        ├── EnergyService       (Crude, Gas, Electricity)
        └── NewsService         (ET Markets news scraper)
```

---

## `>_ API REFERENCE`

### `GET /api/snapshot`

Returns the complete market snapshot as a structured JSON object.

```json
{
  "preciousMetals": [ { "name": "GOLD", "price": 152589.00, "change": "+328.00", "unit": "per 10g" } ],
  "baseMetals":    [ { "name": "COPPER", "price": 1325.40, "change": "+21.15", "unit": "per kg" } ],
  "indices":       [ { "name": "NIFTY50", "price": 24087, "change": "+119.73" } ],
  "energy":        [ { "name": "CRUDEOIL", "price": 9022.00, "change": "-43.00", "unit": "per barrel" } ],
  "bonds":         [ { "name": "GSEC10Y", "yield": "6.85%" } ],
  "tickerItems":   [ "..." ]
}
```

### `WS /ws` — Live Feed

Subscribe via STOMP/SockJS to receive sub-second price updates as they move on the exchange.

```javascript
const socket = new SockJS('/ws');
const client = Stomp.over(socket);
client.connect({}, () => {
  client.subscribe('/topic/prices', (msg) => {
    const prices = JSON.parse(msg.body);
    // update UI
  });
});
```

---

## `>_ DEPLOYMENT`

### 🐳 Docker (Recommended)

```bash
# Build the image
docker build -t sigo-terminal .

# Run the container
docker run -p 8081:8081 sigo-terminal

# Access at
open http://localhost:8081
```

### 💻 Local Development

```bash
# 1. Clone the repository
git clone https://github.com/giri0712/Sigo.git
cd Sigo

# 2. Ensure Java 21 is installed
java -version   # openjdk 21 required

# 3. Run with Maven wrapper
./mvnw spring-boot:run

# 4. Open in browser
open http://localhost:8081
```

> **Note:** The app scrapes live exchange data. An active internet connection is required.

---

## `>_ ROADMAP`

```
[✅] DONE      Multi-source aggregation (MCX · NSE · BSE · ET Markets)
[✅] DONE      Real-time WebSocket price streaming
[✅] DONE      Docker deployment on Render
[✅] DONE      Multi-category terminal UI (Metals · Energy · Bonds · Stocks)

[🔄] NEXT      Redis TTL cache (5-min) to optimize scraper calls
[🔄] NEXT      PostgreSQL for historical price storage & charting
[🔄] NEXT      RSI + Moving Average indicators for trend analysis
[🔄] NEXT      JUnit 5 + Mockito unit test coverage
[🔄] FUTURE    Alert system (price threshold push notifications)
[🔄] FUTURE    Mobile-responsive layout improvements
```

---

## `>_ AUTHOR`

<div align="center">

```
┌──────────────────────────────────────────┐
│  👨‍💻  Girimurugan S                        │
│  🎓  B.Tech — Computer Science (AI/ML)   │
│  🏫  VIT-AP University                   │
│  🔗  github.com/giri0712 │
└──────────────────────────────────────────┘
```

*"Markets never sleep. Neither does the terminal."*

</div>

---

<div align="center">

**[ `MARKET DATA` · `WEBSOCKET` · `SPRING BOOT` · `JAVA 21` · `DOCKER` ]**

Made with ☕ in India &nbsp;|&nbsp; MIT License

</div>
Website is live at:https://sigo-dwk4.onrender.com/
