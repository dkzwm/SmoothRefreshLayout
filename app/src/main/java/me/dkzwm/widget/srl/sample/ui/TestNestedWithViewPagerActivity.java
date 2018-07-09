package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import me.dkzwm.widget.srl.MaterialSmoothRefreshLayout;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.ViewPagerAdapter;
import me.dkzwm.widget.srl.sample.ui.fragment.NestedPageFragment;
import me.dkzwm.widget.srl.sample.widget.PrepareScrollViewPager;
import me.dkzwm.widget.srl.utils.QuickConfigAppBarUtil;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class TestNestedWithViewPagerActivity extends AppCompatActivity {
    private static final int[] sColors = new int[]{Color.WHITE, Color.GREEN, Color.YELLOW,
            Color.BLUE, Color.RED, Color.BLACK};
    private MaterialSmoothRefreshLayout mRefreshLayout;
    private PrepareScrollViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_nested_with_viewpager);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white_72x72);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mViewPager = findViewById(R.id.viewPager_test_nested_with_viewPager);
        final List<NestedPageFragment> fragments = new ArrayList<>();
        for (int sColor : sColors) {
            fragments.add(NestedPageFragment.newInstance(sColor));
        }
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(sColors.length);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_nested_with_viewPager);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.materialStyle();
        mRefreshLayout.setOnRefreshListener(new SmoothRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefreshBegin(final boolean isRefresh) {
                if (!isRefresh) {
                    mViewPager.setEnableScroll(false);
                }
                final int currentItem = mViewPager.getCurrentItem();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRefresh) {
                            fragments.get(currentItem).updateData();
                        } else {
                            fragments.get(currentItem).appendData();
                        }
                        mRefreshLayout.refreshComplete();
                    }
                }, isRefresh ? 2000 : 10000);
            }

            @Override
            public void onRefreshComplete(boolean isSuccessful) {
                mViewPager.setEnableScroll(true);
            }
        });
        mRefreshLayout.setDisableWhenAnotherDirectionMove(true);
        mRefreshLayout.setEnableDynamicEnsureTargetView(true);
        mRefreshLayout.setLifecycleObserver(new QuickConfigAppBarUtil());
        mRefreshLayout.autoRefresh(false);
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
        startActivity(new Intent(TestNestedWithViewPagerActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
