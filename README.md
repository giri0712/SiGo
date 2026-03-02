🪙 SiGo — Live Gold & Silver Market Tracker
🚀 Project Overview

SiGo is a real-time market tracking backend application built using Spring Boot that automatically scrapes live Gold and Silver prices, processes them, stores them in a database, and displays them through a dynamic web dashboard.

The system runs scheduled background jobs to continuously fetch updated market rates and provides a clean web interface for viewing the latest prices.

This project demonstrates real backend engineering concepts like:

Web scraping automation

Scheduled background processing

Data storage using NoSQL

REST API design

Dynamic server-side rendering

✨ Key Features
🔄 Automated Live Price Scraping

Fetches current:

Gold 24K price

Gold 22K price

Silver price

Extracts values directly from market websites using HTML parsing.

⏱ Scheduled Background Updates

Runs automatically every 30 minutes

Keeps prices updated without manual refresh

Uses Spring Scheduler for automation

🧠 Smart Data Processing

Cleans raw scraped values

Converts:

Price per gram → price per kilogram

Automatically calculates 22K price when unavailable.

💾 Database Storage

Stores latest market data in MongoDB

Enables persistent storage instead of temporary memory

Ready for future historical tracking features

🎨 Dynamic Web Dashboard

Backend renders a fully styled HTML page

Shows:

Metal cards

Price comparison table

Live ticker animation

Last updated timestamp

🏗️ Tech Stack
⚙️ Backend

Java 17+

Spring Boot

Spring Scheduler

REST Controller Architecture

🌐 Web Scraping

JSoup HTML Parser

💾 Database

MongoDB (NoSQL Document Database)

🎨 Frontend (Server-Rendered)

HTML5

CSS3

Responsive Layout

🧠 System Architecture
Scheduler
   ↓
Scraper Service (JSoup)
   ↓
Data Processing & Cleaning
   ↓
MongoDB Storage
   ↓
REST Controller
   ↓
Live HTML Dashboard
📂 Project Structure
com.sigo.api
│
├── controller
│   └── MarketController.java
│
├── service
│   └── SigoScraperService.java
│
├── repository
│   └── MarketDataRepository.java
│
└── model
    └── MarketData.java
⚙️ How the System Works
1️⃣ Application Startup

Automatically performs initial scraping using @PostConstruct.

2️⃣ Scheduled Job Execution

Every 30 minutes:

Connects to market rate pages

Parses HTML tables

Extracts price values

Cleans and formats data

Stores latest prices in MongoDB

3️⃣ Data Retrieval & Display

Endpoint:

GET /sigo/prices

Returns a dynamic HTML dashboard showing:

Current Gold 24K price

Current Gold 22K price

Current Silver price

Prices per gram & kilogram

Last update timestamp

🔧 Setup & Installation
Step 1 — Clone Repository
git clone https://github.com/your-username/sigo-market-tracker.git
cd sigo-market-tracker
Step 2 — Configure MongoDB

Ensure MongoDB is running locally:

mongodb://localhost:27017/sigo-db

Update application.properties if needed.

Step 3 — Run Application

Using Maven:

mvn spring-boot:run
Step 4 — Open Dashboard

Visit:

http://localhost:8080/sigo/prices
📊 Concepts Demonstrated

This project showcases practical backend development skills:

Real-time data scraping

Scheduled job automation

NoSQL database integration

RESTful API design

Data transformation logic

Server-side UI rendering

🔮 Future Improvements (Recommended)

To make this production-level, you should add:

Historical price tracking charts

REST JSON API endpoints

Redis caching layer

Retry & failure handling logic

Docker deployment

Price alert notifications

Microservice architecture

👨‍💻 Author

Girimurugan
Backend Developer — Java | Spring Boot | DSA
