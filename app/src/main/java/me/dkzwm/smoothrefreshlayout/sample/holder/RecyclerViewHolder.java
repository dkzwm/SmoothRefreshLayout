package me.dkzwm.smoothrefreshlayout.sample.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(),"Click:"+getAdapterPosition(),Toast.LENGTH_SHORT).show();
            }
        });
        mTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(v.getContext(),"LongClick:"+getAdapterPosition(),Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

}
