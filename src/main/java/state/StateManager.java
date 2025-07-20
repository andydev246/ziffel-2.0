package state;

import model.FsmDefinition;
import model.OracleDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the current state of FSM execution and tracks progress
 */
public class StateManager {
    
    private FsmDefinition currentFsm;
    private OracleDefinition currentOracle;
    private String currentState;
    private int processedTurns;
    private int totalTurns;
    private Map<String, Object> executionState;
    
    public StateManager() {
        this.currentFsm = new FsmDefinition();
        this.currentOracle = new OracleDefinition();
        this.currentState = "Neutral";
        this.processedTurns = 0;
        this.totalTurns = 0;
        this.executionState = new HashMap<>();
        
        // Initialize with start state
        currentFsm.startState = currentState;
        currentFsm.states.put(currentState, new FsmDefinition.FsmState());
        currentOracle.states.put(currentState, new OracleDefinition.OracleState());
    }
    
    public FsmDefinition getCurrentFsm() {
        return currentFsm;
    }
    
    public OracleDefinition getCurrentOracle() {
        return currentOracle;
    }
    
    public String getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(String state) {
        this.currentState = state;
    }
    
    public int getProcessedTurns() {
        return processedTurns;
    }
    
    public void setProcessedTurns(int turns) {
        this.processedTurns = turns;
    }
    
    public int getTotalTurns() {
        return totalTurns;
    }
    
    public void setTotalTurns(int total) {
        this.totalTurns = total;
    }
    
    public Map<String, Object> getExecutionState() {
        executionState.put("currentState", currentState);
        executionState.put("processedTurns", processedTurns);
        executionState.put("totalTurns", totalTurns);
        executionState.put("fsmStates", currentFsm.states.size());
        executionState.put("oracleStates", currentOracle.states.size());
        return executionState;
    }
    
    public void updateProgress(int newProcessedTurns) {
        this.processedTurns = newProcessedTurns;
    }
    
    public double getProgressPercentage() {
        if (totalTurns == 0) return 0.0;
        return (double) processedTurns / totalTurns * 100.0;
    }
} 