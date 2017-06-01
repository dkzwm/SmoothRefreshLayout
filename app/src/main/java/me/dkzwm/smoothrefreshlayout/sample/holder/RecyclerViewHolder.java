package me.dkzwm.smoothrefreshlayout.sample.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import me.dkzwm.smoothrefreshlayout.sample.R;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */

public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;

    public RecyclerViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.textView_list_item);
    }

    public void setData(String data) {
        mTextView.setText(data);
    }

}
