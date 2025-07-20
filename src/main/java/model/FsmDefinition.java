package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FsmDefinition {
    public String startState;
    public Map<String, FsmState> states = new HashMap<>();

    public static class FsmState {
        public Map<String, FsmTransition> transitions = new HashMap<>();
    }

    public static class FsmTransition {
        public String prompt;
        public String intent;
        public String orientation;
        public String tone;
        public String pivot;
        public Object next; // String or List<String> for branching
    }
} 