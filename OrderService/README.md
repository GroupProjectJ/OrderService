# OrderService

A RESTful microservice for managing orders, built with Spring Boot 4.1.0 and Java 21. Part of an Enterprise E-Commerce application. Communicates with ProductService to validate products and calculate order totals.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.1.0 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| ORM | Hibernate / Spring Data JPA |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, MockMvc, H2 |
| Build | Maven (via Maven Wrapper) |
| Container | Docker + Docker Compose |

---

## Project Structure

```
src/
├── main/java/com/ecommerce/orderservice/
│   ├── api/                  # REST Controllers
│   ├── service/              # Business logic (interface + impl)
│   ├── repository/           # Spring Data JPA repositories
│   ├── model/                # JPA entities
│   ├── dto/                  # Data Transfer Objects
│   ├── config/               # RestTemplate configuration
│   └── exception/            # Custom exceptions + global handler
└── test/java/com/ecommerce/orderservice/
    ├── api/                  # Controller tests (MockMvc)
    ├── service/              # Service unit tests (Mockito)
    └── repository/           # Repository tests (H2 + @DataJpaTest)
```

---

## API Endpoints

Base URL: `http://localhost:8080/api/orders`

| Method | Endpoint | Description | Status |
|---|---|---|---|
| POST | `/api/orders` | Create a new order | 201 Created |

### Request Body (POST)

```json
{
  "customerId": 10,
  "productId": 1,
  "quantity": 3
}
```

### Response Body

```json
{
  "orderId": 100,
  "customerId": 10,
  "productId": 1,
  "productName": "Headphone",
  "quantity": 3,
  "totalPrice": 1800.00,
  "orderDate": "2026-06-27T10:00:00",
  "status": "CREATED"
}
```

### Validation Rules

| Field | Rule |
|---|---|
| `customerId` | Required (not null) |
| `productId` | Required (not null) |
| `quantity` | Required, must be positive |

---

## Running the Project

### Option 1 — Docker (Recommended)

Requires [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

```bash
docker compose up --build
```

Starts both PostgreSQL and the application automatically. No manual database setup needed.

- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

To stop:
```bash
docker compose down
```

### Option 2 — Local (Maven Wrapper)

Requires PostgreSQL running locally with a database named `Ecommerce`.

```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

---

## Running Tests

Tests use H2 in-memory database — no PostgreSQL or Docker needed.

```bash
# Windows
.\mvnw.cmd test

# macOS / Linux
./mvnw test
```

### Test Coverage

| Test Class | Coverage |
|---|---|
| `OrderServiceImplTest` | createOrder — success, total price, status, date, product-not-found, service-unavailable, product name (7 tests) |
| `OrderControllerTest` | POST endpoint + validation |
| `OrderRepositoryTest` | CRUD operations via H2 |

---

## Docker Hub

### Build and Push

```bash
docker build -t <your-dockerhub-username>/order-service:latest .
docker login
docker push <your-dockerhub-username>/order-service:latest
```

### Pull and Run from Docker Hub

```bash
docker pull <your-dockerhub-username>/order-service:latest

docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/Ecommerce \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=2001 \
  <your-dockerhub-username>/order-service:latest
```

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/Ecommerce` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `2001` | Database password |

---

## Swagger UI

Interactive API documentation:
```
http://localhost:8080/swagger-ui/index.html
```

API schema (JSON):
```
http://localhost:8080/api-docs
```
