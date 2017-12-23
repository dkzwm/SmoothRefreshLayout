package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.header.WaveTextRefreshView;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithFrameLayoutActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private TextView mTextView;
    private Handler mHandler = new Handler();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_framelayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_frameLayout);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_with_frameLayout);
        mTextView = findViewById(R.id.textView_with_frameLayout_desc);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WithFrameLayoutActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        mTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(WithFrameLayoutActivity.this, "LongClicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        WaveTextRefreshView refreshView = new WaveTextRefreshView(this);
        refreshView.setIncrementalY(.5f);
        mRefreshLayout.setHeaderView(refreshView);
        mRefreshLayout.setOffsetRatioToKeepRefreshViewWhileLoading(1);
        mRefreshLayout.setRatioOfHeaderHeightToRefresh(1);
        mRefreshLayout.setEnableOverScroll(false);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setEnabledInterceptEventWhileLoading(true);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setDisablePerformLoadMore(true);
        mRefreshLayout.setEnableHideFooterView(true);
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(boolean isRefresh) {
                mCount++;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.refreshComplete();
                        String times = getString(R.string.number_of_refresh) + mCount;
                        mTextView.setText(times);
                    }
                }, 8000);
            }
        });
        mRefreshLayout.autoRefresh(true);
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
        startActivity(new Intent(WithFrameLayoutActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
