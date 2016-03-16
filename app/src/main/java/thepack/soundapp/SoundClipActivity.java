package thepack.soundapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import thepack.soundapp.entities.SoundResult;
import thepack.soundapp.utils.Util;

public class SoundClipActivity extends FragmentActivity {

    private Button searchButton;
    private MultiAutoCompleteTextView searchField;

    private static final String REST_SEARCH_URL =
            "http://127.0.0.1:8080/rest/api/soundclip/crud/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_clip);

        searchField = (MultiAutoCompleteTextView) findViewById(R.id.searchField);

        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String query = searchField.getText().toString();

                boolean cancel = false;
                View focusView = null;

                // Check for empty query
                if (query.isEmpty()) {
                    searchField.setError(getString(R.string.error_field_required));
                    focusView = searchField;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
//                    showProgress(true);
                    new SearchTask().execute(query);
                }
            }
        });
    }

    // GET request template
    private class GetStringRequest extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String response = "No response";
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(REST_SEARCH_URL).openConnection();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(conn.getResponseMessage() +": with " + REST_SEARCH_URL);
                }
                response = Util.getResponseString(conn);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(SoundClipActivity.this, s, Toast.LENGTH_LONG).show();
        }
    }


    private class SearchTask extends AsyncTask<String, Void, List<SoundResult>> {

        @Override
        protected List<SoundResult> doInBackground(String... params) {
            String query = params[0];
            List<SoundResult> results = null;

            // Send search request to server
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(REST_SEARCH_URL + query).openConnection();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) cancel(true);
                    throw new IOException(conn.getResponseMessage() +": with " + REST_SEARCH_URL + query);
                }
                results = Util.parseResultJson(Util.getResponseString(conn));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<SoundResult> soundResults) {
            super.onPostExecute(soundResults);
            // TODO Display the results
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            // TODO Display no result notification
            Toast.makeText(SoundClipActivity.this, "No Results found", Toast.LENGTH_LONG).show();
        }
    }
}
