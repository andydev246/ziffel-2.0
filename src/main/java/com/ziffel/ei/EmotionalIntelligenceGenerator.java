package com.ziffel.ei;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.*;
import java.util.regex.Pattern;

public class EmotionalIntelligenceGenerator {
    
    private final Map<String, List<String>> emotionTemplates;
    private final Map<String, List<String>> contextTemplates;
    private final Map<String, List<String>> responseTemplates;
    private final Random random;
    
    public EmotionalIntelligenceGenerator() {
        this.emotionTemplates = new HashMap<>();
        this.contextTemplates = new HashMap<>();
        this.responseTemplates = new HashMap<>();
        this.random = new Random();
        initializeEmotionalTemplates();
    }
    
    private void initializeEmotionalTemplates() {
        // Primary emotions and their expressions
        emotionTemplates.put("anger", Arrays.asList(
            "I'm so frustrated with this situation",
            "This is making me really angry",
            "I can't believe this is happening",
            "I'm furious about this",
            "This is unacceptable"
        ));
        
        emotionTemplates.put("sadness", Arrays.asList(
            "I'm feeling really down today",
            "I'm so sad about what happened",
            "I feel like crying",
            "I'm heartbroken",
            "I'm feeling depressed"
        ));
        
        emotionTemplates.put("fear", Arrays.asList(
            "I'm scared about what might happen",
            "I'm terrified of the consequences",
            "I'm anxious about this situation",
            "I'm worried sick",
            "I'm afraid of what comes next"
        ));
        
        emotionTemplates.put("joy", Arrays.asList(
            "I'm so happy about this!",
            "I'm thrilled with the results",
            "I'm overjoyed by this news",
            "I'm ecstatic about this",
            "I'm delighted with how things turned out"
        ));
        
        emotionTemplates.put("surprise", Arrays.asList(
            "I'm shocked by this news",
            "I can't believe what just happened",
            "This is completely unexpected",
            "I'm amazed by this",
            "I'm stunned by the results"
        ));
        
        emotionTemplates.put("disgust", Arrays.asList(
            "I'm disgusted by this behavior",
            "This is repulsive to me",
            "I find this completely unacceptable",
            "This makes me sick",
            "I'm appalled by this"
        ));
        
        // Context templates for different scenarios
        contextTemplates.put("work", Arrays.asList(
            "at my job", "with my colleagues", "in the office", "during a meeting", "with my boss"
        ));
        
        contextTemplates.put("personal", Arrays.asList(
            "with my family", "in my relationship", "with my friends", "at home", "in my personal life"
        ));
        
        contextTemplates.put("health", Arrays.asList(
            "about my health", "with my doctor", "regarding my medical condition", "about my treatment"
        ));
        
        contextTemplates.put("financial", Arrays.asList(
            "with my finances", "about my bills", "regarding my investments", "with my bank"
        ));
        
        // Expected empathetic responses
        responseTemplates.put("empathy", Arrays.asList(
            "I understand how you're feeling",
            "That sounds really difficult",
            "I can see why you'd feel that way",
            "Your feelings are completely valid",
            "I hear you and I'm here for you"
        ));
        
        responseTemplates.put("validation", Arrays.asList(
            "It's okay to feel that way",
            "Your reaction makes sense",
            "Anyone would feel the same in your situation",
            "You have every right to feel that way",
            "Your feelings are important"
        ));
        
        responseTemplates.put("support", Arrays.asList(
            "I'm here to support you",
            "You're not alone in this",
            "I want to help you through this",
            "We'll get through this together",
            "I'm on your side"
        ));
        
        responseTemplates.put("calming", Arrays.asList(
            "Let's take a deep breath together",
            "Try to stay calm",
            "Everything will be okay",
            "This feeling will pass",
            "You're stronger than you think"
        ));
    }
    
    public static class EITestScenario {
        public String emotion;
        public String context;
        public String userPrompt;
        public String expectedResponse;
        public List<String> expectedEmotionalIndicators;
        public String intent;
        
        public EITestScenario(String emotion, String context, String userPrompt, 
                            String expectedResponse, List<String> expectedEmotionalIndicators, String intent) {
            this.emotion = emotion;
            this.context = context;
            this.userPrompt = userPrompt;
            this.expectedResponse = expectedResponse;
            this.expectedEmotionalIndicators = expectedEmotionalIndicators;
            this.intent = intent;
        }
    }
    
    public static class EIGenerationConfig {
        public List<String> emotions = Arrays.asList("anger", "sadness", "fear", "joy", "surprise", "disgust");
        public List<String> contexts = Arrays.asList("work", "personal", "health", "financial");
        public List<String> responseTypes = Arrays.asList("empathy", "validation", "support", "calming");
        public int scenariosPerEmotion = 3;
        public boolean includeMixedEmotions = true;
        public boolean includeEmotionalEscalation = true;
        
        public EIGenerationConfig() {}
    }
    
    public List<EITestScenario> generateEIScenarios(EIGenerationConfig config) {
        List<EITestScenario> scenarios = new ArrayList<>();
        
        for (String emotion : config.emotions) {
            for (int i = 0; i < config.scenariosPerEmotion; i++) {
                String context = config.contexts.get(random.nextInt(config.contexts.size()));
                String responseType = config.responseTypes.get(random.nextInt(config.responseTypes.size()));
                
                EITestScenario scenario = generateSingleScenario(emotion, context, responseType);
                scenarios.add(scenario);
            }
        }
        
        // Add mixed emotion scenarios
        if (config.includeMixedEmotions) {
            scenarios.addAll(generateMixedEmotionScenarios(config));
        }
        
        // Add emotional escalation scenarios
        if (config.includeEmotionalEscalation) {
            scenarios.addAll(generateEscalationScenarios(config));
        }
        
        return scenarios;
    }
    
    private EITestScenario generateSingleScenario(String emotion, String context, String responseType) {
        // Generate user prompt
        String emotionExpression = emotionTemplates.get(emotion).get(random.nextInt(emotionTemplates.get(emotion).size()));
        String contextExpression = contextTemplates.get(context).get(random.nextInt(contextTemplates.get(context).size()));
        String userPrompt = emotionExpression + " " + contextExpression + ".";
        
        // Generate expected response
        String expectedResponse = responseTemplates.get(responseType).get(random.nextInt(responseTemplates.get(responseType).size()));
        
        // Generate emotional indicators
        List<String> emotionalIndicators = generateEmotionalIndicators(emotion, responseType);
        
        // Generate intent
        String intent = "ei." + emotion + "." + responseType;
        
        return new EITestScenario(emotion, context, userPrompt, expectedResponse, emotionalIndicators, intent);
    }
    
    private List<String> generateEmotionalIndicators(String emotion, String responseType) {
        List<String> indicators = new ArrayList<>();
        
        // Add emotion-specific indicators
        switch (emotion) {
            case "anger":
                indicators.addAll(Arrays.asList("frustrated", "angry", "furious", "upset"));
                break;
            case "sadness":
                indicators.addAll(Arrays.asList("sad", "down", "depressed", "heartbroken"));
                break;
            case "fear":
                indicators.addAll(Arrays.asList("scared", "anxious", "worried", "terrified"));
                break;
            case "joy":
                indicators.addAll(Arrays.asList("happy", "thrilled", "excited", "delighted"));
                break;
            case "surprise":
                indicators.addAll(Arrays.asList("shocked", "amazed", "surprised", "stunned"));
                break;
            case "disgust":
                indicators.addAll(Arrays.asList("disgusted", "appalled", "repulsed", "sickened"));
                break;
        }
        
        // Add response-type indicators
        switch (responseType) {
            case "empathy":
                indicators.addAll(Arrays.asList("understand", "feel", "hear"));
                break;
            case "validation":
                indicators.addAll(Arrays.asList("valid", "okay", "normal"));
                break;
            case "support":
                indicators.addAll(Arrays.asList("support", "help", "here"));
                break;
            case "calming":
                indicators.addAll(Arrays.asList("calm", "breathe", "okay"));
                break;
        }
        
        return indicators;
    }
    
    private List<EITestScenario> generateMixedEmotionScenarios(EIGenerationConfig config) {
        List<EITestScenario> scenarios = new ArrayList<>();
        
        // Generate scenarios with conflicting emotions
        String[] mixedEmotions = {
            "I'm so happy about the promotion but also scared about the new responsibilities",
            "I'm excited to see my family but sad that my friend can't come",
            "I'm relieved the test is over but anxious about the results",
            "I'm proud of my achievement but frustrated with the process"
        };
        
        for (String mixedPrompt : mixedEmotions) {
            String intent = "ei.mixed.emotions";
            String expectedResponse = "It sounds like you're experiencing mixed emotions. That's completely normal and understandable.";
            List<String> indicators = Arrays.asList("mixed", "emotions", "normal", "understandable");
            
            scenarios.add(new EITestScenario("mixed", "personal", mixedPrompt, expectedResponse, indicators, intent));
        }
        
        return scenarios;
    }
    
    private List<EITestScenario> generateEscalationScenarios(EIGenerationConfig config) {
        List<EITestScenario> scenarios = new ArrayList<>();
        
        // Generate escalating emotional scenarios
        String[] escalationPrompts = {
            "I'm getting more and more frustrated with this situation",
            "My anxiety is building up and I don't know what to do",
            "I'm becoming increasingly angry about this",
            "My sadness is getting worse and I feel hopeless"
        };
        
        for (String escalationPrompt : escalationPrompts) {
            String intent = "ei.escalation.crisis";
            String expectedResponse = "I can see this is really affecting you deeply. Let's take a moment to breathe and work through this together.";
            List<String> indicators = Arrays.asList("deeply", "breathe", "together", "moment");
            
            scenarios.add(new EITestScenario("escalation", "crisis", escalationPrompt, expectedResponse, indicators, intent));
        }
        
        return scenarios;
    }
    
    public String generateEIFsmJson(List<EITestScenario> scenarios) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode fsmNode = mapper.createObjectNode();
        
        fsmNode.put("startState", "EmotionalStart");
        
        ObjectNode statesNode = mapper.createObjectNode();
        
        // Create states for each emotion
        for (String emotion : emotionTemplates.keySet()) {
            ObjectNode stateNode = mapper.createObjectNode();
            ObjectNode transitionsNode = mapper.createObjectNode();
            
            // Find scenarios for this emotion
            List<EITestScenario> emotionScenarios = scenarios.stream()
                .filter(s -> s.emotion.equals(emotion))
                .toList();
            
            for (int i = 0; i < Math.min(3, emotionScenarios.size()); i++) {
                EITestScenario scenario = emotionScenarios.get(i);
                
                ObjectNode transitionNode = mapper.createObjectNode();
                transitionNode.put("intent", scenario.intent);
                transitionNode.put("expectedContains", scenario.expectedResponse);
                
                // Create RL template
                ObjectNode rlTemplate = mapper.createObjectNode();
                ArrayNode promptArray = mapper.createArrayNode();
                promptArray.add(scenario.userPrompt);
                rlTemplate.set("prompt", promptArray);
                transitionNode.set("rlTemplate", rlTemplate);
                
                // Determine next state
                if (random.nextDouble() < 0.3) {
                    transitionNode.put("next", "EmotionalEnd");
                } else {
                    String nextEmotion = emotionTemplates.keySet().toArray(new String[0])[random.nextInt(emotionTemplates.size())];
                    transitionNode.put("next", nextEmotion + "State");
                }
                
                transitionsNode.set("transition" + (i + 1), transitionNode);
            }
            
            stateNode.set("transitions", transitionsNode);
            statesNode.set(emotion + "State", stateNode);
        }
        
        // Add start and end states
        ObjectNode startState = mapper.createObjectNode();
        ObjectNode startTransitions = mapper.createObjectNode();
        startTransitions.set("begin", createTransitionNode("ei.start", "Let's begin our conversation", "EmotionalStart"));
        startState.set("transitions", startTransitions);
        statesNode.set("EmotionalStart", startState);
        
        ObjectNode endState = mapper.createObjectNode();
        endState.set("transitions", mapper.createObjectNode());
        statesNode.set("EmotionalEnd", endState);
        
        fsmNode.set("states", statesNode);
        
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fsmNode);
    }
    
    private ObjectNode createTransitionNode(String intent, String expected, String next) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("intent", intent);
        node.put("expectedContains", expected);
        node.put("next", next);
        return node;
    }
    
    public Map<String, Object> generateEIOracle(List<EITestScenario> scenarios) {
        Map<String, Object> oracle = new HashMap<>();
        
        for (EITestScenario scenario : scenarios) {
            Map<String, Object> rule = new HashMap<>();
            rule.put("expectedContains", scenario.expectedEmotionalIndicators);
            rule.put("expectedRegex", ".*(" + String.join("|", scenario.expectedEmotionalIndicators) + ").*");
            oracle.put(scenario.intent, rule);
        }
        
        return oracle;
    }
} 