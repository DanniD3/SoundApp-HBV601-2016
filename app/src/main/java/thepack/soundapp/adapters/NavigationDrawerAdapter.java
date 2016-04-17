package thepack.soundapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import thepack.soundapp.R;
import thepack.soundapp.entities.NavDrawerItem;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {

    List<NavDrawerItem> data = Collections.emptyList();
    private LayoutInflater inflater;

    public NavigationDrawerAdapter(Context context, List<NavDrawerItem> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    /**
     * Deletes item at {@param position}
     *
     * @param position is the position of the item to be deleted
     */
    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Replaces the item at {@param position} with {@param item}
     *
     * @param item is the replacing item
     * @param position is the position of the replaced item in data
     */
    public void replaceItem(NavDrawerItem item, int position) {
        data.set(position, item);
        notifyItemChanged(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NavDrawerItem current = data.get(position);
        holder.title.setText(current.getTitle());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.nav_item_title);
        }
    }
}