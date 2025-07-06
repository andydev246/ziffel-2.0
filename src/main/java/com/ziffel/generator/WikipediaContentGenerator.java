package com.ziffel.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.*;
import java.util.regex.Pattern;

public class WikipediaContentGenerator {
    
    private static final String WIKIPEDIA_API_BASE = "https://en.wikipedia.org/api/rest_v1";
    private static final String WIKIPEDIA_SEARCH_API = "https://en.wikipedia.org/w/api.php";
    private final ObjectMapper objectMapper;
    
    public WikipediaContentGenerator() {
        this.objectMapper = new ObjectMapper();
    }
    
    public static class WikiTopic {
        public String title;
        public String summary;
        public List<String> relatedTopics;
        public Map<String, List<String>> questionTemplates;
        public Map<String, List<String>> answerTemplates;
        
        public WikiTopic(String title, String summary) {
            this.title = title;
            this.summary = summary;
            this.relatedTopics = new ArrayList<>();
            this.questionTemplates = new HashMap<>();
            this.answerTemplates = new HashMap<>();
        }
    }
    
    public static class DomainConfig {
        public String domain;
        public List<String> seedTopics;
        public List<String> questionTypes;
        public Map<String, String> intentMappings;
        
        public DomainConfig(String domain) {
            this.domain = domain;
            this.seedTopics = new ArrayList<>();
            this.questionTypes = new ArrayList<>();
            this.intentMappings = new HashMap<>();
        }
    }
    
    public List<WikiTopic> searchAndExtractTopics(String query, int maxResults) throws Exception {
        List<WikiTopic> topics = new ArrayList<>();
        
        // Search Wikipedia for the query
        Response searchResponse = RestAssured
            .given()
            .param("action", "query")
            .param("format", "json")
            .param("list", "search")
            .param("srsearch", query)
            .param("srlimit", maxResults)
            .get(WIKIPEDIA_SEARCH_API);
        
        JsonNode searchResults = objectMapper.readTree(searchResponse.asString());
        JsonNode pages = searchResults.path("query").path("search");
        
        for (JsonNode page : pages) {
            String title = page.get("title").asText();
            String snippet = page.get("snippet").asText();
            
            // Clean up HTML tags from snippet
            String cleanSnippet = snippet.replaceAll("<[^>]*>", "");
            
            WikiTopic topic = new WikiTopic(title, cleanSnippet);
            
            // Get full article summary
            String summary = getArticleSummary(title);
            if (summary != null) {
                topic.summary = summary;
            }
            
            // Extract related topics from the summary
            topic.relatedTopics = extractRelatedTopics(summary);
            
            // Generate question and answer templates
            generateTemplatesForTopic(topic);
            
            topics.add(topic);
        }
        
        return topics;
    }
    
    private String getArticleSummary(String title) throws Exception {
        try {
            Response response = RestAssured
                .given()
                .get(WIKIPEDIA_API_BASE + "/page/summary/" + title.replace(" ", "_"));
            
            JsonNode article = objectMapper.readTree(response.asString());
            return article.get("extract").asText();
        } catch (Exception e) {
            return null;
        }
    }
    
    private List<String> extractRelatedTopics(String summary) {
        List<String> topics = new ArrayList<>();
        
        // Extract capitalized phrases that might be topics
        Pattern pattern = Pattern.compile("\\b[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b");
        java.util.regex.Matcher matcher = pattern.matcher(summary);
        
        while (matcher.find()) {
            String topic = matcher.group();
            if (topic.length() > 3 && !topics.contains(topic)) {
                topics.add(topic);
            }
        }
        
        return topics.subList(0, Math.min(topics.size(), 5)); // Limit to 5 topics
    }
    
    private void generateTemplatesForTopic(WikiTopic topic) {
        // Generate question templates based on topic type
        List<String> whatQuestions = Arrays.asList(
            "What is " + topic.title + "?",
            "Can you explain " + topic.title + "?",
            "Tell me about " + topic.title,
            "I want to know more about " + topic.title
        );
        
        List<String> howQuestions = Arrays.asList(
            "How does " + topic.title + " work?",
            "How can I use " + topic.title + "?",
            "How do I get started with " + topic.title + "?",
            "How is " + topic.title + " implemented?"
        );
        
        List<String> whyQuestions = Arrays.asList(
            "Why is " + topic.title + " important?",
            "Why should I care about " + topic.title + "?",
            "Why does " + topic.title + " matter?",
            "Why was " + topic.title + " created?"
        );
        
        topic.questionTemplates.put("what", whatQuestions);
        topic.questionTemplates.put("how", howQuestions);
        topic.questionTemplates.put("why", whyQuestions);
        
        // Generate answer templates
        List<String> definitionAnswers = Arrays.asList(
            topic.title + " is " + extractDefinition(topic.summary),
            "Let me explain " + topic.title + ": " + extractDefinition(topic.summary),
            topic.title + " refers to " + extractDefinition(topic.summary)
        );
        
        List<String> benefitAnswers = Arrays.asList(
            "The main benefits of " + topic.title + " include " + extractBenefits(topic.summary),
            topic.title + " provides several advantages: " + extractBenefits(topic.summary),
            "You should consider " + topic.title + " because " + extractBenefits(topic.summary)
        );
        
        topic.answerTemplates.put("definition", definitionAnswers);
        topic.answerTemplates.put("benefits", benefitAnswers);
    }
    
    private String extractDefinition(String summary) {
        // Simple extraction of definition from summary
        String[] sentences = summary.split("\\.");
        if (sentences.length > 0) {
            return sentences[0].trim();
        }
        return "a concept or technology";
    }
    
    private String extractBenefits(String summary) {
        // Extract potential benefits from summary
        String[] sentences = summary.split("\\.");
        if (sentences.length > 1) {
            return sentences[1].trim();
        }
        return "various advantages and capabilities";
    }
    
    public String generateDomainSpecificFsm(List<WikiTopic> topics, DomainConfig config) throws Exception {
        StringBuilder fsmJson = new StringBuilder();
        fsmJson.append("{\n");
        fsmJson.append("  \"startState\": \"").append(topics.size() > 0 ? topics.get(0).title.replace(" ", "") + "State" : "End").append("\",\n");
        fsmJson.append("  \"states\": {\n");
        
        // Generate states based on topics
        for (int i = 0; i < topics.size(); i++) {
            WikiTopic topic = topics.get(i);
            fsmJson.append("    \"").append(topic.title.replace(" ", "")).append("State\": {\n");
            fsmJson.append("      \"transitions\": {\n");
            
            // Generate transitions for each question type
            for (String questionType : config.questionTypes) {
                if (topic.questionTemplates.containsKey(questionType)) {
                    fsmJson.append("        \"").append(questionType).append("Question\": {\n");
                    // Make intent unique per topic to match oracle
                    String topicKey = topic.title.toLowerCase().replace(" ", "_");
                    String intent = topicKey + "." + config.intentMappings.get(questionType);
                    fsmJson.append("          \"intent\": \"").append(intent).append("\",\n");
                    fsmJson.append("          \"expectedContains\": \"").append(topic.title.toLowerCase()).append("\",\n");
                    fsmJson.append("          \"rlTemplate\": {\n");
                    // Use only the first question to avoid duplicate paths
                    List<String> questions = topic.questionTemplates.get(questionType);
                    String firstQuestion = questions.isEmpty() ? "Tell me about " + topic.title : questions.get(0);
                    fsmJson.append("            \"question\": [\"").append(firstQuestion).append("\"]\n");
                    fsmJson.append("          },\n");
                    
                    // Determine next state
                    if (i < topics.size() - 1) {
                        fsmJson.append("          \"next\": \"").append(topics.get(i + 1).title.replace(" ", "")).append("State\"\n");
                    } else {
                        fsmJson.append("          \"next\": \"End\"\n");
                    }
                    fsmJson.append("        }");
                    // Add comma if this is not the last question type for this topic
                    if (!questionType.equals(config.questionTypes.get(config.questionTypes.size() - 1))) {
                        fsmJson.append(",");
                    }
                    fsmJson.append("\n");
                }
            }
            
            fsmJson.append("      }\n");
            fsmJson.append("    }");
            // Always add comma after topic states since "End" state follows
            fsmJson.append(",");
            fsmJson.append("\n");
        }
        
        fsmJson.append("    \"End\": {\n");
        fsmJson.append("      \"transitions\": {}\n");
        fsmJson.append("    }\n");
        fsmJson.append("  }\n");
        fsmJson.append("}");
        
        return fsmJson.toString();
    }
    
    public Map<String, Object> generateOracleFromTopics(List<WikiTopic> topics, DomainConfig config) {
        Map<String, Object> oracle = new HashMap<>();
        
        for (WikiTopic topic : topics) {
            for (String questionType : config.questionTypes) {
                String baseIntent = config.intentMappings.get(questionType);
                if (baseIntent != null && topic.answerTemplates.containsKey(getAnswerType(questionType))) {
                    // Make intent unique per topic to avoid overwriting
                    String topicKey = topic.title.toLowerCase().replace(" ", "_");
                    String intent = topicKey + "." + baseIntent;
                    
                    Map<String, Object> rule = new HashMap<>();
                    rule.put("expectedContains", topic.answerTemplates.get(getAnswerType(questionType)));
                    rule.put("expectedRegex", ".*" + topic.title.toLowerCase() + ".*");
                    oracle.put(intent, rule);
                }
            }
        }
        
        return oracle;
    }
    
    private String getAnswerType(String questionType) {
        switch (questionType) {
            case "what": return "definition";
            case "how": return "benefits";
            case "why": return "benefits";
            default: return "definition";
        }
    }
    
    public void saveOracleToFile(Map<String, Object> oracle, String filePath) throws Exception {
        String oracleJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(oracle);
        java.nio.file.Files.write(java.nio.file.Paths.get(filePath), oracleJson.getBytes());
    }
} 