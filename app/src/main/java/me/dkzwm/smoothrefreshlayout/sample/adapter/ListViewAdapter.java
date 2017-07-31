package me.dkzwm.smoothrefreshlayout.sample.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import me.dkzwm.smoothrefreshlayout.sample.R;
import me.dkzwm.smoothrefreshlayout.sample.holder.ListViewHolder;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class ListViewAdapter extends BaseAdapter {
    private List<String> mList;
    private LayoutInflater mInflater;

    public ListViewAdapter(LayoutInflater inflater) {
        mInflater = inflater;
        mList = new ArrayList<>();
    }

    public void updateData(List<String> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void appendData(List<String> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_list_view_item, parent,false);
            holder = new ListViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ListViewHolder) convertView.getTag();
        }
        holder.setData(mList.get(position),position);
        return convertView;
    }

}
