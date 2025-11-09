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

### Mô hình MVC (Model-View-Controller)

Ứng dụng được thiết kế theo mô hình MVC chuẩn:

```
┌─────────────────────────────────────────────────────────────────┐
│                         VIEW LAYER                              │
│  (JSP Files - Presentation)                                     │
├─────────────────────────────────────────────────────────────────┤
│  • dashboard.jsp      - Giao diện chính, form nhập từ khóa      │
│  • header.jsp         - Header navigation                        │
│  • footer.jsp         - Footer                                   │
│  • index.jsp          - Trang chủ (redirect to dashboard)       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                           │
│  (Servlets - Request Handling)                                   │
├─────────────────────────────────────────────────────────────────┤
│  • DashboardServlet   - Xử lý request dashboard, hiển thị jobs  │
│  • JobServlet         - Tạo job mới, kiểm tra status            │
│  • HealthServlet      - Health check database connection         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                             │
│  (Business Logic)                                                │
├─────────────────────────────────────────────────────────────────┤
│  • KeywordService     - Xử lý keyword, tìm job tương tự          │
│  • SentimentService   - Phân tích cảm xúc (Flask API)           │
│  • CrawlService        - Crawl articles từ Google News RSS       │
│  • JobService         - Quản lý jobs                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DAO LAYER                               │
│  (Data Access Object - Database Operations)                     │
├─────────────────────────────────────────────────────────────────┤
│  • JobDAO             - CRUD operations cho jobs table          │
│  • JobArticleDAO      - CRUD operations cho job_articles table   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      MODEL LAYER                                │
│  (Entity Classes)                                                │
├─────────────────────────────────────────────────────────────────┤
│  • Job                - Entity cho jobs table                    │
│  • JobArticle         - Entity cho job_articles table            │
│  • KeywordEmbedding   - Entity cho keyword embeddings            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                               │
│  (PostgreSQL + pgvector)                                        │
├─────────────────────────────────────────────────────────────────┤
│  • jobs               - Lưu trữ jobs và sentiment results       │
│  • job_articles       - Lưu trữ articles đã crawl                │
│  • HikariCP           - Connection pooling                       │
└─────────────────────────────────────────────────────────────────┘
```

### Sơ đồ hoạt động của ứng dụng

```
┌─────────────────────────────────────────────────────────────────────┐
│  CLIENT (Browser)                                                   │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ 1. User nhập từ khóa → Submit form                           │  │
│  │ 2. AJAX POST /jobs/create                                     │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  CONTROLLER: JobServlet                                             │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ • Nhận keyword từ request                                    │  │
│  │ • Gọi KeywordService.processKeyword()                        │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  SERVICE: KeywordService                                            │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ 1. Gọi Flask API /embed để lấy embedding vector             │  │
│  │ 2. Tìm job tương tự trong DB (JobDAO.findMostSimilarJob)     │  │
│  │ 3. Nếu similarity >= 85% → Trả về job cũ                    │  │
│  │ 4. Nếu không → Tạo job mới (JobDAO.create)                  │  │
│  │ 5. Submit jobId vào JobQueue                                 │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  BACKGROUND PROCESSING: WorkerThread (Tính toán lớn - 30% điểm)   │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ 1. Lấy jobId từ JobQueue (BlockingQueue)                     │  │
│  │ 2. Update status = "RUNNING"                                 │  │
│  │ 3. CrawlService.fetchArticles() → Crawl 10 articles từ RSS   │  │
│  │ 4. Với mỗi article:                                           │  │
│  │    - Gọi SentimentService.classifyArticle()                  │  │
│  │    - SentimentService gọi Flask API /sentiment               │  │
│  │    - Lưu article vào DB (JobArticleDAO.insert)                │  │
│  │ 5. Tính toán sentiment statistics                            │  │
│  │ 6. Update job: sentiment percentages, status = "DONE"       │  │
│  │ 7. Đảm bảo embedding được lưu                                │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  DATABASE CONNECTION (10% điểm)                                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ • DatabaseConfig: Khởi tạo HikariCP DataSource               │  │
│  │ • Connection Pool: min=5, max=10 connections                 │  │
│  │ • PostgreSQL với pgvector extension                         │  │
│  │ • Tất cả DAO operations sử dụng connection pool              │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  CLIENT: Real-time Updates                                          │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ • Polling /jobs/status?id={jobId} mỗi 1.5 giây               │  │
│  │ • Update UI: progress bar, status badge, chart, table         │  │
│  │ • Khi status = "DONE" → Hiển thị kết quả                     │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### Xác nhận yêu cầu dự án

#### ✅ Kết nối cơ sở dữ liệu (10% điểm)

- **Database**: PostgreSQL 16 với extension pgvector
- **Connection Pool**: HikariCP với cấu hình:
  - Maximum pool size: 10 connections
  - Minimum idle: 5 connections
  - Connection timeout: 30 seconds
  - Idle timeout: 10 minutes
  - Max lifetime: 30 minutes
- **DAO Pattern**: Tất cả database operations thông qua DAO classes
- **Health Check**: Endpoint `/health/db` để kiểm tra kết nối
- **Configuration**: File `application.properties` quản lý connection string

#### ✅ Tính toán lớn chạy ngầm (30% điểm)

- **Job Queue**: `JobQueue` sử dụng `BlockingQueue<Long>` để quản lý jobs
- **Worker Threads**: Nhiều worker threads xử lý song song (số lượng = CPU cores / 4)
- **Background Processing**:
  1. **Crawl dữ liệu từ bên thứ 3**: Crawl top 10 articles từ Google News RSS
  2. **Xử lý dữ liệu lớn**: Phân tích sentiment cho 10 articles (gọi Flask API)
  3. **Tính toán embedding**: Tạo embedding vector 384 dimensions
  4. **Semantic matching**: Tìm job tương tự bằng pgvector cosine similarity
- **Async Processing**: Client không cần đợi, nhận kết quả qua polling
- **Status Tracking**: Real-time progress bar và status updates

### Flow chi tiết

1. **Client Request** → `JobServlet.doPost("/create")`
2. **Service Layer** → `KeywordService.processKeyword()`
3. **External API** → Flask API `/embed` để lấy embedding
4. **Database Query** → `JobDAO.findMostSimilarJob()` (semantic search)
5. **Job Creation** → `JobDAO.create()` nếu không tìm thấy job tương tự
6. **Queue Submission** → `JobQueue.submit(jobId)`
7. **Background Worker** → `WorkerThread.run()`:
   - `CrawlService.fetchArticles()` - Crawl từ Google News
   - `SentimentService.classifyArticle()` - Phân tích từng article
   - `JobArticleDAO.insert()` - Lưu articles
   - `JobDAO.updateSentiment()` - Cập nhật kết quả
8. **Client Polling** → `JobServlet.handleStatus()` mỗi 1.5 giây
9. **UI Update** → Dashboard cập nhật real-time qua AJAX

## Bảng liệt kê các Function/Class

### Controller Layer (Servlets)

| Class | Function | Chức năng |
|-------|----------|-----------|
| `DashboardServlet` | `doGet()` | Hiển thị dashboard với danh sách jobs và articles |
| `DashboardServlet` | `init()` | Khởi tạo servlet, lấy JobDAO và JobArticleDAO |
| `JobServlet` | `doGet()` | Xử lý GET request (status check hoặc forward to dashboard) |
| `JobServlet` | `doPost()` | Xử lý POST request tạo job mới (`/create`) |
| `JobServlet` | `handleStatus()` | Trả về JSON status của job (AJAX endpoint) |
| `JobServlet` | `isAjax()` | Kiểm tra request có phải AJAX không |
| `JobServlet` | `writeJson()` | Ghi JSON response |
| `HealthServlet` | `doGet()` | Health check database connection |

### Service Layer (Business Logic)

| Class | Function | Chức năng |
|-------|----------|-----------|
| `KeywordService` | `processKeyword()` | Xử lý keyword: lấy embedding, tìm job tương tự, tạo job mới |
| `SentimentService` | `analyze()` | Phân tích sentiment cho list articles, trả về percentage map |
| `SentimentService` | `analyzeArticles()` | Phân tích sentiment và trả về SentimentStats |
| `SentimentService` | `analyzeSentiment()` | Phân tích sentiment cho 1 article |
| `SentimentService` | `batchAnalyzeSentiment()` | Batch analyze sentiment cho nhiều articles |
| `SentimentService` | `classifyArticle()` | Gọi Flask API `/sentiment` để phân loại article |
| `CrawlService` | `fetchArticles()` | Crawl top 10 articles từ Google News RSS |
| `JobService` | `createJob()` | Tạo job mới |
| `JobService` | `getAllJobs()` | Lấy tất cả jobs |
| `JobService` | `getJobById()` | Lấy job theo ID |
| `JobService` | `updateJobStatus()` | Cập nhật status của job |
| `JobService` | `deleteJob()` | Xóa job |

### DAO Layer (Data Access)

| Class | Function | Chức năng |
|-------|----------|-----------|
| `JobDAO` | `findMostSimilarJob()` | Tìm job tương tự nhất bằng pgvector cosine similarity |
| `JobDAO` | `create()` | Tạo job mới trong database (có/không embedding) |
| `JobDAO` | `findById()` | Tìm job theo ID |
| `JobDAO` | `findAll()` | Lấy tất cả jobs |
| `JobDAO` | `updateStatus()` | Cập nhật status của job (có/không progress) |
| `JobDAO` | `updateSentiment()` | Cập nhật sentiment percentages (positive, negative, neutral) |
| `JobDAO` | `updateEmbedding()` | Cập nhật embedding vector cho job |
| `JobDAO` | `deleteJob()` | Xóa job |
| `JobDAO` | `markFailed()` | Đánh dấu job là FAILED với lý do |
| `JobArticleDAO` | `findByJobId()` | Lấy tất cả articles của một job |
| `JobArticleDAO` | `insert()` | Thêm article mới vào database |
| `JobArticleDAO` | `deleteByJobId()` | Xóa tất cả articles của một job |

### Model Layer (Entity Classes)

| Class | Function | Chức năng |
|-------|----------|-----------|
| `Job` | Getters/Setters | Entity class cho jobs table (id, keyword, status, sentiment, embedding, etc.) |
| `JobArticle` | Getters/Setters | Entity class cho job_articles table (id, jobId, title, url, description, sentiment) |
| `KeywordEmbedding` | Getters/Setters | Entity class cho keyword embeddings |
| `SimilarJob` | `getJobId()` | Lấy job ID của job tương tự |
| `SimilarJob` | `getSimilarity()` | Lấy similarity score |
| `SimilarJob` | `isSimilarEnough()` | Kiểm tra similarity có đủ threshold không |

### Worker Layer (Background Processing)

| Class | Function | Chức năng |
|-------|----------|-----------|
| `JobQueue` | `getInstance()` | Singleton pattern, lấy instance của JobQueue |
| `JobQueue` | `submit()` | Thêm jobId vào queue |
| `JobQueue` | `take()` | Lấy jobId từ queue (blocking) |
| `JobQueue` | `size()` | Lấy số lượng jobs trong queue |
| `WorkerThread` | `run()` | Main loop: lấy job từ queue, xử lý background |
| `WorkerThread` | `ensureEmbedding()` | Đảm bảo embedding được lưu cho job |
| `WorkerThread` | `shutdown()` | Dừng worker thread gracefully |

### Config Layer

| Class | Function | Chức năng |
|-------|----------|-----------|
| `DatabaseConfig` | `contextInitialized()` | Khởi tạo HikariCP DataSource khi app start |
| `DatabaseConfig` | `contextDestroyed()` | Đóng DataSource khi app stop |
| `DatabaseConfig` | `getDataSource()` | Lấy DataSource instance |
| `AppStartupListener` | `contextInitialized()` | Khởi động worker threads khi app start |
| `AppStartupListener` | `contextDestroyed()` | Dừng worker threads khi app stop |

### Util Layer (Utilities)

| Class | Function | Chức năng |
|-------|----------|-----------|
| `HttpClientUtil` | `sendGet()` | Gửi HTTP GET request |
| `HttpClientUtil` | `sendPost()` | Gửi HTTP POST request |
| `HttpClientUtil` | `getEmbedding()` | Gọi Flask API `/embed` để lấy embedding vector |
| `EmbeddingUtil` | `cosineSimilarity()` | Tính cosine similarity giữa 2 vectors |
| `EmbeddingUtil` | `arrayToPgVector()` | Convert Java array sang PostgreSQL vector string |
| `EmbeddingUtil` | `pgVectorToArray()` | Convert PostgreSQL vector string sang Java array |
| `Logger` | `info()` | Log thông tin |
| `Logger` | `error()` | Log lỗi |
| `Logger` | `warn()` | Log cảnh báo |
| `Logger` | `debug()` | Log debug |
| `Logger` | `getLogFilePath()` | Lấy đường dẫn file log |
| `JsonParser` | Parse JSON | Parse JSON responses từ API |
| `PasswordUtil` | `sha256()` | Hash password bằng SHA-256 |
| `PasswordUtil` | `hashPassword()` | Hash password với salt |
| `PasswordUtil` | `verify()` | Verify password |

### View Layer (JSP Files)

| File | Chức năng |
|------|-----------|
| `dashboard.jsp` | Trang chính: form nhập từ khóa, biểu đồ sentiment, bảng articles, real-time updates |
| `header.jsp` | Header navigation với logo và menu |
| `footer.jsp` | Footer của trang |
| `index.jsp` | Trang chủ, redirect đến dashboard |

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

**Cho PostgreSQL localhost:**
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

**Cho PostgreSQL cloud (Aiven, AWS RDS, etc.):**
```properties
# Ví dụ Aiven Cloud
db.url=jdbc:postgresql://your-host:port/database?sslmode=require
db.username=your_username
db.password=your_password
db.driver=org.postgresql.Driver
db.pool.maximum=10
db.pool.minimum.idle=5
db.pool.connection.timeout=30000
db.pool.idle.timeout=600000
db.pool.max.lifetime=1800000
```

**Lưu ý:**
- Thay `your-host`, `port`, `database`, `your_username`, `your_password` bằng thông tin thực tế
- Nếu dùng cloud database, thêm `?sslmode=require` vào URL
- Đảm bảo database đã có extension `vector` (pgvector) được cài đặt

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

### 2.10) Embedding & Sentiment Service (Python API)

#### 2.10.1) Cài đặt Python 3.10+

1. Tải Python 3.10+ từ [python.org](https://www.python.org/downloads/)
2. Chọn **Windows installer (64-bit)**
3. Cài đặt và chọn **"Add Python to PATH"**
4. Kiểm tra:
   ```cmd
   python --version
   pip --version
   ```

#### 2.10.2) Tạo Virtual Environment và Cài Packages

Mở **Command Prompt** tại thư mục `embedding`:

```cmd
cd C:\path\to\sentiment-mvc\embedding
python -m venv .venv
.venv\Scripts\activate
python -m pip install --upgrade pip
pip install -r requirements.txt
```

**Lưu ý:** Quá trình cài đặt có thể mất 5-10 phút do cần tải các thư viện ML lớn (PyTorch, transformers, sentence-transformers).

#### 2.10.3) Tải Models (Tự động khi chạy lần đầu)

Models sẽ được tự động tải từ Hugging Face khi chạy service lần đầu:

- **Embedding Model**: `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` (~420 MB)
- **Sentiment Model**: `wonrax/phobert-base-vietnamese-sentiment` (~450 MB)

**Tổng dung lượng**: ~870 MB (models sẽ được cache tại `~/.cache/huggingface/`)

**Tắt telemetry Hugging Face** (tùy chọn):
```cmd
set HF_HUB_DISABLE_TELEMETRY=1
```

#### 2.10.4) Chạy Service

```cmd
cd C:\path\to\sentiment-mvc\embedding
.venv\Scripts\activate
python embedding_api.py
```

Service sẽ chạy tại:
- **Embedding API**: `http://127.0.0.1:9696/embed`
- **Sentiment API**: `http://127.0.0.1:9696/sentiment`
- **Status API**: `http://127.0.0.1:9696/status`

#### 2.10.5) Test APIs

**Test Embedding API:**
```cmd
curl -X POST http://127.0.0.1:9696/embed -H "Content-Type: application/json" -d "{\"keyword\":\"học máy là gì\"}"
```

**Test Sentiment API:**
```cmd
curl -X POST http://127.0.0.1:9696/sentiment -H "Content-Type: application/json" -d "{\"text\":\"Sản phẩm này rất tuyệt vời và chất lượng cao\"}"
```

**Test Status:**
```cmd
curl http://127.0.0.1:9696/status
```

#### 2.10.6) Troubleshooting

**Lỗi "Module not found":**
- Đảm bảo virtual environment đã được activate: `.venv\Scripts\activate`
- Cài lại packages: `pip install -r requirements.txt`

**Lỗi "CUDA out of memory" hoặc models quá nặng:**
- Service sẽ tự động dùng CPU nếu không có GPU
- Có thể chỉnh `DEVICE` trong `embedding_api.py` để force CPU: `DEVICE = "cpu"`

**Models tải chậm:**
- Models sẽ được cache sau lần tải đầu tiên
- Có thể tải trước models bằng cách chạy Python script:
  ```python
  from sentence_transformers import SentenceTransformer
  from transformers import AutoTokenizer, AutoModelForSequenceClassification
  SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")
  AutoModelForSequenceClassification.from_pretrained("wonrax/phobert-base-vietnamese-sentiment")
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

**Cho PostgreSQL localhost:**
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

**Cho PostgreSQL cloud (Aiven, AWS RDS, etc.):**
```properties
# Ví dụ Aiven Cloud
db.url=jdbc:postgresql://your-host:port/database?sslmode=require
db.username=your_username
db.password=your_password
db.driver=org.postgresql.Driver
db.pool.maximum=10
db.pool.minimum.idle=5
db.pool.connection.timeout=30000
db.pool.idle.timeout=600000
db.pool.max.lifetime=1800000
```

**Lưu ý:**
- Thay `your-host`, `port`, `database`, `your_username`, `your_password` bằng thông tin thực tế
- Nếu dùng cloud database, thêm `?sslmode=require` vào URL
- Đảm bảo database đã có extension `vector` (pgvector) được cài đặt

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

### 6) Embedding & Sentiment Service (Python API)

#### 6.1) Cài đặt Python 3.10+

```bash
# macOS (Homebrew)
brew install python@3.10

# Linux (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install python3.10 python3.10-venv python3-pip

# Kiểm tra
python3 --version
pip3 --version
```

#### 6.2) Tạo Virtual Environment và Cài Packages

```bash
cd embedding
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
```

**Lưu ý:** Quá trình cài đặt có thể mất 5-10 phút do cần tải các thư viện ML lớn (PyTorch, transformers, sentence-transformers).

#### 6.3) Tải Models (Tự động khi chạy lần đầu)

Models sẽ được tự động tải từ Hugging Face khi chạy service lần đầu:

- **Embedding Model**: `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` (~420 MB)
- **Sentiment Model**: `wonrax/phobert-base-vietnamese-sentiment` (~450 MB)

**Tổng dung lượng**: ~870 MB (models sẽ được cache tại `~/.cache/huggingface/`)

**Tắt telemetry Hugging Face** (tùy chọn):
```bash
export HF_HUB_DISABLE_TELEMETRY=1
```

#### 6.4) Chạy Service

```bash
cd embedding
source .venv/bin/activate
python embedding_api.py
```

Service sẽ chạy tại:
- **Embedding API**: `http://127.0.0.1:9696/embed`
- **Sentiment API**: `http://127.0.0.1:9696/sentiment`
- **Status API**: `http://127.0.0.1:9696/status`

#### 6.5) Test APIs

**Test Embedding API:**
```bash
curl -s -X POST http://127.0.0.1:9696/embed \
  -H "Content-Type: application/json" \
  -d '{"keyword":"học máy là gì"}'
```

**Test Sentiment API:**
```bash
curl -s -X POST http://127.0.0.1:9696/sentiment \
  -H "Content-Type: application/json" \
  -d '{"text":"Sản phẩm này rất tuyệt vời và chất lượng cao"}'
```

**Test Status:**
```bash
curl -s http://127.0.0.1:9696/status
```

#### 6.6) Troubleshooting

**Lỗi "Module not found":**
- Đảm bảo virtual environment đã được activate: `source .venv/bin/activate`
- Cài lại packages: `pip install -r requirements.txt`

**Lỗi "CUDA out of memory" hoặc models quá nặng:**
- Service sẽ tự động dùng CPU nếu không có GPU
- Có thể chỉnh `DEVICE` trong `embedding_api.py` để force CPU: `DEVICE = "cpu"`

**Models tải chậm:**
- Models sẽ được cache sau lần tải đầu tiên
- Có thể tải trước models bằng cách chạy Python script:
  ```python
  from sentence_transformers import SentenceTransformer
  from transformers import AutoTokenizer, AutoModelForSequenceClassification
  SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")
  AutoModelForSequenceClassification.from_pretrained("wonrax/phobert-base-vietnamese-sentiment")
  ```

## Cách sử dụng ứng dụng

### 1. Khởi động các services

**Bước 1: Khởi động PostgreSQL**
- Windows: Kiểm tra service PostgreSQL đang chạy trong Services
- macOS/Linux: `brew services start postgresql@16` hoặc `sudo systemctl start postgresql`

**Bước 2: Khởi động Tomcat**
- Windows: Chạy `startup.bat` trong thư mục `bin` của Tomcat
- macOS/Linux: `brew services start tomcat@10` hoặc khởi động Tomcat service

**Bước 3: Khởi động Python API Service**
```bash
cd embedding
# Windows
.venv\Scripts\activate
python embedding_api.py

# macOS/Linux
source .venv/bin/activate
python embedding_api.py
```

### 2. Truy cập ứng dụng

Mở browser và truy cập:
```
http://localhost:8080/sentiment-mvc/
```

Ứng dụng sẽ tự động chuyển đến trang Dashboard.

### 3. Phân tích cảm xúc từ khóa

1. **Nhập từ khóa**: Trong ô tìm kiếm trên Dashboard, nhập từ khóa bạn muốn phân tích (ví dụ: "VinFast VF9", "Bitcoin", "AI")
2. **Click "Phân tích cảm xúc"**: Hệ thống sẽ:
   - Hiển thị loading indicator
   - Tìm kiếm job tương tự trong database (dựa trên embedding)
   - Nếu không tìm thấy job tương tự, tạo job mới và:
     - Crawl top 10 bài viết từ Google News RSS
     - Phân tích cảm xúc từng bài viết bằng Flask API
     - Cập nhật kết quả vào database
3. **Xem kết quả**: Sau khi hoàn thành, bạn sẽ thấy:
   - **Biểu đồ tròn**: Tỷ lệ cảm xúc (Tích cực, Tiêu cực, Trung lập)
   - **Bảng bài viết**: Danh sách 10 bài viết với:
     - Tiêu đề (click để mở link)
     - Tóm tắt
     - Badge cảm xúc (màu xanh = Tích cực, đỏ = Tiêu cực, cam = Trung lập)

### 4. Tính năng

- **Tự động tìm job tương tự**: Nếu từ khóa đã được phân tích trước đó (similarity >= 85%), hệ thống sẽ tái sử dụng kết quả cũ
- **Real-time updates**: Kết quả được cập nhật real-time qua AJAX, không cần reload trang
- **Background processing**: Job được xử lý nền, bạn có thể tiếp tục sử dụng ứng dụng

### 5. Trạng thái job

Trên Dashboard, bạn sẽ thấy:
- **Trạng thái xử lý**: Badge hiển thị trạng thái hiện tại:
  - 🟦 **Đang chờ** (QUEUED): Job đã được tạo, đang chờ xử lý
  - 🔵 **Đang xử lý** (RUNNING): Đang crawl và phân tích
  - 🟢 **Hoàn thành** (DONE): Đã hoàn tất
  - 🔴 **Thất bại** (FAILED): Có lỗi xảy ra
- **Progress bar**: Hiển thị tiến độ xử lý (0-100%)

## Xem log ứng dụng

### Vị trí log file

Log file được tự động tạo tại:
- **macOS/Linux**: `~/sentiment-mvc-logs/sentiment-mvc.log` hoặc `{project_dir}/logs/sentiment-mvc.log`
- **Windows**: `C:\Users\{username}\sentiment-mvc-logs\sentiment-mvc.log` hoặc `{project_dir}\logs\sentiment-mvc.log`

### Cách xem log

#### Windows

**1. Xem log bằng Notepad:**
```cmd
notepad %USERPROFILE%\sentiment-mvc-logs\sentiment-mvc.log
```

**2. Xem log bằng PowerShell (real-time):**
```powershell
Get-Content %USERPROFILE%\sentiment-mvc-logs\sentiment-mvc.log -Wait -Tail 50
```

**3. Xem log bằng Command Prompt:**
```cmd
type %USERPROFILE%\sentiment-mvc-logs\sentiment-mvc.log
```

**4. Tìm kiếm trong log:**
```cmd
findstr /i "error" %USERPROFILE%\sentiment-mvc-logs\sentiment-mvc.log
findstr /i "WorkerThread" %USERPROFILE%\sentiment-mvc-logs\sentiment-mvc.log
```

#### macOS/Linux

**1. Xem toàn bộ log:**
```bash
cat ~/sentiment-mvc-logs/sentiment-mvc.log
```

**2. Xem log real-time (tail -f):**
```bash
tail -f ~/sentiment-mvc-logs/sentiment-mvc.log
```

**3. Xem 50 dòng cuối:**
```bash
tail -n 50 ~/sentiment-mvc-logs/sentiment-mvc.log
```

**4. Tìm kiếm trong log:**
```bash
grep -i "error" ~/sentiment-mvc-logs/sentiment-mvc.log
grep -i "WorkerThread" ~/sentiment-mvc-logs/sentiment-mvc.log
```

**5. Xem log với màu sắc (nếu có `grc`):**
```bash
tail -f ~/sentiment-mvc-logs/sentiment-mvc.log | grep --color=always -E "ERROR|WARN|INFO|DEBUG"
```

### Log levels

Log được ghi với các mức độ:
- **INFO**: Thông tin hoạt động bình thường
- **WARN**: Cảnh báo (không nghiêm trọng)
- **ERROR**: Lỗi cần chú ý
- **DEBUG**: Thông tin debug (chi tiết)

### Ví dụ log entries

```
[2025-11-08 16:30:15] [INFO] [WorkerThread] Worker thread started: SentimentWorker-1
[2025-11-08 16:30:20] [INFO] [WorkerThread] Worker started job: VinFast VF9 (#123)
[2025-11-08 16:30:25] [INFO] [CrawlService] Fetched 10 articles for: VinFast VF9
[2025-11-08 16:30:30] [INFO] [SentimentService] Sentiment stats -> positive: 60.00% negative: 20.00% neutral: 20.00%
[2025-11-08 16:30:35] [INFO] [WorkerThread] ✅ Job VinFast VF9 completed (10 articles)
```

### Xem log Tomcat (nếu cần)

**Windows:**
```cmd
notepad "C:\Program Files\Apache\apache-tomcat-10.1.x\logs\catalina.YYYY-MM-DD.log"
```

**macOS/Linux:**
```bash
tail -f /opt/homebrew/var/log/tomcat@10/catalina.$(date +%F).log
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

### Backend (Java)
- Java 17
- Jakarta Servlet API 6.0
- Jakarta JSP API 3.1
- JSTL 3.0
- HikariCP 5.0
- PostgreSQL JDBC Driver 42.x
- pgvector extension (PostgreSQL)
- Gson 2.10
- org.json 20231013
- Jsoup 1.17.2 (HTML parsing)
- JUnit 5

### ML Service (Python)
- Python 3.10+
- Flask 3.1.2
- PyTorch 2.9.0
- transformers 4.57.1
- sentence-transformers 5.1.2
- scikit-learn 1.7.2
- numpy 2.3.4

### Models (Hugging Face)
- **Embedding**: `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` (~420 MB)
- **Sentiment**: `wonrax/phobert-base-vietnamese-sentiment` (~450 MB)

## License

TODO: Add license information

