package arnes.respati.mqtt_app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MyAdapter extends BaseAdapter {

    private ArrayList <String> beacons = new ArrayList<>();
//    private Object[] beacons = new Object[10];
    private Context context;

    public MyAdapter(Context context, ArrayList<String> names) {
        this.context = context;
        this.beacons = names;
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Object getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final viewHolder holder;
        LayoutInflater layoutInflater;

        if (convertView == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_items, null);

            holder = new viewHolder();
            holder.tvLists = (TextView) convertView.findViewById(R.id.tvLists);
            convertView.setTag(holder);
        }

        else {
            holder = (viewHolder) convertView.getTag();
        }

        holder.tvLists.setText(beacons.get(position));
        return convertView;
    }

    public class viewHolder {
        TextView tvLists;
    }
}
