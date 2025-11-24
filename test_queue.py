import requests
import time

URL = "http://localhost:8080/sentiment-mvc/jobs/create"

keywords = [
    "điện thoại",
    "cà phê",
    "trí tuệ nhân tạo",
    "học máy",
    "bóng đá",
    "thời trang",
    "âm nhạc",
    "du lịch",
    "mua sắm",
    "công nghệ"
]

print("=== BẮT ĐẦU GỬI 10 JOB ===")

for i, kw in enumerate(keywords, 1):
    print(f"[{i}/10] Gửi job với keyword: {kw}")
    
    resp = requests.post(URL, data={"keyword": kw})
    print("→ Status:", resp.status_code)
    print("→ Body:", resp.text)
    print("-" * 40)

    time.sleep(0.3)

print("=== HOÀN TẤT GỬI JOB ===")
