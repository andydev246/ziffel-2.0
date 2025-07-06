package com.ziffel.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.*;
import java.util.regex.Pattern;

public class GrammarBasedFsmGenerator {
    
    private final Map<String, List<String>> intentTemplates;
    private final Map<String, List<String>> responseTemplates;
    private final Random random;
    
    public GrammarBasedFsmGenerator() {
        this.intentTemplates = new HashMap<>();
        this.responseTemplates = new HashMap<>();
        this.random = new Random();
        initializeGrammarTemplates();
    }
    
    private void initializeGrammarTemplates() {
        // Intent templates for different conversation types
        intentTemplates.put("greeting", Arrays.asList(
            "Hello", "Hi", "Hey", "Good morning", "Good afternoon", "Good evening"
        ));
        
        intentTemplates.put("farewell", Arrays.asList(
            "Goodbye", "Bye", "See you", "Take care", "Have a good day", "Until next time"
        ));
        
        intentTemplates.put("question", Arrays.asList(
            "What is", "How do I", "Can you tell me", "I want to know", "Could you explain"
        ));
        
        intentTemplates.put("problem", Arrays.asList(
            "I have a problem with", "There's an issue with", "I'm having trouble with",
            "Something is wrong with", "I can't seem to"
        ));
        
        intentTemplates.put("request", Arrays.asList(
            "I need", "I want", "Can you help me with", "Please help me", "I'd like to"
        ));
        
        // Response templates for different intents
        responseTemplates.put("greeting", Arrays.asList(
            "Hello! How can I help you today?", "Hi there! What can I assist you with?",
            "Greetings! How may I be of service?", "Hello! What brings you here today?"
        ));
        
        responseTemplates.put("farewell", Arrays.asList(
            "Goodbye! Have a great day!", "See you later! Take care!",
            "Bye! Feel free to return if you need more help!", "Take care! Come back anytime!"
        ));
        
        responseTemplates.put("question", Arrays.asList(
            "I'd be happy to explain that to you.", "Let me help you understand that.",
            "Here's what you need to know about that.", "I can provide you with that information."
        ));
        
        responseTemplates.put("problem", Arrays.asList(
            "I understand you're having an issue. Let me help you resolve it.",
            "I'm sorry to hear about that problem. Let's work on fixing it.",
            "That sounds frustrating. I'll help you troubleshoot this.",
            "Let me assist you with solving that problem."
        ));
        
        responseTemplates.put("request", Arrays.asList(
            "I'd be happy to help you with that.", "Let me assist you with your request.",
            "I can definitely help you with that.", "Consider it done! Let me help you."
        ));
    }
    
    public static class FsmGenerationConfig {
        public int maxStates = 5;
        public int maxTransitionsPerState = 3;
        public double conversationEndProbability = 0.3;
        public List<String> topics = new ArrayList<>();
        public Map<String, String> topicIntents = new HashMap<>();
        
        public FsmGenerationConfig() {
            // Default topics
            topics.addAll(Arrays.asList("account", "billing", "support", "product", "general"));
            
            // Map topics to intents
            topicIntents.put("account", "account.management");
            topicIntents.put("billing", "billing.inquiry");
            topicIntents.put("support", "support.request");
            topicIntents.put("product", "product.information");
            topicIntents.put("general", "general.inquiry");
        }
    }
    
    public String generateFsmJson(FsmGenerationConfig config) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode fsmNode = mapper.createObjectNode();
        
        // Generate states
        ObjectNode statesNode = mapper.createObjectNode();
        List<String> stateNames = generateStateNames(config.maxStates);
        
        for (String stateName : stateNames) {
            ObjectNode stateNode = mapper.createObjectNode();
            ObjectNode transitionsNode = mapper.createObjectNode();
            
            // Generate transitions for this state
            int numTransitions = random.nextInt(config.maxTransitionsPerState) + 1;
            for (int i = 0; i < numTransitions; i++) {
                String transitionName = generateTransitionName();
                ObjectNode transitionNode = generateTransitionNode(config, mapper);
                transitionsNode.set(transitionName, transitionNode);
            }
            
            stateNode.set("transitions", transitionsNode);
            statesNode.set(stateName, stateNode);
        }
        
        fsmNode.put("startState", stateNames.get(0));
        fsmNode.set("states", statesNode);
        
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fsmNode);
    }
    
    private List<String> generateStateNames(int maxStates) {
        List<String> states = new ArrayList<>();
        states.add("Start");
        
        String[] stateTypes = {"Question", "Problem", "Solution", "Confirmation", "FollowUp"};
        for (int i = 1; i < maxStates; i++) {
            String stateType = stateTypes[random.nextInt(stateTypes.length)];
            states.add(stateType + i);
        }
        
        states.add("End");
        return states;
    }
    
    private String generateTransitionName() {
        String[] actions = {"ask", "report", "request", "confirm", "clarify", "resolve"};
        String[] objects = {"help", "information", "support", "details", "assistance"};
        
        String action = actions[random.nextInt(actions.length)];
        String object = objects[random.nextInt(objects.length)];
        
        return action + object.charAt(0) + object.substring(1);
    }
    
    private ObjectNode generateTransitionNode(FsmGenerationConfig config, ObjectMapper mapper) {
        ObjectNode transitionNode = mapper.createObjectNode();
        
        // Select a random topic and intent
        String topic = config.topics.get(random.nextInt(config.topics.size()));
        String intent = config.topicIntents.get(topic);
        
        transitionNode.put("intent", intent);
        
        // Generate RL template
        ObjectNode rlTemplate = mapper.createObjectNode();
        ArrayNode prefixArray = mapper.createArrayNode();
        ArrayNode actionArray = mapper.createArrayNode();
        ArrayNode objectArray = mapper.createArrayNode();
        
        // Add varied prefixes
        prefixArray.add("");
        prefixArray.add("Hey");
        prefixArray.add("Hi");
        
        // Add topic-specific actions
        if (topic.equals("account")) {
            actionArray.add("forgot");
            actionArray.add("lost");
            actionArray.add("need to reset");
            objectArray.add("my password");
            objectArray.add("my account");
        } else if (topic.equals("billing")) {
            actionArray.add("have a question about");
            actionArray.add("need help with");
            actionArray.add("problem with");
            objectArray.add("my bill");
            objectArray.add("my payment");
        } else {
            actionArray.add("need");
            actionArray.add("want");
            actionArray.add("looking for");
            objectArray.add("help");
            objectArray.add("information");
        }
        
        rlTemplate.set("prefix", prefixArray);
        rlTemplate.set("action", actionArray);
        rlTemplate.set("object", objectArray);
        
        transitionNode.set("rlTemplate", rlTemplate);
        
        // Generate expected content
        String expectedContent = generateExpectedContent(intent);
        transitionNode.put("expectedContains", expectedContent);
        
        // Determine next state
        if (random.nextDouble() < config.conversationEndProbability) {
            transitionNode.put("next", "End");
        } else {
            String[] nextStates = {"Question1", "Problem1", "Solution1", "Confirmation1"};
            transitionNode.put("next", nextStates[random.nextInt(nextStates.length)]);
        }
        
        return transitionNode;
    }
    
    private String generateExpectedContent(String intent) {
        if (intent.startsWith("account")) {
            return "account management";
        } else if (intent.startsWith("billing")) {
            return "billing information";
        } else if (intent.startsWith("support")) {
            return "support assistance";
        } else if (intent.startsWith("product")) {
            return "product details";
        } else {
            return "general help";
        }
    }
    
    public void saveFsmToFile(String fsmJson, String filePath) throws Exception {
        // Implementation to save FSM to file
        java.nio.file.Files.write(
            java.nio.file.Paths.get(filePath), 
            fsmJson.getBytes()
        );
    }
} 