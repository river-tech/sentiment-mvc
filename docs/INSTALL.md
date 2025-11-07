# Installation Guide

## Prerequisites

- Java 17 or higher
- Apache Maven 3.8 or higher
- Apache Tomcat 10
- PostgreSQL 16 with pgvector extension
- (Optional) Python 3.10+ to run the embedding Flask API

## Step 1: Database Setup

1. Create PostgreSQL database/user (example):
```bash
createuser -s admin || true
createdb -O admin admin || true
psql -d postgres -c "ALTER USER admin WITH PASSWORD '123456';"
```

2. Enable pgvector and run migrations:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
-- Apply scripts under src/main/resources/db/migration/
```

## Step 2: Configure Application

1. Edit `src/main/resources/application.properties`
2. Update database connection settings:
   - `db.url`: `jdbc:postgresql://localhost:6969/admin`
   - `db.username`: `admin`
   - `db.password`: `123456`
   - `db.driver`: `org.postgresql.Driver`

## Step 3: Build Project

```bash
cd sentiment-mvc
mvn clean package
```

This will create `target/sentiment-mvc.war`

## Step 4: Deploy to Tomcat

1. Copy WAR file to Tomcat webapps directory:
```bash
cp target/sentiment-mvc.war /opt/homebrew/opt/tomcat@10/libexec/webapps/
```

2. Start Tomcat (Homebrew example):
```bash
brew services start tomcat@10
```

3. Access application:
```
http://localhost:8080/sentiment-mvc/
```

Trang chủ (`/`) sẽ tự động chuyển đến `/dashboard`. Không cần đăng nhập.

## Optional: Embedding Flask API

Ứng dụng gọi `http://127.0.0.1:9696/embed` để tạo embedding keyword.

```bash
cd embedding
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python embedding_api.py
```

## Troubleshooting

- Check Tomcat logs: `/opt/homebrew/var/log/tomcat@10/catalina.$(date +%F).log`
- Verify database connection settings và pgvector
- Ensure Java 17 is installed and on PATH
- Đảm bảo port 8080 (Tomcat) và 9696 (Flask) không bị chiếm dụng

## TODO

- Thêm script tự động chạy migration (Flyway/liquibase)
- Bổ sung hướng dẫn triển khai embedding service production

