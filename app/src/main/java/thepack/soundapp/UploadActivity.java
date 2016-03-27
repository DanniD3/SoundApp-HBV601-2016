package thepack.soundapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
            if (data == null)  return;

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
            this.fileEncData = encodeFile(uploadFile);
            this.fileName = uploadFile.getName();
            this.fileExt = upFileExt;
            this.uploader = null;
            this.isPrivate = false;
        }

        /*
            Extract file data from actual file in storage and encode for transfer
            @params uploadFile is the file link in storage to be transferred
         */
        private String encodeFile(File uploadFile) {
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
                Toast.makeText(UploadActivity.this, Integer.toString(data.length()), Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException fnfe) {
                Toast.makeText(UploadActivity.this, R.string.file_not_found, Toast.LENGTH_LONG).show();
                fnfe.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(UploadActivity.this, R.string.file_access_fail, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                try {
                    if (in != null)
                        in.close();
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    Toast.makeText(UploadActivity.this, R.string.file_access_fail, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            return data;
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

                // Get response from server
                if (conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    throw new IOException(conn.getResponseMessage() +": with " + REST_UPLOAD_URL);
                }
                response = Util.getResponseString(conn);
            } catch (IOException | JSONException e) {
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
            if (!s.isEmpty())
                Toast.makeText(UploadActivity.this, s, Toast.LENGTH_LONG).show();
        }
    }
}
