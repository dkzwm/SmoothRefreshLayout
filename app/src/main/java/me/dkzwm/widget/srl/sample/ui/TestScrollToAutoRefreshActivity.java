package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import me.dkzwm.widget.srl.MaterialSmoothRefreshLayout;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.header.CustomLottieHeader;
import me.dkzwm.widget.srl.sample.utils.DataUtil;
import me.dkzwm.widget.srl.util.AutoRefreshUtil;

/**
 * Created by dkzwm on 2017/12/23.
 *
 * @author dkzwm
 */
public class TestScrollToAutoRefreshActivity extends AppCompatActivity
        implements View.OnClickListener {
    private MaterialSmoothRefreshLayout mRefreshLayout;
    private AutoRefreshUtil mAutoRefreshUtil;
    private RecyclerViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private int mCount = 0;
    private int mIndex = 0;
    private CustomLottieHeader mLottieHeader;
    private static final String[] NAME_OF_ANIMATION_JSON = new String[6];

    static {
        NAME_OF_ANIMATION_JSON[0] = "13808-tenor-2x20.json";
        NAME_OF_ANIMATION_JSON[1] = "13908-animated-bears-2.json";
        NAME_OF_ANIMATION_JSON[2] = "13995-animated-bears-7.json";
        NAME_OF_ANIMATION_JSON[3] = "14175-testing-123.json";
        NAME_OF_ANIMATION_JSON[4] = "14156-mingmingming.json";
        NAME_OF_ANIMATION_JSON[5] = "13679-fast-food-mobile-app-loading.json";
    }

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
        mLottieHeader = new CustomLottieHeader(this);
        mLottieHeader.setAnimation(NAME_OF_ANIMATION_JSON[mIndex]);
        mLottieHeader.setScale(.15f);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_scroll_to_auto_refresh);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setHeaderView(mLottieHeader);
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
        mRefreshLayout.addOnStatusChangedListener(
                new SmoothRefreshLayout.OnStatusChangedListener() {
                    @Override
                    public void onStatusChanged(byte old, byte now) {
                        if (now == SmoothRefreshLayout.SR_STATUS_INIT
                                && old == SmoothRefreshLayout.SR_STATUS_COMPLETE) {
                            if (mIndex >= NAME_OF_ANIMATION_JSON.length - 1) {
                                mIndex = 0;
                            } else {
                                mIndex++;
                            }
                            mLottieHeader.setAnimation(NAME_OF_ANIMATION_JSON[mIndex]);
                        }
                    }
                });
        mRefreshLayout.setSpringInterpolator(new OvershootInterpolator(3));
        mRefreshLayout.autoRefresh(false);
        mAutoRefreshUtil = new AutoRefreshUtil(recyclerView);
        mRefreshLayout.addLifecycleObserver(mAutoRefreshUtil);
        findViewById(R.id.button_test_scroll_to_auto_refresh_left).setOnClickListener(this);
        findViewById(R.id.button_test_scroll_to_auto_refresh_right).setOnClickListener(this);
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
                mAutoRefreshUtil.autoRefresh(false, true);
                break;
            case R.id.button_test_scroll_to_auto_refresh_right:
                mAutoRefreshUtil.autoLoadMore(false, true);
                break;
        }
    }
}
