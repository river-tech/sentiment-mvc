from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer

app = Flask(__name__)
model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")

@app.route("/embed", methods=["POST"])
def embed():
    data = request.get_json()
    keyword = data["keyword"]
    emb = model.encode(keyword).tolist()
    return jsonify({"embedding": emb})

if __name__ == "__main__":
    app.run(port=9696)
