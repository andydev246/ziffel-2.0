package model;

import java.util.HashMap;
import java.util.Map;

public class OracleDefinition {
    public Map<String, OracleState> states = new HashMap<>();

    public static class OracleState {
        public Map<String, OracleTransition> transitions = new HashMap<>();
    }

    public static class OracleTransition {
        public String expectedContains;
        public String expectedTone;
        public String expectedPivot;
        public String expectedIntent;
    }
} 