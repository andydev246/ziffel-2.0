package fetcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RedditMarkdownFetcher {

    private static final String API_URL_TEMPLATE =
            "https://www.reddit.com/r/%s/comments/%s.json";

    /**
     * Downloads and extracts dialog turns from a Reddit thread (post and comments).
     * @param subreddit Subreddit name (e.g. "DecidingToBeBetter")
     * @param threadId Reddit thread ID (e.g. "im_scared_of_just_being_myself_and_even_forming")
     * @param outputFilePath Markdown-like file for extracted turns
     */
    public void fetchAndExtractMarkdown(String subreddit, String threadId, String outputFilePath) throws IOException {
        String apiUrl = String.format(API_URL_TEMPLATE, subreddit, threadId);

        System.out.println(apiUrl);

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Ziffel Bot)");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new IOException("Failed to fetch Reddit thread: HTTP " + status);
        }

        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }

        JSONArray root = new JSONArray(jsonBuilder.toString());
        JSONObject postData = root.getJSONObject(0)
                .getJSONObject("data")
                .getJSONArray("children")
                .getJSONObject(0)
                .getJSONObject("data");

        JSONObject commentTree = root.getJSONObject(1)
                .getJSONObject("data");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, StandardCharsets.UTF_8))) {
            writer.write("**[POST]** " + postData.getString("author") + ": " + postData.getString("title") + "\n\n");
            writer.write(postData.optString("selftext", "") + "\n\n");

            JSONArray comments = commentTree.getJSONArray("children");
            for (int i = 0; i < comments.length(); i++) {
                extractComment(comments.getJSONObject(i), writer, 0);
            }

            System.out.println("✅ Extracted dialog saved to: " + outputFilePath);
        }
    }

    private void extractComment(JSONObject commentNode, BufferedWriter writer, int depth) throws IOException {
        if (!commentNode.getString("kind").equals("t1")) return;

        JSONObject data = commentNode.getJSONObject("data");
        String author = data.optString("author", "[deleted]");
        String body = data.optString("body", "").replaceAll("\n", " ").trim();

        if (!body.isEmpty()) {
            writer.write("**" + indent(depth) + author + "**: " + body + "\n");
        }

        if (data.has("replies") && !data.isNull("replies")) {
            Object repliesObj = data.get("replies");
            
            // Handle case where replies is a string (no replies) vs JSONObject (has replies)
            if (repliesObj instanceof JSONObject) {
                JSONObject repliesData = (JSONObject) repliesObj;
                if (repliesData.has("data")) {
                    JSONObject dataObj = repliesData.getJSONObject("data");
                    if (dataObj.has("children")) {
                        JSONArray children = dataObj.getJSONArray("children");
                        for (int i = 0; i < children.length(); i++) {
                            extractComment(children.getJSONObject(i), writer, depth + 1);
                        }
                    }
                }
            }
            // If replies is a string, it means no replies, so we skip
        }
    }

    private String indent(int depth) {
        return depth > 0 ? "  ".repeat(depth) : "";
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java fetcher.RedditMarkdownFetcher <subreddit> <threadId> <outputFile>");
            return;
        }

        try {
            new RedditMarkdownFetcher().fetchAndExtractMarkdown(args[0], args[1], args[2]);
        } catch (IOException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}
