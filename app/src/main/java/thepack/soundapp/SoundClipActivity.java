package thepack.soundapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import thepack.soundapp.adapters.SoundResultAdapter;
import thepack.soundapp.entities.SoundClip;
import thepack.soundapp.entities.SoundResult;
import thepack.soundapp.utils.Util;

public class SoundClipActivity extends FragmentActivity {

    private Button searchButton;
    private MultiAutoCompleteTextView searchField;
    private ListView resultListView;
    private SoundResultAdapter srAdapter;

    private List<SoundResult> results;

    private static final String REST_SEARCH_URL =
            "http://" + Util.HOST_URL + "/rest/api/soundclip/crud/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_clip);

        searchField = (MultiAutoCompleteTextView) findViewById(R.id.searchField);
        searchButton = (Button) findViewById(R.id.searchButton);
        resultListView = (ListView) findViewById(R.id.searchResults);
        resultListView.setClickable(true);

        searchField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Limit input to 1 line only
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    searchButton.callOnClick();
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean cancel = false;
                View focusView = null;

                String query = searchField.getText().toString();

                // Check for empty query
                if (query.isEmpty()) {
                    searchField.setError(getString(R.string.error_field_required));
                    focusView = searchField;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error, focus on error field.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to search
//                    showProgress(true);
                    searchField.setError(null);
                    Util.hideKeyboardFromView(SoundClipActivity.this, searchField);
                    new SearchTask().execute(query);
                }
            }
        });

        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SoundResult sr = (SoundResult) resultListView.getItemAtPosition(position);
                new FetchTask(sr.getId()).execute();
            }
        });
    }

    /*
        Displays search results in the ListView
     */
    private void displayResults() {
        if (results == null) return;

        // Need to execute on separate thread to update UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                srAdapter = new SoundResultAdapter(
                        SoundClipActivity.this,
                        R.layout.view_search_result,
                        results
                );
                resultListView.setAdapter(srAdapter);
            }
        });
    }

    /*
        Clears all search results in the ListView
     */
    private void clearResults() {
        if (results != null) results.clear();
        if (srAdapter != null) srAdapter.notifyDataSetChanged();
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
                    // Check for server response
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                        cancel(true);
                    else
                        throw new IOException(conn.getResponseMessage() +": with " + REST_SEARCH_URL + query);
                }
                // Receive response data from server
                results = Util.parseSoundResultJson(Util.getResponseString(conn));
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
            String numResultsDisplay = "There are " + soundResults.size() + " results matching your query";
            Toast.makeText(SoundClipActivity.this, numResultsDisplay, Toast.LENGTH_LONG).show();

            results = soundResults;
            displayResults();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            clearResults();
            Toast.makeText(SoundClipActivity.this, "No Results found", Toast.LENGTH_LONG).show();
        }
    }

    private class FetchTask extends AsyncTask<Void, Void, SoundClip> {

        private long id;

        public FetchTask(long id) {
            id = id;
        }

        @Override
        protected SoundClip doInBackground(Void... params) {
            SoundClip sc = null;
            // TODO retrieve selected file
            // Send search request to server
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(REST_SEARCH_URL).openConnection();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    // Check for server response
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                        cancel(true);
                    else
                        throw new IOException(conn.getResponseMessage() +": with " + REST_SEARCH_URL);
                }
                // Receive response data from server
                results = Util.parseSoundResultJson(Util.getResponseString(conn));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return sc;
        }

        @Override
        protected void onPostExecute(SoundClip soundClip) {
            super.onPostExecute(soundClip);
            Toast.makeText(SoundClipActivity.this, "You have selected a sound clip." + id, Toast.LENGTH_LONG).show();
        }
    }
}
