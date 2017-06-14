package me.dkzwm.smoothrefreshlayout.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.TwoLevelSmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.sample.R;
import me.dkzwm.smoothrefreshlayout.sample.header.CustomTwoLevelHeader;

/**
 * Created by dkzwm1 on 2017/6/12.
 */

public class TestTwoLevelRefreshActivity extends AppCompatActivity {
    private TwoLevelSmoothRefreshLayout mRefreshLayout;
    private TextView mTextView;
    private Handler mHandler = new Handler();
    private int mCount = 0;
    private int mTwoLevelCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_two_level_refresh);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_two_level_refresh);
        mRefreshLayout = (TwoLevelSmoothRefreshLayout) findViewById(R.id.smoothRefreshLayout_test_two_level_refresh_activity);
        mTextView = (TextView) findViewById(R.id.textView_test_two_level_refresh_activity_desc);
        mRefreshLayout.setMode(SmoothRefreshLayout.MODE_REFRESH);
        mRefreshLayout.setHeaderView(new CustomTwoLevelHeader(this));
        mRefreshLayout.setEnableKeepRefreshView(true);
        //设置启用触发二级刷新
        mRefreshLayout.setEnableTwoLevelPullToRefresh(true);
        //设置触发二级刷新后立即回到起始位置
        mRefreshLayout.setEnableBackToStartPosAtOnce(true);
        //设置保持头部的Offset（占头部的高度比例）
        mRefreshLayout.setOffsetRatioToKeepHeaderWhileLoading(.35f);
        //设置触发刷新的头部高度比
        mRefreshLayout.setRatioOfHeaderHeightToRefresh(.35f);
        //设置触发提示二级刷新的头部高度比
        mRefreshLayout.setRatioOfHeaderHeightToHintTwoLevelRefresh(.45f);
        //设置触发二级刷新的头部高度比
        mRefreshLayout.setRatioOfHeaderHeightToTwoLevelRefresh(.65f);
        mRefreshLayout.setOnRefreshListener(new TwoLevelSmoothRefreshLayout.OnRefreshListener() {
            @Override
            public void onTwoLevelRefreshBegin() {
                mTwoLevelCount++;
                mRefreshLayout.refreshComplete();
                mTextView.setText("二级刷新次数：" + mTwoLevelCount);
            }

            @Override
            public void onRefreshBegin(boolean isRefresh) {
                mCount++;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.refreshComplete();
                        mTextView.setText("一级刷新次数：" + mCount);
                    }
                }, 2000);
            }

            @Override
            public void onRefreshComplete() {

            }
        });
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
        startActivity(new Intent(TestTwoLevelRefreshActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
