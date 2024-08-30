package nl.utwente.di.interactief2.rest_api;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScoreBoardKeeper {

    private static String fileName = "./settings.json";



    public static synchronized boolean getVisible() {
        // Open file
        String content = null;
        try {
            content = Files.readString(Path.of(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Parse JSON
        JSONObject json = new JSONObject(content);
        return json.getBoolean("leaderboardVisible");

    }

    protected static synchronized void setVisible(boolean update) {
        // Open file
        String content = null;
        try {
            content = Files.readString(Path.of(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Parse JSON
        JSONObject json = new JSONObject(content);
        json.put("leaderboardVisible", update);
        try {
            Files.writeString(Path.of(fileName), json.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
