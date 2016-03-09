package thepack.soundapp;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UploadActivity extends FragmentActivity {

    private Button chooseButton, uploadButton;
    private TextView titleView;

    private File uploadFile;

    private static final int FILE_SELECT_CODE = 0;
    private static final String REST_UPLOAD_URL = R.string.REST_UPLOAD;

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
                // TODO Upload selected file to REST controller
//                Toast.makeText(UploadActivity.this, R.string.upload_success, Toast.LENGTH_LONG).show();
                if (!isNetworkAvailableAndConnected()) {
                    Toast.makeText(UploadActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                    return;
                }

                new GetUpload().execute();
//                AsyncTask upload = new UploadTask(uploadFile);
//                upload.execute();
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

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }

    private class GetUpload extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String response = "No response";
            HttpURLConnection conn = null;
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                conn = (HttpURLConnection) new URL(REST_UPLOAD_URL).openConnection();
                InputStream in = conn.getInputStream();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(conn.getResponseMessage() +": with " + REST_UPLOAD_URL);
                }
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead= in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                response = new String(out.toByteArray());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(UploadActivity.this, s, Toast.LENGTH_LONG).show();
        }
    }

    private class UploadTask extends AsyncTask<Void,Void,Void> {

        File uploadFile;

        public UploadTask(File uploadFile) {
            this.uploadFile = uploadFile;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String encodedFile = encodeFile();

//            URL url = new URL(REST_UPLOAD_URL);
//            HttpURLConnection conn = (HttpURLConnection) new URL(REST_UPLOAD_URL).openConnection();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private String encodeFile() {
            InputStream in = null;
            ByteArrayOutputStream out = null;
            String encodedFile = null;
            try {
                byte[] buffer = new byte[2048];
                in = new FileInputStream(uploadFile);
                out = new ByteArrayOutputStream((int)uploadFile.length());

                int read = in.read(buffer);
                while (read != -1) {
                    out.write(buffer, 0, read);
                }
                encodedFile = Base64.encodeToString(buffer,Base64.DEFAULT);
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
            return encodedFile;
        }
    }
}
