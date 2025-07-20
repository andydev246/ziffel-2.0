package fsm;

import classifier.OrientationClassifier;
import classifier.PivotExtractor;
import classifier.ToneClassifier;
import model.DialogTurn;
import model.FsmDefinition;

import java.util.List;
import java.util.Map;

/**
 * Expands the FSM with new dialog turns incrementally
 */
public class FsmExpander {
    
    private int transitionCounter = 0;
    
    /**
     * Expands the FSM with a new batch of dialog turns
     */
    public void expandWithBatch(List<DialogTurn> batch, Map<String, String> intentMap, FsmDefinition fsm) {
        String currentState = fsm.startState;
        
        for (DialogTurn turn : batch) {
            if (turn.getDepth() != 0) continue; // only top-level user prompts
            
            String prompt = turn.getMessage().trim();
            String intent = intentMap.getOrDefault(prompt, "intent.unknown");
            String tone = ToneClassifier.classify(prompt);
            String orientation = OrientationClassifier.classify(turn);
            String pivot = PivotExtractor.extractFrom(prompt, intent, orientation);
            String nextState = "S" + (transitionCounter + 1);
            
            // Create FSM transition
            FsmDefinition.FsmTransition transition = new FsmDefinition.FsmTransition();
            transition.prompt = prompt;
            transition.intent = intent;
            transition.orientation = orientation;
            transition.tone = tone;
            transition.pivot = pivot;
            transition.next = nextState;
            
            // Add transition to current state
            fsm.states.get(currentState).transitions.put("t" + transitionCounter, transition);
            
            // Create next state if it doesn't exist
            if (!fsm.states.containsKey(nextState)) {
                fsm.states.put(nextState, new FsmDefinition.FsmState());
            }
            
            // Update current state
            currentState = nextState;
            transitionCounter++;
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