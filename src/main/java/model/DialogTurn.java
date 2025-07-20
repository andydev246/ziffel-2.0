package model;

public class DialogTurn {
    private String turnId;
    private String speaker;
    private String message;
    private int depth;

    public DialogTurn(String turnId, String speaker, String message, int depth) {
        this.turnId = turnId;
        this.speaker = speaker;
        this.message = message;
        this.depth = depth;
    }

    public String getTurnId() {
        return turnId;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getMessage() {
        return message;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return "[" + depth + "] " + speaker + ": " + message;
    }
}
