package thepack.soundapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import thepack.soundapp.entities.SoundResult;
import thepack.soundapp.utils.Util;

public class SoundClipFragment extends Fragment {

    private Button searchButton;
    private MultiAutoCompleteTextView searchField;
    private ListView resultListView;

    private SoundResultAdapter srAdapter;
    private List<SoundResult> results;

    private Activity act;

    private static final String REST_SEARCH_URL =
            "http://" + Util.HOST_URL + "/rest/api/soundclip/crud/";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the calling activity for reference
        act = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sound_clip, container, false);

        searchField = (MultiAutoCompleteTextView) rootView.findViewById(R.id.searchField);
        searchButton = (Button) rootView.findViewById(R.id.searchButton);
        resultListView = (ListView) rootView.findViewById(R.id.searchResults);
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

                    // Reset input tools
                    searchField.setError(null);
                    Util.hideKeyboardFromView(act, searchField);

                    // Check for Internet connection
                    if (!Util.isNetworkAvailableAndConnected(act, Activity.CONNECTIVITY_SERVICE)) {
                        Toast.makeText(act, R.string.error_no_network, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Do search
                    new SearchTask().execute(query);
                }
            }
        });

        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                    Display the details of the selected SoundResult in another activity
                 */
                SoundResult sr = (SoundResult) resultListView.getItemAtPosition(position);
                Intent detailActivity = new Intent(act, SoundDetailActivity.class);
                detailActivity.putExtra("SoundResult", sr);
                startActivity(detailActivity);
            }
        });

        return rootView;
    }

    /**
     * Displays search results in the ListView
     */
    private void displayResults() {
        if (results == null) return;

        // Need to execute on separate thread to update UI
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                srAdapter = new SoundResultAdapter(
                        act,
                        R.layout.view_search_result,
                        results
                );
                resultListView.setAdapter(srAdapter);
            }
        });
    }

    /**
     * Clears all search results in the ListView
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
            Toast.makeText(act, s, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * An AsyncTask that queries the server for a result
     */
    private class SearchTask extends AsyncTask<String, Void, List<SoundResult>> {

        @Override
        protected List<SoundResult> doInBackground(String... params) {
            String query = params[0];
            List<SoundResult> results = null;

            // Send search request to server
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(REST_SEARCH_URL + query).openConnection();
                // Check for server response
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                        cancel(true);
                    else
                        throw new IOException(conn.getResponseMessage() +": with " + REST_SEARCH_URL + query);
                }
                // Receive response data from server
                results = Util.parseSoundResultListJson(Util.getResponseString(conn));
                conn.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<SoundResult> soundResults) {
            super.onPostExecute(soundResults);
            try {
                String numResultsDisplay = "There are " + soundResults.size() + " results matching your query";
                Toast.makeText(act, numResultsDisplay, Toast.LENGTH_LONG).show();

                results = soundResults;
                displayResults();
            } catch (NullPointerException e) {
                Toast.makeText(act, R.string.search_invalid, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            clearResults();
            Toast.makeText(act, "No Results found", Toast.LENGTH_LONG).show();
        }
    }
}
