package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithTextViewActivity extends AppCompatActivity implements View.OnClickListener {
    private SmoothRefreshLayout mRefreshLayout;
    private TextView mTextView;
    private Handler mHandler = new Handler();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_textview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_textView);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_with_textView);
        mTextView = findViewById(R.id.textView_with_textView_desc);
        mRefreshLayout.setEnableKeepRefreshView(true);
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
                        mRefreshLayout.setState(SmoothRefreshLayout.STATE_CONTENT, true);
                        mRefreshLayout.refreshComplete();
                        String times = getString(R.string.number_of_refresh) + mCount;
                        mTextView.setText(times);
                    }
                }, 2000);
            }
        });
        mRefreshLayout.autoRefresh(true);
        mRefreshLayout.setIndicatorOffsetCalculator(new IIndicator.IOffsetCalculator() {
            @Override
            public float calculate(@IIndicator.MovingStatus int status, int currentPos, float
                    offset) {
                if (status == IIndicator.MOVING_HEADER) {
                    if (offset < 0) {
                        //如果希望拖动缩回时类似QQ一样没有阻尼效果，阻尼效果只存在于下拉则可以在此返回offset
                        //如果希望拖动缩回时类似QQ一样有阻尼效果，那么请注释掉这个判断语句
                        return offset;
                    }
                    return (float) Math.pow(Math.pow(currentPos / 2, 1.28d) + offset, 1 / 1.28d) *
                            2 - currentPos;
                } else if (status == IIndicator.MOVING_FOOTER) {
                    if (offset > 0) {
                        //如果希望拖动缩回时类似QQ一样没有阻尼效果，阻尼效果只存在于上拉则可以在此返回offset
                        //如果希望拖动缩回时类似QQ一样有阻尼效果，那么请注释掉这个判断语句
                        return offset;
                    }
                    return -((float) Math.pow(Math.pow(currentPos / 2, 1.28d) - offset, 1 / 1.28d) *
                            2 - currentPos);
                } else {
                    if (offset > 0) {
                        return (float) Math.pow(offset, 1 / 1.28d) * 2;
                    } else if (offset < 0) {
                        return -(float) Math.pow(-offset, 1 / 1.28d) * 2;
                    } else {
                        return offset;
                    }
                }
            }
        });
        findViewById(R.id.button_with_textView_change_empty_state)
                .setOnClickListener(this);
        findViewById(R.id.button_with_textView_change_content_state)
                .setOnClickListener(this);
        findViewById(R.id.button_with_textView_change_error_state)
                .setOnClickListener(this);
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
        startActivity(new Intent(WithTextViewActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_with_textView_change_empty_state:
                mRefreshLayout.setState(SmoothRefreshLayout.STATE_EMPTY, true);
                break;
            case R.id.button_with_textView_change_content_state:
                mRefreshLayout.setState(SmoothRefreshLayout.STATE_CONTENT, true);
                break;
            case R.id.button_with_textView_change_error_state:
                mRefreshLayout.setState(SmoothRefreshLayout.STATE_ERROR, true);
                break;
        }
    }
}
