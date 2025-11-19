import logging
import time
from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
from torch.nn.functional import softmax

# ======================================================
# ⚙️ Cấu hình logging
# ======================================================
logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] %(levelname)s: %(message)s"
)

# ======================================================
# 🚀 Khởi tạo Flask app
# ======================================================
app = Flask(__name__)

# ======================================================
# 🧠 Model embedding (đa ngôn ngữ, nhẹ)
# ======================================================
EMBED_MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
embed_model = SentenceTransformer(EMBED_MODEL_NAME)

# ======================================================
# 💬 Model sentiment (PhoBERT-base fine-tuned cho tiếng Việt)
# ======================================================
SENTIMENT_MODEL_NAME = "wonrax/phobert-base-vietnamese-sentiment"
DEVICE = "cuda" if torch.cuda.is_available() else ("mps" if torch.backends.mps.is_available() else "cpu")

logging.info(f"🧠 Loading sentiment model on device: {DEVICE}")
sent_tokenizer = AutoTokenizer.from_pretrained(SENTIMENT_MODEL_NAME)
sent_model = AutoModelForSequenceClassification.from_pretrained(SENTIMENT_MODEL_NAME).to(DEVICE)

# Map nhãn model → label thống nhất (cho Java backend)
LABEL_MAP = {
    "LABEL_0": "negative",
    "LABEL_1": "positive",
    "LABEL_2": "neutral"
}

# ======================================================
# Trạng thái runtime
# ======================================================
startup_time = time.time()
request_count = {"embed": 0, "sentiment": 0}

logging.info(f"✅ Loaded models:\n - Embed: {EMBED_MODEL_NAME}\n - Sentiment: {SENTIMENT_MODEL_NAME}")


# ======================================================
# 1️⃣ API: tạo embedding từ keyword
# ======================================================
@app.route("/embed", methods=["POST"])
def embed():
    start_time = time.time()
    request_count["embed"] += 1
    data = request.get_json(force=True)
    keyword = data.get("keyword", "").strip()
    if not keyword:
        return jsonify({"error": "Missing 'keyword'"}), 400

    logging.info(f"📩 /embed keyword: {keyword}")
    emb = embed_model.encode(keyword).tolist()
    response = {
        "keyword": keyword,
        "embedding": emb,
        "dim": len(emb),
        "model": EMBED_MODEL_NAME
    }
    elapsed = (time.time() - start_time) * 1000
    logging.info(f"⏱️ /embed response time: {elapsed:.2f} ms")
    return jsonify(response)


# ======================================================
# 2️⃣ API: phân tích cảm xúc văn bản dài
# ======================================================
@app.route("/sentiment", methods=["POST"])
def analyze_sentiment():
    start_time = time.time()
    request_count["sentiment"] += 1
    data = request.get_json(force=True)
    text = data.get("text", "").strip()

    if not text:
        return jsonify({"error": "Missing 'text'"}), 400

    logging.info(f"🧠 /sentiment text: {text[:80]}...")

    # Tokenize + predict
    inputs = sent_tokenizer(
        text,
        return_tensors="pt",
        truncation=True,
        padding=True,
        max_length=256
    ).to(DEVICE)

    with torch.no_grad():
        logits = sent_model(**inputs).logits
        probs = softmax(logits, dim=-1)[0].cpu().numpy()

    # Xác suất từng lớp
    neg = float(probs[0])
    pos = float(probs[1])
    neu = float(probs[2])

    # ✅ Quy tắc custom theo confidence
    if pos > 0.7:
        label = "positive"
        confidence = pos
    elif neg > 0.7:
        label = "negative"
        confidence = neg
    else:
        label = "neutral"
        confidence = max(neu, pos, neg)  # confidence lấy max lớp hiện tại

    result = {
        "label": label,
        "confidence": round(confidence, 4),
    }

    logging.info(f"✅ Custom Sentiment result: {result}")
    elapsed = (time.time() - start_time) * 1000
    logging.info(f"⏱️ /sentiment response time: {elapsed:.2f} ms")
    return jsonify(result)

# ======================================================
# 3️⃣ API: kiểm tra trạng thái server
# ======================================================
@app.route("/status", methods=["GET"])
def status():
    start_time = time.time()
    uptime = round(time.time() - startup_time, 2)
    resp = jsonify({
        "status": "online",
        "models": {
            "embedding": EMBED_MODEL_NAME,
            "sentiment": SENTIMENT_MODEL_NAME
        },
        "requests": request_count,
        "uptime_seconds": uptime,
        "device": DEVICE,
        "ready": True
    })
    elapsed = (time.time() - start_time) * 1000
    logging.info(f"⏱️ /status response time: {elapsed:.2f} ms")
    return resp


# ======================================================
# ⚙️ Run Flask server
# ======================================================
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=9697, debug=False)