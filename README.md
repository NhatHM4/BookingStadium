# Booking Stadium

## Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven 3.8+

## Quick Start

### 1. Start Docker containers (MySQL + Redis)
```bash
docker-compose up -d
```

### 2. Verify containers are running
```bash
docker-compose ps
```

### 3. Run Spring Boot application
```bash
./mvnw spring-boot:run
```
Or on Windows:
```bash
mvnw.cmd spring-boot:run
```

### 4. Verify connections
```
GET http://localhost:8080/api/v1/health
```

### 5. Swagger UI
```
http://localhost:8080/swagger-ui.html
```

## Docker Services

| Service | Container | Port (Host) | Port (Container) | Credentials |
|---------|-----------|-------------|-------------------|-------------|
| MySQL 8.0 | booking-stadium-mysql | 3307 | 3306 | booking_user / booking_pass |
| Redis 7 | booking-stadium-redis | 6380 | 6379 | password: redis123 |

## Stop & Cleanup
```bash
# Stop containers
docker-compose down

# Stop & remove volumes (reset data)
docker-compose down -v
```

## Telegram Bot (Optional)

Set these env vars before starting app:

- `TELEGRAM_BOT_ENABLED` (`true|false`)
- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_WEBHOOK_SECRET`
- `APP_BASE_URL` (frontend base URL used in link thông báo kèo)

Webhook endpoint:

- `POST /api/v1/telegram/webhook`
- Required header when secret configured: `X-Telegram-Bot-Api-Secret-Token`
