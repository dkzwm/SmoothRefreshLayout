package me.dkzwm.widget.srl.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class RecyclerViewAdapter
        extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<String> mList = new ArrayList<>();

    public RecyclerViewAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
    }

    public void updateData(List<String> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void insertData(List<String> list) {
        mList.addAll(0, list);
        notifyItemRangeInserted(0, list.size());
    }

    public void appendData(List<String> list) {
        int size = mList.size();
        mList.addAll(list);
        notifyItemInserted(size);
    }

    @Override
    @NonNull
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_list_view_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.mTextView.setText(String.valueOf(position));
        Glide.with(mContext).asBitmap().load(mList.get(position)).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private ImageView mImageView;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageView_list_item);
            mTextView = itemView.findViewById(R.id.textView_list_item);
            itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(
                                            v.getContext(),
                                            "Click:" + getAdapterPosition(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            itemView.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Toast.makeText(
                                            v.getContext(),
                                            "LongClick:" + getAdapterPosition(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            return true;
                        }
                    });
        }
    }
}
