<div align="center">
<h1>
  <span style="color:#c9a84c">Si</span>Go — Live Metal Market Rates
</h1>
<p>A Spring Boot backend that scrapes and serves live precious metal prices (Gold 24K, Gold 22K, Silver) with a sleek, auto-refreshing market dashboard.</p>
<p>
  <a href="https://github.com/your-username/sigo/issues">Report Bug</a> ·
  <a href="https://github.com/your-username/sigo/issues">Request Feature</a>
</p>
</div>

About The Project
SiGo is a lightweight market data API built with Java Spring Boot. It scrapes real-time precious metal prices and exposes them through a REST endpoint that renders a polished, dark-themed HTML dashboard — complete with a live ticker, price cards, and a summary table for Gold 24K, Gold 22K, and Silver rates (per gram & per kilogram).
Key highlights:

Live price scraping via SigoScraperService
Responsive, luxury-styled HTML dashboard served directly from the API
Prices displayed per gram and per kilogram with auto-detected currency symbol
Timestamped updates so you always know how fresh the data is
Clean N-Layer architecture: Controller → Service → Scraper


Built With

Show Image
Show Image
Show Image


Project Structure
src/
└── main/
    └── java/com/sigo/api/
        ├── controller/
        │   └── MarketController.java      # REST endpoint, HTML rendering
        ├── service/
        │   └── SigoScraperService.java    # Scraping & caching logic
        └── SigoApplication.java

Endpoints
MethodEndpointResponseDescriptionGET/sigo/pricestext/htmlLive market dashboard (Gold, Silver)
Sample Response
Visiting /sigo/prices in your browser renders a live dashboard showing:
MetalPer GramPer KilogramGold 24K₹X,XXX.XX₹X,XX,XXX.XXGold 22K₹X,XXX.XX₹X,XX,XXX.XXSilver₹XX.XX₹XX,XXX.XX

Prices are scraped in real time. The dashboard displays the timestamp of the last successful update.


Getting Started
Prerequisites

Java 17+
Maven 3.8+

Installation

Clone the repository

sh   git clone https://github.com/your-username/sigo.git

Navigate to the project directory

sh   cd sigo

Build the project

sh   mvn clean install

Run the application

sh   mvn spring-boot:run

Open your browser and visit

   http://localhost:8080/sigo/prices

Usage
Once running, the /sigo/prices endpoint serves a fully self-contained HTML page featuring:

Live Ticker — scrolling market status bar
Price Cards — per-metal cards showing gram & kilo rates
Summary Table — side-by-side comparison of all metals
Last Updated — timestamp of the most recent scrape

No frontend setup needed — it's all rendered server-side.

Contributing
Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are greatly appreciated.

Fork the Project
Create your Feature Branch

sh   git checkout -b feature/AmazingFeature

Commit your Changes

sh   git commit -m 'Add some AmazingFeature'

Push to the Branch

sh   git push origin feature/AmazingFeature

Open a Pull Request


License
Distributed under the MIT License. See LICENSE for more information.

Contact
Your Name — your.email@example.com
Project Link: https://github.com/your-username/sigo
