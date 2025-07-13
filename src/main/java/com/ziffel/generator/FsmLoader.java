package com.ziffel.generator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class FsmLoader {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FsmDefinition {
        public String name;
        public String description;
        public String startState;
        public Map<String, FsmState> states;
        public List<ConversationalTurn> conversationalTurns;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FsmState {
        public String id;
        public String name;
        public String description;
        public Map<String, FsmTransition> transitions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FsmTransition {
        public String id;
        public String prompt;
        public String description;
        public String intent;
        public String expectedContains;
        public Map<String, List<String>> rlTemplate;
        
        @JsonProperty("target")
        public String next;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConversationalTurn {
        public String id;
        public String prompt;
        public String description;
    }

    public static FsmDefinition loadFromFile(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = FsmLoader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new RuntimeException("FSM file not found: " + filePath);
            }
            return mapper.readValue(is, FsmDefinition.class);
        }
    }
}
