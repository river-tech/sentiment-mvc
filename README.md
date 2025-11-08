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
 │   │   │       ├─ controller/       # Servlets (DashboardServlet, JobServlet, HealthServlet)
 │   │   │       ├─ service/          # Business logic (KeywordService, JobService, SentimentService)
 │   │   │       ├─ dao/              # Database access (JobDAO, JobArticleDAO)
 │   │   │       ├─ model/            # Entity classes (Job, JobArticle)
 │   │   │       ├─ worker/           # Background Queue + WorkerThread
 │   │   │       ├─ config/           # DB Config (HikariCP)
 │   │   │       └─ util/             # Helpers (HttpClientUtil, JsonParser)
 │   │   ├─ resources/
 │   │   │   └─ application.properties  # DB connection configs
 │   │   └─ webapp/
 │   │       ├─ WEB-INF/
 │   │       │   ├─ web.xml
 │   │       │   └─ views/
 │   │       │       ├─ dashboard.jsp
 │   │       │       ├─ jobs.jsp
 │   │       │       ├─ header.jsp
 │   │       │       └─ footer.jsp
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

- **Global dataset**: Không còn đăng nhập/đăng ký; mọi từ khóa và kết quả được chia sẻ cho toàn hệ thống.
- **Flow chuẩn**: JSP View → Servlet Controller → Service → DAO → PostgreSQL (pgvector) → Job Queue/Worker.
- **Embedding Service**: Keyword mới gọi Flask API (`/embed`) để lấy vector trước khi lưu DB.
- **Worker Thread**: Xử lý nền, cập nhật cảm xúc và đảm bảo cột `embedding` có dữ liệu.
- **HikariCP**: Connection pool cho mọi thao tác JDBC.

```
Trình duyệt (JSP) ─► Servlet (Job/Dashboard) ─► Service (Keyword/Sentiment)
                       │                                  │
                       ▼                                  ▼
                     DAO (JobDAO, JobArticleDAO) ───► PostgreSQL + pgvector
                                                           │
                                                           ▼
                                                  Flask Embedding API
                                                           │
                                                           ▼
                                                  JobQueue + WorkerThread
```

## Quick Start

### 1) Khởi tạo Git (nếu chưa có)

```bash
# macOS/Linux
cd /Users/admin/Documents/laptrinhmag/sentiment-mvc

# Windows
cd C:\path\to\sentiment-mvc

git init
git add .
git commit -m "Initial commit"
# git remote add origin <your-repo-url>
# git push -u origin main
```

`.gitignore` đã được thêm để bỏ qua `target/`, `.idea/`, `.venv/`, v.v.

---

## Setup cho Windows

### 2.1) Cài đặt Java 17

1. Tải Java 17 từ [Adoptium](https://adoptium.net/temurin/releases/?version=17) hoặc [Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
2. Chọn bản **Windows x64 Installer**
3. Cài đặt và chọn "Add to PATH"
4. Kiểm tra:
   ```cmd
   java -version
   javac -version
   ```
   Kết quả phải hiển thị version 17.x.x

### 2.2) Cài đặt Maven

1. Tải Maven từ [Apache Maven](https://maven.apache.org/download.cgi)
2. Giải nén vào thư mục (ví dụ: `C:\Program Files\Apache\maven`)
3. Thêm biến môi trường:
   - Mở **System Properties** → **Environment Variables**
   - Thêm `MAVEN_HOME` = `C:\Program Files\Apache\maven`
   - Thêm vào `Path`: `%MAVEN_HOME%\bin`
4. Kiểm tra:
   ```cmd
   mvn -version
   ```

### 2.3) Cài đặt PostgreSQL 16

1. Tải PostgreSQL từ [postgresql.org](https://www.postgresql.org/download/windows/)
2. Chạy installer, chọn:
   - Port: `5432` (mặc định)
   - Superuser password: `123456` (hoặc password bạn muốn)
   - Locale: `Vietnamese, Vietnam` (tùy chọn)
3. Cài đặt **pgAdmin** (tùy chọn, để quản lý DB)
4. Kiểm tra service đang chạy:
   - Mở **Services** (Win+R → `services.msc`)
   - Tìm **postgresql-x64-16** → phải ở trạng thái **Running**

### 2.4) Cài đặt Apache Tomcat 10

1. Tải Tomcat từ [Apache Tomcat](https://tomcat.apache.org/download-10.cgi)
2. Chọn **64-bit Windows zip** (ví dụ: `apache-tomcat-10.1.x-windows-x64.zip`)
3. Giải nén vào thư mục (ví dụ: `C:\Program Files\Apache\apache-tomcat-10.1.x`)
4. Cấu hình biến môi trường (tùy chọn):
   - `CATALINA_HOME` = `C:\Program Files\Apache\apache-tomcat-10.1.x`
5. Khởi động Tomcat:
   ```cmd
   cd C:\Program Files\Apache\apache-tomcat-10.1.x\bin
   startup.bat
   ```
6. Kiểm tra: Mở browser → `http://localhost:8080` → phải thấy trang Tomcat
7. Dừng Tomcat (khi cần):
   ```cmd
   shutdown.bat
   ```

### 2.5) Tạo Database PostgreSQL

1. Mở **Command Prompt** hoặc **PowerShell**
2. Thêm PostgreSQL vào PATH (nếu chưa có):
   ```cmd
   set PATH=%PATH%;C:\Program Files\PostgreSQL\16\bin
   ```
3. Tạo database và user:
   ```cmd
   psql -U postgres
   ```
   Nhập password superuser (ví dụ: `123456`)

4. Trong psql prompt, chạy:
   ```sql
   CREATE USER admin WITH PASSWORD '123456';
   ALTER USER admin WITH SUPERUSER;
   CREATE DATABASE admin OWNER admin;
   \c admin
   CREATE EXTENSION IF NOT EXISTS vector;
   \q
   ```

Hoặc dùng **pgAdmin**:
- Mở pgAdmin → Connect to Server (localhost)
- Right-click **Databases** → **Create** → **Database**
  - Name: `admin`
  - Owner: `admin`
- Right-click database `admin` → **Query Tool** → chạy:
  ```sql
  CREATE EXTENSION IF NOT EXISTS vector;
  ```

### 2.6) Cấu hình Database Connection

Chỉnh sửa file `src/main/resources/application.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/admin
db.username=admin
db.password=123456
db.driver=org.postgresql.Driver
db.pool.maximum=10
db.pool.minimum.idle=5
db.pool.connection.timeout=30000
db.pool.idle.timeout=600000
db.pool.max.lifetime=1800000
```

### 2.7) Tạo Schema Database

Chạy script SQL từ `docs/sql/schema_postgres.sql`:

```cmd
psql -U admin -d admin -f docs\sql\schema_postgres.sql
```

Hoặc copy nội dung file và chạy trong **pgAdmin Query Tool**.

### 2.8) Build và Deploy

1. Mở **Command Prompt** hoặc **PowerShell** tại thư mục project:
   ```cmd
   cd C:\path\to\sentiment-mvc
   ```

2. Build project:
   ```cmd
   mvn clean package -DskipTests
   ```

3. Copy WAR file vào Tomcat:
   ```cmd
   copy target\sentiment-mvc.war "C:\Program Files\Apache\apache-tomcat-10.1.x\webapps\"
   ```

4. Đợi Tomcat tự động deploy (~5-15 giây)

5. Truy cập ứng dụng:
   ```
   http://localhost:8080/sentiment-mvc/
   ```

### 2.9) Health-check Database

```cmd
curl http://localhost:8080/sentiment-mvc/health/db
```

Hoặc mở browser: `http://localhost:8080/sentiment-mvc/health/db`

### 2.10) Embedding Service (Tùy chọn)

1. Cài đặt Python 3.10+ từ [python.org](https://www.python.org/downloads/)
2. Mở **Command Prompt** tại thư mục `embedding`:
   ```cmd
   cd C:\path\to\sentiment-mvc\embedding
   python -m venv .venv
   .venv\Scripts\activate
   python -m pip install --upgrade pip
   pip install -r requirements.txt
   set HF_HUB_DISABLE_TELEMETRY=1
   python embedding_api.py
   ```

3. Service sẽ chạy tại: `http://127.0.0.1:9696/embed`

4. Test:
   ```cmd
   curl -X POST http://127.0.0.1:9696/embed -H "Content-Type: application/json" -d "{\"keyword\":\"học máy là gì\"}"
   ```

---

## Setup cho macOS/Linux

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

- Ứng dụng không còn bước đăng nhập/đăng ký; trang chủ (`/`) tự động chuyển đến `/dashboard`.
- Điều hướng chính: Trang chủ → `/`, Phân tích từ khóa → `/jobs`, Kết quả mới nhất → `/dashboard`.
- CSS public tại `src/main/webapp/assets/css/style.css`.

## Troubleshooting

### Windows

**Trang cũ/không đổi:**
1. Dừng Tomcat:
   ```cmd
   cd C:\Program Files\Apache\apache-tomcat-10.1.x\bin
   shutdown.bat
   ```
2. Xóa deploy cũ:
   ```cmd
   rmdir /s /q "C:\Program Files\Apache\apache-tomcat-10.1.x\webapps\sentiment-mvc"
   del "C:\Program Files\Apache\apache-tomcat-10.1.x\webapps\sentiment-mvc.war"
   ```
3. Copy WAR mới:
   ```cmd
   copy target\sentiment-mvc.war "C:\Program Files\Apache\apache-tomcat-10.1.x\webapps\"
   ```
4. Khởi động lại Tomcat:
   ```cmd
   startup.bat
   ```

**Kiểm tra log Tomcat:**
- Log nằm tại: `C:\Program Files\Apache\apache-tomcat-10.1.x\logs\`
- File log chính: `catalina.YYYY-MM-DD.log`
- Mở bằng Notepad hoặc PowerShell:
  ```cmd
  notepad "C:\Program Files\Apache\apache-tomcat-10.1.x\logs\catalina.YYYY-MM-DD.log"
  ```

**Lỗi "Port 8080 already in use":**
- Tìm process đang dùng port 8080:
  ```cmd
  netstat -ano | findstr :8080
  ```
- Kill process (thay `PID` bằng Process ID):
  ```cmd
  taskkill /PID <PID> /F
  ```

**Lỗi "Cannot connect to PostgreSQL":**
- Kiểm tra service PostgreSQL đang chạy:
  ```cmd
  services.msc
  ```
- Tìm `postgresql-x64-16` → phải ở trạng thái **Running**
- Nếu chưa chạy, right-click → **Start**

**Lỗi "Maven not found":**
- Kiểm tra biến môi trường:
  ```cmd
  echo %MAVEN_HOME%
  echo %PATH%
  ```
- Nếu thiếu, thêm lại trong **System Properties** → **Environment Variables**

### macOS/Linux

**Trang cũ/không đổi:**
```bash
WEBAPPS=/opt/homebrew/opt/tomcat@10/libexec/webapps
rm -rf $WEBAPPS/sentiment-mvc $WEBAPPS/sentiment-mvc.war
cp target/sentiment-mvc.war $WEBAPPS/
brew services restart tomcat@10
```

**Kiểm tra log Tomcat:**
```bash
ls -lah /opt/homebrew/var/log/tomcat@10/
tail -f catalina.$(date +%F).log
```

## Technologies

- Java 17
- Jakarta Servlet API 6.0
- Jakarta JSP API 3.1
- JSTL 3.0
- HikariCP 5.0
- PostgreSQL JDBC Driver 42.x
- pgvector extension (PostgreSQL)
- Gson 2.10
- JUnit 5

## License

TODO: Add license information

