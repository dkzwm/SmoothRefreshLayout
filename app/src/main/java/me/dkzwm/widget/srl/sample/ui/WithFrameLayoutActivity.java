package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
    private static final String[] NAME_OF_TYPEFACES = new String[4];

    static {
        NAME_OF_TYPEFACES[0] = "Facon-2.ttf";
        NAME_OF_TYPEFACES[1] = "DontMeltDrip-Regular-2.otf";
        NAME_OF_TYPEFACES[2] = "DrSugiyama-Pro-Regular-2.ttf";
        NAME_OF_TYPEFACES[3] = "Squiggles-wKm6-2.ttf";
    }

    private SmoothRefreshLayout mRefreshLayout;
    private TextView mTextView;
    private WaveTextRefreshView mRefreshView;
    private Handler mHandler = new Handler();
    private int mCount = 0;
    private int mIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_framelayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_frameLayout);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_with_frameLayout);
        mTextView = findViewById(R.id.textView_with_frameLayout_desc);
        mTextView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(WithFrameLayoutActivity.this, "Clicked", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        mTextView.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(
                                        WithFrameLayoutActivity.this,
                                        "LongClicked",
                                        Toast.LENGTH_SHORT)
                                .show();
                        return true;
                    }
                });
        mRefreshView = new WaveTextRefreshView(this);
        mRefreshView.setIncrementalY(.5f);
        mRefreshView.setTextSize(18);
        mRefreshLayout.setHeaderView(mRefreshView);
        mRefreshLayout.setRatioToKeep(1);
        mRefreshLayout.setRatioOfHeaderToRefresh(1);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setEnableInterceptEventWhileLoading(true);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setDisablePerformLoadMore(true);
        mRefreshLayout.getFooterView().getView().setVisibility(View.GONE);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mCount++;
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mRefreshLayout.refreshComplete();
                                        String times =
                                                getString(R.string.number_of_refresh) + mCount;
                                        mTextView.setText(times);
                                    }
                                },
                                8000);
                    }
                });
        mRefreshLayout.addOnStatusChangedListener(
                new SmoothRefreshLayout.OnStatusChangedListener() {
                    @Override
                    public void onStatusChanged(byte old, byte now) {
                        if (now == SmoothRefreshLayout.SR_STATUS_INIT
                                && old == SmoothRefreshLayout.SR_STATUS_COMPLETE) {
                            if (mIndex >= NAME_OF_TYPEFACES.length - 1) {
                                mIndex = 0;
                            } else {
                                mIndex++;
                            }
                            mRefreshView.setTypeface(
                                    Typeface.createFromAsset(
                                            getAssets(), NAME_OF_TYPEFACES[mIndex]));
                        }
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
