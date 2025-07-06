package com.ziffel.generator;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class FsmLoader {

    public static class FsmDefinition {
        public Map<String, FsmState> states;
    }

    public static class FsmState {
        public Map<String, FsmTransition> transitions;
    }

    public static class FsmTransition {
        public String intent;
        public String prompt;
        public String expectedContains;
        public Map<String, List<String>> rlTemplate;
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
