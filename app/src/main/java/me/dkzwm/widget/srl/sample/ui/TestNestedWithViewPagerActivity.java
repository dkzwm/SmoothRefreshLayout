package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import java.util.List;
import me.dkzwm.widget.srl.MaterialSmoothRefreshLayout;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.ViewPagerAdapter;
import me.dkzwm.widget.srl.sample.ui.fragment.NestedPageFragment;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class TestNestedWithViewPagerActivity extends AppCompatActivity {
    private static final int[] sColors =
            new int[] {Color.WHITE, Color.GREEN, Color.YELLOW, Color.BLUE, Color.RED, Color.BLACK};
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
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
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
        mRefreshLayout.setOnRefreshListener(
                new SmoothRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < mFragments.size(); i++) {
                                            NestedPageFragment fragment = mFragments.get(i);
                                            fragment.updateData();
                                        }
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
                                        for (int i = 0; i < mFragments.size(); i++) {
                                            NestedPageFragment fragment = mFragments.get(i);
                                            fragment.appendData();
                                        }
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }
                });
        mRefreshLayout.setDisableWhenAnotherDirectionMove(true);
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
