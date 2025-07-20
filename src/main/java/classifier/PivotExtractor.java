package classifier;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Extracts pivot questions from user prompts based on intent and orientation
 */
public class PivotExtractor {
    
    private static final Map<String, String> pivotTemplates = new HashMap<>();
    
    static {
        // Pivot questions by intent
        pivotTemplates.put("user.burnout", "What's one small thing you could do to take care of yourself today?");
        pivotTemplates.put("user.fear", "Where do you think that fear comes from?");
        pivotTemplates.put("user.stuck", "What's holding you back from moving forward?");
        pivotTemplates.put("user.seekingMeaning", "What matters most to you right now?");
        pivotTemplates.put("user.seekingFix", "What would success look like for you in this situation?");
        pivotTemplates.put("user.needsStructure", "What's one habit you could start with?");
        pivotTemplates.put("user.unmotivated", "What's one thing you could do today, no matter how small?");
        
        // Default pivots by orientation
        pivotTemplates.put("practical", "What's the first step you could take?");
        pivotTemplates.put("emotional", "How are you feeling about this?");
        pivotTemplates.put("spiritual", "What does your heart tell you?");
        pivotTemplates.put("reflective", "What have you learned about yourself?");
        pivotTemplates.put("mixed", "What aspect of this feels most important to you?");
        pivotTemplates.put("neutral", "What would be most helpful for you right now?");
    }
    
    /**
     * Extracts a pivot question from a user prompt
     */
    public static String extractFrom(String prompt, String intent, String orientation) {
        // First try to get pivot based on intent
        String intentPivot = pivotTemplates.get(intent);
        if (intentPivot != null) {
            return intentPivot;
        }
        
        // Fall back to orientation-based pivot
        String orientationPivot = pivotTemplates.get(orientation);
        if (orientationPivot != null) {
            return orientationPivot;
        }
        
        // Default pivot
        return "What would be most helpful for you right now?";
    }
    
    /**
     * Generates a pivot question based on the prompt content
     */
    public static String generatePivotFromPrompt(String prompt) {
        prompt = prompt.toLowerCase();
        
        if (prompt.contains("burnout") || prompt.contains("tired") || prompt.contains("exhausted")) {
            return "What's one small thing you could do to take care of yourself today?";
        }
        
        if (prompt.contains("fear") || prompt.contains("afraid") || prompt.contains("scared")) {
            return "Where do you think that fear comes from?";
        }
        
        if (prompt.contains("stuck") || prompt.contains("trapped") || prompt.contains("can't move")) {
            return "What's holding you back from moving forward?";
        }
        
        if (prompt.contains("meaning") || prompt.contains("purpose") || prompt.contains("point")) {
            return "What matters most to you right now?";
        }
        
        if (prompt.contains("routine") || prompt.contains("schedule") || prompt.contains("structure")) {
            return "What's one habit you could start with?";
        }
        
        if (prompt.contains("motivation") || prompt.contains("unmotivated") || prompt.contains("can't start")) {
            return "What's one thing you could do today, no matter how small?";
        }
        
        // Default pivot
        return "What would be most helpful for you right now?";
    }
} 