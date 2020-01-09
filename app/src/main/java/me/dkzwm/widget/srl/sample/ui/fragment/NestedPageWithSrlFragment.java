package me.dkzwm.widget.srl.sample.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import me.dkzwm.widget.srl.MaterialSmoothRefreshLayout;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2017/6/2.
 *
 * @author dkzwm
 */
public class NestedPageWithSrlFragment extends Fragment {
    private int mColor;
    private int mCount;
    private Handler mHandler = new Handler();
    private MaterialSmoothRefreshLayout mRefreshLayout;
    private RecyclerViewAdapter mAdapter;
    private ArrayList<String> mList = new ArrayList<>();

    public static NestedPageWithSrlFragment newInstance(int color) {
        NestedPageWithSrlFragment fragment = new NestedPageWithSrlFragment();
        fragment.mColor = color;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nested_page_with_srl, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_nested_page_with_srl);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerViewAdapter(getActivity(), inflater);
        mAdapter.updateData(mList);
        recyclerView.setAdapter(mAdapter);
        mRefreshLayout = view.findViewById(R.id.smoothRefreshLayout_nested_page_with_srl);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.materialStyle();
        mRefreshLayout.setDisableWhenAnotherDirectionMove(true);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        List<String> list = DataUtil.createList(mCount, 20);
                                        mCount = list.size();
                                        mList.clear();
                                        mList.addAll(list);
                                        mAdapter.updateData(list);
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }

                    @Override
                    public void onLoadingMore() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        List<String> list = DataUtil.createList(mCount, 20);
                                        mCount += list.size();
                                        mList.addAll(list);
                                        mAdapter.appendData(list);
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }
                });
        mRefreshLayout.setBackgroundColor(mColor);
        mRefreshLayout.autoRefresh(true);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
