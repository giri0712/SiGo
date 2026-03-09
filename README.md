🪙 SiGo – Live Gold & Silver Market Tracker 🚀
Real-Time Precious Metal Scraper • Spring Boot • MongoDB • JSoup
const sigo = {
  name: "SiGo Market Tracker",
  type: "Backend Data Scraping System",
  purpose: "Track live Gold & Silver market prices",
  stack: ["Java", "Spring Boot", "MongoDB", "JSoup"],
  features: [
    "Live price scraping",
    "Automated scheduler",
    "REST APIs",
    "HTML dashboard"
  ],
  updateFrequency: "Every 30 minutes",
  status: "🟢 Running"
};
📊 Project Overview

SiGo is a backend service that scrapes live Gold and Silver prices from Groww and exposes them through REST APIs and a dynamic HTML dashboard.

Instead of manually checking market prices, SiGo automatically:

Scrapes latest prices

Stores them in MongoDB

Calculates price per gram & kilogram

Displays them in a clean UI

✨ Features

🟡 Live Gold Price Tracking

Gold 24K price

Gold 22K price

Price per gram & kilogram

⚪ Silver Market Tracking

Silver price scraping

Gram → Kilogram conversion

⏱ Automated Scheduler

Scrapes new data every 30 minutes

🌐 REST API

Access stored historical price data

📦 Database Storage

MongoDB for persistence

🎨 Minimal Market Dashboard

Displays live metal prices

Clean financial-style UI

🧠 How It Works
Groww Website
      │
      ▼
JSoup Web Scraper
      │
      ▼
Spring Boot Service
      │
      ▼
MongoDB Database
      │
      ▼
REST API + HTML Dashboard
🛠 Tech Stack
⚙ Backend

☕ Java

🌱 Spring Boot

🔎 Web Scraping

JSoup

🗄 Database

MongoDB

🧩 Architecture

REST API

MVC Pattern

Scheduled Jobs

📡 API Endpoints
Get Live Market Dashboard
GET /sigo/prices

Returns an HTML dashboard displaying:

Gold 24K price

Gold 22K price

Silver price

Price per gram

Price per kilogram

Open in browser:

http://localhost:5050/sigo/prices
Get Gold Price History
GET /api/prices/gold

Returns stored gold price entries.

Get Silver Price History
GET /api/prices/silver

Returns stored silver price entries.

🗂 Project Structure
sigo
 ┣ controller
 ┃ ┣ MarketController
 ┃ ┗ PriceController
 ┣ model
 ┃ ┣ GoldPrice
 ┃ ┗ SilverPrice
 ┣ repository
 ┃ ┣ GoldPriceRepository
 ┃ ┗ SilverPriceRepository
 ┣ service
 ┃ ┗ SigoScraperService
 ┣ ApiApplication
 ┗ application.properties
⏰ Scheduled Scraping

The system automatically updates prices every 30 minutes.

@Scheduled(fixedRate = 1800000)

This ensures the dashboard always shows fresh market data.

🧾 Example Database Record
Gold Price
{
 "metal": "Gold 24K",
 "pricePerGm": 7250,
 "pricePerKg": 7250000,
 "timestamp": "2026-03-09T10:22:14"
}
⚙ Installation Guide
1️⃣ Clone Repository
git clone https://github.com/YOUR_USERNAME/sigo.git
2️⃣ Navigate to Project
cd sigo
3️⃣ Start MongoDB

Make sure MongoDB is running locally.

mongodb://localhost:27017/sigo_db
4️⃣ Run Spring Boot
mvn spring-boot:run
5️⃣ Open Dashboard
http://localhost:5050/sigo/prices
🚀 Future Improvements

Possible upgrades:

📈 Price trend graphs

📊 Historical analytics

☁ Cloud deployment

🐳 Docker support

🔔 Price alerts

📱 React frontend dashboard

👨‍💻 Author

Girimurugan

CSE (AI & ML) Student
Backend Developer | Java | Spring Boot | APIs

Interested in building data systems, automation tools, and scalable backend services.
