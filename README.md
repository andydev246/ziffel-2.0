# 🧠 From Conversations to Validation:
### A Dynamic FSM Framework for Testing Emotional Intelligence in AI

**By Andres de Vivanco**

---

## 🎯 The Problem

Modern LLMs can generate coherent text — but can they show **empathy**?

Can they detect when someone is burned out or lost? Can they follow up with the right **pivot question** — one that nudges the user toward clarity?

This is more than NLP. This is about testing **emotional intelligence**.

As an SDET, I wanted a system that doesn't just test grammar or syntax, but can assess whether an AI:
- Understands user **tone**
- Responds with **appropriate empathy**
- Follows up with the right **pivot questions**

---

## 🛠️ Enter: Ziffel 2.0

**Ziffel 2.0** is a dynamic framework that:
- Scrapes emotional Reddit threads
- Classifies user turns semantically using **local Sentence Transformers**
- Extracts **intents**, **orientations**, **tones**, and **pivot questions**
- Builds and **walks a Finite State Machine (FSM)** in real-time
- Validates every AI response against a structured **Oracle**

---

## 🧱 Architecture Overview


---

### 1. 🧵 Reddit Thread Fetching

Given only the name of a subreddit (`r/DecidingToBeBetter`), we use:

- **`RedditMarkdownFetcher`** – Downloads conversation threads
- **`MarkdownParser`** – Parses these into structured dialog turns between a user and a "therapist" or responder.

---

### 2. 🧠 Intent Detection via Local Embeddings

No OpenAI used here. We use **Sentence Transformers** (via HuggingFace) locally to embed each utterance, then:

- Reduce embeddings via **UMAP**
- Cluster them with **KMeans or DBSCAN**
- Assign inferred **intent IDs** per cluster (e.g. `user.burnout`, `user.fear`)

This allows us to build the FSM semantically — **no pre-labeled data needed**.

---

### 3. 🎭 Orientation, Tone & Pivot Extraction

Each turn is classified by:
- **Orientation:** `practical`, `emotional`, `spiritual`, `mixed`
- **Tone:** `empathetic`, `encouraging`, `reflective`, etc.
- **Pivot Question:** Identified by looking for question marks and analyzing context.

---

### 4. 🔄 FSM & Oracle Generation

The FSM represents conversation flow. The Oracle defines what we expect.

Here’s a **complex FSM** dynamically generated from Reddit:

---

### ✅ FSM Example: Multi-Path Emotional Branching

```json
{
  "startState": "Neutral",
  "states": {
    "Neutral": {
      "transitions": {
        "t0": {
          "prompt": "I just feel stuck, like I’m not making progress in life.",
          "intent": "user.stuck",
          "orientation": "mixed",
          "expectedContains": "That can be tough to deal with",
          "expectedTone": "empathetic",
          "expectedPivot": "what's holding you back?",
          "next": ["ExploreFear", "SeekMotivation", "DiscussRoutine"]
        }
      }
    },
    "ExploreFear": {
      "transitions": {
        "t1": {
          "prompt": "I think I’m afraid of failing again.",
          "intent": "user.fear",
          "orientation": "emotional",
          "expectedContains": "Fear of failure is very common",
          "expectedTone": "reassuring",
          "expectedPivot": "where do you think that fear comes from?",
          "next": "Exit"
        }
      }
    },
    "SeekMotivation": {
      "transitions": {
        "t2": {
          "prompt": "I just need something to get me moving.",
          "intent": "user.unmotivated",
          "orientation": "action-oriented",
          "expectedContains": "Let's try identifying small wins",
          "expectedTone": "encouraging",
          "expectedPivot": "what’s one thing you could do today?",
          "next": "Exit"
        }
      }
    },
    "DiscussRoutine": {
      "transitions": {
        "t3": {
          "prompt": "My days feel chaotic. I need more structure.",
          "intent": "user.needsStructure",
          "orientation": "practical",
          "expectedContains": "Structure can really help with consistency",
          "expectedTone": "practical",
          "expectedPivot": "have you tried scheduling small habits?",
          "next": "Exit"
        }
      }
    },
    "Exit": {
      "transitions": {}
    }
  }
}
```

### 🧪 Oracle (Validation Layer)

```json
{
  "Neutral": {
    "t0": {
      "expectedContains": "That can be tough to deal with",
      "expectedTone": "empathetic",
      "expectedPivot": "what's holding you back?"
    }
  },
  "ExploreFear": {
    "t1": {
      "expectedContains": "Fear of failure is very common",
      "expectedTone": "reassuring",
      "expectedPivot": "where do you think that fear comes from?"
    }
  },
  "SeekMotivation": {
    "t2": {
      "expectedContains": "Let's try identifying small wins",
      "expectedTone": "encouraging",
      "expectedPivot": "what’s one thing you could do today?"
    }
  },
  "DiscussRoutine": {
    "t3": {
      "expectedContains": "Structure can really help with consistency",
      "expectedTone": "practical",
      "expectedPivot": "have you tried scheduling small habits?"
    }
  }
}
```
---

### 5 🧪 How We Validate

Every step of the FSM is tested in real time:
- Prompt is sent to OpenAI or another LLM
- Response is checked for:
  - `expectedContains`
  - `expectedTone` (via lightweight sentiment model)
  - `expectedPivot` (string matching or fuzzy matching)
- If correct → walk to next state
- If incorrect → fail and log

---

### 6 🔁 Progressive Execution

The tool expands the FSM and Oracle dynamically:
1. Start with first N dialog turns from Reddit
2. Build FSM + Oracle
3. Run test runner to validate
4. Continue reading next N turns
5. Expand FSM and Oracle
6. Resume walk from last known state

This makes it capable of running against entire threads over time — like an infinite test crawl of emotional intelligence.

---

### 7. Why This Matters

✅ No human labeling required

✅ Fully dynamic and evolving

✅ Tests tone, empathy, and conversation flow

✅ Uses real human data

✅ Independent of the LLM being tested

---

### 🧬 Origins

Ziffel 2.0 is inspired by Ziffel, the model-based testing framework I wrote during my internship at Microsoft in 2000, under the mentorship of Harry Robinson.

This new version takes that legacy forward — into the realm of AI and human emotions.

---

### 🚀 What’s Next

I'm preparing to release the tool as open source. If you care about AI alignment, emotional UX, or human-centered QA, I'd love to collaborate.



