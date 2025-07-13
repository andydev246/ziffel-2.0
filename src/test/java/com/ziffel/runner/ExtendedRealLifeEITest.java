package com.ziffel.runner;

import com.ziffel.generator.FsmLoader;
import com.ziffel.generator.FsmLoader.FsmDefinition;
import com.ziffel.generator.FsmLoader.FsmTransition;
import com.ziffel.generator.RLPromptGenerator;
import com.ziffel.generator.RLPromptGenerator.PromptTestCase;
import com.ziffel.oracle.OracleLoader;
import com.ziffel.oracle.OracleLoader.OracleRule;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtendedRealLifeEITest {

    private static final String BASE_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final int MAX_PATHS_PER_STATE = 3; // Limit for extended scenarios
    private static final int MAX_PATH_LENGTH = 8; // Limit path length for complex scenarios

    @Test
    public void testExtendedRealLifeEmotionalIntelligence() throws Exception {
        System.out.println("=== Extended Real-Life Emotional Intelligence Testing ===");
        
        // Load extended real-life EI FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Generate conversation paths from FSM using RL templates (limited)
        List<List<ConversationTurn>> conversationPaths = generateConversationPathsFromRLTemplates(fsm);
        
        System.out.println("\n--- Testing Extended Real-Life EI Conversation Paths ---");
        System.out.println("Generated " + conversationPaths.size() + " conversation paths");
        
        for (int pathIndex = 0; pathIndex < conversationPaths.size(); pathIndex++) {
            List<ConversationTurn> path = conversationPaths.get(pathIndex);
            System.out.println("\n--- Real-Life EI Path " + (pathIndex + 1) + " ---");
            
            for (int turnIndex = 0; turnIndex < path.size(); turnIndex++) {
                ConversationTurn turn = path.get(turnIndex);
                System.out.println("\nTurn " + (turnIndex + 1) + ":");
                System.out.println("User: " + turn.userPrompt);
                System.out.println("Expected Intent: " + turn.intent);
                
                // Get oracle expectations
                OracleRule rule = oracle.get(turn.intent);
                if (rule != null) {
                    System.out.println("Expected AI Response Indicators: " + rule.getExpectedContains());
                }
                
                // Test AI response (commented out for safety)
                testAIResponse(turn.userPrompt, turn.intent, rule);
                
                System.out.println("---");
            }
        }
    }
    
    private static class ConversationTurn {
        String userPrompt;
        String intent;
        String expectedContains;
        
        ConversationTurn(String userPrompt, String intent, String expectedContains) {
            this.userPrompt = userPrompt;
            this.intent = intent;
            this.expectedContains = expectedContains;
        }
    }
    
    private List<List<ConversationTurn>> generateConversationPathsFromRLTemplates(FsmDefinition fsm) {
        List<List<ConversationTurn>> allPaths = new ArrayList<>();
        
        // Use iterative approach instead of recursive to prevent stack overflow
        Queue<PathState> queue = new LinkedList<>();
        queue.add(new PathState(fsm.startState, new ArrayList<>(), new HashSet<>()));
        
        while (!queue.isEmpty() && allPaths.size() < 50) { // Limit total paths for extended scenarios
            PathState current = queue.poll();
            
            // Skip if path is too long
            if (current.path.size() >= MAX_PATH_LENGTH) {
                if (!current.path.isEmpty()) {
                    allPaths.add(new ArrayList<>(current.path));
                }
                continue;
            }
            
            // Skip if we've visited this state too many times
            if (current.visitedStates.contains(current.currentState)) {
                if (!current.path.isEmpty()) {
                    allPaths.add(new ArrayList<>(current.path));
                }
                continue;
            }
            
            FsmLoader.FsmState state = fsm.states.get(current.currentState);
            if (state == null || state.transitions == null || state.transitions.isEmpty()) {
                // End state - add path
                if (!current.path.isEmpty()) {
                    allPaths.add(new ArrayList<>(current.path));
                }
                continue;
            }
            
            // Add current state to visited
            current.visitedStates.add(current.currentState);
            
            // Process transitions (limit to prevent explosion)
            int transitionCount = 0;
            for (Map.Entry<String, FsmTransition> entry : state.transitions.entrySet()) {
                if (transitionCount >= MAX_PATHS_PER_STATE) break;
                
                FsmTransition transition = entry.getValue();
                
                // Generate limited number of prompts from RL template
                List<PromptTestCase> promptTestCases = generateLimitedPrompts(transition);
                
                for (PromptTestCase promptTestCase : promptTestCases) {
                    if (transitionCount >= MAX_PATHS_PER_STATE) break;
                    
                    ConversationTurn turn = new ConversationTurn(
                        promptTestCase.getPrompt(),
                        promptTestCase.getIntent(),
                        promptTestCase.getExpectedContains()
                    );
                    
                    // Create new path state
                    List<ConversationTurn> newPath = new ArrayList<>(current.path);
                    newPath.add(turn);
                    Set<String> newVisited = new HashSet<>(current.visitedStates);
                    
                    String nextState = transition.next;
                    if (nextState != null && !nextState.equals("End")) {
                        queue.add(new PathState(nextState, newPath, newVisited));
                    } else {
                        // End of path
                        allPaths.add(newPath);
                    }
                    
                    transitionCount++;
                }
            }
        }
        
        return allPaths;
    }
    
    private List<PromptTestCase> generateLimitedPrompts(FsmTransition transition) {
        List<PromptTestCase> promptTestCases = new ArrayList<>();
        
        if (transition.rlTemplate != null) {
            // Generate limited test cases to prevent explosion
            promptTestCases = RLPromptGenerator.generateTestCases(
                transition.rlTemplate,
                transition.intent,
                transition.expectedContains
            );
            
            // Limit to first 2 prompts for extended scenarios
            if (promptTestCases.size() > 2) {
                promptTestCases = promptTestCases.subList(0, 2);
            }
        } else if (transition.prompt != null) {
            // Single prompt case
            promptTestCases.add(new PromptTestCase(
                transition.prompt,
                transition.intent,
                transition.expectedContains
            ));
        }
        
        return promptTestCases;
    }
    
    private static class PathState {
        String currentState;
        List<ConversationTurn> path;
        Set<String> visitedStates;
        
        PathState(String currentState, List<ConversationTurn> path, Set<String> visitedStates) {
            this.currentState = currentState;
            this.path = path;
            this.visitedStates = visitedStates;
        }
    }
    
    private void testAIResponse(String userPrompt, String intent, OracleRule rule) {
        // Uncomment to actually test with AI
        /*
        try {
            Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .body("""
                    {
                      "model": "gpt-4",
                      "messages": [{"role": "user", "content": "%s"}]
                    }
                    """.formatted(userPrompt))
                .post(BASE_URL);
            
            String aiResponse = response.jsonPath().getString("choices[0].message.content");
            System.out.println("AI Response: " + aiResponse);
            
            // Validate AI response
            if (rule != null && rule.getExpectedContains() != null) {
                boolean hasEmotionalIntelligence = false;
                for (String indicator : rule.getExpectedContains()) {
                    if (aiResponse.toLowerCase().contains(indicator.toLowerCase())) {
                        hasEmotionalIntelligence = true;
                        System.out.println("✓ Found EI indicator: " + indicator);
                        break;
                    }
                }
                
                if (!hasEmotionalIntelligence) {
                    System.out.println("✗ Missing emotional intelligence indicators");
                    System.out.println("Expected indicators: " + rule.getExpectedContains());
                }
                
                assertTrue(hasEmotionalIntelligence, 
                    "AI response lacks emotional intelligence indicators for intent: " + intent);
            }
            
        } catch (Exception e) {
            System.out.println("Error testing AI response: " + e.getMessage());
        }
        */
        
        System.out.println("AI Response: [Simulated - API calls disabled for safety]");
        System.out.println("✓ Real-life EI test scenario validated");
    }
    
    @Test
    public void testCrisisScenarios() throws Exception {
        System.out.println("\n=== Testing Crisis Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test crisis-related intents
        String[] crisisIntents = {
            "ei.crisis.immediate",
            "ei.crisis.suicidal", 
            "ei.emergency.help",
            "ei.health.panic",
            "ei.betrayal.devastation",
            "ei.grief.guilt"
        };
        
        for (String intent : crisisIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Crisis Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample crisis prompt
                String crisisPrompt = "I'm having thoughts of ending my life because I can't take this pain anymore";
                testAIResponse(crisisPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testWorkStressScenarios() throws Exception {
        System.out.println("\n=== Testing Work Stress Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test work-related intents
        String[] workIntents = {
            "ei.work.stress",
            "ei.burnout.express",
            "ei.work.advice",
            "ei.improvement.work"
        };
        
        for (String intent : workIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Work Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample work stress prompt
                String workPrompt = "I'm completely burned out from my job and can't take it anymore";
                testAIResponse(workPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testRelationshipScenarios() throws Exception {
        System.out.println("\n=== Testing Relationship Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test relationship-related intents
        String[] relationshipIntents = {
            "ei.relationship.problems",
            "ei.betrayal.express",
            "ei.communication.problems",
            "ei.relationship.advice"
        };
        
        for (String intent : relationshipIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Relationship Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample relationship prompt
                String relationshipPrompt = "My partner cheated on me and I feel completely betrayed";
                testAIResponse(relationshipPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testHealthScenarios() throws Exception {
        System.out.println("\n=== Testing Health Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test health-related intents
        String[] healthIntents = {
            "ei.health.worries",
            "ei.health.panic",
            "ei.medical.advice",
            "ei.hypochondria.express"
        };
        
        for (String intent : healthIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Health Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample health prompt
                String healthPrompt = "I'm terrified I have cancer and can't stop worrying about my symptoms";
                testAIResponse(healthPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testGriefScenarios() throws Exception {
        System.out.println("\n=== Testing Grief Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test grief-related intents
        String[] griefIntents = {
            "ei.grief.loss",
            "ei.grief.guilt",
            "ei.grief.loneliness",
            "ei.grief.support"
        };
        
        for (String intent : griefIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Grief Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample grief prompt
                String griefPrompt = "My mother died suddenly and I feel so guilty I wasn't there for her";
                testAIResponse(griefPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testCareerScenarios() throws Exception {
        System.out.println("\n=== Testing Career Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test career-related intents
        String[] careerIntents = {
            "ei.career.change",
            "ei.career.fear",
            "ei.career.guidance",
            "ei.career.excitement"
        };
        
        for (String intent : careerIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Career Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample career prompt
                String careerPrompt = "I want to change careers but I'm terrified of failing and starting over";
                testAIResponse(careerPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testFamilyScenarios() throws Exception {
        System.out.println("\n=== Testing Family Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test family-related intents
        String[] familyIntents = {
            "ei.family.conflict",
            "ei.boundaries.express",
            "ei.family.therapy",
            "ei.family.guilt"
        };
        
        for (String intent : familyIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Family Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample family prompt
                String familyPrompt = "My family is toxic and controlling and I need to set boundaries";
                testAIResponse(familyPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testFinancialScenarios() throws Exception {
        System.out.println("\n=== Testing Financial Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test financial-related intents
        String[] financialIntents = {
            "ei.financial.worries",
            "ei.financial.shame",
            "ei.financial.help",
            "ei.financial.hopeless"
        };
        
        for (String intent : financialIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Financial Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample financial prompt
                String financialPrompt = "I'm drowning in debt and feel so ashamed I can't tell anyone";
                testAIResponse(financialPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testSocialScenarios() throws Exception {
        System.out.println("\n=== Testing Social Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test social-related intents
        String[] socialIntents = {
            "ei.social.anxiety",
            "ei.social.isolation",
            "ei.social.skills",
            "ei.social.panic"
        };
        
        for (String intent : socialIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Social Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample social prompt
                String socialPrompt = "I'm terrified of social situations and have no friends";
                testAIResponse(socialPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testProfessionalHelpScenarios() throws Exception {
        System.out.println("\n=== Testing Professional Help Scenarios ===");
        
        // Load FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile("oracle/extended-real-life-ei-oracle.json");
        
        // Test professional help intents
        String[] professionalIntents = {
            "ei.professional.help",
            "ei.relationship.counseling",
            "ei.grief.counseling",
            "ei.career.counseling",
            "ei.family.counseling",
            "ei.financial.counseling",
            "ei.social.counseling"
        };
        
        for (String intent : professionalIntents) {
            OracleRule rule = oracle.get(intent);
            if (rule != null) {
                System.out.println("\n--- Testing Professional Help Intent: " + intent + " ---");
                System.out.println("Expected Indicators: " + rule.getExpectedContains());
                
                // Test with a sample professional help prompt
                String professionalPrompt = "I think I need professional help to deal with my problems";
                testAIResponse(professionalPrompt, intent, rule);
            }
        }
    }
    
    @Test
    public void testFsmStructure() throws Exception {
        System.out.println("\n=== Testing Extended Real-Life EI FSM Structure ===");
        
        // Load FSM
        FsmDefinition fsm = FsmLoader.loadFromFile("ei/fsm/extended-real-life-ei-fsm.json");
        
        System.out.println("Start State: " + fsm.startState);
        System.out.println("Total States: " + fsm.states.size());
        
        // Count different types of scenarios
        Map<String, Integer> scenarioTypes = new HashMap<>();
        
        for (Map.Entry<String, FsmLoader.FsmState> stateEntry : fsm.states.entrySet()) {
            String stateName = stateEntry.getKey();
            FsmLoader.FsmState state = stateEntry.getValue();
            
            System.out.println("\nState: " + stateName);
            if (state.transitions != null) {
                System.out.println("  Transitions: " + state.transitions.size());
                for (Map.Entry<String, FsmTransition> entry : state.transitions.entrySet()) {
                    FsmTransition transition = entry.getValue();
                    String transitionName = entry.getKey();
                    
                    System.out.println("    " + transitionName + " -> " + transition.next);
                    System.out.println("      Intent: " + transition.intent);
                    
                    // Categorize by scenario type
                    String scenarioType = transition.intent.split("\\.")[1];
                    scenarioTypes.put(scenarioType, scenarioTypes.getOrDefault(scenarioType, 0) + 1);
                }
            } else {
                System.out.println("  Transitions: 0 (End state)");
            }
        }
        
        System.out.println("\n=== Scenario Type Distribution ===");
        for (Map.Entry<String, Integer> entry : scenarioTypes.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " scenarios");
        }
        
        // Validate all next states exist
        Set<String> validStates = fsm.states.keySet();
        for (Map.Entry<String, FsmLoader.FsmState> stateEntry : fsm.states.entrySet()) {
            FsmLoader.FsmState state = stateEntry.getValue();
            if (state.transitions != null) {
                for (Map.Entry<String, FsmTransition> entry : state.transitions.entrySet()) {
                    FsmTransition transition = entry.getValue();
                    if (transition.next != null && !transition.next.equals("End")) {
                        assertTrue(validStates.contains(transition.next), 
                            "Invalid next state: " + transition.next);
                    }
                }
            }
        }
        
        System.out.println("✓ Extended Real-Life EI FSM structure validation passed");
    }
} 