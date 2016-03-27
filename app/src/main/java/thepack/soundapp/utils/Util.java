package thepack.soundapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import thepack.soundapp.R;
import thepack.soundapp.entities.SoundClip;
import thepack.soundapp.entities.SoundResult;
import thepack.soundapp.entities.User;

public class Util {

    public static final String HOST_URL = "192.168.1.98:8080";
//    public static final String HOST_URL = "127.0.0.1:8080";

    /*
        Extract file data from actual file in storage and encode for transfer
        @params uploadFile is the file link in storage to be transferred
     */
    public static String encodeFile(Context c, File uploadFile) {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        String data = null;
        try {
            byte[] buffer = new byte[2048];
            in = new FileInputStream(uploadFile);
            out = new ByteArrayOutputStream((int)uploadFile.length());

            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            data = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
            Toast.makeText(c, Integer.toString(data.length()), Toast.LENGTH_LONG).show();

            // Close streams
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(c, R.string.file_not_found, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(c, R.string.file_access_fail, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return data;
    }

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

    public static List<SoundResult> parseSoundResultListJson(String jsonString) throws JSONException {
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
            // Set default uploader if none
            SoundResult sr = soundResults.get(i);
            if (sr.getUploader().isEmpty()) sr.setUploader("Anonymous");
        }
        return soundResults;
    }

    public static SoundClip parseSoundClipJson(String jsonString) throws JSONException {
        JSONObject jsonResult = new JSONObject(jsonString);
        SoundClip sc = new SoundClip(
                jsonResult.getLong("id"),
                jsonResult.getString("name"),
                jsonResult.getString("ext"),
                jsonResult.getString("data"),
                jsonResult.getString("uploader"),
                jsonResult.getBoolean("isPrivate")
        );
        return sc;
    }

    public static User parseUserJson(String jsonString) throws JSONException {
        JSONObject userJson = new JSONObject(jsonString);

        return new User(
                userJson.getLong("id"),
                userJson.getString("name"),
                userJson.getString("pw")
        );
    }

    public static void hideKeyboardFromView(Context c, View v) {
        InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();
    }
}
