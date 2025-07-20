package classifier;

import model.DialogTurn;

import java.util.*;
import java.util.regex.Pattern;

public class OrientationClassifier {

    private static final Map<String, List<Pattern>> orientationPatterns = new HashMap<>();

    static {
        orientationPatterns.put("spiritual", List.of(
                Pattern.compile("\\b(purpose|meaning|soul|spiritual|fulfillment)\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b(lost|empty|what really matters)\\b")
        ));

        orientationPatterns.put("practical", List.of(
                Pattern.compile("\\b(schedule|routine|plan|goal|productivity|exercise|habits|diet)\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b(do|fix|get|make|implement)\\b")
        ));

        orientationPatterns.put("reflective", List.of(
                Pattern.compile("\\b(think|reflect|consider|realize|feel|ponder|understand)\\b", Pattern.CASE_INSENSITIVE)
        ));
    }

    /**
     * Classify the orientation of a given dialog turn.
     * @param turn The dialog turn
     * @return A string like "practical", "spiritual", "reflective", or "mixed"
     */
    public static String classify(DialogTurn turn) {
        Set<String> matched = new HashSet<>();
        String msg = turn.getMessage();

        for (Map.Entry<String, List<Pattern>> entry : orientationPatterns.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(msg).find()) {
                    matched.add(entry.getKey());
                    break;
                }
            }
        }

        if (matched.size() == 1) return matched.iterator().next();
        if (matched.size() > 1) return "mixed";
        return "neutral";
    }
}
