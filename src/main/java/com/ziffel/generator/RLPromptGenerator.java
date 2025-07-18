package com.ziffel.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ziffel.generator.FsmLoader.FsmDefinition;
import com.ziffel.generator.FsmLoader.FsmState;
import com.ziffel.generator.FsmLoader.FsmTransition;

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

        @Override
        public String toString() {
            return "[prompt=\"" + prompt + "\", intent=\"" + intent + "\", expectedContains=\"" + expectedContains + "\"]";
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

    public static List<List<PromptTestCase>> generateConversationPaths(FsmDefinition fsm, String startState) {
        List<List<PromptTestCase>> conversations = new ArrayList<>();
        walkFsm(fsm, startState, new ArrayList<>(), conversations, new java.util.HashSet<>());
        return conversations;
    }

    private static void walkFsm(FsmDefinition fsm, String currentState,
                                List<PromptTestCase> currentPath,
                                List<List<PromptTestCase>> allPaths,
                                java.util.Set<String> visitedStates) {
        // Check for cycles
        if (visitedStates.contains(currentState)) {
            allPaths.add(new ArrayList<>(currentPath)); // End path at cycle
            return;
        }
        
        FsmState state = fsm.states.get(currentState);
        if (state == null || state.transitions == null || state.transitions.isEmpty()) {
            allPaths.add(new ArrayList<>(currentPath)); // End of path
            return;
        }

        // Add current state to visited set
        visitedStates.add(currentState);

        for (Map.Entry<String, FsmTransition> entry : state.transitions.entrySet()) {
            FsmTransition transition = entry.getValue();
            String nextState = transition.next;  // Use next for target state

            // Generate prompt from RL template or use existing prompt
            String prompt;
            if (transition.rlTemplate != null) {
                // Generate a single prompt from the RL template
                List<String> generatedPrompts = generateCombinations(transition.rlTemplate);
                prompt = generatedPrompts.isEmpty() ? "Tell me about this topic" : generatedPrompts.get(0);
            } else {
                prompt = transition.prompt != null ? transition.prompt : "Tell me about this topic";
            }
            
            PromptTestCase testCase = new PromptTestCase(prompt, transition.intent, transition.expectedContains);
            currentPath.add(testCase);
            walkFsm(fsm, nextState, currentPath, allPaths, visitedStates);
            currentPath.remove(currentPath.size() - 1);
        }
        
        // Remove current state from visited set when backtracking
        visitedStates.remove(currentState);
    }
}
