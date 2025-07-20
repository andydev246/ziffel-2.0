package utils;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;

public class ClusteredIntentLoader {

    public static Map<String, String> loadUtteranceIntentMap(String filePath) throws Exception {
        Map<String, String> intentMap = new HashMap<>();
        Gson gson = new Gson();

        JsonObject json = JsonParser.parseReader(new FileReader(filePath)).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String intent = entry.getKey();
            JsonArray utterances = entry.getValue().getAsJsonArray();

            for (JsonElement utteranceElem : utterances) {
                String utterance = utteranceElem.getAsString();
                intentMap.put(utterance.trim(), intent);
            }
        }

        return intentMap;
    }
}
