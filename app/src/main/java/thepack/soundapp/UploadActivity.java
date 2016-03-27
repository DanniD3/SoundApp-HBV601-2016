package thepack.soundapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import thepack.soundapp.utils.Util;

public class UploadActivity extends FragmentActivity {

    private Button chooseButton, uploadButton;
    private TextView titleView;

    private File uploadFile;

    private static final int FILE_SELECT_CODE = 0;
    private static final String REST_UPLOAD_URL =
            "http://" + Util.HOST_URL + "/rest/api/soundclip/crud/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        titleView = (TextView) findViewById(R.id.uploadTitle);
        chooseButton = (Button) findViewById(R.id.chooseButton);
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileChoose = new Intent(UploadActivity.this, FileChooserActivity.class);
                startActivityForResult(fileChoose, FILE_SELECT_CODE);
            }
        });
        uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for MimeType before upload
                Uri uploadFileUri = Uri.fromFile(uploadFile);
                String upFileExt = MimeTypeMap.getFileExtensionFromUrl(uploadFileUri.toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(upFileExt);
                if (mimeType == null || !mimeType.split("/")[0].equalsIgnoreCase("audio")) {
                    Toast.makeText(UploadActivity.this, R.string.file_invalid, Toast.LENGTH_LONG).show();
                    return;
                }

                // Check for Internet connection
                if (!Util.isNetworkAvailableAndConnected(UploadActivity.this, CONNECTIVITY_SERVICE)) {
                    Toast.makeText(UploadActivity.this, R.string.error_no_network, Toast.LENGTH_LONG).show();
                    return;
                }

                // Do upload
                new UploadTask(uploadFile, upFileExt).execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == FILE_SELECT_CODE) {
            if (data == null) return;
            // Retrieves file uri from FileChooserActivity
            uploadFile = new File(data.getStringExtra("filepath"));
            titleView.setText(uploadFile.getName());
            uploadButton.setEnabled(true);
        }
    }

    private class UploadTask extends AsyncTask<Void,Void,String> {

        String fileEncData;
        String fileName;
        String fileExt;
        String uploader;
        boolean isPrivate;

        public UploadTask(File uploadFile, String upFileExt) {
            this.fileEncData = Util.encodeFile(UploadActivity.this, uploadFile);
            this.fileName = uploadFile.getName();
            this.fileExt = upFileExt;
            this.uploader = null;
            this.isPrivate = false;
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = "No response";
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(REST_UPLOAD_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type","application/json");
                conn.setDoOutput(true);
                conn.setChunkedStreamingMode(2048);

                // POST Upload JSON SoundClip
                JSONObject POST_PARAM = new JSONObject();
                POST_PARAM.put("name", fileName);
                POST_PARAM.put("ext", fileExt);
                POST_PARAM.put("data", fileEncData);
                POST_PARAM.put("uploader", uploader);
                POST_PARAM.put("isPrivate", isPrivate);
                POST_PARAM.put("url", null);
                Util.sendPostJSON(conn, POST_PARAM);

                if (conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    // Check for server response
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_CONFLICT)
                        cancel(true);
                    else
                        throw new IOException(conn.getResponseMessage() +": with " + REST_UPLOAD_URL);
                }
                // Receive uploaded file location from server
                response = conn.getHeaderField("Location");
                conn.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(UploadActivity.this, s, Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            String conflict = "A SoundClip with name " + fileName + " already exist";
            Toast.makeText(UploadActivity.this, conflict, Toast.LENGTH_LONG).show();
        }
    }
}
