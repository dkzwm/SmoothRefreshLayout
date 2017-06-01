package me.dkzwm.smoothrefreshlayout.sample.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.dkzwm.smoothrefreshlayout.sample.R;
import me.dkzwm.smoothrefreshlayout.sample.holder.RecyclerViewHolder;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    private LayoutInflater mInflater;
    private List<String> mList = new ArrayList<>();

    public RecyclerViewAdapter(LayoutInflater inflater) {
        mInflater = inflater;
    }

    public void updateData(List<String> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void appendData(List<String> list) {
        int size = mList.size();
        mList.addAll(list);
        notifyItemInserted(size);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_view_item, parent,false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.setData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
