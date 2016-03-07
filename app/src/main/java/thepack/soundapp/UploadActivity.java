package thepack.soundapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class UploadActivity extends FragmentActivity {

    private Button uploadButton;
    private File uploadFile;
    private static final int FILE_SELECT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadButton = (Button) findViewById(R.id.filechoose);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileChoose = new Intent();
                startActivityForResult(
                        Intent.createChooser(fileChoose, "Select a File to Upload"),
                        FILE_SELECT_CODE
                );
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == FILE_SELECT_CODE) {
            if (data == null) {
                return;
            }
            uploadFile = new File(data.getStringExtra("Upload File Path"));
        }
    }
}
