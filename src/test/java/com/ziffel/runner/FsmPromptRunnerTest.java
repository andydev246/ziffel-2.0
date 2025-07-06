package com.ziffel.runner;

import com.ziffel.generator.FsmLoader;
import com.ziffel.generator.FsmLoader.FsmDefinition;
import com.ziffel.generator.FsmLoader.FsmState;
import com.ziffel.generator.FsmLoader.FsmTransition;
import com.ziffel.generator.RLPromptGenerator;
import com.ziffel.generator.RLPromptGenerator.PromptTestCase;
import com.ziffel.oracle.OracleLoader;
import com.ziffel.oracle.OracleLoader.OracleRule;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FsmPromptRunnerTest {

    private static final String BASE_URL = "https://api.openai.com/v1/chat/completions"; // replace if needed
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    public static class ExtendedPromptTestCase extends PromptTestCase {
        private List<String> expectedVariants;
        private String expectedRegex;

        public ExtendedPromptTestCase(String prompt, String intent, String expectedContains) {
            super(prompt, intent, expectedContains);
        }

        public List<String> getExpectedVariants() {
            return expectedVariants;
        }

        public void setExpectedVariants(List<String> expectedVariants) {
            this.expectedVariants = expectedVariants;
        }

        public String getExpectedRegex() {
            return expectedRegex;
        }

        public void setExpectedRegex(String expectedRegex) {
            this.expectedRegex = expectedRegex;
        }
    }

    @Test
    public void runFsmPromptTests() throws Exception {
        FsmDefinition fsm = FsmLoader.loadFromFile("fsm/example-fsm-rl.json");
        Map<String, OracleRule> oracleMap = OracleLoader.loadFromFile("oracle/intents.json");

        for (Map.Entry<String, FsmState> stateEntry : fsm.states.entrySet()) {
            for (Map.Entry<String, FsmTransition> transEntry : stateEntry.getValue().transitions.entrySet()) {
                FsmTransition trans = transEntry.getValue();

                List<PromptTestCase> testCases;
                if (trans.rlTemplate != null) {
                    testCases = RLPromptGenerator.generateTestCases(
                            trans.rlTemplate,
                            trans.intent,
                            ""
                    );
                } else if (trans.prompt != null) {
                    testCases = List.of(new PromptTestCase(
                            trans.prompt,
                            trans.intent,
                            ""
                    ));
                } else {
                    continue;
                }

                OracleRule rule = oracleMap.get(trans.intent);

                for (PromptTestCase testCase : testCases) {
                    ExtendedPromptTestCase extCase = new ExtendedPromptTestCase(
                            testCase.getPrompt(), testCase.getIntent(), ""
                    );

                    if (rule != null) {
                        extCase.setExpectedVariants(rule.getExpectedContains());
                        extCase.setExpectedRegex(rule.getExpectedRegex());
                    }

                    Response response = RestAssured
                            .given()
                            .header("Authorization", "Bearer " + API_KEY)
                            .header("Content-Type", "application/json")
                            .body("""
                            {
                              "model": "gpt-4",
                              "messages": [{"role": "user", "content": "%s"}]
                            }
                            """.formatted(extCase.getPrompt()))
                            .post(BASE_URL);

                    String reply = response.jsonPath().getString("choices[0].message.content");

                    System.out.println("\nPrompt: " + extCase.getPrompt());
                    System.out.println("Response: " + reply);

                    boolean passed = false;
                    if (extCase.getExpectedVariants() != null) {
                        for (String variant : extCase.getExpectedVariants()) {
                            if (reply.toLowerCase().contains(variant.toLowerCase())) {
                                passed = true;
                                break;
                            }
                        }
                    } else if (extCase.getExpectedRegex() != null) {
                        passed = reply.matches(extCase.getExpectedRegex());
                    }

                    assertTrue(passed, "Response did not match expected output");
                }
            }
        }
    }
}
