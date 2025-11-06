# Sentiment MVC

MVC-based Java web application for sentiment analysis using JSP + Servlet architecture.

## Requirements

- Java 17+
- Apache Maven 3.8+
- Apache Tomcat 10 (Homebrew on macOS)
- PostgreSQL 16 (đã chuyển từ MySQL)
- (Tùy chọn) Python 3.10+ để chạy service embedding

## Project Structure

```
sentiment-mvc/
 ├─ pom.xml
 ├─ src/
 │   ├─ main/
 │   │   ├─ java/
 │   │   │   └─ com/team/app/
 │   │   │       ├─ controller/       # Servlets (JobServlet, AuthServlet)
 │   │   │       ├─ service/          # Business logic (JobService, SentimentService)
 │   │   │       ├─ dao/              # Database access (UserDAO, JobDAO, JobArticleDAO)
 │   │   │       ├─ model/            # Entity classes (User, Job, JobArticle)
 │   │   │       ├─ worker/           # Background Queue + WorkerThread
 │   │   │       ├─ config/           # DB Config (HikariCP)
 │   │   │       └─ util/             # Helpers (HttpClientUtil, JsonParser)
 │   │   ├─ resources/
 │   │   │   └─ application.properties  # DB connection configs
 │   │   └─ webapp/
 │   │       ├─ WEB-INF/
 │   │       │   ├─ web.xml
 │   │       │   └─ views/
 │   │       │       ├─ login.jsp
 │   │       │       ├─ register.jsp
 │   │       │       ├─ dashboard.jsp
 │   │       │       └─ jobs.jsp
 │   │       └─ index.jsp
 │   └─ test/
 │       └─ java/
 │           └─ com/team/app/test/
 │               └─ SampleTest.java
 ├─ README.md
 └─ docs/
     ├─ ERD.png
     ├─ mvc-diagram.png
     └─ INSTALL.md
```

## Architecture

- **MVC Pattern**: Controller (Servlet) → Service (Business Logic) → DAO (Data Access) → Database
- **Worker Threads**: Background processing for sentiment analysis jobs
- **HikariCP**: Connection pooling for database operations

## Quick Start

### 1) Khởi tạo Git (nếu chưa có)

```bash
cd /Users/admin/Documents/laptrinhmag/sentiment-mvc
git init
git add .
git commit -m "Initial commit"
# git remote add origin <your-repo-url>
# git push -u origin main
```

`.gitignore` đã được thêm để bỏ qua `target/`, `.idea/`, `.venv/`, v.v.

### 2) Cài công cụ (macOS bằng Homebrew)

```bash
brew install openjdk@17 maven tomcat@10 postgresql@16
brew services start postgresql@16
brew services start tomcat@10
```

### 3) Tạo database PostgreSQL

```bash
createuser -s admin || true
createdb -O admin admin || true
psql -d postgres -c "ALTER USER admin WITH PASSWORD '123456';"
```

Hoặc tùy DB của bạn, cập nhật `src/main/resources/application.properties`:

```properties
db.url=jdbc:postgresql://localhost:6969/admin
db.username=admin
db.password=123456
db.driver=org.postgresql.Driver
```

### 4) Build & Deploy

```bash
mvn -DskipTests package
cp target/sentiment-mvc.war /opt/homebrew/opt/tomcat@10/libexec/webapps/
# đợi Tomcat bung WAR (~5-15s)
```

Truy cập: `http://localhost:8080/sentiment-mvc/`

### 5) Health-check DB

```bash
curl -s http://localhost:8080/sentiment-mvc/health/db
```

### 6) Embedding service (tùy chọn)

```bash
cd embedding
python3 -m venv .venv && source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
export HF_HUB_DISABLE_TELEMETRY=1
python embedding_api.py  # http://127.0.0.1:9696/embed
```

Test nhanh:

```bash
curl -s -X POST http://127.0.0.1:9696/embed \
  -H "Content-Type: application/json" \
  -d '{"keyword":"học máy là gì"}'
```

## Notes

- Auth filter chặn toàn bộ route khi chưa đăng nhập, ngoại trừ: `/`, `/login`, `/register`, `/assets/*`, `/health/*`.
- Sau khi đăng nhập thành công, redirect về `/dashboard`.
- CSS public tại `src/main/webapp/assets/css/style.css`.

## Troubleshooting

- Trang cũ/không đổi: xoá deploy cũ và restart Tomcat

```bash
WEBAPPS=/opt/homebrew/opt/tomcat@10/libexec/webapps
rm -rf $WEBAPPS/sentiment-mvc $WEBAPPS/sentiment-mvc.war
cp target/sentiment-mvc.war $WEBAPPS/
brew services restart tomcat@10
```

- Kiểm tra log Tomcat: `ls -lah /opt/homebrew/var/log/tomcat@10/` và `tail -f catalina.$(date +%F).log`

## Technologies

- Java 17
- Jakarta Servlet API 6.0
- Jakarta JSP API 3.1
- JSTL 3.0
- HikariCP 5.0
- MySQL Connector 8.0
- Gson 2.10
- JUnit 5

## License

TODO: Add license information

