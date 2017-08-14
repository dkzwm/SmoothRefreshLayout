package me.dkzwm.widget.srl.sample.holder;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import me.dkzwm.widget.sample.R;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class ListViewHolder {
    private TextView mTextView;

    public ListViewHolder(View view) {
        mTextView = (TextView) view.findViewById(R.id.textView_list_item);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object obj = v.getTag();
                if (obj instanceof Integer) {
                    Toast.makeText(v.getContext(), "Click:" + obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Object obj = v.getTag();
                if (obj instanceof Integer) {
                    Toast.makeText(v.getContext(), "LongClick:" + obj, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    public void setData(String data, int position) {
        mTextView.setText(data);
        mTextView.setTag(position);
    }
}
