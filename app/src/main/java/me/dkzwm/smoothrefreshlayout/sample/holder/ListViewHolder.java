package me.dkzwm.smoothrefreshlayout.sample.holder;

import android.view.View;
import android.widget.TextView;

import me.dkzwm.smoothrefreshlayout.sample.R;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class ListViewHolder {
    private TextView mTextView;

    public ListViewHolder(View view) {
        mTextView = (TextView) view.findViewById(R.id.textView_list_item);
    }

    public void setData(String data) {
        mTextView.setText(data);
    }
}
