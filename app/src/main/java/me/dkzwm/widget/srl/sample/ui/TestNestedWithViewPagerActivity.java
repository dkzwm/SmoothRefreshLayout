package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
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
    private Handler mHandler = new Handler();
    private List<NestedPageFragment> mFragments;

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
        ViewPager viewPager = findViewById(R.id.viewPager_test_nested_with_viewPager);
        mFragments = new ArrayList<>();
        for (int sColor : sColors) {
            mFragments.add(NestedPageFragment.newInstance(sColor));
        }
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), mFragments);
        viewPager.setAdapter(adapter);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_nested_with_viewPager);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.materialStyle();
        mRefreshLayout.setOnRefreshListener(new SmoothRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefreshBegin(final boolean isRefresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRefresh) {
                            for (int i = 0; i < mFragments.size(); i++) {
                                NestedPageFragment fragment = mFragments.get(i);
                                fragment.updateData();
                            }
                        } else {
                            for (int i = 0; i < mFragments.size(); i++) {
                                NestedPageFragment fragment = mFragments.get(i);
                                fragment.appendData();
                            }
                        }
                        mRefreshLayout.refreshComplete();
                    }
                }, 2000);
            }

            @Override
            public void onRefreshComplete(boolean isSuccessful) {
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
        mFragments.clear();
        mHandler.removeCallbacksAndMessages(null);
    }
}
