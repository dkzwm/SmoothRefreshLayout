package me.dkzwm.widget.srl.sample.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2017/6/2.
 *
 * @author dkzwm
 */

public class NestedPageFragment extends Fragment {
    private int mColor;
    private int mCount;
    private RecyclerViewAdapter mAdapter;
    private ArrayList<String> mList = new ArrayList<>();

    public static NestedPageFragment newInstance(int color) {
        NestedPageFragment fragment = new NestedPageFragment();
        fragment.mColor = color;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nested_page, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_nested_page);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setBackgroundColor(mColor);
        mAdapter = new RecyclerViewAdapter(getActivity(), inflater);
        recyclerView.setAdapter(mAdapter);
        mAdapter.updateData(mList);
        return view;
    }

    public void updateData() {
        List<String> list = DataUtil.createList(0, 20);
        mCount = list.size();
        mList.clear();
        mList.addAll(list);
        if (mAdapter != null)
            mAdapter.updateData(list);
    }

    public void appendData() {
        List<String> list = DataUtil.createList(mCount, 20);
        mCount += list.size();
        mList.addAll(list);
        if (mAdapter != null)
            mAdapter.appendData(list);
    }

}
