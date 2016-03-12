package thepack.soundapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import thepack.soundapp.entities.SoundResult;

public class Util {

    public static boolean isNetworkAvailableAndConnected(Context c, String CONNECTIVITY_SERVICE) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }

    public static void sendPostJSON(HttpURLConnection conn, JSONObject POST_PARAM) throws IOException {
        OutputStream out = new BufferedOutputStream(conn.getOutputStream());
        out.write(POST_PARAM.toString().getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    public static String getResponseString(HttpURLConnection conn) throws IOException {
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        return new String(out.toByteArray());
    }

    public static List<SoundResult> parseResultJson(String jsonString) throws JSONException {
        List<SoundResult> soundResults = new ArrayList<>();
        JSONArray jsonResults = new JSONArray(jsonString);

        for (int i = 0; i < jsonResults.length(); i++) {
            JSONObject jsonResult = jsonResults.getJSONObject(i);
            soundResults.add(new SoundResult(
                    jsonResult.getLong("id"),
                    jsonResult.getString("name"),
                    jsonResult.getString("ext"),
                    jsonResult.getString("uploader"),
                    jsonResult.getBoolean("isPrivate")
            ));
        }
        return soundResults;
    }
}
