package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import me.dkzwm.widget.srl.MaterialSmoothRefreshLayout;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;
import me.dkzwm.widget.srl.utils.QuickConfigAutoRefreshUtil;

/**
 * Created by dkzwm on 2017/12/23.
 *
 * @author dkzwm
 */
public class TestScrollToAutoRefreshActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialSmoothRefreshLayout mRefreshLayout;
    private QuickConfigAutoRefreshUtil mAutoRefreshUtil;
    private RecyclerViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_scroll_to_auto_refresh);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_scroll_to_auto_refresh);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_test_scroll_to_auto_refresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerViewAdapter(this, getLayoutInflater());
        recyclerView.setAdapter(mAdapter);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_scroll_to_auto_refresh);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.materialStyle();
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(final boolean isRefresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRefresh) {
                            mCount = 0;
                            List<String> list = DataUtil.createList(mCount, 20);
                            mCount += 20;
                            mAdapter.updateData(list);
                        } else {
                            List<String> list = DataUtil.createList(mCount, 20);
                            mCount += 20;
                            mAdapter.appendData(list);
                        }
                        mRefreshLayout.refreshComplete();
                    }
                }, 2000);
            }
        });
        mRefreshLayout.setEnabledCanNotInterruptScrollWhenRefreshCompleted(true);
        mRefreshLayout.autoRefresh(false);
        mAutoRefreshUtil = new QuickConfigAutoRefreshUtil(recyclerView);
        mRefreshLayout.setLifecycleObserver(mAutoRefreshUtil);
        findViewById(R.id.button_test_scroll_to_auto_refresh_left).setOnClickListener(this);
        findViewById(R.id.button_test_scroll_to_auto_refresh_right).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TestScrollToAutoRefreshActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_test_scroll_to_auto_refresh_left:
                mAutoRefreshUtil.autoRefresh(false, false, true);
                break;
            case R.id.button_test_scroll_to_auto_refresh_right:
                mAutoRefreshUtil.autoLoadMore(false, false, true);
                break;
        }
    }
}
