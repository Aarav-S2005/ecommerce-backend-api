# E-Commerce Backend & Autonomous AI Shopping Co-Pilot

A E-Commerce Backend REST API built with **Spring Boot 4**, **Java 21**, **PostgreSQL**, **Redis**, and **Spring AI**. The system features a locally hosted, context-aware **Autonomous AI Shopping Co-Pilot** (powered by Ollama `qwen3:14b-q8_0`) that autonomously interacts across catalog, cart, user, and order domains via 29 fine-grained tools while driving real-time frontend UI actions.

---

## Key Highlights

- **Local Autonomous AI Co-Pilot**: Integrated Spring AI with Ollama running `qwen3:14b-q8_0`.
- **29 Deterministic Domain Tools**: Autonomous function-calling across 4 business domains (User, Catalog, Cart, Order).
- **Interactive Generative UI**: Dual-channel responses containing conversational markdown and machine-readable `uiAction` signals (`OPEN_CART_DRAWER`, `PROCEED_TO_CHECKOUT`, `APPLY_CATALOG_FILTER`, `NAVIGATE_TO_PRODUCT`).

---

## Functional Requirements

### 1. User & Authentication
- User registration with immediate automatic session creation and cookie dispatch.
- Multi-address management with default shipping designations.
- Customer interest tracking (recently viewed and liked products).

### 2. Catalog & Discovery
- Product management with brands and categories.
- Dynamic searching, category/price filtering.
- Customer reviews and rating aggregation.

### 3. Shopping Cart
- Persistent cart state bound to authenticated customer ID.
- Pre-checkout validation ensuring stock availability, active pricing, and minimum order values.

### 4. Order Management & Policies
- Strict Checkout Invariants: Direct AI-driven checkout permitted only for `CASH_ON_DELIVERY` (COD). 
- Business Policy Guardrails:
  - **Undelivered Orders**: Immediate cancellation supported for COD; automated refund initiated for prepaid orders.
  - **Delivered Orders**: Cancellation disallowed; customers may request **Refund** or **Exchange** strictly within a 7-day post-delivery window.

### 5. AI Shopping Co-Pilot
- **Intent Preemption**: Seamlessly abandons pending clarification questions if the user pivots to a new topic without stubborn state locks.
- **Dynamic Context Injection**: Automatically ingests the user's current screen (`pageType`, active `productId`, active search) so relative questions like *"Is this in stock?"* resolve without explicit naming.
- **Minimized Hallucination Guardrails**: The LLM is strictly grounded against the 29 domain tools with dynamic query limits capped at 10 items.

---

## Technical Stack

| Layer | Technology |
|---|---|
| **Framework** | Spring Boot 4.1 |
| **Language** | Java 21 (LTS) |
| **Database** | PostgreSQL 16 with Spring Data JPA / Hibernate |
| **Caching** | Redis (with Jackson serialization & Graceful DB Fallback) |
| **Security** | Spring Security, JJWT (Cookie-based Stateless) |
| **AI Framework** | Spring AI 2.0.0 |
| **Local LLM Engine** | Ollama (`qwen3:14b-q8_0` with 12K context window) |

---

## Getting Started

### 1. Prerequisites
- Java 21 JDK
- Maven 3.9+
- PostgreSQL 16
- Redis (optional, graceful fallback enabled)
- [Ollama](https://ollama.ai) installed and running locally

### 2. Pull Local AI Model
```bash
ollama run qwen3:14b-q8_0
```

### 3. Configure Database
Update `src/main/resources/application.yml` with your PostgreSQL credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: postgres
    password: your_password
```

### 4. Build and Run
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

---
