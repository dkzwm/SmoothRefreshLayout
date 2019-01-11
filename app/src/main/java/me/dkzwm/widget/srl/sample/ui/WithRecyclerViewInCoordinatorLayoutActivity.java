package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithRecyclerViewInCoordinatorLayoutActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_recyclerview_in_coordinatorlayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white_72x72);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
        mRecyclerView = findViewById(R.id.recyclerView_with_recyclerView_in_coordinatorLayout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerViewAdapter(this, getLayoutInflater());
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout =
                findViewById(R.id.smoothRefreshLayout_with_recyclerView_in_coordinatorLayout);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setOnRefreshListener(
                new SmoothRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        List<String> list = DataUtil.createList(mCount, 20);
                                        mCount = list.size();
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
                                        mAdapter.appendData(list);
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }
                });
        mRefreshLayout.setDisableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.autoRefresh(false);
    }

    @Override
    public void onBackPressed() {
        startActivity(
                new Intent(WithRecyclerViewInCoordinatorLayoutActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
