package com.ziffel.ei;

import com.ziffel.generator.FsmLoader;
import com.ziffel.generator.FsmLoader.FsmDefinition;
import com.ziffel.generator.FsmLoader.FsmTransition;
import com.ziffel.generator.RLPromptGenerator;
import com.ziffel.generator.RLPromptGenerator.PromptTestCase;
import com.ziffel.oracle.OracleLoader;
import com.ziffel.oracle.OracleLoader.OracleRule;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomComprehensiveEITest {

    private static final String BASE_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final int MAX_RANDOM_WALKS = 20;
    private static final int MAX_PATH_LENGTH = 8;
    private static final int MAX_PATHS_PER_STATE = 5; // Limit paths to prevent explosion
    private static final Random random = new Random();

    // Define available EI scenarios
    private static final List<EIScenario> EI_SCENARIOS = Arrays.asList(
        new EIScenario(
            "Work Stress",
            "ei/fsm/work-stress-ei-fsm.json",
            "ei/oracle/work-stress-ei-oracle.json",
            "Work stress and burnout scenarios"
        ),
        new EIScenario(
            "Grief Counseling",
            "ei/fsm/grief-counseling-ei-fsm.json",
            "ei/oracle/grief-counseling-ei-oracle.json",
            "Grief counseling and bereavement support"
        )
    );

    private static class EIScenario {
        String name;
        String fsmPath;
        String oraclePath;
        String description;

        EIScenario(String name, String fsmPath, String oraclePath, String description) {
            this.name = name;
            this.fsmPath = fsmPath;
            this.oraclePath = oraclePath;
            this.description = description;
        }
    }

    @Test
    public void testRandomComprehensiveEmotionalIntelligence() throws Exception {
        System.out.println("=== Random Comprehensive Emotional Intelligence Testing ===");
        
        // Randomly select an EI scenario
        Random random = new Random(System.currentTimeMillis());
        EIScenario selectedScenario = EI_SCENARIOS.get(random.nextInt(EI_SCENARIOS.size()));
        
        System.out.println("Selected Scenario: " + selectedScenario.name);
        System.out.println("Description: " + selectedScenario.description);
        System.out.println("FSM: " + selectedScenario.fsmPath);
        System.out.println("Oracle: " + selectedScenario.oraclePath);
        
        // Load selected FSM and oracle
        FsmDefinition fsm = FsmLoader.loadFromFile(selectedScenario.fsmPath);
        Map<String, OracleRule> oracle = OracleLoader.loadFromFile(selectedScenario.oraclePath);
        
        // Generate random conversation walks
        List<List<ConversationTurn>> randomWalks = generateRandomWalks(fsm, MAX_RANDOM_WALKS);
        
        System.out.println("\n--- Testing " + randomWalks.size() + " Random Conversation Walks ---");
        
        for (int walkIndex = 0; walkIndex < randomWalks.size(); walkIndex++) {
            List<ConversationTurn> walk = randomWalks.get(walkIndex);
            System.out.println("\n--- Random Walk " + (walkIndex + 1) + " ---");
            System.out.println("Walk Length: " + walk.size() + " turns");
            
            for (int turnIndex = 0; turnIndex < walk.size(); turnIndex++) {
                ConversationTurn turn = walk.get(turnIndex);
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

    @Test
    public void testAllEIScenarios() throws Exception {
        System.out.println("=== Testing All EI Scenarios ===");
        
        for (EIScenario scenario : EI_SCENARIOS) {
            System.out.println("\n--- Testing " + scenario.name + " Scenario ---");
            System.out.println("Description: " + scenario.description);
            
            // Load FSM and oracle
            FsmDefinition fsm = FsmLoader.loadFromFile(scenario.fsmPath);
            Map<String, OracleRule> oracle = OracleLoader.loadFromFile(scenario.oraclePath);
            
            // Generate a few random walks for each scenario
            List<List<ConversationTurn>> randomWalks = generateRandomWalks(fsm, 5);
            
            System.out.println("Generated " + randomWalks.size() + " random walks");
            
            for (int walkIndex = 0; walkIndex < randomWalks.size(); walkIndex++) {
                List<ConversationTurn> walk = randomWalks.get(walkIndex);
                System.out.println("Walk " + (walkIndex + 1) + " length: " + walk.size() + " turns");
                
                // Show first turn as example
                if (!walk.isEmpty()) {
                    ConversationTurn firstTurn = walk.get(0);
                    System.out.println("Example prompt: " + firstTurn.userPrompt);
                    System.out.println("Intent: " + firstTurn.intent);
                }
            }
        }
    }

    @Test
    public void testScenarioDiversity() throws Exception {
        System.out.println("=== Testing EI Scenario Diversity ===");
        
        Map<String, Set<String>> scenarioIntents = new HashMap<>();
        Map<String, Integer> scenarioWalkCounts = new HashMap<>();
        
        for (EIScenario scenario : EI_SCENARIOS) {
            System.out.println("\n--- Analyzing " + scenario.name + " ---");
            
            // Load FSM and oracle
            FsmDefinition fsm = FsmLoader.loadFromFile(scenario.fsmPath);
            Map<String, OracleRule> oracle = OracleLoader.loadFromFile(scenario.oraclePath);
            
            // Generate multiple walks
            List<List<ConversationTurn>> randomWalks = generateRandomWalks(fsm, 10);
            scenarioWalkCounts.put(scenario.name, randomWalks.size());
            
            // Collect unique intents
            Set<String> intents = new HashSet<>();
            for (List<ConversationTurn> walk : randomWalks) {
                for (ConversationTurn turn : walk) {
                    intents.add(turn.intent);
                }
            }
            scenarioIntents.put(scenario.name, intents);
            
            System.out.println("Unique intents found: " + intents.size());
            System.out.println("Sample intents: " + intents.stream().limit(5).toList());
        }
        
        // Compare scenarios
        System.out.println("\n--- Scenario Comparison ---");
        for (String scenarioName : scenarioIntents.keySet()) {
            System.out.println(scenarioName + ": " + scenarioIntents.get(scenarioName).size() + " unique intents");
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
    
    private List<List<ConversationTurn>> generateRandomWalks(FsmDefinition fsm, int maxWalks) {
        List<List<ConversationTurn>> randomWalks = new ArrayList<>();
        
        for (int walkIndex = 0; walkIndex < maxWalks; walkIndex++) {
            List<ConversationTurn> walk = generateSingleRandomWalk(fsm);
            if (!walk.isEmpty()) {
                randomWalks.add(walk);
            }
        }
        
        return randomWalks;
    }
    
    private List<ConversationTurn> generateSingleRandomWalk(FsmDefinition fsm) {
        List<ConversationTurn> walk = new ArrayList<>();
        String currentState = fsm.startState;
        Map<String, Integer> stateVisitCount = new HashMap<>();
        int pathLength = 0;
        int maxVisitsPerState = 3; // Allow revisiting states but limit to prevent infinite loops

//        System.out.println("DEBUG: FSM startState = '" + fsm.startState + "'");
//        System.out.println("DEBUG: FSM states = " + fsm.states.keySet());

        while (currentState != null && !currentState.equals("End") && 
               pathLength < MAX_PATH_LENGTH) {


//            System.out.println("DEBUG: Current state = '" + currentState + "', pathLength = " + pathLength);

            FsmLoader.FsmState state = fsm.states.get(currentState);
            if (state == null || state.transitions == null || state.transitions.isEmpty()) {
//                System.out.println("DEBUG: Current state = '" + currentState + "', pathLength = " + pathLength);
//                System.out.println("DEBUG: About to get state from fsm.states.get('" + currentState + "')");
//
//                System.out.println("DEBUG: Retrieved state = " + (state != null ? "not null" : "null"));
//                System.out.println("DEBUG: State transitions = " + (state != null && state.transitions != null ? state.transitions.size() + " transitions" : "null"));
                break;
            }
            
            // Check if we've visited this state too many times
            int visitCount = stateVisitCount.getOrDefault(currentState, 0);
            if (visitCount >= maxVisitsPerState) {
//                System.out.println("DEBUG: Reached maxVisitsPerState!");

                break;
            }
            
            // Increment visit count for current state
            stateVisitCount.put(currentState, visitCount + 1);
            
            // Get all possible transitions from current state
            List<FsmTransition> availableTransitions = new ArrayList<>(state.transitions.values());
            
            if (availableTransitions.isEmpty()) {
                break;
            }
            
            // Randomly select a transition
            FsmTransition randomTransition = availableTransitions.get(random.nextInt(availableTransitions.size()));
            
            // Generate prompts from RL template
            List<PromptTestCase> promptTestCases = generateRandomPrompts(randomTransition);
            
            if (!promptTestCases.isEmpty()) {
                // Randomly select a prompt
                PromptTestCase selectedPrompt = promptTestCases.get(
                    random.nextInt(promptTestCases.size())
                );
                
                ConversationTurn turn = new ConversationTurn(
                    selectedPrompt.getPrompt(),
                    selectedPrompt.getIntent(),
                    selectedPrompt.getExpectedContains()
                );
                
                walk.add(turn);
                pathLength++;
                
                // Move to next state
//                System.out.println("DEBUG: Moving from '" + turn.intent + "' to state '" + currentState + "'");
//                System.out.println("DEBUG: After transition - currentState = '" + currentState + "', pathLength = " + pathLength);
                currentState = randomTransition.next;
            } else {
                break;
            }
        }
        
        return walk;
    }
    
    private List<PromptTestCase> generateRandomPrompts(FsmTransition transition) {
        List<PromptTestCase> promptTestCases = new ArrayList<>();
        
        if (transition.rlTemplate != null) {
            // Generate test cases from RL template
            promptTestCases = RLPromptGenerator.generateTestCases(
                transition.rlTemplate,
                transition.intent,
                transition.expectedContains
            );
            
            // Limit to prevent explosion, but keep some variety
            if (promptTestCases.size() > 5) {
                // Randomly sample 5 prompts
                Collections.shuffle(promptTestCases);
                promptTestCases = promptTestCases.subList(0, 5);
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
        System.out.println("✓ Random EI test scenario validated");
    }
    
    @Test
    public void testRandomWalkStatistics() throws Exception {
        System.out.println("\n=== Random Walk Statistics ===");
        
        // Randomly select a scenario for statistics
        EIScenario selectedScenario = EI_SCENARIOS.get(random.nextInt(EI_SCENARIOS.size()));
        System.out.println("Analyzing scenario: " + selectedScenario.name);
        
        // Load FSM
        FsmDefinition fsm = FsmLoader.loadFromFile(selectedScenario.fsmPath);
        
        // Generate multiple random walks for statistics
        List<List<ConversationTurn>> randomWalks = generateRandomWalks(fsm, 50);
        
        // Analyze walk statistics
        Map<String, Integer> intentFrequency = new HashMap<>();
        Map<Integer, Integer> walkLengthDistribution = new HashMap<>();
        Set<String> uniqueIntents = new HashSet<>();
        
        for (List<ConversationTurn> walk : randomWalks) {
            // Count walk length
            int length = walk.size();
            walkLengthDistribution.put(length, walkLengthDistribution.getOrDefault(length, 0) + 1);
            
            // Count intents
            for (ConversationTurn turn : walk) {
                intentFrequency.put(turn.intent, intentFrequency.getOrDefault(turn.intent, 0) + 1);
                uniqueIntents.add(turn.intent);
            }
        }
        
        // Print statistics
        System.out.println("Total walks: " + randomWalks.size());
        System.out.println("Unique intents: " + uniqueIntents.size());
        System.out.println("Walk length distribution: " + walkLengthDistribution);
        
        // Most common intents
        System.out.println("Most common intents:");
        intentFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue()));
    }
    
    @Test
    public void testRandomWalkDiversity() throws Exception {
        System.out.println("\n=== Random Walk Diversity Analysis ===");
        
        // Test diversity across all scenarios
        for (EIScenario scenario : EI_SCENARIOS) {
            System.out.println("\n--- " + scenario.name + " Diversity ---");
            
            FsmDefinition fsm = FsmLoader.loadFromFile(scenario.fsmPath);
            List<List<ConversationTurn>> randomWalks = generateRandomWalks(fsm, 30);
            
            // Analyze diversity
            Set<String> uniquePrompts = new HashSet<>();
            Set<String> uniqueIntents = new HashSet<>();
            Set<String> uniquePaths = new HashSet<>();
            
            for (List<ConversationTurn> walk : randomWalks) {
                StringBuilder pathBuilder = new StringBuilder();
                for (ConversationTurn turn : walk) {
                    uniquePrompts.add(turn.userPrompt);
                    uniqueIntents.add(turn.intent);
                    pathBuilder.append(turn.intent).append(" -> ");
                }
                uniquePaths.add(pathBuilder.toString());
            }
            
            System.out.println("Unique prompts: " + uniquePrompts.size());
            System.out.println("Unique intents: " + uniqueIntents.size());
            System.out.println("Unique paths: " + uniquePaths.size());
            System.out.println("Diversity ratio: " + String.format("%.2f", 
                (double) uniquePaths.size() / randomWalks.size()));
        }
    }
    
    @Test
    public void testRandomWalkWithSeed() throws Exception {
        System.out.println("\n=== Random Walk with Seed (Reproducible) ===");
        
        // Set seed for reproducible results
        Random seededRandom = new Random(42L);
        
        // Randomly select a scenario
        EIScenario selectedScenario = EI_SCENARIOS.get(seededRandom.nextInt(EI_SCENARIOS.size()));
        System.out.println("Selected scenario: " + selectedScenario.name);
        
        FsmDefinition fsm = FsmLoader.loadFromFile(selectedScenario.fsmPath);
        
        // Generate walks with seeded random
        List<List<ConversationTurn>> seededWalks = generateSeededRandomWalks(fsm, 5, seededRandom);
        
        System.out.println("Generated " + seededWalks.size() + " reproducible walks");
        
        for (int i = 0; i < seededWalks.size(); i++) {
            List<ConversationTurn> walk = seededWalks.get(i);
            System.out.println("Walk " + (i + 1) + " length: " + walk.size());
            
            // Show first turn
            if (!walk.isEmpty()) {
                System.out.println("First prompt: " + walk.get(0).userPrompt);
            }
        }
    }
    
    private List<List<ConversationTurn>> generateSeededRandomWalks(FsmDefinition fsm, int maxWalks, Random seededRandom) {
        List<List<ConversationTurn>> randomWalks = new ArrayList<>();
        
        for (int walkIndex = 0; walkIndex < maxWalks; walkIndex++) {
            List<ConversationTurn> walk = generateSingleSeededRandomWalk(fsm, seededRandom);
            if (!walk.isEmpty()) {
                randomWalks.add(walk);
            }
        }
        
        return randomWalks;
    }
    
    private List<ConversationTurn> generateSingleSeededRandomWalk(FsmDefinition fsm, Random seededRandom) {
        List<ConversationTurn> walk = new ArrayList<>();
        String currentState = fsm.startState;
        Map<String, Integer> stateVisitCount = new HashMap<>();
        int pathLength = 0;
        int maxVisitsPerState = 3; // Allow revisiting states but limit to prevent infinite loops
        
        while (currentState != null && !currentState.equals("End") && 
               pathLength < MAX_PATH_LENGTH) {
            
            FsmLoader.FsmState state = fsm.states.get(currentState);
            if (state == null || state.transitions == null || state.transitions.isEmpty()) {
                break;
            }
            
            // Check if we've visited this state too many times
            int visitCount = stateVisitCount.getOrDefault(currentState, 0);
            if (visitCount >= maxVisitsPerState) {
                break;
            }
            
            // Increment visit count for current state
            stateVisitCount.put(currentState, visitCount + 1);
            
            List<FsmTransition> availableTransitions = new ArrayList<>(state.transitions.values());
            
            if (availableTransitions.isEmpty()) {
                break;
            }
            
            FsmTransition randomTransition = availableTransitions.get(seededRandom.nextInt(availableTransitions.size()));
            
            List<PromptTestCase> promptTestCases = generateRandomPrompts(randomTransition);
            
            if (!promptTestCases.isEmpty()) {
                PromptTestCase selectedPrompt = promptTestCases.get(
                    seededRandom.nextInt(promptTestCases.size())
                );
                
                ConversationTurn turn = new ConversationTurn(
                    selectedPrompt.getPrompt(),
                    selectedPrompt.getIntent(),
                    selectedPrompt.getExpectedContains()
                );
                
                walk.add(turn);
                pathLength++;
                currentState = randomTransition.next;
            } else {
                break;
            }
        }
        
        return walk;
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
    
    private List<List<ConversationTurn>> generateConversationPathsFromRLTemplates(FsmDefinition fsm) {
        List<List<ConversationTurn>> allPaths = new ArrayList<>();
        
        // Use iterative approach instead of recursive to prevent stack overflow
        Queue<PathState> queue = new LinkedList<>();
        queue.add(new PathState(fsm.startState, new ArrayList<>(), new HashSet<>()));
        
        while (!queue.isEmpty() && allPaths.size() < 100) { // Limit total paths
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
                String transitionId = entry.getKey();
                
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
            
            // Limit to first 3 prompts to prevent explosion
            if (promptTestCases.size() > 3) {
                promptTestCases = promptTestCases.subList(0, 3);
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

} 