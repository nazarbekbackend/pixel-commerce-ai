📱 Pixel Commerce AI

AI-powered backend system for smartphone inventory management, sales analytics, purchase planning and profit optimization.

The project is designed for small smartphone commerce businesses that need to understand sales performance, manage inventory and decide what products should be purchased next.

🚀 Main Features

Product management

Supplier management

Purchase management

Sales management

Inventory tracking

FIFO inventory cost calculation

Revenue and profit analytics

Sales velocity and stock forecasting

Automatic restock recommendations

Budget-based purchase optimization

AI-powered purchase advisor

🤖 AI Purchase Advisor

The system combines deterministic business logic with a local Large Language Model.

Business calculations are performed by the Java backend. The AI layer receives the calculated business data and explains the recommended purchasing strategy in natural language. This prevents the LLM from modifying financial calculations.

Example recommendation:

Google Pixel 8 256GB
Quantity: 3
Required budget: 85,140 KGS
Remaining budget: 14,860 KGS
Expected revenue: 135,000 KGS
Expected profit: 49,860 KGS

🧠 Architecture

REST API
   ↓
Spring Boot
   ↓
Products / Purchases / Sales
   ↓
Inventory / FIFO
   ↓
Analytics Engine
   ↓
Budget Optimizer
   ↓
Spring AI
   ↓
Ollama
   ↓
Local LLM

📊 Analytics

The analytics engine calculates:

Units sold

Current stock

Revenue

Profit

Sales per day

Estimated days of stock

Restock recommendations

Possible recommendations:

URGENT_ORDER
MONITOR
WAIT

💰 Budget Optimizer

The backend can optimize a purchasing plan based on a predefined budget.

Example:

Available budget: 100,000 KGS
Recommended product: Google Pixel 8 256GB
Quantity: 3
Unit cost: 28,380 KGS
Total cost: 85,140 KGS
Expected revenue: 135,000 KGS
Expected profit: 49,860 KGS
Remaining budget: 14,860 KGS

The optimizer makes the financial calculations before the AI receives the data.

🌐 REST API

Products

Method

Endpoint

Description

GET

/api/products

Get all products

GET

/api/products/{id}

Get product by ID

POST

/api/products

Create product

PUT

/api/products/{id}

Update product

DELETE

/api/products/{id}

Delete product

Purchases

Method

Endpoint

Description

POST

/api/purchases

Register purchase

GET

/api/purchases

Get purchases

Sales

Method

Endpoint

Description

POST

/api/sales

Register sale

GET

/api/sales

Get sales

AI Purchase Advisor

POST /api/ai/purchase-advice

Example request:

{
  "exchangeRate": 12.9,
  "budget": 100000
}

Example response:

{
  "budget": 100000,
  "usedBudget": 85140,
  "remainingBudget": 14860,
  "expectedRevenue": 135000,
  "expectedProfit": 49860,
  "advice": "..."
}

🛠 Tech Stack

Java 21

Spring Boot

Spring MVC

Spring Data JPA

Hibernate

Spring Validation

Spring AI

PostgreSQL

Maven

Lombok

Ollama

Qwen 3

Architecture patterns include REST API, layered architecture, DTOs, service/repository layers, mappers, global exception handling and FIFO inventory cost calculation.

🤖 Local AI with Ollama

The project uses Ollama for local LLM inference. No OpenAI API key is required.

Example configuration:

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen3:4b

Download a model:

ollama pull qwen3:4b

Verify installed models:

ollama list

You can use another Ollama model depending on your hardware.

⚙️ Configuration

Configure the database in src/main/resources/application.properties.

Example:

spring.datasource.url=jdbc:postgresql://localhost:5433/pixel_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8082

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen3:4b

Do not commit real passwords, API keys or other secrets to GitHub.

▶️ Running the Project

Requirements

Java 21

PostgreSQL

Maven

Ollama

Start the application

If Ollama is installed as a system service, it may already be running automatically. Otherwise:

ollama serve

Then start Spring Boot:

./mvnw spring-boot:run

The application runs on:

http://localhost:8082

🗂 Project Structure

src/main/java/kg/dev/pixel_commerce_ai
│
├── controller
├── dto
├── entity
├── exception
├── mapper
├── repository
└── service

🔄 Business Flow

Purchase
   ↓
Stock Batch
   ↓
Inventory
   ↓
Sale
   ↓
FIFO Cost Calculation
   ↓
Analytics
   ↓
Sales Velocity
   ↓
Restock Recommendation
   ↓
Budget Optimizer
   ↓
AI Purchase Advisor

📌 Project Status

Completed

Product management

Supplier management

Purchase management

Sales management

Inventory management

FIFO stock batches

Revenue and profit calculation

Product analytics

Sales velocity

Stock forecasting

Restock recommendations

Budget optimization

AI purchase advisor

Local Ollama integration

Planned

Telegram bot

AI business chat

Advanced demand forecasting

Product comparison

Swagger / OpenAPI documentation

Automated unit and integration tests

Docker / Docker Compose

Frontend dashboard

Authentication and authorization

Deployment

🎯 Goal

Pixel Commerce AI is being developed as a practical backend system for smartphone commerce.

The long-term goal is to turn the backend into a complete business assistant that can help a store owner track inventory, understand profitability, identify best-selling products, predict stock shortages, optimize purchasing decisions and receive AI-powered business recommendations through Telegram.

👨‍💻 Author

Nazarbek — Java Backend Developer

GitHub: https://github.com/nazarbekbackend# 📱 Pixel Commerce AI

AI-powered backend system for smartphone inventory, sales analytics,
purchase planning, and profit optimization.

The application analyzes sales, stock levels, product profitability and
available purchasing budget to generate data-driven purchase recommendations.

## 🚀 Features

- Product management
- Supplier management
- Purchase management
- Sales management
- Inventory tracking
- FIFO inventory cost calculation
- Revenue and profit analytics
- Product sales analytics
- Sales velocity calculation
- Stock availability forecasting
- Automatic restock recommendations
- Budget-based purchase optimization
- Expected revenue calculation
- Expected profit calculation
- AI-powered purchase advisor

## 🤖 AI Purchase Advisor

The system combines deterministic business analytics with an LLM.

Business calculations are performed by the backend, while the AI layer
explains the calculated purchase strategy in natural language.

Example:

```json
{
  "exchangeRate": 12.9,
  "budget": 100000
}
The system can recommend how many smartphones to purchase based on:

available budget
historical sales
current stock
product cost
expected selling price
expected profit
🧠 Architecture
REST API
   |
   v
Spring Boot
   |
   +---- Sales
   +---- Purchases
   +---- Inventory
   +---- FIFO Stock Batches
   |
   v
Analytics Engine
   |
   v
Budget Optimizer
   |
   v
Spring AI
   |
   v
LLM
🛠 Tech Stack
Java 21
Spring Boot
Spring MVC
Spring Data JPA
Hibernate
PostgreSQL
Spring Validation
Spring AI
Maven
Lombok
📊 Example Analytics

The analytics engine calculates:

units sold
current stock
revenue
profit
sales per day
estimated days of stock
restock recommendation

Possible recommendations:

URGENT_ORDER
MONITOR
WAIT
💰 Budget Optimizer

Example with a budget of 100,000 KGS:

Recommended product: Google Pixel 8 256GB
Quantity: 3
Required budget: 85,140 KGS
Remaining budget: 14,860 KGS
Expected revenue: 135,000 KGS
Expected profit: 49,860 KGS
🔐 Configuration

Secrets must not be committed to the repository.

Use environment variables for API credentials:

The project uses Ollama for local LLM inference.
No OpenAI API key is required.

The project can also use a locally hosted LLM through Ollama.

▶️ Running the project

Requirements:

Java 21
PostgreSQL
Maven
Ollama or configured AI provider

Run:

./mvnw spring-boot:run
📌 Project Status

Under active development.

Planned improvements:

Local LLM integration with Ollama
AI business chat
Advanced demand forecasting
Product comparison
REST API documentation
Automated tests
Docker
Frontend dashboard
