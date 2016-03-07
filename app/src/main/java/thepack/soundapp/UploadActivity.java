package thepack.soundapp;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UploadActivity extends FragmentActivity {

    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadButton = (Button) findViewById(R.id.login);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(UploadActivity.this, UserActivity.class));
            }
        });
    }
}
