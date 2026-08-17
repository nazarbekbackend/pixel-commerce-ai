# 📱 Pixel Commerce AI

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

spring.ai.openai.api-key=${OPENAI_API_KEY}

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
