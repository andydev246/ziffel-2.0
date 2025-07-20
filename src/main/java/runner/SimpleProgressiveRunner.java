package runner;

import fetcher.RedditMarkdownFetcher;
import fsm.FsmExpander;
import model.*;
import oracle.OracleExpander;
import parser.MarkdownParser;
import state.StateManager;
import test.TestRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simplified Progressive FSM Runner - Java-only version without Python dependencies
 * Implements the 6-step workflow:
 * 1. Start with first N dialog turns from Reddit
 * 2. Build FSM + Oracle
 * 3. Run test runner to validate
 * 4. Continue reading next N turns
 * 5. Expand FSM and Oracle
 * 6. Resume walk from last known state
 */
public class SimpleProgressiveRunner {
    
    private final StateManager stateManager;
    private final FsmExpander fsmExpander;
    private final OracleExpander oracleExpander;
    private final TestRunner testRunner;
    private final int batchSize;
    
    public SimpleProgressiveRunner(int batchSize) {
        this.batchSize = batchSize;
        this.stateManager = new StateManager();
        this.fsmExpander = new FsmExpander();
        this.oracleExpander = new OracleExpander();
        this.testRunner = new TestRunner();
    }
    
    /**
     * Main progressive execution method
     */
    public void runProgressiveExecution(String subreddit, String threadId) throws Exception {
        System.out.println("🚀 Starting Simple Progressive FSM Execution");
        System.out.println("📊 Processing: r/" + subreddit + " thread " + threadId);
        
        // Step 1: Fetch Reddit data
        String markdownFile = "src/main/resources/threads/" + subreddit + "_" + threadId + ".md";
        fetchRedditData(subreddit, threadId, markdownFile);
        
        // Step 2: Parse all dialog turns
        List<DialogTurn> allTurns = MarkdownParser.parseDialogTurns(markdownFile);
        System.out.println("📝 Total dialog turns: " + allTurns.size());
        
        // Set total turns for progress tracking
        stateManager.setTotalTurns(allTurns.size());
        
        // Step 3: Process in batches
        int processedTurns = 0;
        while (processedTurns < allTurns.size()) {
            int endIndex = Math.min(processedTurns + batchSize, allTurns.size());
            List<DialogTurn> batch = allTurns.subList(processedTurns, endIndex);
            
            System.out.println("\n🔄 Processing batch " + (processedTurns / batchSize + 1) + 
                             " (turns " + processedTurns + "-" + (endIndex - 1) + ")");
            
            // Step 4: Expand FSM and Oracle with new batch
            expandFsmAndOracle(batch, processedTurns);
            
            // Step 5: Run validation tests
            runValidationTests();
            
            // Step 6: Save current state
            saveCurrentState();
            
            processedTurns = endIndex;
            stateManager.updateProgress(processedTurns);
            
            System.out.println("📈 Progress: " + String.format("%.1f", stateManager.getProgressPercentage()) + "%");
        }
        
        System.out.println("\n✅ Progressive execution complete!");
        System.out.println("📁 Generated files:");
        System.out.println("   - FSM: src/main/resources/generated/simple_progressive_fsm.json");
        System.out.println("   - Oracle: src/main/resources/generated/simple_progressive_oracle.json");
        System.out.println("   - State: src/main/resources/generated/simple_execution_state.json");
    }
    
    private void fetchRedditData(String subreddit, String threadId, String outputFile) throws IOException {
        System.out.println("📥 Fetching Reddit data...");
        RedditMarkdownFetcher fetcher = new RedditMarkdownFetcher();
        fetcher.fetchAndExtractMarkdown(subreddit, threadId, outputFile);
    }
    
    private void expandFsmAndOracle(List<DialogTurn> batch, int startIndex) throws Exception {
        System.out.println("🔧 Expanding FSM and Oracle...");
        
        // Create simple intent mapping based on content analysis
        Map<String, String> intentMap = createSimpleIntentMap(batch);
        
        // Expand FSM
        fsmExpander.expandWithBatch(batch, intentMap, stateManager.getCurrentFsm());
        
        // Expand Oracle
        oracleExpander.expandWithBatch(batch, intentMap, stateManager.getCurrentOracle());
        
        System.out.println("   ✅ Added " + batch.size() + " new transitions");
    }
    
    /**
     * Creates a simple intent mapping based on content analysis
     */
    private Map<String, String> createSimpleIntentMap(List<DialogTurn> batch) {
        Map<String, String> intentMap = new HashMap<>();
        
        for (DialogTurn turn : batch) {
            if (turn.getDepth() != 0) continue; // only top-level user prompts
            
            String message = turn.getMessage().toLowerCase();
            String intent = "intent.unknown";
            
            // Simple keyword-based intent classification
            if (message.contains("burnout") || message.contains("exhausted") || message.contains("tired")) {
                intent = "user.burnout";
            } else if (message.contains("fear") || message.contains("afraid") || message.contains("scared")) {
                intent = "user.fear";
            } else if (message.contains("stuck") || message.contains("trapped") || message.contains("can't move")) {
                intent = "user.stuck";
            } else if (message.contains("meaning") || message.contains("purpose") || message.contains("point")) {
                intent = "user.seekingMeaning";
            } else if (message.contains("routine") || message.contains("schedule") || message.contains("structure")) {
                intent = "user.needsStructure";
            } else if (message.contains("motivation") || message.contains("unmotivated") || message.contains("can't start")) {
                intent = "user.unmotivated";
            } else if (message.contains("help") || message.contains("fix") || message.contains("solution")) {
                intent = "user.seekingFix";
            }
            
            intentMap.put(turn.getMessage().trim(), intent);
        }
        
        return intentMap;
    }
    
    private void runValidationTests() {
        System.out.println("🧪 Running validation tests...");
        
        try {
            testRunner.runTests(stateManager.getCurrentFsm(), stateManager.getCurrentOracle());
            System.out.println("   ✅ All tests passed");
        } catch (Exception e) {
            System.out.println("   ⚠️  Some tests failed: " + e.getMessage());
        }
    }
    
    private void saveCurrentState() throws IOException {
        System.out.println("💾 Saving current state...");
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Save FSM
        try (FileWriter writer = new FileWriter("src/main/resources/generated/simple_progressive_fsm.json")) {
            gson.toJson(stateManager.getCurrentFsm(), writer);
        }
        
        // Save Oracle
        try (FileWriter writer = new FileWriter("src/main/resources/generated/simple_progressive_oracle.json")) {
            gson.toJson(stateManager.getCurrentOracle(), writer);
        }
        
        // Save execution state
        try (FileWriter writer = new FileWriter("src/main/resources/generated/simple_execution_state.json")) {
            gson.toJson(stateManager.getExecutionState(), writer);
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java runner.SimpleProgressiveRunner <subreddit> <threadId> <batchSize>");
            System.out.println("Example: java runner.SimpleProgressiveRunner DecidingToBeBetter im_scared_of_just_being_myself_and_even_forming 5");
            return;
        }
        
        String subreddit = args[0];
        String threadId = args[1];
        int batchSize = Integer.parseInt(args[2]);
        
        try {
            SimpleProgressiveRunner runner = new SimpleProgressiveRunner(batchSize);
            runner.runProgressiveExecution(subreddit, threadId);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 