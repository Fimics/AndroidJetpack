package com.mic.nav.volces.llm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LineResult {

    private static final String STOP="[DONE]";
    public String content;
    public String role;
    public String id;
    public String sid;
    public boolean isEnd = false;
    public String prompt;

    public static LineResult fromString(String line,String sid) {
        if (!line.startsWith("data:")){
            return null;
        }
        LineResult result = new LineResult();
        String jsonStr = line.substring(5);
        result.isEnd = jsonStr.equals(STOP);
        if (result.isEnd){
            result.content="。";
            result.sid=sid;
            return result;
        }

        try {
            JSONObject json = new JSONObject(jsonStr);
            result.id = json.getString("id");
            result.sid = sid;
            JSONArray choices= json.getJSONArray("choices");
            JSONObject choice = choices.getJSONObject(0);
            JSONObject delta = choice.getJSONObject("delta");
            result.content = delta.getString("content");
            result.role = delta.getString("role");

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String toString() {
        return "LineResult [content=" + content + ", role=" + role + ", id=" + id + ", sid=" + sid + "]";
    }
}
