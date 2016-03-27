package thepack.soundapp;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import thepack.soundapp.entities.SoundClip;
import thepack.soundapp.entities.SoundResult;
import thepack.soundapp.utils.Util;

public class SoundDetailActivity extends AppCompatActivity {

    private TextView titleView;
    private TextView extensionView;
    private TextView uploaderView;
    private Button playButton;
    private Button downloadButton;

    private static final String REST_SEARCH_URL =
            "http://" + Util.HOST_URL + "/rest/api/soundclip/crud/";

    private static SoundResult sr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_detail);

        // Get the SoundResult to be displayed
        sr = (SoundResult) getIntent().getSerializableExtra("SoundResult");

        // Display details
        titleView = (TextView) findViewById(R.id.detailTitle);
        extensionView = (TextView) findViewById(R.id.detailExtension);
        uploaderView = (TextView) findViewById(R.id.detailUploader);

        titleView.setText(sr.getName());
        extensionView.setText("Extension: " + sr.getExt());
        uploaderView.setText("Uploaded by: " + sr.getUploader());

        // Attach events for MediaPlayer
        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for Internet connection
                if (!Util.isNetworkAvailableAndConnected(SoundDetailActivity.this, CONNECTIVITY_SERVICE)) {
                    Toast.makeText(SoundDetailActivity.this, R.string.error_no_network, Toast.LENGTH_LONG).show();
                    return;
                }
                // Retrieve actual SoundClip from server
                Toast.makeText(SoundDetailActivity.this, "Streaming...", Toast.LENGTH_LONG).show();
                new FetchTask(FetchTask.STREAM).execute(sr.getId());
            }
        });
        downloadButton = (Button) findViewById(R.id.downButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for Internet connection
                if (!Util.isNetworkAvailableAndConnected(SoundDetailActivity.this, CONNECTIVITY_SERVICE)) {
                    Toast.makeText(SoundDetailActivity.this, R.string.error_no_network, Toast.LENGTH_LONG).show();
                    return;
                }
                // Retrieve actual SoundClip from server
                Toast.makeText(SoundDetailActivity.this, "Trying to start download...", Toast.LENGTH_LONG).show();
                new FetchTask(FetchTask.DOWNLOAD).execute(sr.getId());
            }
        });
    }

    /*
        Plays the selected SoundClip
        {@params sc} is the sound clip to be played
     */
    private void playSoundClip(SoundClip sc) {
        // TODO Stream play SoundClip
        Toast.makeText(SoundDetailActivity.this, "Play has not yet been implemented.", Toast.LENGTH_LONG).show();
        // TODO implement actual stream play
    }

    /*
        Stores the selected SoundClip into Downloads
        {@params sc} is the sound clip to be stored
     */
    private void storeDownload(SoundClip sc) {
        if (sc == null) return;

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File scPath = new File(path, "/" + sc.getName());
        try {
            if (!scPath.createNewFile()) {
                // TODO Handles duplicates
                return;
            }
            byte[] buffer = new byte[2048];
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(sc.getData(), Base64.DEFAULT));
            FileOutputStream out = new FileOutputStream(scPath);
            int read = in.read(buffer);
            while (read > 0) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(SoundDetailActivity.this, "Downloaded file size: " +
                (scPath.length()/1024) + "kB", Toast.LENGTH_LONG).show();
    }

    private class FetchTask extends AsyncTask<Long, Void, SoundClip> {

        protected final static int STREAM = 0;
        protected final static int DOWNLOAD = 1;
        private final int flag;

        public FetchTask(int flag) {
            this.flag = flag;
        }

        @Override
        protected SoundClip doInBackground(Long... params) {
            long id = params[0];
            SoundClip sc = null;

            // Send search request to server
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(REST_SEARCH_URL + "?id=" + id).openConnection();
                // Check for server response
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                        cancel(true);
                    else
                        throw new IOException(conn.getResponseMessage() +": with " + REST_SEARCH_URL);
                }
                // Receive response data from server
                sc = Util.parseSoundClipJson(Util.getResponseString(conn));
                conn.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return sc;
        }

        @Override
        protected void onPostExecute(SoundClip soundClip) {
            super.onPostExecute(soundClip);
            if (flag == STREAM) playSoundClip(soundClip);
            if (flag == DOWNLOAD) storeDownload(soundClip);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            String fileNotFound = "The selected file has not been found on the server.";
            Toast.makeText(SoundDetailActivity.this, fileNotFound, Toast.LENGTH_LONG).show();
        }
    }
}
