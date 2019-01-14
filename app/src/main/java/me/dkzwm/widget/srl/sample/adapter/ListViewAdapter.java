package me.dkzwm.widget.srl.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class ListViewAdapter extends BaseAdapter {
    private List<String> mList;
    private LayoutInflater mInflater;
    private Context mContext;

    public ListViewAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
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
            convertView = mInflater.inflate(R.layout.layout_list_view_item, parent, false);
            holder = new ListViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ListViewHolder) convertView.getTag();
        }
        holder.mTextView.setText(String.valueOf(position));
        holder.mTextView.setTag(position);
        Glide.with(mContext).asBitmap().load(mList.get(position)).into(holder.mImageView);
        return convertView;
    }

    private class ListViewHolder {
        private TextView mTextView;
        private ImageView mImageView;

        private ListViewHolder(View view) {
            mImageView = view.findViewById(R.id.imageView_list_item);
            mTextView = view.findViewById(R.id.textView_list_item);
            view.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Object obj = mTextView.getTag();
                            if (obj instanceof Integer) {
                                Toast.makeText(v.getContext(), "Click:" + obj, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
            view.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Object obj = mTextView.getTag();
                            if (obj instanceof Integer) {
                                Toast.makeText(
                                                v.getContext(),
                                                "LongClick:" + obj,
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                            return true;
                        }
                    });
        }
    }
}
