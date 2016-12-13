package me.moxun.dreamcatcher.connector.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by moxun on 16/3/17.
 */
public class LogFilter {
    public static JSONObject filter(JSONObject request, JSONObject response) {
        JSONObject result = null;
        try {
            if (response.has("result")) {
                if (!response.getJSONObject("result").has("exceptionDetails")) {
                    result = new JSONObject();
                    result.put("request", request);
                    result.put("response", response);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
