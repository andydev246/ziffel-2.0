package classifier;

import java.util.HashMap;
import java.util.Map;

public class ToneClassifier {
    
    private static final Map<String, String> toneKeywords = new HashMap<>();
    
    static {
        toneKeywords.put("empathetic", "feel, feeling, understand, tough, difficult, hard");
        toneKeywords.put("encouraging", "can, will, try, start, begin, positive, good");
        toneKeywords.put("reassuring", "normal, common, okay, fine, alright, don't worry");
        toneKeywords.put("practical", "step, plan, schedule, routine, habit, action");
        toneKeywords.put("reflective", "think, thought, consider, reflect, why, what");
    }
    
    public static String classify(String text) {
        text = text.toLowerCase();
        
        for (Map.Entry<String, String> entry : toneKeywords.entrySet()) {
            String tone = entry.getKey();
            String keywords = entry.getValue();
            
            for (String keyword : keywords.split(", ")) {
                if (text.contains(keyword)) {
                    return tone;
                }
            }
        }
        
        return "neutral";
    }
} 