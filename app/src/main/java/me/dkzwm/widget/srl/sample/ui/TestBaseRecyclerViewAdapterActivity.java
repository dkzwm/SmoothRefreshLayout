package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.LoadMoreRecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;
import me.dkzwm.widget.srl.utils.ScrollCompat;

/**
 * Created by dkzwm on 2017/8/8.
 *
 * @author dkzwm
 */

public class TestBaseRecyclerViewAdapterActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private BaseQuickAdapter<String, BaseViewHolder> mAdapter;
    private Handler mHandler = new Handler();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_base_recyclerView_adapter);
        setContentView(R.layout.activity_test_base_recyclerview_adapter);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_base_recyclerView_adapter);
        ClassicHeader classicHeader = new ClassicHeader(this);
        classicHeader.setLastUpdateTimeKey("header_last_update_time");
        mRefreshLayout.setHeaderView(classicHeader);
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(boolean isRefresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCount = 0;
                        List<String> list = DataUtil.createList(mCount, 20);
                        mCount += 20;
                        mAdapter.setNewData(list);
                        mRefreshLayout.refreshComplete();
                    }
                }, 2000);
            }
        });
        mAdapter = new LoadMoreRecyclerViewAdapter(this);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List<String> list = DataUtil.createList(mCount, 20);
                        mCount += 20;
                        mAdapter.addData(list);
                        mAdapter.loadMoreComplete();
                        mRefreshLayout.refreshComplete();
                    }
                }, 2000);
            }
        }, mRecyclerView);
        mRefreshLayout.setOnChildNotYetInEdgeCannotMoveHeaderCallBack(new SmoothRefreshLayout
                .OnChildNotYetInEdgeCannotMoveHeaderCallBack() {
            @Override
            public boolean isChildNotYetInEdgeCannotMoveHeader(SmoothRefreshLayout parent,
                                                               @Nullable View child,
                                                               @Nullable IRefreshView header) {
                return ScrollCompat.canChildScrollUp(mRecyclerView);
            }
        });
        mRefreshLayout.setOnChildNotYetInEdgeCannotMoveFooterCallBack(new SmoothRefreshLayout
                .OnChildNotYetInEdgeCannotMoveFooterCallBack() {
            @Override
            public boolean isChildNotYetInEdgeCannotMoveFooter(SmoothRefreshLayout parent,
                                                               @Nullable View child,
                                                               @Nullable IRefreshView footer) {
                return ScrollCompat.canChildScrollDown(mRecyclerView);
            }
        });
        mRefreshLayout.autoRefresh(true);
        mRecyclerView = findViewById(R.id.recyclerView_test_base_recyclerView_adapter);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout.setLoadMoreScrollTargetView(mRecyclerView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TestBaseRecyclerViewAdapterActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
