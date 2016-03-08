package thepack.soundapp;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.nio.*;

import thepack.soundapp.fileChooser.FileArrayAdapter;
import thepack.soundapp.fileChooser.Option;

public class FileChooserActivity extends ListActivity {

    private File currentDir;
    private FileArrayAdapter adapter;
    private static final int FILE_SELECT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File(Environment.getExternalStorageDirectory().getPath());
        fill(currentDir);
    }

    private void fill(File currentDir) {
        this.setTitle("Current Directory: " + currentDir.getName());

        File[] dirs = currentDir.listFiles();
        List<Option> dir = new ArrayList<Option>();
        List<Option> fls = new ArrayList<Option>();
        try {
            for(File ff: dirs)
            {
                if(ff.isDirectory())
                    dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
                else
                {
                    fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
                }
            }
        } catch(Exception e) {
            Toast.makeText(this,R.string.file_chooser_fill_error,Toast.LENGTH_SHORT).show();
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if(!currentDir.getName().equalsIgnoreCase(new File(
                Environment.getExternalStorageDirectory().getPath()).getName()))
            dir.add(0,new Option("..","Parent Directory",currentDir.getParent()));
        adapter = new FileArrayAdapter(
                FileChooserActivity.this,
                R.layout.activity_file_chooser,
                dir
        );
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Option o = adapter.getItem(position);
        if(o.getData().equalsIgnoreCase("folder") ||
                o.getData().equalsIgnoreCase("parent directory")){
            currentDir = new File(o.getPath());
            fill(currentDir);
        } else {
            onFileClick(o);
        }
    }

    private void onFileClick(Option o) {
        // TODO Could check type or let Server check type
        Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_SHORT).show();
        Intent uploadFile = new Intent();
        uploadFile.putExtra("filepath", o.getPath());
        setResult(Activity.RESULT_OK, uploadFile);
        finish();
    }
}
