#!/bin/zsh
# 🚀 Script tự động chạy Flask API cho embedding service

# Di chuyển đến thư mục script
cd "$(dirname "$0")"

# Kích hoạt môi trường ảo
source venv/bin/activate

# Chạy Flask API
echo "Starting Embedding API on http://127.0.0.1:9697 ..."
python3 embedding_api.py
