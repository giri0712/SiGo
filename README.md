# 🪙 SiGo – Live Gold & Silver Market Tracker

### Real-Time Precious Metal Price Scraper • Spring Boot • MongoDB • JSoup

---

## 🚀 Project Identity

```javascript
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

SiGo is a backend service that scrapes live Gold and Silver prices from the Groww website and exposes them through REST APIs and a dynamic HTML dashboard.

Instead of manually checking market prices, the system automatically:

Scrapes latest prices

Calculates price per gram & kilogram

Stores the data in MongoDB

Displays it in a simple web dashboard

✨ Features
🟡 Gold Price Tracking

Gold 24K price

Gold 22K price

Gram to kilogram conversion

⚪ Silver Price Tracking

Live silver price scraping

Price per gram and kilogram calculation

⏱ Automated Scheduler

Data scraping runs every 30 minutes

🌐 REST APIs

Retrieve stored gold price history

Retrieve stored silver price history

🗄 MongoDB Storage

Stores historical price records

🎨 Minimal HTML Dashboard

Displays current market prices

Shows last updated timestamp
🧠 System Architecture

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
Backend

Java

Spring Boot

Web Scraping

JSoup

Database

MongoDB

Architecture

REST APIs

MVC Pattern

Scheduled Jobs

📡 API Endpoints
Live Market Dashboard

GET /sigo/prices


Returns an HTML dashboard displaying:

Gold 24K price

Gold 22K price

Silver price

Price per gram

Price per kilogram

Access in browser:


http://localhost:5050/sigo/prices

Gold Price History

GET /api/prices/gold


Returns all stored gold price records.

Silver Price History

GET /api/prices/silver


Returns all stored silver price records.

🗂 Project Structure

sigo
│
├── controller
│   ├── MarketController.java
│   └── PriceController.java
│
├── model
│   ├── GoldPrice.java
│   └── SilverPrice.java
│
├── repository
│   ├── GoldPriceRepository.java
│   └── SilverPriceRepository.java
│
├── service
│   └── SigoScraperService.java
│
├── ApiApplication.java
│
└── application.properties

⏰ Scheduled Scraping

The scraper automatically runs every 30 minutes.

@Scheduled(fixedRate = 1800000)

This ensures the system keeps updating:

Gold 24K price

Gold 22K price

Silver price

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
4️⃣ Run Spring Boot Application
mvn spring-boot:run
5️⃣ Open Dashboard
http://localhost:5050/sigo/prices
🚀 Future Improvements

Possible upgrades for this project:

📈 Price trend charts

📊 Historical analytics

🐳 Docker deployment

☁ Cloud deployment

🔔 Price alert notifications

⚡ React frontend dashboard

👨‍💻 Author

Girimurugan

Computer Science Engineering Student
Backend Developer focused on Java, Spring Boot, APIs, and Data Systems
