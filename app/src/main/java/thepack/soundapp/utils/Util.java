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
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;

import thepack.soundapp.R;
import thepack.soundapp.entities.SoundClip;
import thepack.soundapp.entities.SoundResult;
import thepack.soundapp.entities.User;

public class Util {

//    public static final String HOST_URL = "192.168.1.98:8080";
    public static final String HOST_URL = "44181e2a.ngrok.io";
//    public static final String HOST_URL = "10.0.2.2:8080";
//    public static final String HOST_URL = "127.0.0.1:8080";

    /**
     * Extract file data from actual file in storage and encode for transfer
     *
     * @param c is the context of the calling Activity
     * @param uploadFile is the file link in storage to be transferred
     * @return returns the encoded {@param uploadFile}
     */
    public static String encodeFile(Context c, File uploadFile) {
        InputStream in;
        ByteArrayOutputStream out;
        String data = null;
        try {
            byte[] buffer = new byte[2048];
            in = new FileInputStream(uploadFile);
            out = new ByteArrayOutputStream((int)uploadFile.length());

            int read;
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

    /**
     * Checks the connectivity to the Internet of the device
     *
     * @param c is the context of the calling Activity
     * @param CONNECTIVITY_SERVICE is the network flag of the context
     * @return true if network is connected, false otherwise
     */
    public static boolean isNetworkAvailableAndConnected(Context c, String CONNECTIVITY_SERVICE) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Sets the connection {@param conn} to 'POST' mode
     *
     * @param conn is the connected connection
     * @return the {@param conn} in 'POST' mode
     */
    public static HttpURLConnection setPostConnection(HttpURLConnection conn) {
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(2048);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Sends the json object {@param POST_PARAM} over the connection {@param conn}
     *
     * @param conn is the connected connection
     * @param POST_PARAM is the json object to be sent
     * @throws IOException
     */
    public static void sendPostJSON(HttpURLConnection conn, JSONObject POST_PARAM) throws IOException {
        OutputStream out = new BufferedOutputStream(conn.getOutputStream());
        out.write(POST_PARAM.toString().getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    /**
     * Receives response from the connected connection {@param conn} as String
     *
     * @param conn is the connected connection
     * @return the response from {@param conn} as String
     * @throws IOException
     */
    public static String getResponseString(HttpURLConnection conn) throws IOException {
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        return new String(out.toByteArray());
    }

    /**
     * Parses the received json array string as a list of SoundResults
     *
     * @param jsonString is the json array to be parsed
     * @return a list of SoundResults from the {@param jsonString}
     * @throws JSONException
     */
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

    /**
     * Parses the received json object string as a SoundClip
     *
     * @param jsonString is the json object to be parsed
     * @return a SoundClip
     * @throws JSONException
     */
    public static SoundClip parseSoundClipJson(String jsonString) throws JSONException {
        JSONObject jsonResult = new JSONObject(jsonString);

        return new SoundClip(
                jsonResult.getLong("id"),
                jsonResult.getString("name"),
                jsonResult.getString("ext"),
                jsonResult.getString("data"),
                jsonResult.getString("uploader"),
                jsonResult.getBoolean("isPrivate")
        );
    }

    /**
     * Parses the received json object string as a User
     *
     * @param jsonString is the json object to be parsed
     * @return a User
     * @throws JSONException
     */
    public static User parseUserJson(String jsonString) throws JSONException {
        JSONObject userJson = new JSONObject(jsonString);

        return new User(
                userJson.getLong("id"),
                userJson.getString("name"),
                userJson.getString("pw")
        );
    }

    /**
     * Hides the device's keyboard from view
     *
     * @param c is the context of the calling Activity
     * @param v is the input view
     */
    public static void hideKeyboardFromView(Context c, View v) {
        InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();
    }
}
