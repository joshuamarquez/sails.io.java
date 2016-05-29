package me.joshuamarquez.sails.io.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SIJUtils {

    public static JSONObject getExpectedResponses() throws Exception {
        return readJSONFile("src/test/resources/expectedResponses.json");
    }

    public static JSONObject readJSONFile(String filePath) throws IOException {
        String jsonData = "";
        BufferedReader br = null;

        String line;
        br = new BufferedReader(new FileReader(filePath));
        while ((line = br.readLine()) != null) {
            jsonData += line + "\n";
        }

        if (br != null)
            br.close();

        return new JSONObject(jsonData);
    }

}
