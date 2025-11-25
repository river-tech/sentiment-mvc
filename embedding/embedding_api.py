import logging
import time
from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
from torch.nn.functional import softmax

logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] %(levelname)s: %(message)s"
)

app = Flask(__name__)

EMBED_MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
embed_model = SentenceTransformer(EMBED_MODEL_NAME)

SENTIMENT_MODEL_NAME = "wonrax/phobert-base-vietnamese-sentiment"
DEVICE = "cuda" if torch.cuda.is_available() else ("mps" if torch.backends.mps.is_available() else "cpu")

logging.info(f"🧠 Loading sentiment model on device: {DEVICE}")
sent_tokenizer = AutoTokenizer.from_pretrained(SENTIMENT_MODEL_NAME)
sent_model = AutoModelForSequenceClassification.from_pretrained(SENTIMENT_MODEL_NAME).to(DEVICE)

LABEL_MAP = {
    "LABEL_0": "negative",
    "LABEL_1": "positive",
    "LABEL_2": "neutral"
}

startup_time = time.time()
request_count = {"embed": 0, "sentiment": 0}

logging.info(f" Loaded models:\n - Embed: {EMBED_MODEL_NAME}\n - Sentiment: {SENTIMENT_MODEL_NAME}")


@app.route("/embed", methods=["POST"])
def embed():
    start_time = time.time()
    request_count["embed"] += 1
    data = request.get_json(force=True)
    keyword = data.get("keyword", "").strip()
    if not keyword:
        return jsonify({"error": "Missing 'keyword'"}), 400

    logging.info(f" /embed keyword: {keyword}")
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

@app.route("/sentiment", methods=["POST"])
def analyze_sentiment():
    start_time = time.time()
    request_count["sentiment"] += 1
    data = request.get_json(force=True)
    text = data.get("text", "").strip()

    if not text:
        return jsonify({"error": "Missing 'text'"}), 400

    logging.info(f" /sentiment text: {text[:80]}...")

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

    neg = float(probs[0])
    pos = float(probs[1])
    neu = float(probs[2])

    if pos > 0.7:
        label = "positive"
        confidence = pos
    elif neg > 0.7:
        label = "negative"
        confidence = neg
    else:
        label = "neutral"
        confidence = max(neu, pos, neg)  

    result = {
        "label": label,
        "confidence": round(confidence, 4),
    }

    logging.info(f" Custom Sentiment result: {result}")
    elapsed = (time.time() - start_time) * 1000
    logging.info(f"sentiment response time: {elapsed:.2f} ms")
    return jsonify(result)

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
    logging.info(f"status response time: {elapsed:.2f} ms")
    return resp


if __name__ == "__main__":
    host = "0.0.0.0"
    port = 9697
    logging.info(f" Starting Flask embedding API on http://{host}:{port} (use_reloader=False)")
    try:
        app.run(host=host, port=port, debug=False, use_reloader=False)
    except OSError as e:
        logging.error(f"Failed to start Flask server on {host}:{port} - {e}")
        logging.error("If you see 'Address already in use' or 'Permission denied', check if the port is free and try another port.")
        logging.error("To run: `python embedding/embedding_api.py` or `python -m embedding.embedding_api` from project root.")
        raise