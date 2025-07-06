# Ziffel 2.0

**Model-Based Testing for LLMs using FSMs, RL Templates, and External Oracles**

Ziffel 2.0 is an open-source framework to test Large Language Models (LLMs) like ChatGPT through automated, reproducible, and semantically meaningful test cases. It brings model-based testing principles from traditional software testing into the world of natural language.

---

## ✨ What Makes Ziffel 2.0 Unique?

| Feature                   | Description                                                                 |
|--------------------------|-----------------------------------------------------------------------------|
| FSM-based flow control   | Define test flows as state machines with labeled transitions                 |
| Prompt generation (RL)   | Generate varied user prompts using structured DSLs                          |
| Oracle-based validation  | Validate LLM output using JSON-defined expectations                         |
| LLM regression testing   | Catch behavior drift across versions or over time                           |
| CI/CD compatible         | JUnit-driven tests with REST Assured to call OpenAI or other LLM APIs       |

---

## 📁 Project Structure

```tree
src/
├── main/
│ └── java/com/ziffel/
│ ├── generator/ # FSM and RL prompt generator
│ └── oracle/ # Oracle loading logic
├── test/
│ └── java/com/ziffel/runner/ # JUnit test runner
│
└── test/resources/
├── fsm/ # FSM definitions with RL templates
└── oracle/ # External oracle JSON by intent
```

## 🛠️ How It Works

### 1. Define FSM Flow (Manual or Auto-Generated)
In `fsm/example-fsm-rl.json`:
```json
"transitions": {
  "forgotPassword": {
    "intent": "account.reset",
    "rlTemplate": {
      "action": ["forgot", "lost"],
      "object": ["password"]
    }
  }
}
```

### 2. Define Expected Outputs (Oracle)
In `oracle/intents.json`:
```json
"account.reset": {
"expectedContains": ["reset your password", "recover access"]
}
```

### 3. Run the Tests
```bash
mvn test
```
* Prompts are generated from RL templates
* Sent to the LLM via API
* Responses are matched against oracles
* Failures indicate unexpected or degraded model behavior

## 🤖 Auto-Generation Features

### Grammar-Based FSM Generation
Generate FSMs using predefined grammar templates:

```bash
# Using CLI
java -cp target/classes com.ziffel.cli.FsmGeneratorCLI grammar fsm.json 5 3 0.4

# Using Java API
GrammarBasedFsmGenerator generator = new GrammarBasedFsmGenerator();
FsmGenerationConfig config = new FsmGenerationConfig();
config.maxStates = 5;
config.maxTransitionsPerState = 3;
String fsmJson = generator.generateFsmJson(config);
```

### Wikipedia-Based Content Generation
Generate domain-specific FSMs using Wikipedia content:

```bash
# Using CLI
java -cp target/classes com.ziffel.cli.FsmGeneratorCLI wikipedia "artificial intelligence" 3 fsm.json oracle.json

# Using Java API
WikipediaContentGenerator generator = new WikipediaContentGenerator();
List<WikiTopic> topics = generator.searchAndExtractTopics("machine learning", 5);
String fsmJson = generator.generateDomainSpecificFsm(topics, config);
Map<String, Object> oracle = generator.generateOracleFromTopics(topics, config);
```

### Hybrid Generation
Combine grammar templates with Wikipedia content:

```bash
# Using CLI
java -cp target/classes com.ziffel.cli.FsmGeneratorCLI hybrid "software testing" 2 fsm.json oracle.json
```

## 🔮 Why This Matters
LLMs like ChatGPT often respond fluently—but not always correctly. Ziffel 2.0 focuses on semantic validation by explicitly modeling the expected intent and content of responses.

## 📦 Dependencies
* Java 17+
* Maven
* JUnit 5
* Rest Assured
* Jackson (for JSON)
* Jsoup (for web scraping)
* Stanford CoreNLP (for NLP)
* Apache OpenNLP (for grammar processing)

## 🚀 Roadmap
* Support for multi-turn conversations
* LLM-as-an-oracle fallback validation
* CLI for FSM & oracle management
* LangChain/OpenAI eval integration
* Plugin for VSCode/intelliJ authoring

## 🙌 Credits
Ziffel 2.0 is inspired by the original Ziffel model-based testing tool developed at Microsoft, now reimagined for the LLM era.

## 📫 Contribute
Open an issue, fork the repo, or reach out. Help shape the future of automated LLM testing!
