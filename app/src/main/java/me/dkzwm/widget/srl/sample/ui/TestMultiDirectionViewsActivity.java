package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import java.util.List;
import me.dkzwm.widget.srl.HorizontalSmoothRefreshLayout;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.ViewPagerAdapter;
import me.dkzwm.widget.srl.sample.footer.CustomLoadDetailFooter;
import me.dkzwm.widget.srl.sample.ui.fragment.PageFragment;

/**
 * Created by dkzwm on 2017/11/3.
 *
 * @author dkzwm
 */
public class TestMultiDirectionViewsActivity extends AppCompatActivity {
    private static final int[] sColors =
            new int[] {Color.WHITE, Color.GREEN, Color.YELLOW, Color.BLUE, Color.RED, Color.BLACK};
    private SmoothRefreshLayout mRefreshLayout;
    private HorizontalSmoothRefreshLayout mInnerRefreshLayout;
    private ViewPager mViewPager;
    private ScrollView mScrollView;
    private TextView mTextView;
    private ViewPagerAdapter mAdapter;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_multi_direction_views);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_multi_direction_views);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_multi_direction_views);
        ClassicHeader header = new ClassicHeader(this);
        mRefreshLayout.setHeaderView(header);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setDisableWhenAnotherDirectionMove(true);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mViewPager.setCurrentItem(0, true);
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }
                });
        mViewPager = findViewById(R.id.viewPager_test_multi_direction_views_pager);
        List<PageFragment> fragments = new ArrayList<>();
        for (int i = 0; i < sColors.length; i++) {
            fragments.add(PageFragment.newInstance(i, sColors[i]));
        }
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mAdapter);
        mRefreshLayout.autoRefresh(false);
        mTextView = findViewById(R.id.textView_load_detail_footer_details);
        mScrollView = findViewById(R.id.scrollView_test_multi_direction_views);
        mInnerRefreshLayout =
                findViewById(R.id.smoothRefreshLayout_test_multi_direction_views_inner);
        mInnerRefreshLayout.setDisableRefresh(true);
        mInnerRefreshLayout.setDisableLoadMore(false);
        mInnerRefreshLayout.setEnableOverScroll(false);
        CustomLoadDetailFooter footer = new CustomLoadDetailFooter(this);
        mInnerRefreshLayout.setFooterView(footer);
        mInnerRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {}

                    @Override
                    public void onLoadingMore() {
                        mInnerRefreshLayout.setDurationToClose(0);
                        mInnerRefreshLayout.refreshComplete();
                        mRefreshLayout.setDurationToClose(0);
                        mRefreshLayout.refreshComplete();
                        mInnerRefreshLayout.setDurationToClose(500);
                        mRefreshLayout.setDurationToClose(500);
                        mScrollView.smoothScrollTo(0, mTextView.getTop());
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
        startActivity(new Intent(TestMultiDirectionViewsActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
