package oracle;

import classifier.OrientationClassifier;
import classifier.PivotExtractor;
import classifier.ToneClassifier;
import model.DialogTurn;
import model.OracleDefinition;

import java.util.List;
import java.util.Map;

/**
 * Expands the Oracle with new validation rules incrementally
 */
public class OracleExpander {
    
    private int transitionCounter = 0;
    
    /**
     * Expands the Oracle with a new batch of dialog turns
     */
    public void expandWithBatch(List<DialogTurn> batch, Map<String, String> intentMap, OracleDefinition oracle) {
        String currentState = "Neutral";
        
        for (DialogTurn turn : batch) {
            if (turn.getDepth() != 0) continue; // only top-level user prompts
            
            String prompt = turn.getMessage().trim();
            String intent = intentMap.getOrDefault(prompt, "intent.unknown");
            String tone = ToneClassifier.classify(prompt);
            String orientation = OrientationClassifier.classify(turn);
            String pivot = PivotExtractor.extractFrom(prompt, intent, orientation);
            
            // Create Oracle transition with expected responses
            OracleDefinition.OracleTransition oracleTransition = new OracleDefinition.OracleTransition();
            oracleTransition.expectedContains = generateExpectedContains(prompt, intent, orientation);
            oracleTransition.expectedTone = generateExpectedTone(tone, orientation);
            oracleTransition.expectedPivot = pivot;
            oracleTransition.expectedIntent = intent;
            
            // Add transition to current state
            oracle.states.get(currentState).transitions.put("t" + transitionCounter, oracleTransition);
            
            // Create next state if it doesn't exist
            String nextState = "S" + (transitionCounter + 1);
            if (!oracle.states.containsKey(nextState)) {
                oracle.states.put(nextState, new OracleDefinition.OracleState());
            }
            
            // Update current state
            currentState = nextState;
            transitionCounter++;
        }
    }
    
    /**
     * Generates expected content based on user prompt and context
     */
    private String generateExpectedContains(String prompt, String intent, String orientation) {
        // Simple rule-based expected content generation
        if (intent.contains("burnout")) {
            return "That sounds really challenging";
        } else if (intent.contains("fear")) {
            return "Fear is a natural response";
        } else if (intent.contains("stuck")) {
            return "It's okay to feel stuck";
        } else if (orientation.equals("practical")) {
            return "Let's work on a plan";
        } else if (orientation.equals("emotional")) {
            return "I understand how you feel";
        } else {
            return "I hear you";
        }
    }
    
    /**
     * Generates expected tone based on user tone and orientation
     */
    private String generateExpectedTone(String userTone, String orientation) {
        // Map user tone to expected response tone
        switch (userTone) {
            case "empathetic":
                return "empathetic";
            case "encouraging":
                return "encouraging";
            case "practical":
                return "practical";
            case "reflective":
                return "reflective";
            default:
                if (orientation.equals("practical")) {
                    return "practical";
                } else {
                    return "empathetic";
                }
        }
    }
    
    /**
     * Gets the current transition counter
     */
    public int getTransitionCounter() {
        return transitionCounter;
    }
    
    /**
     * Resets the transition counter (useful for testing)
     */
    public void resetTransitionCounter() {
        this.transitionCounter = 0;
    }
} 