package me.dkzwm.widget.srl.sample.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import me.dkzwm.widget.srl.sample.R;


/**
 * Created by dkzwm on 2017/8/8.
 *
 * @author dkzwm
 */

public class LoadMoreRecyclerViewAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public LoadMoreRecyclerViewAdapter() {
        super(R.layout.layout_list_view_item);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.textView_list_item, item);
    }
}
