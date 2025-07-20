# intent_clusterer.py
from sentence_transformers import SentenceTransformer
from sklearn.cluster import KMeans
import json
import sys

def load_dialog_turns(path):
    with open(path, 'r', encoding='utf-8') as f:
        turns = json.load(f)
    return [turn['message'] for turn in turns if turn['depth'] == 0]  # filter top-level

def cluster_sentences(sentences, num_clusters=5):
    model = SentenceTransformer('all-MiniLM-L6-v2')
    embeddings = model.encode(sentences)

    kmeans = KMeans(n_clusters=num_clusters, random_state=42)
    labels = kmeans.fit_predict(embeddings)

    clustered = {}
    for i, sentence in enumerate(sentences):
        cluster = f"intent_{labels[i]}"
        clustered.setdefault(cluster, []).append(sentence)

    return clustered

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python intent_clusterer.py <dialog_turns.json>")
        sys.exit(1)

    path = sys.argv[1]
    sentences = load_dialog_turns(path)
    clustered = cluster_sentences(sentences)

    with open("clustered_intents.json", "w", encoding='utf-8') as out:
        json.dump(clustered, out, indent=2)
    print("✅ clustered_intents.json written")
