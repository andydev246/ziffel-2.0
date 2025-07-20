package test;

import model.FsmDefinition;
import model.OracleDefinition;

import java.util.Map;

/**
 * TestRunner validates AI responses against Oracle expectations
 */
public class TestRunner {
    
    /**
     * Runs validation tests on the current FSM and Oracle
     */
    public void runTests(FsmDefinition fsm, OracleDefinition oracle) throws Exception {
        System.out.println("   🧪 Running " + fsm.states.size() + " state validations...");
        
        int passedTests = 0;
        int totalTests = 0;
        
        for (Map.Entry<String, FsmDefinition.FsmState> stateEntry : fsm.states.entrySet()) {
            String stateName = stateEntry.getKey();
            FsmDefinition.FsmState fsmState = stateEntry.getValue();
            
            // Skip states with no transitions
            if (fsmState.transitions.isEmpty()) {
                continue;
            }
            
            OracleDefinition.OracleState oracleState = oracle.states.get(stateName);
            if (oracleState == null) {
                System.out.println("   ⚠️  Warning: No Oracle state found for " + stateName);
                continue;
            }
            
            // Test each transition
            for (Map.Entry<String, FsmDefinition.FsmTransition> transitionEntry : fsmState.transitions.entrySet()) {
                String transitionId = transitionEntry.getKey();
                FsmDefinition.FsmTransition fsmTransition = transitionEntry.getValue();
                OracleDefinition.OracleTransition oracleTransition = oracleState.transitions.get(transitionId);
                
                if (oracleTransition == null) {
                    System.out.println("   ⚠️  Warning: No Oracle transition found for " + stateName + "." + transitionId);
                    continue;
                }
                
                totalTests++;
                
                // Simulate AI response (in real implementation, this would call an LLM)
                String simulatedResponse = generateSimulatedResponse(fsmTransition, oracleTransition);
                
                // Validate response
                boolean testPassed = validateResponse(simulatedResponse, oracleTransition);
                
                if (testPassed) {
                    passedTests++;
                } else {
                    System.out.println("   ❌ Test failed: " + stateName + "." + transitionId);
                    System.out.println("      Expected: " + oracleTransition.expectedContains);
                    System.out.println("      Got: " + simulatedResponse);
                }
            }
        }
        
        System.out.println("   📊 Test Results: " + passedTests + "/" + totalTests + " passed");
        
        if (passedTests < totalTests) {
            throw new Exception("Some tests failed: " + (totalTests - passedTests) + " failures");
        }
    }
    
    /**
     * Generates a simulated AI response for testing
     */
    private String generateSimulatedResponse(FsmDefinition.FsmTransition fsmTransition, OracleDefinition.OracleTransition oracleTransition) {
        // Simple simulation - in real implementation, this would call an LLM API
        String prompt = fsmTransition.prompt;
        String expectedTone = oracleTransition.expectedTone;
        
        // Generate response based on expected tone
        switch (expectedTone) {
            case "empathetic":
                return "I understand how you feel. " + oracleTransition.expectedContains;
            case "encouraging":
                return "You can do this! " + oracleTransition.expectedContains;
            case "practical":
                return "Let's work on this together. " + oracleTransition.expectedContains;
            case "reflective":
                return "That's an interesting perspective. " + oracleTransition.expectedContains;
            default:
                return oracleTransition.expectedContains;
        }
    }
    
    /**
     * Validates an AI response against Oracle expectations
     */
    private boolean validateResponse(String response, OracleDefinition.OracleTransition oracleTransition) {
        // Check if response contains expected content
        boolean containsCheck = response.toLowerCase().contains(oracleTransition.expectedContains.toLowerCase());
        
        // Check if response has expected tone (simplified)
        boolean toneCheck = validateTone(response, oracleTransition.expectedTone);
        
        // Check if response contains pivot question (simplified)
        boolean pivotCheck = oracleTransition.expectedPivot == null || 
                           oracleTransition.expectedPivot.isEmpty() ||
                           response.toLowerCase().contains(oracleTransition.expectedPivot.toLowerCase());
        
        return containsCheck && toneCheck && pivotCheck;
    }
    
    /**
     * Validates tone of response (simplified implementation)
     */
    private boolean validateTone(String response, String expectedTone) {
        response = response.toLowerCase();
        
        switch (expectedTone) {
            case "empathetic":
                return response.contains("understand") || response.contains("feel") || response.contains("hear");
            case "encouraging":
                return response.contains("can") || response.contains("will") || response.contains("try");
            case "practical":
                return response.contains("work") || response.contains("plan") || response.contains("step");
            case "reflective":
                return response.contains("think") || response.contains("consider") || response.contains("reflect");
            default:
                return true;
        }
    }
} 