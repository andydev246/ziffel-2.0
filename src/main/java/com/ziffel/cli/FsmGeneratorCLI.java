package com.ziffel.cli;

import com.ziffel.generator.GrammarBasedFsmGenerator;
import com.ziffel.generator.WikipediaContentGenerator;
import com.ziffel.generator.WikipediaContentGenerator.DomainConfig;
import com.ziffel.generator.WikipediaContentGenerator.WikiTopic;

import java.util.List;
import java.util.Map;

public class FsmGeneratorCLI {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        String command = args[0];
        
        try {
            switch (command.toLowerCase()) {
                case "grammar":
                    handleGrammarGeneration(args);
                    break;
                case "wikipedia":
                    handleWikipediaGeneration(args);
                    break;
                case "hybrid":
                    handleHybridGeneration(args);
                    break;
                case "help":
                    printUsage();
                    break;
                default:
                    System.err.println("Unknown command: " + command);
                    printUsage();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void handleGrammarGeneration(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: grammar <output-file> <max-states> [max-transitions] [end-probability]");
            return;
        }
        
        String outputFile = args[1];
        int maxStates = Integer.parseInt(args[2]);
        int maxTransitions = args.length > 3 ? Integer.parseInt(args[3]) : 3;
        double endProbability = args.length > 4 ? Double.parseDouble(args[4]) : 0.3;
        
        System.out.println("Generating grammar-based FSM...");
        
        GrammarBasedFsmGenerator generator = new GrammarBasedFsmGenerator();
        GrammarBasedFsmGenerator.FsmGenerationConfig config = new GrammarBasedFsmGenerator.FsmGenerationConfig();
        config.maxStates = maxStates;
        config.maxTransitionsPerState = maxTransitions;
        config.conversationEndProbability = endProbability;
        
        String fsmJson = generator.generateFsmJson(config);
        generator.saveFsmToFile(fsmJson, outputFile);
        
        System.out.println("FSM generated successfully: " + outputFile);
    }
    
    private static void handleWikipediaGeneration(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: wikipedia <query> <max-results> <output-fsm> <output-oracle>");
            return;
        }
        
        String query = args[1];
        int maxResults = Integer.parseInt(args[2]);
        String outputFsm = args[3];
        String outputOracle = args[4];
        
        System.out.println("Searching Wikipedia for: " + query);
        
        WikipediaContentGenerator generator = new WikipediaContentGenerator();
        DomainConfig config = new DomainConfig("general");
        config.questionTypes.addAll(List.of("what", "how", "why"));
        config.intentMappings.put("what", "definition.request");
        config.intentMappings.put("how", "process.request");
        config.intentMappings.put("why", "reason.request");
        
        List<WikiTopic> topics = generator.searchAndExtractTopics(query, maxResults);
        
        System.out.println("Found " + topics.size() + " topics:");
        for (WikiTopic topic : topics) {
            System.out.println("  - " + topic.title);
        }
        
        // Generate FSM
        String fsmJson = generator.generateDomainSpecificFsm(topics, config);
        java.nio.file.Files.write(java.nio.file.Paths.get(outputFsm), fsmJson.getBytes());
        
        // Generate Oracle
        Map<String, Object> oracle = generator.generateOracleFromTopics(topics, config);
        generator.saveOracleToFile(oracle, outputOracle);
        
        System.out.println("Generated FSM: " + outputFsm);
        System.out.println("Generated Oracle: " + outputOracle);
    }
    
    private static void handleHybridGeneration(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: hybrid <wikipedia-query> <max-results> <output-fsm> <output-oracle>");
            return;
        }
        
        String query = args[1];
        int maxResults = Integer.parseInt(args[2]);
        String outputFsm = args[3];
        String outputOracle = args[4];
        
        System.out.println("Generating hybrid FSM with Wikipedia content...");
        
        // Get Wikipedia topics
        WikipediaContentGenerator wikiGenerator = new WikipediaContentGenerator();
        DomainConfig wikiConfig = new DomainConfig("hybrid");
        wikiConfig.questionTypes.addAll(List.of("what", "how"));
        wikiConfig.intentMappings.put("what", "definition.request");
        wikiConfig.intentMappings.put("how", "process.request");
        
        List<WikiTopic> topics = wikiGenerator.searchAndExtractTopics(query, maxResults);
        
        // Configure grammar generator with Wikipedia topics
        GrammarBasedFsmGenerator grammarGenerator = new GrammarBasedFsmGenerator();
        GrammarBasedFsmGenerator.FsmGenerationConfig grammarConfig = new GrammarBasedFsmGenerator.FsmGenerationConfig();
        grammarConfig.topics.clear();
        grammarConfig.topicIntents.clear();
        
        for (WikiTopic topic : topics) {
            String topicKey = topic.title.toLowerCase().replace(" ", "_");
            grammarConfig.topics.add(topicKey);
            grammarConfig.topicIntents.put(topicKey, "wiki." + topicKey);
        }
        
        // Generate hybrid FSM
        String hybridFsmJson = grammarGenerator.generateFsmJson(grammarConfig);
        grammarGenerator.saveFsmToFile(hybridFsmJson, outputFsm);
        
        // Generate hybrid oracle
        Map<String, Object> hybridOracle = wikiGenerator.generateOracleFromTopics(topics, wikiConfig);
        wikiGenerator.saveOracleToFile(hybridOracle, outputOracle);
        
        System.out.println("Generated Hybrid FSM: " + outputFsm);
        System.out.println("Generated Hybrid Oracle: " + outputOracle);
    }
    
    private static void printUsage() {
        System.out.println("Ziffel 2.0 FSM Generator CLI");
        System.out.println("=============================");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  grammar <output-file> <max-states> [max-transitions] [end-probability]");
        System.out.println("    Generate FSM using grammar templates");
        System.out.println();
        System.out.println("  wikipedia <query> <max-results> <output-fsm> <output-oracle>");
        System.out.println("    Generate FSM using Wikipedia content");
        System.out.println();
        System.out.println("  hybrid <wikipedia-query> <max-results> <output-fsm> <output-oracle>");
        System.out.println("    Generate FSM combining grammar and Wikipedia");
        System.out.println();
        System.out.println("  help");
        System.out.println("    Show this usage information");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -cp target/classes com.ziffel.cli.FsmGeneratorCLI grammar fsm.json 5 3 0.4");
        System.out.println("  java -cp target/classes com.ziffel.cli.FsmGeneratorCLI wikipedia \"artificial intelligence\" 3 fsm.json oracle.json");
        System.out.println("  java -cp target/classes com.ziffel.cli.FsmGeneratorCLI hybrid \"machine learning\" 2 fsm.json oracle.json");
    }
} 