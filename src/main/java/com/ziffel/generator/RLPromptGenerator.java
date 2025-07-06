package com.ziffel.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RLPromptGenerator {

    public static class PromptTestCase {
        private String prompt;
        private String intent;
        private String expectedContains;

        public PromptTestCase(String prompt, String intent, String expectedContains) {
            this.prompt = prompt;
            this.intent = intent;
            this.expectedContains = expectedContains;
        }

        public String getPrompt() {
            return prompt;
        }

        public String getIntent() {
            return intent;
        }

        public String getExpectedContains() {
            return expectedContains;
        }
    }

    public static List<PromptTestCase> generateTestCases(
            Map<String, List<String>> rlTemplate,
            String intent,
            String expectedContains
    ) {
        List<PromptTestCase> cases = new ArrayList<>();
        List<String> generated = generateCombinations(rlTemplate);
        for (String prompt : generated) {
            cases.add(new PromptTestCase(prompt, intent, expectedContains));
        }
        return cases;
    }

    private static List<String> generateCombinations(Map<String, List<String>> template) {
        List<String> result = new ArrayList<>();
        build(template, new ArrayList<>(template.keySet()), 0, "", result);
        return result;
    }

    private static void build(Map<String, List<String>> template, List<String> keys,
                              int index, String current, List<String> result) {
        if (index == keys.size()) {
            result.add(current.trim());
            return;
        }
        String key = keys.get(index);
        List<String> options = template.get(key);
        for (String option : options) {
            build(template, keys, index + 1, current + " " + option, result);
        }
    }
}
