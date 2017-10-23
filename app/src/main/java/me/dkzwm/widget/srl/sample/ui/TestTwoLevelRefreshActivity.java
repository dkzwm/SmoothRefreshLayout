package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import me.dkzwm.widget.srl.TwoLevelRefreshingListenerAdapter;
import me.dkzwm.widget.srl.TwoLevelSmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.header.CustomTwoLevelHeader;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
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
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_two_level_refresh);
        mTextView = findViewById(R.id.textView_test_two_level_refresh_desc);
        mRefreshLayout.setHeaderView(new CustomTwoLevelHeader(this));
        mRefreshLayout.setEnableKeepRefreshView(true);
        //设置保持头部的Offset（占头部的高度比）
        mRefreshLayout.setOffsetRatioToKeepHeaderWhileLoading(.12f);
        //设置触发刷新的头部高度比
        mRefreshLayout.setRatioOfHeaderHeightToRefresh(.12f);
        //设置滚动到保持二级刷新的头部位置的时长
        mRefreshLayout.setDurationOfBackToKeepTwoLevelHeaderViewPosition(1000);
        //设置关闭二级刷新头部回滚到起始位置的时长
        mRefreshLayout.setDurationToCloseTwoLevelHeader(0);
        //设置刷新时保持头部的Offset(占头部的高度比)
        mRefreshLayout.setOffsetRatioToKeepTwoLevelHeaderWhileLoading(1f);
        //设置触发提示二级刷新的头部高度比
        mRefreshLayout.setRatioOfHeaderHeightToHintTwoLevelRefresh(.15f);
        //设置触发二级刷新的头部高度比
        mRefreshLayout.setRatioOfHeaderHeightToTwoLevelRefresh(.25f);
        mRefreshLayout.setOnRefreshListener(new TwoLevelRefreshingListenerAdapter() {
            @Override
            public void onTwoLevelRefreshBegin() {
                mRefreshLayout.setEnabledInterceptEventWhileLoading(true);
                mTwoLevelCount++;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.refreshComplete();
                        String times = getString(R.string.number_of_two_level_refresh) + mTwoLevelCount;
                        mTextView.setText(times);
                    }
                }, 2000);
            }

            @Override
            public void onRefreshBegin(boolean isRefresh) {
                mCount++;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String times = getString(R.string.number_of_one_level_refresh) + mCount;
                        mRefreshLayout.refreshComplete();
                        mTextView.setText(times);
                    }
                }, 1000);
            }

            @Override
            public void onRefreshComplete(boolean isSuccessful) {
                mRefreshLayout.setEnabledInterceptEventWhileLoading(false);
            }
        });
        mRefreshLayout.autoTwoLevelRefreshHint(false, 2000, false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case Menu.FIRST:
                if (!mRefreshLayout.isDisabledTwoLevelRefresh()) {
                    item.setTitle(R.string.enable_two_level_refresh);
                    mRefreshLayout.setDisableTwoLevelRefresh(true);
                } else {
                    item.setTitle(R.string.disable_two_level_refresh);
                    mRefreshLayout.setDisableTwoLevelRefresh(false);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.disable_two_level_refresh);
        return super.onCreateOptionsMenu(menu);
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
