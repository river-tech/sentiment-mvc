import logging
from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer

# ⚙️ Cấu hình logging hiển thị ra console
logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] %(levelname)s: %(message)s"
)

app = Flask(__name__)
model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")
logging.info("✅ Model loaded successfully.")

@app.route("/embed", methods=["POST"])
def embed():
    data = request.get_json()
    keyword = data.get("keyword", "").strip()
    logging.info(f"📩 Received keyword: {keyword}")

    emb = model.encode(keyword).tolist()
    logging.info(f"✅ Embedding for '{keyword}' generated successfully.")
    return jsonify({"embedding": emb})

if __name__ == "__main__":
    # ⚠️ Bắt buộc thêm "debug=False" để Flask không che log mặc định
    app.run(host="0.0.0.0", port=9696, debug=False)