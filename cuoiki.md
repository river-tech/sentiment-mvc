Chức năng chính của hệ thống sentiment-mvc
Phân tích cảm xúc (sentiment analysis) tin tức theo từ khóa
Người dùng nhập một từ khóa trên giao diện dashboard.jsp, gửi request tạo job mới.
Hệ thống sẽ crawl tối đa 10 bài báo liên quan từ Google News RSS (CrawlService.fetchArticles()), lưu vào DB.
Xử lý từ khóa và tái sử dụng kết quả cũ bằng semantic search
KeywordService.processKeyword() gọi Flask API /embed để lấy embedding cho từ khóa.
Dùng pgvector + cosine similarity tìm job cũ tương tự trong DB; nếu độ tương đồng ≥ 85% thì tái sử dụng job cũ, không crawl/phân tích lại.
Nếu không có job đủ giống, hệ thống tạo job mới và đưa jobId vào hàng đợi JobQueue.
Xử lý nền (background) khối lượng lớn bằng worker thread
WorkerThread chạy nền, liên tục lấy jobId từ JobQueue.
Cập nhật trạng thái job (RUNNING → DONE/FAILED), xóa dữ liệu cũ, crawl bài viết mới, phân tích cảm xúc từng bài với Flask API /sentiment (SentimentService), lưu bài và kết quả vào DB, tính tỷ lệ positive/negative/neutral cho job.
Giao diện web + cập nhật real-time
Controller JobServlet / DashboardServlet điều phối request, trả JSON status cho AJAX.
Frontend polling /jobs/status?id={jobId} mỗi ~1.5s để cập nhật progress bar, status badge, biểu đồ, bảng bài viết theo thời gian thực.
Kết nối cơ sở dữ liệu với connection pool
Sử dụng PostgreSQL + pgvector, quản lý kết nối bằng HikariCP (DatabaseConfig).
Toàn bộ truy vấn qua lớp DAO (JobDAO, JobArticleDAO), có HealthServlet để kiểm tra kết nối DB.

Ý tưởng chính:
Request HTTP chỉ làm việc nhẹ: nhận keyword, tạo/tìm Job rồi đưa jobId vào hàng đợi (JobQueue) để xử lý nền.
Xử lý nặng (crawl báo, gọi API Flask, phân tích sentiment…) chạy trong WorkerThread ở background, không chặn request.
Client dùng AJAX/polling gọi /jobs/status?id=... để cập nhật tiến độ, nên không có request nào phải chờ quá lâu → tránh timeout.
[1 tool called]

### Cách tránh “request timeout” với xử lý lâu trong bài của bạn

**Ý tưởng chính**:  
- Request HTTP chỉ làm việc nhẹ: nhận keyword, tạo/tìm `Job` rồi **đưa `jobId` vào hàng đợi** (`JobQueue`) để xử lý nền.  
- **Xử lý nặng** (crawl báo, gọi API Flask, phân tích sentiment…) chạy trong `WorkerThread` ở background, không chặn request.  
- Client dùng AJAX/polling gọi `/jobs/status?id=...` để cập nhật tiến độ, nên không có request nào phải chờ quá lâu → tránh timeout.

**Đoạn code thể hiện việc đẩy job sang xử lý nền (tối đa 10 dòng):**

```121:130:src/main/java/com/team/app/controller/JobServlet.java
            if ("QUEUED".equals(job.getStatus())) {
                Logger.info("   Submit job vào JobQueue để xử lý background");
                JobQueue.getInstance().submit(job.getId());
                Logger.info("      Job ID " + job.getId() + " đã được thêm vào queue");
            } else {
                Logger.info("   ℹ Job status: " + job.getStatus() + " - không cần submit vào queue");
                Logger.info("    Đây là job tương tự (similar) - articles sẽ được lấy từ DB");
            }

            if (isAjax(request)) {
```