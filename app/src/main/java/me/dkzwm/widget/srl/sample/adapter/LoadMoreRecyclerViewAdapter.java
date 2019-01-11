package me.dkzwm.widget.srl.sample.adapter;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/8/8.
 *
 * @author dkzwm
 */
public class LoadMoreRecyclerViewAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    private Context mContext;

    public LoadMoreRecyclerViewAdapter(Context context) {
        super(R.layout.layout_list_view_item);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.textView_list_item, String.valueOf(helper.getAdapterPosition()));
        ImageView view = helper.getView(R.id.imageView_list_item);
        Glide.with(mContext).asBitmap().load(item).into(view);
    }
}
