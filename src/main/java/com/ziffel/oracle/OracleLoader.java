package com.ziffel.oracle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class OracleLoader {

    public static class OracleRule {
        public String intent;
        private List<String> expectedContains;
        private String expectedRegex;

        public OracleRule() {}

        public OracleRule(String intent, List<String> expectedContains) {
            this.intent = intent;
            this.expectedContains = expectedContains;
        }

        public List<String> getExpectedContains() {
            return expectedContains;
        }

        public void setExpectedContains(String intent, List<String> expectedContains) {
            this.expectedContains = expectedContains;
        }

        public String getExpectedRegex() {
            return expectedRegex;
        }

        public void setExpectedRegex(String expectedRegex) {
            this.expectedRegex = expectedRegex;
        }
    }

    public static class IntentExpectation {
        public String intent;
        public List<String> expectedContains;

        public IntentExpectation() {}

        public IntentExpectation(String intent, List<String> expectedContains) {
            this.intent = intent;
            this.expectedContains = expectedContains;
        }
    }

    public static Map<String, OracleRule> loadFromFile(String path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = OracleLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Oracle file not found: " + path);
            }
            return mapper.readValue(is, new TypeReference<>() {});
        }
    }
}
