package runner;

import fetcher.RedditMarkdownFetcher;
import fsm.FsmExpander;
import model.*;
import oracle.OracleExpander;
import parser.MarkdownParser;
import state.StateManager;
import test.TestRunner;
import utils.ClusteredIntentLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Progressive FSM Runner - Implements the 6-step workflow:
 * 1. Start with first N dialog turns from Reddit
 * 2. Build FSM + Oracle
 * 3. Run test runner to validate
 * 4. Continue reading next N turns
 * 5. Expand FSM and Oracle
 * 6. Resume walk from last known state
 */
public class ProgressiveFsmRunner {
    
    private final StateManager stateManager;
    private final FsmExpander fsmExpander;
    private final OracleExpander oracleExpander;
    private final TestRunner testRunner;
    private final int batchSize;
    
    public ProgressiveFsmRunner(int batchSize) {
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
        System.out.println("🚀 Starting Progressive FSM Execution");
        System.out.println("📊 Processing: r/" + subreddit + " thread " + threadId);
        
        // Step 1: Fetch Reddit data
        String markdownFile = "src/main/resources/threads/" + subreddit + "_" + threadId + ".md";
        fetchRedditData(subreddit, threadId, markdownFile);
        
        // Step 2: Parse all dialog turns
        List<DialogTurn> allTurns = MarkdownParser.parseDialogTurns(markdownFile);
        System.out.println("📝 Total dialog turns: " + allTurns.size());
        
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
        }
        
        System.out.println("\n✅ Progressive execution complete!");
        System.out.println("📁 Generated files:");
        System.out.println("   - FSM: src/main/resources/generated/progressive_fsm.json");
        System.out.println("   - Oracle: src/main/resources/generated/progressive_oracle.json");
        System.out.println("   - State: src/main/resources/generated/execution_state.json");
    }
    
    private void fetchRedditData(String subreddit, String threadId, String outputFile) throws IOException {
        System.out.println("📥 Fetching Reddit data...");
        RedditMarkdownFetcher fetcher = new RedditMarkdownFetcher();
        fetcher.fetchAndExtractMarkdown(subreddit, threadId, outputFile);
    }
    
    private void expandFsmAndOracle(List<DialogTurn> batch, int startIndex) throws Exception {
        System.out.println("🔧 Expanding FSM and Oracle...");
        
        // Generate intent clustering for this batch
        String batchJsonFile = "src/main/resources/data/batch_turns_" + (startIndex / batchSize) + ".json";
        String clusteredIntentFile = "src/main/resources/data/clustered_intents_" + (startIndex / batchSize) + ".json";
        
        // Save batch to JSON for Python processing
        saveBatchToJson(batch, batchJsonFile);
        
        // Run Python intent clustering
        runPythonIntentClusterer(batchJsonFile, clusteredIntentFile);
        
        // Load intent mapping
        Map<String, String> intentMap = ClusteredIntentLoader.loadUtteranceIntentMap(clusteredIntentFile);
        
        // Expand FSM
        fsmExpander.expandWithBatch(batch, intentMap, stateManager.getCurrentFsm());
        
        // Expand Oracle
        oracleExpander.expandWithBatch(batch, intentMap, stateManager.getCurrentOracle());
        
        System.out.println("   ✅ Added " + batch.size() + " new transitions");
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
        try (FileWriter writer = new FileWriter("src/main/resources/generated/progressive_fsm.json")) {
            gson.toJson(stateManager.getCurrentFsm(), writer);
        }
        
        // Save Oracle
        try (FileWriter writer = new FileWriter("src/main/resources/generated/progressive_oracle.json")) {
            gson.toJson(stateManager.getCurrentOracle(), writer);
        }
        
        // Save execution state
        try (FileWriter writer = new FileWriter("src/main/resources/generated/execution_state.json")) {
            gson.toJson(stateManager.getExecutionState(), writer);
        }
    }
    
    private void saveBatchToJson(List<DialogTurn> batch, String outputPath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(outputPath)) {
            gson.toJson(batch, writer);
        }
    }
    
    private void runPythonIntentClusterer(String inputJson, String outputJson) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "python3", "src/main/python/intent_clusterer.py",
                "--input", inputJson,
                "--output", outputJson
        );
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python intent clustering failed with exit code: " + exitCode);
        }
    }
    
    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: java runner.ProgressiveFsmRunner <subreddit> <threadCode> <threadId> <batchSize>");
            System.out.println("Example: java runner.ProgressiveFsmRunner DecidingToBeBetter abc123 5");
            return;
        }
        
        String subreddit = args[0];
        String threadId = args[1];
        int batchSize = Integer.parseInt(args[2]);
        
        try {
            ProgressiveFsmRunner runner = new ProgressiveFsmRunner(batchSize);
            runner.runProgressiveExecution(subreddit, threadId);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 