package parser;

import model.DialogTurn;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.*;

public class MarkdownParser {

    /**
     * Parses a markdown-style Reddit thread file into dialog turns.
     * @param markdownFilePath Path to .md file (from RedditMarkdownFetcher)
     * @return List of dialog turns
     * @throws IOException If the file cannot be read
     */
    public static List<DialogTurn> parseDialogTurns(String markdownFilePath) throws IOException {
        List<DialogTurn> turns = new ArrayList<>();
        int turnCounter = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(markdownFilePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("**") && line.contains("**:")) {
                    // Extract speaker
                    int start = line.indexOf("**") + 2;
                    int end = line.indexOf("**:", start);
                    String speakerRaw = line.substring(start, end).trim();
                    String message = line.substring(end + 3).trim();

                    int depth = (line.indexOf(speakerRaw) - 2) / 2; // every 2 spaces is 1 depth

                    DialogTurn turn = new DialogTurn(
                            "t" + turnCounter++,
                            speakerRaw,
                            message,
                            depth
                    );
                    turns.add(turn);
                }
            }
        }

        return turns;
    }


    public static void writeAsJson(List<DialogTurn> turns, String outputPath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(outputPath)) {
            gson.toJson(turns, writer);
        }
    }

}

