package runner;

import model.DialogTurn;
import model.FsmDefinition;
import model.OracleDefinition;
import fsm.FsmExpander;
import oracle.OracleExpander;
import test.TestRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple test to demonstrate progressive execution with sample data
 */
public class SimpleProgressiveTest {
    
    public static void main(String[] args) throws Exception {
        System.out.println("🧪 Simple Progressive FSM Test");
        
        // Create sample dialog turns
        List<DialogTurn> sampleTurns = Arrays.asList(
            new DialogTurn("t0", "user1", "I feel so burned out and exhausted lately", 0),
            new DialogTurn("t1", "assistant", "That sounds really challenging. Burnout can be overwhelming.", 0),
            new DialogTurn("t2", "user1", "I'm afraid I'll never get my motivation back", 0),
            new DialogTurn("t3", "assistant", "Fear of losing motivation is very common. What's one small thing you could try?", 0),
            new DialogTurn("t4", "user1", "I need more structure in my daily routine", 0)
        );
        
        // Create intent mapping (simulated)
        Map<String, String> intentMap = new HashMap<>();
        intentMap.put("I feel so burned out and exhausted lately", "user.burnout");
        intentMap.put("I'm afraid I'll never get my motivation back", "user.fear");
        intentMap.put("I need more structure in my daily routine", "user.needsStructure");
        
        // Initialize FSM and Oracle
        FsmDefinition fsm = new FsmDefinition();
        OracleDefinition oracle = new OracleDefinition();
        fsm.startState = "Neutral";
        fsm.states.put("Neutral", new FsmDefinition.FsmState());
        oracle.states.put("Neutral", new OracleDefinition.OracleState());
        
        // Expand FSM and Oracle
        FsmExpander fsmExpander = new FsmExpander();
        OracleExpander oracleExpander = new OracleExpander();
        
        System.out.println("🔧 Expanding FSM and Oracle with sample data...");
        fsmExpander.expandWithBatch(sampleTurns, intentMap, fsm);
        oracleExpander.expandWithBatch(sampleTurns, intentMap, oracle);
        
        // Run validation tests
        System.out.println("🧪 Running validation tests...");
        TestRunner testRunner = new TestRunner();
        testRunner.runTests(fsm, oracle);
        
        // Save results
        System.out.println("💾 Saving results...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        try (FileWriter writer = new FileWriter("src/main/resources/generated/simple_test_fsm.json")) {
            gson.toJson(fsm, writer);
        }
        
        try (FileWriter writer = new FileWriter("src/main/resources/generated/simple_test_oracle.json")) {
            gson.toJson(oracle, writer);
        }
        
        System.out.println("✅ Simple progressive test complete!");
        System.out.println("📁 Generated files:");
        System.out.println("   - FSM: src/main/resources/generated/simple_test_fsm.json");
        System.out.println("   - Oracle: src/main/resources/generated/simple_test_oracle.json");
    }
} 