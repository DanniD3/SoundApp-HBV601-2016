package thepack.soundapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import thepack.soundapp.R;
import thepack.soundapp.entities.SoundResult;

public class SoundResultAdapter extends ArrayAdapter<SoundResult> {

    private Context c;
    private int id;
    private List<SoundResult> items;

    public SoundResultAdapter(Context context, int textViewResourceId, List<SoundResult> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public SoundResult getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }
        final SoundResult sr = items.get(position);
        if (sr != null) {
            TextView title = (TextView) v.findViewById(R.id.titleView);
            TextView uploader = (TextView) v.findViewById(R.id.uploaderView);

            if(title!=null)
                title.setText(sr.getName());
            if(uploader!=null)
                uploader.setText(sr.getUploader());
        }
        return v;
    }
}
