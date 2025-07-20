package classifier;

import model.DialogTurn;


import java.util.*;

public class PivotExtractorFromReddit {

    private final Map<String, List<String>> pivotByIntent = new HashMap<>();

    public void analyzeTurns(List<DialogTurn> turns, Map<Integer, String> intentMap) {
        for (int i = 0; i < turns.size(); i++) {
            DialogTurn turn = turns.get(i);

            if (isAssistant(turn) && turn.toString().trim().endsWith("?")) {
                String intent = intentMap.getOrDefault(i, "unknown");
                pivotByIntent.computeIfAbsent(intent, k -> new ArrayList<>()).add(cleanQuestion(turn.toString()));
            }
        }
    }

    public String getBestPivot(String intent) {
        List<String> options = pivotByIntent.getOrDefault(intent, List.of());
        return options.isEmpty() ? null : options.get(0);
    }

    public Map<String, List<String>> getAllLearnedPivots() {
        return pivotByIntent;
    }

    private boolean isAssistant(DialogTurn turn) {
        return turn.getSpeaker().toLowerCase().contains("assistant") || turn.getSpeaker().equalsIgnoreCase("user2");
    }

    private String cleanQuestion(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }
}
