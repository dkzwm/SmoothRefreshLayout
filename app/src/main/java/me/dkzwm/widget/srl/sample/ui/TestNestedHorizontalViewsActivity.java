package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.ToxicBakery.viewpager.transforms.DrawerTransformer;
import java.util.ArrayList;
import java.util.List;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.ViewPagerAdapter;
import me.dkzwm.widget.srl.sample.ui.fragment.PageFragment;

/**
 * Created by dkzwm on 2017/9/13.
 *
 * @author dkzwm
 */
public class TestNestedHorizontalViewsActivity extends AppCompatActivity {
    private static final int[] sColors =
            new int[] {Color.WHITE, Color.GREEN, Color.YELLOW, Color.BLUE, Color.RED, Color.BLACK};
    private SmoothRefreshLayout mRefreshLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_nested_horizontal_views);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_nested_horizontal_views);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_nested_horizontal_views);
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
                                        Toast.makeText(
                                                        TestNestedHorizontalViewsActivity.this,
                                                        R.string.sr_refresh_complete,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                },
                                4000);
                    }
                });
        mViewPager = findViewById(R.id.viewPager_test_nested_horizontal_views_pager);
        List<PageFragment> fragments = new ArrayList<>();
        for (int i = 0; i < sColors.length; i++) {
            fragments.add(PageFragment.newInstance(i, sColors[i]));
        }
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageTransformer(true, new DrawerTransformer());
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
        startActivity(new Intent(TestNestedHorizontalViewsActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
