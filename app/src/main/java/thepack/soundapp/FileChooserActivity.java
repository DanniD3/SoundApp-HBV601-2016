package thepack.soundapp;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import thepack.soundapp.adapters.FileArrayAdapter;
import thepack.soundapp.entities.Option;

public class FileChooserActivity extends ListActivity {

    private File currentDir;
    private FileArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File(Environment.getExternalStorageDirectory().getPath());
        fill(currentDir);
    }

    /**
     * Lists all the folders and files of the current directory
     *
     * @param currentDir the File object holding the current directory
     */
    private void fill(File currentDir) {
        this.setTitle("Current Directory: " + currentDir.getName());

        File[] dirs = currentDir.listFiles();
        List<Option> dir = new ArrayList<Option>();
        List<Option> fls = new ArrayList<Option>();
        try {
            for(File ff: dirs) {
                if(ff.isDirectory())
                    dir.add(new Option(ff.getName(), "Folder", ff.getAbsolutePath()));
                else {
                    fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
                }
            }
        } catch(Exception e) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if(!currentDir.getName().equalsIgnoreCase(new File(
                Environment.getExternalStorageDirectory().getPath()).getName()))
            dir.add(0,new Option("..","Parent Directory",currentDir.getParent()));
        adapter = new FileArrayAdapter(
                FileChooserActivity.this,
                R.layout.view_file_chooser,
                dir
        );
        this.setListAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Option o = adapter.getItem(0);
        // Move up a folder when back
        if(o.getData().equalsIgnoreCase("folder") ||
                o.getData().equalsIgnoreCase("parent directory")){
            currentDir = new File(o.getPath());
            fill(currentDir);
            Toast.makeText(this, R.string.file_back_hold, Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) finish();
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
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

    /**
     * Handler for file click on option {@param o}
     * @param o is the select Option
     */
    private void onFileClick(Option o) {
        Toast.makeText(this, "File Selected: " + o.getName(), Toast.LENGTH_SHORT).show();
        Intent uploadFile = new Intent();
        uploadFile.putExtra("filepath", o.getPath());
        setResult(Activity.RESULT_OK, uploadFile);
        finish();
    }
}
